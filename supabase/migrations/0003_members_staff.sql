-- 0003_members_staff.sql
-- Member and staff management. Derived from requirements/PRD_v2.0_FINAL.md §7, §10.

create table if not exists public.members (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  profile_id        uuid references public.profiles(id) on delete set null,
  member_code       text not null,               -- human-facing Member ID, unique per mosque
  name_bn           text not null,
  name_en           text,
  mobile            text,
  email             text,
  nid               text,                        -- sensitive
  address           text,
  village_area      text,
  role_code         text references public.roles(code),
  monthly_chanda    numeric(12,2) not null default 0 check (monthly_chanda >= 0),
  join_date         date not null default current_date,
  photo_url         text,
  status            text not null default 'active'
                      check (status in ('active','inactive','archived')),
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  archived_at       timestamptz,
  unique (mosque_id, member_code)
);

create index if not exists idx_members_mosque_id on public.members(mosque_id);
create index if not exists idx_members_status on public.members(mosque_id, status);

create table if not exists public.member_preferences (
  member_id      uuid primary key references public.members(id) on delete cascade,
  mosque_id      uuid not null references public.mosques(id) on delete cascade,
  language       text not null default 'bn',
  notify_prayer  boolean not null default true,
  notify_notice  boolean not null default true,
  updated_at     timestamptz not null default now()
);

create table if not exists public.staff (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  member_id         uuid references public.members(id) on delete set null,
  role_code         text not null references public.roles(code)
                      check (role_code in ('imam','muazzin','khadem','other_staff')),
  designation_bn    text,
  designation_en    text,
  employment_type   text not null default 'full_time'
                      check (employment_type in ('full_time','part_time','contract')),
  join_date         date not null default current_date,
  end_date          date,
  salary_amount     numeric(12,2) not null default 0 check (salary_amount >= 0), -- sensitive: staff.salary.view
  salary_frequency  text not null default 'monthly'
                      check (salary_frequency in ('monthly','weekly','one_time')),
  meal_required     boolean not null default false,
  is_active         boolean not null default true,
  notes             text,
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  check (end_date is null or end_date >= join_date)
);

create index if not exists idx_staff_mosque_id on public.staff(mosque_id);

create table if not exists public.staff_availability (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  staff_id      uuid not null references public.staff(id) on delete cascade,
  range_start   date not null,
  range_end     date not null,
  status        text not null check (status in ('available','on_leave','requested_leave','unavailable')),
  reason        text,
  substitute_staff_id uuid references public.staff(id),
  approved_by   uuid references public.profiles(id),
  approved_at   timestamptz,
  created_at    timestamptz not null default now(),
  check (range_end >= range_start)
);

create index if not exists idx_staff_availability_staff_range
  on public.staff_availability(staff_id, range_start, range_end);

-- Prevent overlapping availability/leave ranges for the same staff member (server-side, PRD §10).
create extension if not exists btree_gist;
alter table public.staff_availability
  add constraint no_overlapping_availability
  exclude using gist (
    staff_id with =,
    daterange(range_start, range_end, '[]') with &&
  );

comment on column public.staff.salary_amount is 'Sensitive: gated by staff.salary.view permission (docs/PERMISSION_MATRIX.md).';
comment on constraint no_overlapping_availability on public.staff_availability is
  'PRD §10: server-side overlap checks mandatory for staff availability/leave.';
