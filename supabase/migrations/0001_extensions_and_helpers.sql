-- 0001_extensions_and_helpers.sql
-- Extensions and security-helper functions used by every RLS policy and RPC below.
-- Derived from requirements/PRD_v2.0_FINAL.md §5, §31 and docs/SECURITY_MODEL.md.

create extension if not exists "uuid-ossp";
create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------------
-- current_profile_id(): the profiles.id row for the authenticated user.
-- One auth user = one profile (PRD §4: "One user = one mosque tenant").
-- ---------------------------------------------------------------------------
create or replace function public.current_profile_id()
returns uuid
language sql
stable
security invoker
set search_path = public
as $$
  select id from public.profiles where auth_user_id = auth.uid();
$$;

-- ---------------------------------------------------------------------------
-- current_mosque_id(): tenant context is derived ONLY from the authenticated
-- identity, never from a client-supplied parameter (PRD §31).
-- ---------------------------------------------------------------------------
create or replace function public.current_mosque_id()
returns uuid
language sql
stable
security invoker
set search_path = public
as $$
  select mosque_id from public.profiles where auth_user_id = auth.uid();
$$;

-- ---------------------------------------------------------------------------
-- has_permission(perm): implements the role + individual-override half of the
-- authorization formula in docs/SECURITY_MODEL.md. Subscription entitlement
-- and relationship checks are applied separately where relevant.
-- ---------------------------------------------------------------------------
create or replace function public.has_permission(p_permission text)
returns boolean
language plpgsql
stable
security invoker
set search_path = public
as $$
declare
  v_profile_id uuid := public.current_profile_id();
  v_override boolean;
  v_role_grant boolean;
begin
  if v_profile_id is null then
    return false;
  end if;

  -- Individual override always wins when present (grant or revoke).
  select allowed into v_override
  from public.user_permission_overrides upo
  join public.permissions p on p.id = upo.permission_id
  where upo.profile_id = v_profile_id
    and p.code = p_permission
  limit 1;

  if v_override is not null then
    return v_override;
  end if;

  -- Otherwise fall back to role-derived permission.
  select exists (
    select 1
    from public.user_roles ur
    join public.role_permissions rp on rp.role_id = ur.role_id
    join public.permissions p on p.id = rp.permission_id
    where ur.profile_id = v_profile_id
      and p.code = p_permission
  ) into v_role_grant;

  return coalesce(v_role_grant, false);
end;
$$;

comment on function public.current_mosque_id() is
  'Tenant id derived from the authenticated session only. Never trust a client-supplied mosque_id (PRD §31).';
comment on function public.has_permission(text) is
  'Role permission OR individual override, per the authorization formula in docs/SECURITY_MODEL.md. Does not evaluate subscription/relationship axes.';
