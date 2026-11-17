-- 0007_supabase_auth_rls.sql
-- Full Supabase Auth migration.
--
-- Before this migration every public table had a single `public / using true /
-- check true` policy (demo mode): anyone with the anon key could read/write all
-- tenants. This migration:
--   1. adds text-based tenant helpers (the app's IDs are text, not uuid),
--   2. adds narrowly-scoped SECURITY DEFINER lookup RPCs so a brand-new device
--      can resolve a committee ID / mobile BEFORE it has a JWT,
--   3. replaces the open policies with JWT-based tenant isolation.
--
-- UI contract is unchanged: president logs in with committee ID + email +
-- password, member with committee ID + mobile + password. Supabase Auth is
-- email/password only, so the app derives a deterministic synthetic email for
-- mobile-only members; that mapping lives in the Android client.

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Tenant helpers (text, because mosque_id / profiles.id are text columns)
-- ─────────────────────────────────────────────────────────────────────────────
create or replace function public.seba_current_mosque_id()
returns text
language sql
stable
security definer
set search_path = public
as $$
  select mosque_id
  from public.profiles
  where auth_user_id = auth.uid()::text
  limit 1;
$$;

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. Anonymous bootstrap lookup RPCs
--    A device with no session must be able to find its mosque by committee ID
--    and its member by mobile. These functions expose only the few columns the
--    login screens need and never a password hash or another tenant's rows.
-- ─────────────────────────────────────────────────────────────────────────────
create or replace function public.seba_lookup_committee(p_committee_id text)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v jsonb;
begin
  select jsonb_build_object(
    'id', id,
    'committee_id', committee_id,
    'name_bn', name_bn,
    'name_en', name_en,
    'address', address,
    'established_year', established_year,
    'committee_start_year', committee_start_year,
    'committee_end_year', committee_end_year,
    'status', status,
    'district', district,
    'upazila', upazila,
    'waqf_reg_no', waqf_reg_no,
    'capacity', capacity,
    'bank_account_info', bank_account_info,
    'logo_url', logo_url,
    'seal_url', seal_url
  )
  into v
  from public.mosques
  where committee_id = p_committee_id
  limit 1;
  return v;
end;
$$;

create or replace function public.seba_lookup_member_by_mobile(
  p_mosque_id text,
  p_mobile text
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v jsonb;
  v_digits text := regexp_replace(regexp_replace(coalesce(p_mobile, ''), '[^0-9]', '', 'g'), '^88', '');
begin
  select jsonb_build_object(
    'id', id,
    'member_code', member_code,
    'name_bn', name_bn,
    'name_en', name_en,
    'mobile', mobile,
    'email', email,
    'role_code', role_code,
    'status', status,
    'join_date', join_date,
    'designation_bn', designation_bn
  )
  into v
  from public.members
  where mosque_id = p_mosque_id
    and regexp_replace(regexp_replace(coalesce(mobile, ''), '[^0-9]', '', 'g'), '^88', '') = v_digits
  limit 1;
  return v;
end;
$$;

-- A signed-in member may create an Auth-linked profile for a NEW member of
-- their own mosque. The called passes the Auth UID returned by signup; the
-- function refuses any mosque the caller does not belong to.
create or replace function public.seba_create_member_profile(
  p_mosque_id text,
  p_member_id text,
  p_auth_uid text,
  p_name_bn text,
  p_email text,
  p_mobile text,
  p_password_hash text
)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() is null then
    raise exception 'authentication required';
  end if;
  if p_mosque_id is distinct from public.seba_current_mosque_id() then
    raise exception 'mosque scope violation';
  end if;

  insert into public.profiles
    (id, auth_user_id, mosque_id, member_id, name_bn, email, mobile, password_hash, status)
  values
    ('prof-' || p_member_id, p_auth_uid, p_mosque_id, p_member_id,
     p_name_bn, p_email, p_mobile, p_password_hash, 'active')
  on conflict (id) do update set
    auth_user_id  = excluded.auth_user_id,
    mosque_id     = excluded.mosque_id,
    name_bn       = excluded.name_bn,
    email         = excluded.email,
    mobile        = excluded.mobile,
    password_hash = excluded.password_hash;
end;
$$;

revoke all on function public.seba_current_mosque_id() from public;
grant execute on function public.seba_current_mosque_id() to authenticated;

revoke all on function public.seba_lookup_committee(text) from public;
grant execute on function public.seba_lookup_committee(text) to anon, authenticated;

revoke all on function public.seba_lookup_member_by_mobile(text, text) from public;
grant execute on function public.seba_lookup_member_by_mobile(text, text) to anon, authenticated;

revoke all on function public.seba_create_member_profile(text, text, text, text, text, text, text) from public;
grant execute on function public.seba_create_member_profile(text, text, text, text, text, text, text) to authenticated;

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Replace the 45 demo policies with JWT-based tenant isolation
-- ─────────────────────────────────────────────────────────────────────────────
do $$
declare
  pol record;
  t   record;
begin
  -- drop every existing policy on public tables
  for pol in
    select p.polname, c.relname as tbl
    from pg_policy p
    join pg_class c on c.oid = p.polrelid
    where c.relnamespace = 'public'::regnamespace and c.relkind = 'r'
  loop
    execute format('drop policy if exists %I on public.%I', pol.polname, pol.tbl);
  end loop;

  for t in
    select c.relname as tbl,
           exists (
             select 1 from information_schema.columns ic
             where ic.table_schema = 'public'
               and ic.table_name = c.relname
               and ic.column_name = 'mosque_id'
           ) as has_mosque_id
    from pg_class c
    where c.relnamespace = 'public'::regnamespace and c.relkind = 'r'
  loop
    if t.tbl = 'profiles' then
      -- one Auth user == one profile (auth_user_id is UNIQUE)
      execute format(
        'create policy seba_own_profile on public.%I for all to authenticated ' ||
        'using (auth_user_id = auth.uid()::text) ' ||
        'with check (auth_user_id = auth.uid()::text)', t.tbl);

    elsif t.tbl = 'mosques' then
      -- any authenticated user may CREATE a mosque (registration), but may
      -- only read/update/delete the mosque their profile points to.
      execute format(
        'create policy seba_mosque_select on public.%I for select to authenticated ' ||
        'using (id = public.seba_current_mosque_id())', t.tbl);
      execute format(
        'create policy seba_mosque_insert on public.%I for insert to authenticated ' ||
        'with check (true)', t.tbl);
      execute format(
        'create policy seba_mosque_update on public.%I for update to authenticated ' ||
        'using (id = public.seba_current_mosque_id()) ' ||
        'with check (id = public.seba_current_mosque_id())', t.tbl);
      execute format(
        'create policy seba_mosque_delete on public.%I for delete to authenticated ' ||
        'using (id = public.seba_current_mosque_id())', t.tbl);

    elsif t.has_mosque_id then
      execute format(
        'create policy seba_tenant_policy on public.%I for all to authenticated ' ||
        'using (mosque_id = public.seba_current_mosque_id()) ' ||
        'with check (mosque_id = public.seba_current_mosque_id())', t.tbl);

    else
      -- global reference / server-managed tables: authenticated read only
      execute format(
        'create policy seba_reference_read on public.%I for select to authenticated using (true)', t.tbl);
    end if;
  end loop;
end;
$$;
