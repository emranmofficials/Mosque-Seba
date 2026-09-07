-- 0003_helper_functions.sql
-- Security-helper functions used by every RLS policy and RPC after this
-- point. Must run after 0002_core_identity_tenant.sql (queries public.profiles,
-- public.user_roles, public.user_permission_overrides, public.permissions,
-- public.role_permissions). Derived from requirements/PRD_v2.0_FINAL.md §5,
-- §31 and docs/SECURITY_MODEL.md.
--
-- IMPORTANT — why these are SECURITY DEFINER, not SECURITY INVOKER:
-- public.profiles, public.user_roles, public.user_permission_overrides,
-- public.permissions, and public.role_permissions all have RLS enabled
-- (see supabase/policies/0001_core_identity_tenant_rls.sql), and several of
-- their own SELECT policies call current_mosque_id()/has_permission() to
-- decide row visibility. If these helper functions were SECURITY INVOKER,
-- calling them from inside a policy re-triggers RLS on the very tables they
-- query, which calls the same functions again — infinite recursion
-- ("stack depth limit exceeded"). This was caught by a local apply+query
-- test (see supabase/README.md "Verifying"), not by the CREATE statements
-- alone, which succeed either way.
--
-- Making them SECURITY DEFINER (owned by a role with full table access,
-- e.g. postgres, and NOT BYPASSRLS-independent — the function body simply
-- runs as the owner) breaks the cycle: the function reads profiles/roles
-- tables directly without re-evaluating their RLS policies. This is safe
-- specifically because each function returns only a scalar (an id, or a
-- boolean) derived from auth.uid()'s own row — it never lets the caller
-- read someone else's raw profile/role/permission data.

-- ---------------------------------------------------------------------------
-- current_profile_id(): the profiles.id row for the authenticated user.
-- One auth user = one profile (PRD §4: "One user = one mosque tenant").
-- ---------------------------------------------------------------------------
create or replace function public.current_profile_id()
returns uuid
language sql
stable
security definer
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
security definer
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
security definer
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

comment on function public.current_profile_id() is
  'SECURITY DEFINER: reads public.profiles directly to avoid RLS self-recursion (see file header). Returns only the caller''s own profile id.';
comment on function public.current_mosque_id() is
  'SECURITY DEFINER: reads public.profiles directly to avoid RLS self-recursion (see file header). Tenant id derived from the authenticated session only — never trust a client-supplied mosque_id (PRD §31).';
comment on function public.has_permission(text) is
  'SECURITY DEFINER: reads permission-related tables directly to avoid RLS self-recursion (see file header). Role permission OR individual override, per the authorization formula in docs/SECURITY_MODEL.md. Does not evaluate subscription/relationship axes.';

revoke all on function public.current_profile_id() from public;
revoke all on function public.current_mosque_id() from public;
revoke all on function public.has_permission(text) from public;
grant execute on function public.current_profile_id() to authenticated;
grant execute on function public.current_mosque_id() to authenticated;
grant execute on function public.has_permission(text) to authenticated;
