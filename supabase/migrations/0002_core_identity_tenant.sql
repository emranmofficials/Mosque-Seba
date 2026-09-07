-- 0002_core_identity_tenant.sql
-- Core identity/tenant tables. Derived from requirements/PRD_v2.0_FINAL.md §4, §5, §30.

create table if not exists public.mosques (
  id                    uuid primary key default gen_random_uuid(),
  committee_id          text not null unique,               -- human-facing business ID
  name_bn               text not null,
  name_en               text,
  logo_url              text,
  address               text,
  latitude              numeric(9,6),
  longitude             numeric(9,6),
  established_year      smallint,
  committee_start_year  smallint,
  committee_end_year    smallint,
  status                text not null default 'pending'
                          check (status in ('pending','active','suspended','archived')),
  created_at            timestamptz not null default now(),
  updated_at            timestamptz not null default now(),
  archived_at           timestamptz
);

create table if not exists public.roles (
  id          uuid primary key default gen_random_uuid(),
  code        text not null unique,   -- e.g. 'president', 'general_member', 'imam', 'system_super_admin'
  name_bn     text not null,
  name_en     text,
  is_platform boolean not null default false, -- true only for System Super Admin
  created_at  timestamptz not null default now()
);

create table if not exists public.permissions (
  id          uuid primary key default gen_random_uuid(),
  code        text not null unique,   -- e.g. 'finance.approve', 'staff.salary.view'
  domain      text not null,          -- e.g. 'finance', 'staff', 'madrasa'
  description text,
  created_at  timestamptz not null default now()
);

create table if not exists public.role_permissions (
  role_id       uuid not null references public.roles(id) on delete cascade,
  permission_id uuid not null references public.permissions(id) on delete cascade,
  primary key (role_id, permission_id)
);

create table if not exists public.profiles (
  id            uuid primary key default gen_random_uuid(),
  auth_user_id  uuid not null unique,               -- references auth.users(id), Supabase-managed
  mosque_id     uuid references public.mosques(id) on delete restrict,
  member_id     text unique,                        -- human-facing Member ID, nullable until assigned
  name_bn       text not null,
  name_en       text,
  email         text,
  mobile        text,                                -- Bangladesh mobile, used for member login
  photo_url     text,
  nid           text,                                -- sensitive: mask/least-privilege per docs/SECURITY_MODEL.md
  status        text not null default 'pending'
                  check (status in ('pending','active','inactive','suspended','archived')),
  password_change_required boolean not null default true,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  archived_at   timestamptz
);

create index if not exists idx_profiles_mosque_id on public.profiles(mosque_id);

create table if not exists public.user_roles (
  profile_id  uuid not null references public.profiles(id) on delete cascade,
  role_id     uuid not null references public.roles(id) on delete cascade,
  mosque_id   uuid not null references public.mosques(id) on delete cascade,
  assigned_at timestamptz not null default now(),
  primary key (profile_id, role_id)
);

create index if not exists idx_user_roles_mosque_id on public.user_roles(mosque_id);

create table if not exists public.user_permission_overrides (
  profile_id    uuid not null references public.profiles(id) on delete cascade,
  permission_id uuid not null references public.permissions(id) on delete cascade,
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  allowed       boolean not null,   -- true = explicit grant, false = explicit revoke
  reason        text,
  created_by    uuid references public.profiles(id),
  created_at    timestamptz not null default now(),
  primary key (profile_id, permission_id)
);

create index if not exists idx_user_permission_overrides_mosque_id on public.user_permission_overrides(mosque_id);

comment on table public.mosques is 'One row per mosque tenant. All other tenant-owned tables key off mosque_id (PRD §31).';
comment on table public.profiles is 'One profile per authenticated user; one user = one mosque tenant (PRD §4). NID is sensitive data (PRD §36).';
comment on column public.profiles.password_change_required is
  'PRD §4: suggested password (last 6 digits of mobile) is a suggestion only; first login should force a change.';
