-- 0001_core_identity_tenant_rls.sql
-- RLS for mosques, profiles, roles/permissions tables.
-- Rule (docs/SECURITY_MODEL.md): tenant context comes only from the
-- authenticated session (public.current_mosque_id()), never a client param.

alter table public.mosques enable row level security;
alter table public.profiles enable row level security;
alter table public.roles enable row level security;
alter table public.permissions enable row level security;
alter table public.role_permissions enable row level security;
alter table public.user_roles enable row level security;
alter table public.user_permission_overrides enable row level security;

-- mosques: a user may only read their own mosque. Creation happens via a
-- secure RPC (system_super_admin approval flow, PRD §4), not direct insert.
create policy mosques_select_own on public.mosques
  for select
  using (id = public.current_mosque_id());

-- roles/permissions/role_permissions are global reference data — readable by
-- any authenticated user, never writable by clients directly.
create policy roles_select_authenticated on public.roles
  for select
  using (auth.role() = 'authenticated');

create policy permissions_select_authenticated on public.permissions
  for select
  using (auth.role() = 'authenticated');

create policy role_permissions_select_authenticated on public.role_permissions
  for select
  using (auth.role() = 'authenticated');

-- profiles: a user can always read/update their own profile. Committee
-- executives with member.view can read profiles within their own mosque.
create policy profiles_select_self on public.profiles
  for select
  using (auth_user_id = auth.uid());

create policy profiles_select_same_mosque_with_permission on public.profiles
  for select
  using (
    mosque_id = public.current_mosque_id()
    and public.has_permission('member.view')
  );

create policy profiles_update_self on public.profiles
  for update
  using (auth_user_id = auth.uid())
  with check (auth_user_id = auth.uid());

-- user_roles / overrides: visible only within the same tenant, and only to
-- users who can manage members (role assignment is a privileged action).
create policy user_roles_select_same_mosque on public.user_roles
  for select
  using (mosque_id = public.current_mosque_id());

create policy user_roles_write_with_permission on public.user_roles
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.update'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.update'));

create policy user_permission_overrides_select_same_mosque on public.user_permission_overrides
  for select
  using (mosque_id = public.current_mosque_id());

create policy user_permission_overrides_write_president_only on public.user_permission_overrides
  for all
  using (
    mosque_id = public.current_mosque_id()
    and exists (
      select 1 from public.user_roles ur
      join public.roles r on r.id = ur.role_id
      where ur.profile_id = public.current_profile_id() and r.code = 'president'
    )
  )
  with check (
    mosque_id = public.current_mosque_id()
    and exists (
      select 1 from public.user_roles ur
      join public.roles r on r.id = ur.role_id
      where ur.profile_id = public.current_profile_id() and r.code = 'president'
    )
  );

comment on policy mosques_select_own on public.mosques is
  'No client can select another mosque''s row; there is intentionally no client-facing insert/update policy here.';
