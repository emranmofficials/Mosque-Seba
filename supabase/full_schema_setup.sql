-- ============================================================================
-- 🕌 MOSQUE SEBA (মসজিদ সেবা) — FULL SUPABASE BACKEND MASTER SCHEMA SETUP
-- Single-Click Database Setup Script for Supabase SQL Editor
-- Architecture: PostgreSQL 16+, Row-Level Security (RLS), Multi-Tenant, SSoT
-- ============================================================================



-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/migrations/0001_extensions_and_helpers.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0001_extensions_and_helpers.sql
-- Extensions only. The security-helper functions (current_profile_id,
-- current_mosque_id, has_permission) live in 0003_helper_functions.sql,
-- AFTER the tables they query (public.profiles etc.) are created in
-- 0002_core_identity_tenant.sql. Do not move them back here — that ordering
-- bug was caught by a local apply test (see supabase/README.md "Verifying").

create extension if not exists "uuid-ossp";
create extension if not exists pgcrypto;


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/migrations/0002_core_identity_tenant.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/migrations/0003_helper_functions.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/migrations/0004_members_staff.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/migrations/0005_finance_chanda_payroll.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0004_finance_chanda_payroll.sql
-- Finance, chanda (monthly subscription), and payroll. Derived from
-- requirements/PRD_v2.0_FINAL.md §8, §9, §10, §30.

create table if not exists public.finance_categories (
  id          uuid primary key default gen_random_uuid(),
  mosque_id   uuid not null references public.mosques(id) on delete cascade,
  type        text not null check (type in ('income','expense')),
  code        text not null,   -- e.g. 'chanda','donation','madrasa_fee','jummah','electricity','salary'
  name_bn     text not null,
  name_en     text,
  is_active   boolean not null default true,
  created_at  timestamptz not null default now(),
  unique (mosque_id, type, code)
);

create table if not exists public.approval_policies (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  max_amount        numeric(12,2) not null,   -- inclusive ceiling for this tier
  required_role_code text not null references public.roles(code),
  tier_label        text,                     -- e.g. 'assigned_authority','president','emergency','higher_committee'
  created_at        timestamptz not null default now(),
  check (max_amount >= 0)
);

create index if not exists idx_approval_policies_mosque_amount
  on public.approval_policies(mosque_id, max_amount);

create table if not exists public.transactions (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  transaction_code  text not null,             -- human-facing transaction ID
  category_id       uuid not null references public.finance_categories(id),
  type              text not null check (type in ('income','expense')),
  amount            numeric(12,2) not null check (amount > 0),
  txn_date          date not null default current_date,
  description       text,
  source_type       text,                      -- e.g. 'chanda_payment','fee_payment','manual','payroll'
  source_id         uuid,                      -- id of the originating record, when applicable
  attachment_url    text,
  created_by        uuid not null references public.profiles(id),
  approved_by       uuid references public.profiles(id),
  approval_status   text not null default 'pending'
                      check (approval_status in ('pending','approved','rejected')),
  reversed_by_id    uuid references public.transactions(id), -- links a reversal to the original
  is_reversal       boolean not null default false,
  idempotency_key   text,
  created_at        timestamptz not null default now(),
  approved_at       timestamptz,
  unique (mosque_id, transaction_code),
  -- Self-approval prohibition (PRD §9, docs/SECURITY_MODEL.md):
  check (approved_by is null or approved_by <> created_by),
  -- Double-post prevention for anything originating from another module (PRD §8, §20):
  unique (mosque_id, source_type, source_id)
);

create index if not exists idx_transactions_mosque_id on public.transactions(mosque_id, txn_date);
create index if not exists idx_transactions_idempotency on public.transactions(mosque_id, idempotency_key);

comment on constraint transactions_mosque_id_source_type_source_id_key on public.transactions is
  'PRD §8/§20: prevents duplicate posting of the same source record (chanda/fee payment, payroll, etc.) to Finance.';

create table if not exists public.financial_reconciliations (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  period_start  date not null,
  period_end    date not null,
  is_locked     boolean not null default false,   -- PRD §9: period lock mandatory
  locked_by     uuid references public.profiles(id),
  locked_at     timestamptz,
  notes         text,
  created_at    timestamptz not null default now(),
  check (period_end >= period_start)
);

create index if not exists idx_financial_reconciliations_mosque
  on public.financial_reconciliations(mosque_id, period_start, period_end);

-- ---------------------------------------------------------------------------
-- Chanda (monthly subscription)
-- ---------------------------------------------------------------------------

create table if not exists public.chanda_schedules (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  member_id     uuid not null references public.members(id) on delete cascade,
  amount        numeric(12,2) not null check (amount >= 0),
  effective_from date not null default current_date,
  effective_to   date,
  created_at    timestamptz not null default now(),
  check (effective_to is null or effective_to >= effective_from)
);

create index if not exists idx_chanda_schedules_member on public.chanda_schedules(member_id, effective_from);

create table if not exists public.chanda_payments (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  member_id         uuid not null references public.members(id) on delete cascade,
  month             date not null,              -- first day of the covered month
  amount_due        numeric(12,2) not null check (amount_due >= 0),
  amount_paid       numeric(12,2) not null default 0 check (amount_paid >= 0),
  method            text not null check (method in ('cash','mobile_banking','online')),
  status            text not null default 'pending'
                      check (status in ('pending','paid','partial','verified')),
  receipt_code      text unique,                -- human-facing receipt ID
  qr_verification_code text unique,
  received_by       uuid references public.profiles(id),
  verified_by       uuid references public.profiles(id),
  paid_at           timestamptz,
  created_at        timestamptz not null default now(),
  unique (mosque_id, member_id, month)           -- one ledger row per member per month
);

create index if not exists idx_chanda_payments_mosque_month on public.chanda_payments(mosque_id, month);

comment on table public.chanda_payments is
  'PRD §8: successful cash/mobile-banking payment is posted exactly once to transactions via source_type=''chanda_payment''.';

-- ---------------------------------------------------------------------------
-- Payroll (PRD §10 describes this flow; table added here to fulfil the
-- one-payment -> one-payroll-row -> one-expense-row invariant it specifies)
-- ---------------------------------------------------------------------------

create table if not exists public.payroll_payments (
  id             uuid primary key default gen_random_uuid(),
  mosque_id      uuid not null references public.mosques(id) on delete cascade,
  staff_id       uuid not null references public.staff(id) on delete cascade,
  period_start   date not null,
  period_end     date not null,
  amount         numeric(12,2) not null check (amount >= 0),
  status         text not null default 'pending'
                   check (status in ('pending','finance_pending','approved','finalized','rejected')),
  transaction_id uuid references public.transactions(id), -- the linked Finance expense
  created_by     uuid not null references public.profiles(id),
  created_at     timestamptz not null default now(),
  finalized_at   timestamptz,
  check (period_end >= period_start),
  unique (staff_id, period_start, period_end)
);

create index if not exists idx_payroll_payments_mosque on public.payroll_payments(mosque_id, period_start);

comment on column public.payroll_payments.transaction_id is
  'PRD §10: one salary payment creates exactly one linked payroll payment and one Finance expense.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/migrations/0006_notices_notifications_audit.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0005_notices_notifications_audit.sql
-- Notices, notifications/outbox, device tokens, and audit logging.
-- Derived from requirements/PRD_v2.0_FINAL.md §14, §27, §28, §36.

create table if not exists public.notices (
  id             uuid primary key default gen_random_uuid(),
  mosque_id      uuid not null references public.mosques(id) on delete cascade,
  title          text not null,
  content        text not null,                 -- rich content (markdown/HTML per client rendering)
  priority       text not null default 'normal' check (priority in ('normal','important','urgent')),
  attachment_url text,
  is_pinned      boolean not null default false,
  publish_at     date not null default current_date,
  expiry_at      date,
  ai_generated   boolean not null default false, -- true if drafted via AI assistant (PRD §14, §28)
  created_by     uuid not null references public.profiles(id),
  published_by   uuid references public.profiles(id),  -- a human must publish; never the AI (PRD §28)
  status         text not null default 'draft' check (status in ('draft','published','archived')),
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  check (expiry_at is null or expiry_at >= publish_at)
);

create index if not exists idx_notices_mosque_status on public.notices(mosque_id, status, publish_at);

create table if not exists public.notice_audiences (
  id         uuid primary key default gen_random_uuid(),
  notice_id  uuid not null references public.notices(id) on delete cascade,
  audience   text not null check (audience in ('all','committee_only','role','specific_user')),
  role_code  text references public.roles(code),   -- used when audience = 'role'
  profile_id uuid references public.profiles(id)   -- used when audience = 'specific_user'
);

create unique index if not exists uq_notice_audiences_target
  on public.notice_audiences (
    notice_id, audience, coalesce(role_code, ''), coalesce(profile_id::text, '')
  );

create table if not exists public.device_tokens (
  id          uuid primary key default gen_random_uuid(),
  profile_id  uuid not null references public.profiles(id) on delete cascade,
  mosque_id   uuid not null references public.mosques(id) on delete cascade,
  fcm_token   text not null,
  platform    text not null default 'android',
  status      text not null default 'active' check (status in ('active','refreshed','deactivated','logged_out')),
  registered_at timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  unique (profile_id, fcm_token)
);

create table if not exists public.event_outbox (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  event_type    text not null,      -- e.g. 'chanda.due','notice.published','payroll.finalized'
  payload       jsonb not null,
  status        text not null default 'pending' check (status in ('pending','processing','delivered','failed')),
  attempts      int not null default 0,
  created_at    timestamptz not null default now(),
  processed_at  timestamptz
);

create index if not exists idx_event_outbox_status on public.event_outbox(status, created_at);

create table if not exists public.notifications (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  profile_id    uuid not null references public.profiles(id) on delete cascade,
  type          text not null,      -- subscription_reminder, payment_approval, notice, meeting, prayer,
                                     -- duty, leave, attendance, exam_result, homework, fee, guardian_update
  title         text not null,
  body          text,
  source_event_id uuid references public.event_outbox(id),
  read_at       timestamptz,
  created_at    timestamptz not null default now()
);

create index if not exists idx_notifications_profile on public.notifications(profile_id, created_at);

create table if not exists public.notification_deliveries (
  id              uuid primary key default gen_random_uuid(),
  notification_id uuid not null references public.notifications(id) on delete cascade,
  device_token_id uuid not null references public.device_tokens(id) on delete cascade,
  status          text not null default 'pending' check (status in ('pending','sent','failed')),
  sent_at         timestamptz,
  error           text
);

-- ---------------------------------------------------------------------------
-- AI requests (PRD §28)
-- ---------------------------------------------------------------------------

create table if not exists public.ai_requests (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  requested_by  uuid not null references public.profiles(id),
  purpose       text not null check (purpose in ('notice_draft','meeting_minutes','report_summary','academic_summary','suggestion')),
  input_prompt  text not null,
  output_draft  text,
  status        text not null default 'pending'
                  check (status in ('pending','processing','completed','failed','rejected')),
  linked_notice_id uuid references public.notices(id),
  created_at    timestamptz not null default now(),
  completed_at  timestamptz
);

comment on table public.ai_requests is
  'PRD §28: AI may draft only. Publishing/approval/mutation of authoritative records always requires a separate human-attributed action.';

-- ---------------------------------------------------------------------------
-- Audit & security events (PRD §36)
-- ---------------------------------------------------------------------------

create table if not exists public.audit_logs (
  id           uuid primary key default gen_random_uuid(),
  mosque_id    uuid references public.mosques(id) on delete set null,
  actor_id     uuid references public.profiles(id),
  action       text not null,         -- e.g. 'member.create','finance.approve','role.assign'
  entity_type  text not null,
  entity_id    uuid,
  metadata     jsonb,
  request_id   text,
  created_at   timestamptz not null default now()
);

create index if not exists idx_audit_logs_mosque_created on public.audit_logs(mosque_id, created_at);
create index if not exists idx_audit_logs_entity on public.audit_logs(entity_type, entity_id);

create table if not exists public.security_events (
  id           uuid primary key default gen_random_uuid(),
  mosque_id    uuid references public.mosques(id) on delete set null,
  actor_id     uuid references public.profiles(id),
  event_type   text not null,   -- e.g. 'cross_tenant_attempt','permission_denied','forged_role_claim'
  details      jsonb,
  created_at   timestamptz not null default now()
);

create index if not exists idx_security_events_mosque_created on public.security_events(mosque_id, created_at);


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/policies/0001_core_identity_tenant_rls.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/policies/0002_members_staff_rls.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0002_members_staff_rls.sql
-- RLS for members, member_preferences, staff, staff_availability.

alter table public.members enable row level security;
alter table public.member_preferences enable row level security;
alter table public.staff enable row level security;
alter table public.staff_availability enable row level security;

-- members
create policy members_select_same_mosque on public.members
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.view'));

create policy members_select_self on public.members
  for select
  using (profile_id = public.current_profile_id());

create policy members_insert_with_permission on public.members
  for insert
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.create'));

create policy members_update_with_permission on public.members
  for update
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.update'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.update'));

-- Archival only, never a hard delete from the client (docs/DATABASE_ARCHITECTURE.md).
create policy members_archive_with_permission on public.members
  for update
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.archive'))
  with check (mosque_id = public.current_mosque_id());

-- member_preferences: owner or same-mosque admin with member.view
create policy member_preferences_select on public.member_preferences
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (
      public.has_permission('member.view')
      or member_id in (select id from public.members where profile_id = public.current_profile_id())
    )
  );

create policy member_preferences_update_self on public.member_preferences
  for update
  using (member_id in (select id from public.members where profile_id = public.current_profile_id()))
  with check (member_id in (select id from public.members where profile_id = public.current_profile_id()));

-- staff: general visibility via staff.view; salary columns are additionally
-- gated at the application/RPC layer by staff.salary.view (RLS alone cannot
-- hide individual columns — see docs/SECURITY_MODEL.md for the RPC pattern
-- used to serve a salary-redacted view to holders of staff.view only).
create policy staff_select_same_mosque on public.staff
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.view'));

create policy staff_write_with_permission on public.staff
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'));

create policy staff_availability_select on public.staff_availability
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.view'));

create policy staff_availability_write on public.staff_availability
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'));

comment on policy staff_select_same_mosque on public.staff is
  'RLS grants row visibility only. Salary amount must additionally be redacted at the RPC layer unless the caller also holds staff.salary.view.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/policies/0003_finance_chanda_payroll_rls.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0003_finance_chanda_payroll_rls.sql
-- RLS for finance_categories, approval_policies, transactions,
-- financial_reconciliations, chanda_schedules, chanda_payments, payroll_payments.

alter table public.finance_categories enable row level security;
alter table public.approval_policies enable row level security;
alter table public.transactions enable row level security;
alter table public.financial_reconciliations enable row level security;
alter table public.chanda_schedules enable row level security;
alter table public.chanda_payments enable row level security;
alter table public.payroll_payments enable row level security;

-- finance_categories / approval_policies: read for anyone with finance.view,
-- write only for finance.create-capable roles managing config (President/Treasurer).
create policy finance_categories_select on public.finance_categories
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy finance_categories_write on public.finance_categories
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.create'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('finance.create'));

create policy approval_policies_select on public.approval_policies
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy approval_policies_write on public.approval_policies
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'));

-- transactions
create policy transactions_select on public.transactions
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy transactions_insert on public.transactions
  for insert
  with check (
    mosque_id = public.current_mosque_id()
    and public.has_permission('finance.create')
    and created_by = public.current_profile_id()
  );

-- Approval is a distinct action from creation and is enforced again here:
-- self-approval is blocked both by the table CHECK constraint and this policy.
create policy transactions_approve on public.transactions
  for update
  using (
    mosque_id = public.current_mosque_id()
    and public.has_permission('finance.approve')
    and created_by <> public.current_profile_id()
  )
  with check (
    mosque_id = public.current_mosque_id()
    and approved_by = public.current_profile_id()
  );

-- No update/delete policy is defined for posted (approval_status = 'approved')
-- transactions beyond the approve action above: corrections must be modeled
-- as a new reversal/adjustment transaction (docs/DATABASE_ARCHITECTURE.md),
-- created through the standard insert policy with is_reversal = true.

create policy financial_reconciliations_select on public.financial_reconciliations
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy financial_reconciliations_write on public.financial_reconciliations
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'));

-- chanda
create policy chanda_schedules_select on public.chanda_schedules
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (
      public.has_permission('chanda.view')
      or member_id in (select id from public.members where profile_id = public.current_profile_id())
    )
  );

create policy chanda_schedules_write on public.chanda_schedules
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.update'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.update'));

create policy chanda_payments_select on public.chanda_payments
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (
      public.has_permission('chanda.view')
      or public.has_permission('chanda.receive')
      or member_id in (select id from public.members where profile_id = public.current_profile_id())
    )
  );

-- Members can create their own pending payment record ("চাঁদা পরিশোধ করুন");
-- admins with chanda.receive can create/verify on behalf of a member.
create policy chanda_payments_insert_self on public.chanda_payments
  for insert
  with check (
    mosque_id = public.current_mosque_id()
    and member_id in (select id from public.members where profile_id = public.current_profile_id())
  );

create policy chanda_payments_insert_admin on public.chanda_payments
  for insert
  with check (mosque_id = public.current_mosque_id() and public.has_permission('chanda.receive'));

create policy chanda_payments_update_admin on public.chanda_payments
  for update
  using (mosque_id = public.current_mosque_id() and public.has_permission('chanda.receive'))
  with check (mosque_id = public.current_mosque_id());

-- payroll
create policy payroll_payments_select on public.payroll_payments
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.salary.view'));

create policy payroll_payments_write on public.payroll_payments
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.salary.pay'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('staff.salary.pay'));


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/policies/0004_notices_notifications_audit_rls.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0004_notices_notifications_audit_rls.sql
-- RLS for notices, notice_audiences, device_tokens, event_outbox,
-- notifications, notification_deliveries, ai_requests, audit_logs, security_events.

alter table public.notices enable row level security;
alter table public.notice_audiences enable row level security;
alter table public.device_tokens enable row level security;
alter table public.event_outbox enable row level security;
alter table public.notifications enable row level security;
alter table public.notification_deliveries enable row level security;
alter table public.ai_requests enable row level security;
alter table public.audit_logs enable row level security;
alter table public.security_events enable row level security;

-- notices: published notices are visible to everyone in the tenant; drafts
-- are visible only to holders of notice.manage.
create policy notices_select_published on public.notices
  for select
  using (mosque_id = public.current_mosque_id() and status = 'published');

create policy notices_select_draft_with_permission on public.notices
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('notice.manage'));

create policy notices_write_with_permission on public.notices
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('notice.manage'))
  with check (
    mosque_id = public.current_mosque_id()
    and public.has_permission('notice.manage')
    -- An AI-authored draft cannot self-publish: publishing requires published_by
    -- to be set to a human profile explicitly, never left to a default/trigger
    -- driven purely by ai_generated = true (PRD §14, §28).
    and (status <> 'published' or published_by is not null)
  );

create policy notice_audiences_select on public.notice_audiences
  for select
  using (
    notice_id in (
      select id from public.notices
      where mosque_id = public.current_mosque_id()
        and (status = 'published' or public.has_permission('notice.manage'))
    )
  );

create policy notice_audiences_write on public.notice_audiences
  for all
  using (
    notice_id in (
      select id from public.notices
      where mosque_id = public.current_mosque_id() and public.has_permission('notice.manage')
    )
  )
  with check (
    notice_id in (
      select id from public.notices
      where mosque_id = public.current_mosque_id() and public.has_permission('notice.manage')
    )
  );

-- device_tokens: a user manages only their own tokens.
create policy device_tokens_own on public.device_tokens
  for all
  using (profile_id = public.current_profile_id())
  with check (profile_id = public.current_profile_id() and mosque_id = public.current_mosque_id());

-- event_outbox: internal/system table — not directly readable or writable by
-- client roles; only service-role/Edge Functions (which bypass RLS) touch it.
create policy event_outbox_no_client_access on public.event_outbox
  for all
  using (false)
  with check (false);

-- notifications: a user can read/mark-read only their own notifications.
create policy notifications_select_own on public.notifications
  for select
  using (profile_id = public.current_profile_id());

create policy notifications_update_own_read_state on public.notifications
  for update
  using (profile_id = public.current_profile_id())
  with check (profile_id = public.current_profile_id());

create policy notification_deliveries_select_own on public.notification_deliveries
  for select
  using (
    notification_id in (
      select id from public.notifications where profile_id = public.current_profile_id()
    )
  );

-- ai_requests: visible to the requester and to holders of madrasa.ai.use /
-- notice.manage within the same tenant; writes go through a secure RPC that
-- enforces the human-in-the-loop rule, not direct client insert of results.
create policy ai_requests_select on public.ai_requests
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (requested_by = public.current_profile_id() or public.has_permission('notice.manage'))
  );

create policy ai_requests_insert_own on public.ai_requests
  for insert
  with check (
    mosque_id = public.current_mosque_id()
    and requested_by = public.current_profile_id()
    and public.has_permission('madrasa.ai.use')
  );

-- audit_logs / security_events: read-only, and only for roles that can view
-- finance reports or hold platform-level access; never client-writable
-- (writes happen exclusively via SECURITY DEFINER audit-logging functions).
create policy audit_logs_select_with_permission on public.audit_logs
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.report'));

create policy audit_logs_no_client_write on public.audit_logs
  for insert
  with check (false);

create policy security_events_no_client_access on public.security_events
  for all
  using (false)
  with check (false);


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0001_log_audit_event.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0001_log_audit_event.sql
-- Internal helper, SECURITY DEFINER, used by every other RPC in this
-- directory to write to audit_logs (which has no client insert policy —
-- see supabase/policies/0004_notices_notifications_audit_rls.sql).
-- Derived from requirements/PRD_v2.0_FINAL.md §36 and docs/SECURITY_MODEL.md.

create or replace function public.log_audit_event(
  p_mosque_id   uuid,
  p_action      text,
  p_entity_type text,
  p_entity_id   uuid,
  p_metadata    jsonb default null,
  p_request_id  text default null
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_id uuid;
begin
  insert into public.audit_logs (mosque_id, actor_id, action, entity_type, entity_id, metadata, request_id)
  values (p_mosque_id, public.current_profile_id(), p_action, p_entity_type, p_entity_id, p_metadata, p_request_id)
  returning id into v_id;

  return v_id;
end;
$$;

revoke all on function public.log_audit_event(uuid, text, text, uuid, jsonb, text) from public;
grant execute on function public.log_audit_event(uuid, text, text, uuid, jsonb, text) to authenticated;

comment on function public.log_audit_event is
  'SECURITY DEFINER helper: the only writer of audit_logs. Never call with a mosque_id other than public.current_mosque_id() from a client-facing RPC.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0002_generate_business_code.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0002_generate_business_code.sql
-- Generates sequential, human-facing business IDs like RCPT-2026-000123,
-- TXN-2026-000045, STU-2026-0001, scoped per mosque + code series + year.
-- Derived from requirements/PRD_v2.0_FINAL.md §16 (STU-YYYY-0001 pattern)
-- and the general "separate business IDs" rule in docs/DATABASE_ARCHITECTURE.md.

create table if not exists public.mosque_sequences (
  mosque_id   uuid not null references public.mosques(id) on delete cascade,
  series      text not null,   -- e.g. 'transaction', 'chanda_receipt', 'student', 'payroll'
  year        smallint not null,
  last_value  integer not null default 0,
  primary key (mosque_id, series, year)
);

alter table public.mosque_sequences enable row level security;

-- Not directly readable/writable by clients; only via generate_business_code().
create policy mosque_sequences_no_client_access on public.mosque_sequences
  for all
  using (false)
  with check (false);

create or replace function public.generate_business_code(
  p_mosque_id uuid,
  p_series    text,
  p_prefix    text,
  p_pad_width int default 6
)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
  v_year  smallint := extract(year from now())::smallint;
  v_value integer;
begin
  insert into public.mosque_sequences (mosque_id, series, year, last_value)
  values (p_mosque_id, p_series, v_year, 1)
  on conflict (mosque_id, series, year)
  do update set last_value = public.mosque_sequences.last_value + 1
  returning last_value into v_value;

  return p_prefix || '-' || v_year::text || '-' || lpad(v_value::text, p_pad_width, '0');
end;
$$;

revoke all on function public.generate_business_code(uuid, text, text, int) from public;
grant execute on function public.generate_business_code(uuid, text, text, int) to authenticated;

comment on function public.generate_business_code is
  'Atomic, per-mosque, per-series, per-year sequence generator for human-facing IDs (e.g. TXN-2026-000123). Concurrency-safe via ON CONFLICT ... DO UPDATE.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0003_request_chanda_payment.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0003_request_chanda_payment.sql
-- Member-initiated "চাঁদা পরিশোধ করুন" flow. Creates/updates a chanda_payments
-- row in 'pending' status. Does NOT post to Finance — only an admin action
-- (public.receive_chanda_payment, cash) or a verified gateway callback posts
-- to Finance, per requirements/PRD_v2.0_FINAL.md §8.

create or replace function public.request_chanda_payment(
  p_month           date,
  p_amount          numeric,
  p_method          text,
  p_idempotency_key text
)
returns public.chanda_payments
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id uuid := public.current_mosque_id();
  v_member_id uuid;
  v_row public.chanda_payments;
begin
  if v_mosque_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  select id into v_member_id
  from public.members
  where mosque_id = v_mosque_id and profile_id = public.current_profile_id();

  if v_member_id is null then
    raise exception 'PERMISSION_DENIED: no member record for current user' using errcode = '42501';
  end if;

  if p_amount <= 0 then
    raise exception 'INVALID_AMOUNT' using errcode = '22003';
  end if;

  if p_method not in ('cash','mobile_banking','online') then
    raise exception 'VALIDATION_ERROR: invalid method' using errcode = '22023';
  end if;

  insert into public.chanda_payments (
    mosque_id, member_id, month, amount_due, amount_paid, method, status
  )
  values (
    v_mosque_id, v_member_id, date_trunc('month', p_month)::date, p_amount, p_amount, p_method, 'pending'
  )
  on conflict (mosque_id, member_id, month)
  do update set
    amount_paid = excluded.amount_paid,
    method      = excluded.method,
    status      = 'pending'
  where public.chanda_payments.status <> 'verified'   -- never overwrite an already-verified month
  returning * into v_row;

  if v_row.id is null then
    raise exception 'PAYMENT_ALREADY_PROCESSED' using errcode = '23505';
  end if;

  perform public.log_audit_event(
    v_mosque_id, 'chanda.request', 'chanda_payments', v_row.id,
    jsonb_build_object('idempotency_key', p_idempotency_key, 'amount', p_amount, 'method', p_method)
  );

  return v_row;
end;
$$;

revoke all on function public.request_chanda_payment(date, numeric, text, text) from public;
grant execute on function public.request_chanda_payment(date, numeric, text, text) to authenticated;


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0004_receive_chanda_payment.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0004_receive_chanda_payment.sql
-- Admin "চাঁদা গ্রহণ করুন" flow: verifies/receives a chanda payment and posts
-- it to Finance Income exactly once, generating an official receipt with QR
-- verification code. Derived from requirements/PRD_v2.0_FINAL.md §8, §9.
--
-- Idempotency & double-post prevention: this function is safe to retry with
-- the same p_idempotency_key — if a transaction already exists for this
-- chanda_payment (source_type/source_id unique constraint), the existing
-- transaction is returned rather than a duplicate being created.

create or replace function public.receive_chanda_payment(
  p_chanda_payment_id uuid,
  p_idempotency_key    text
)
returns public.transactions
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id     uuid := public.current_mosque_id();
  v_actor_id      uuid := public.current_profile_id();
  v_payment       public.chanda_payments;
  v_category_id   uuid;
  v_txn           public.transactions;
  v_txn_code      text;
  v_receipt_code  text;
  v_qr_code       text;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('chanda.receive') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  select * into v_payment
  from public.chanda_payments
  where id = p_chanda_payment_id and mosque_id = v_mosque_id
  for update;   -- lock the row to avoid concurrent double-receive

  if v_payment.id is null then
    raise exception 'VALIDATION_ERROR: chanda payment not found for this mosque' using errcode = '22023';
  end if;

  -- Idempotent replay: a transaction already exists for this payment.
  select * into v_txn
  from public.transactions
  where mosque_id = v_mosque_id
    and source_type = 'chanda_payment'
    and source_id = v_payment.id;

  if v_txn.id is not null then
    return v_txn;
  end if;

  if v_payment.status = 'verified' then
    raise exception 'PAYMENT_ALREADY_PROCESSED' using errcode = '23505';
  end if;

  select id into v_category_id
  from public.finance_categories
  where mosque_id = v_mosque_id and type = 'income' and code = 'chanda'
  limit 1;

  if v_category_id is null then
    raise exception 'VALIDATION_ERROR: chanda income category not configured for this mosque' using errcode = '22023';
  end if;

  v_txn_code     := public.generate_business_code(v_mosque_id, 'transaction', 'TXN');
  v_receipt_code := public.generate_business_code(v_mosque_id, 'chanda_receipt', 'RCPT');
  v_qr_code      := encode(gen_random_bytes(16), 'hex');

  insert into public.transactions (
    mosque_id, transaction_code, category_id, type, amount, txn_date,
    description, source_type, source_id, created_by, approved_by,
    approval_status, idempotency_key
  )
  values (
    v_mosque_id, v_txn_code, v_category_id, 'income', v_payment.amount_paid, current_date,
    'Chanda payment received', 'chanda_payment', v_payment.id, v_actor_id, null,
    'approved', p_idempotency_key
  )
  returning * into v_txn;

  update public.chanda_payments
  set status = 'verified',
      receipt_code = v_receipt_code,
      qr_verification_code = v_qr_code,
      received_by = v_actor_id,
      verified_by = v_actor_id,
      paid_at = now()
  where id = v_payment.id;

  perform public.log_audit_event(
    v_mosque_id, 'chanda.receive', 'transactions', v_txn.id,
    jsonb_build_object('chanda_payment_id', v_payment.id, 'receipt_code', v_receipt_code)
  );

  return v_txn;
end;
$$;

revoke all on function public.receive_chanda_payment(uuid, text) from public;
grant execute on function public.receive_chanda_payment(uuid, text) to authenticated;

comment on function public.receive_chanda_payment is
  'Only path that posts a chanda payment to Finance Income. Chanda income category (finance_categories: type=income, code=chanda) must exist per mosque before use. approved_by is intentionally left null: routine chanda posting is an automatic system entry (PRD §8), not a discretionary transaction subject to the tiered approval workflow in PRD §9 (see submit_finance_transaction / approve_finance_transaction for that flow).';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0005_submit_finance_transaction.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0005_submit_finance_transaction.sql
-- Creates a pending income/expense transaction (PRD §9). This is the
-- discretionary path (donations, expenses, etc.) — distinct from the
-- automatic chanda/fee posting in 0004_receive_chanda_payment.sql, which
-- bypasses this approval queue entirely for routine small postings.

create or replace function public.submit_finance_transaction(
  p_category_id     uuid,
  p_type            text,
  p_amount          numeric,
  p_txn_date        date,
  p_description     text,
  p_attachment_url  text,
  p_idempotency_key text
)
returns public.transactions
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id uuid := public.current_mosque_id();
  v_actor_id  uuid := public.current_profile_id();
  v_txn_code  text;
  v_txn       public.transactions;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('finance.create') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  if p_amount <= 0 then
    raise exception 'INVALID_AMOUNT' using errcode = '22003';
  end if;

  if p_type not in ('income','expense') then
    raise exception 'VALIDATION_ERROR: invalid type' using errcode = '22023';
  end if;

  -- Idempotent replay guard.
  select * into v_txn
  from public.transactions
  where mosque_id = v_mosque_id and idempotency_key = p_idempotency_key;

  if v_txn.id is not null then
    return v_txn;
  end if;

  v_txn_code := public.generate_business_code(v_mosque_id, 'transaction', 'TXN');

  insert into public.transactions (
    mosque_id, transaction_code, category_id, type, amount, txn_date,
    description, attachment_url, created_by, approval_status, idempotency_key
  )
  values (
    v_mosque_id, v_txn_code, p_category_id, p_type, p_amount, coalesce(p_txn_date, current_date),
    p_description, p_attachment_url, v_actor_id, 'pending', p_idempotency_key
  )
  returning * into v_txn;

  perform public.log_audit_event(
    v_mosque_id, 'finance.submit', 'transactions', v_txn.id,
    jsonb_build_object('type', p_type, 'amount', p_amount)
  );

  return v_txn;
end;
$$;

revoke all on function public.submit_finance_transaction(uuid, text, numeric, date, text, text, text) from public;
grant execute on function public.submit_finance_transaction(uuid, text, numeric, date, text, text, text) to authenticated;


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0006_approve_finance_transaction.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0006_approve_finance_transaction.sql
-- Approves a pending transaction according to the mosque's approval_policies
-- tiers, blocking self-approval. Derived from requirements/PRD_v2.0_FINAL.md §9.

create or replace function public.approve_finance_transaction(
  p_transaction_id uuid,
  p_decision       text   -- 'approved' or 'rejected'
)
returns public.transactions
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id      uuid := public.current_mosque_id();
  v_actor_id       uuid := public.current_profile_id();
  v_txn            public.transactions;
  v_required_role  text;
  v_actor_has_role boolean;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('finance.approve') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  if p_decision not in ('approved','rejected') then
    raise exception 'VALIDATION_ERROR: invalid decision' using errcode = '22023';
  end if;

  select * into v_txn
  from public.transactions
  where id = p_transaction_id and mosque_id = v_mosque_id
  for update;

  if v_txn.id is null then
    raise exception 'VALIDATION_ERROR: transaction not found for this mosque' using errcode = '22023';
  end if;

  if v_txn.approval_status <> 'pending' then
    raise exception 'PAYMENT_ALREADY_PROCESSED' using errcode = '23505';
  end if;

  -- Self-approval prohibition (also enforced at the RLS + CHECK-constraint layer).
  if v_txn.created_by = v_actor_id then
    raise exception 'SELF_APPROVAL_NOT_ALLOWED' using errcode = '42501';
  end if;

  if p_decision = 'approved' then
    -- Determine the minimum-authority tier that covers this amount.
    select required_role_code into v_required_role
    from public.approval_policies
    where mosque_id = v_mosque_id and max_amount >= v_txn.amount
    order by max_amount asc
    limit 1;

    if v_required_role is not null then
      select exists (
        select 1 from public.user_roles ur
        join public.roles r on r.id = ur.role_id
        where ur.profile_id = v_actor_id and r.code = v_required_role
      ) into v_actor_has_role;

      if not coalesce(v_actor_has_role, false) then
        raise exception 'FINANCE_APPROVAL_REQUIRED: requires role %', v_required_role using errcode = '42501';
      end if;
    end if;
  end if;

  update public.transactions
  set approval_status = p_decision,
      approved_by = v_actor_id,
      approved_at = now()
  where id = p_transaction_id
  returning * into v_txn;

  perform public.log_audit_event(
    v_mosque_id, 'finance.' || p_decision, 'transactions', v_txn.id,
    jsonb_build_object('amount', v_txn.amount, 'required_role', v_required_role)
  );

  return v_txn;
end;
$$;

revoke all on function public.approve_finance_transaction(uuid, text) from public;
grant execute on function public.approve_finance_transaction(uuid, text) to authenticated;

comment on function public.approve_finance_transaction is
  'PRD §9: self-approval blocked; approval authority tier resolved from approval_policies by amount. Rejects with FINANCE_APPROVAL_REQUIRED if the caller lacks the required role for this amount tier, even if they generically hold finance.approve.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0007_reverse_finance_transaction.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0007_reverse_finance_transaction.sql
-- Corrections to a posted (approved) transaction must never mutate or delete
-- the original row (docs/DATABASE_ARCHITECTURE.md). This creates an
-- equal-and-opposite reversal transaction linked back to the original.

create or replace function public.reverse_finance_transaction(
  p_transaction_id uuid,
  p_reason         text
)
returns public.transactions
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id uuid := public.current_mosque_id();
  v_actor_id  uuid := public.current_profile_id();
  v_original  public.transactions;
  v_reversal  public.transactions;
  v_txn_code  text;
  v_opposite_type text;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('finance.approve') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  select * into v_original
  from public.transactions
  where id = p_transaction_id and mosque_id = v_mosque_id and approval_status = 'approved'
  for update;

  if v_original.id is null then
    raise exception 'VALIDATION_ERROR: approved transaction not found for this mosque' using errcode = '22023';
  end if;

  if v_original.reversed_by_id is not null then
    raise exception 'PAYMENT_ALREADY_PROCESSED: transaction already reversed' using errcode = '23505';
  end if;

  v_opposite_type := case when v_original.type = 'income' then 'expense' else 'income' end;
  v_txn_code := public.generate_business_code(v_mosque_id, 'transaction', 'TXN');

  insert into public.transactions (
    mosque_id, transaction_code, category_id, type, amount, txn_date,
    description, created_by, approved_by, approval_status, is_reversal
  )
  values (
    v_mosque_id, v_txn_code, v_original.category_id, v_opposite_type, v_original.amount, current_date,
    coalesce(p_reason, 'Reversal of ' || v_original.transaction_code),
    v_actor_id, null, 'approved', true
  )
  returning * into v_reversal;

  update public.transactions set reversed_by_id = v_reversal.id where id = v_original.id;

  perform public.log_audit_event(
    v_mosque_id, 'finance.reverse', 'transactions', v_reversal.id,
    jsonb_build_object('original_transaction_id', v_original.id, 'reason', p_reason)
  );

  return v_reversal;
end;
$$;

revoke all on function public.reverse_finance_transaction(uuid, text) from public;
grant execute on function public.reverse_finance_transaction(uuid, text) to authenticated;

comment on function public.reverse_finance_transaction is
  'A reversal is a self-approved system correction by an authorized finance.approve holder — distinct from the two-person approval flow for new discretionary transactions. It is auditable, linked, and cannot be applied twice to the same original transaction.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0008_finalize_payroll_payment.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0008_finalize_payroll_payment.sql
-- PRD §10: one salary payment creates exactly one linked payroll payment and
-- one Finance expense. Enforced here by keying off payroll_payments.id as
-- the transactions.source_id under the unique (mosque_id, source_type, source_id)
-- constraint.

create or replace function public.finalize_payroll_payment(
  p_payroll_payment_id uuid
)
returns public.payroll_payments
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id   uuid := public.current_mosque_id();
  v_actor_id    uuid := public.current_profile_id();
  v_payroll     public.payroll_payments;
  v_category_id uuid;
  v_txn_code    text;
  v_txn         public.transactions;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('staff.salary.pay') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  select * into v_payroll
  from public.payroll_payments
  where id = p_payroll_payment_id and mosque_id = v_mosque_id
  for update;

  if v_payroll.id is null then
    raise exception 'VALIDATION_ERROR: payroll payment not found for this mosque' using errcode = '22023';
  end if;

  if v_payroll.status = 'finalized' then
    raise exception 'PAYMENT_ALREADY_PROCESSED' using errcode = '23505';
  end if;

  -- Idempotent replay: an expense already exists for this payroll payment.
  select * into v_txn
  from public.transactions
  where mosque_id = v_mosque_id and source_type = 'payroll' and source_id = v_payroll.id;

  if v_txn.id is null then
    select id into v_category_id
    from public.finance_categories
    where mosque_id = v_mosque_id and type = 'expense' and code = 'salary'
    limit 1;

    if v_category_id is null then
      raise exception 'VALIDATION_ERROR: salary expense category not configured for this mosque' using errcode = '22023';
    end if;

    v_txn_code := public.generate_business_code(v_mosque_id, 'transaction', 'TXN');

    insert into public.transactions (
      mosque_id, transaction_code, category_id, type, amount, txn_date,
      description, source_type, source_id, created_by, approval_status
    )
    values (
      v_mosque_id, v_txn_code, v_category_id, 'expense', v_payroll.amount, current_date,
      'Staff salary payment', 'payroll', v_payroll.id, v_actor_id, 'pending'
    )
    returning * into v_txn;
  end if;

  update public.payroll_payments
  set status = case when v_txn.approval_status = 'approved' then 'finalized' else 'finance_pending' end,
      transaction_id = v_txn.id,
      finalized_at = case when v_txn.approval_status = 'approved' then now() else null end
  where id = v_payroll.id
  returning * into v_payroll;

  perform public.log_audit_event(
    v_mosque_id, 'payroll.finalize_request', 'payroll_payments', v_payroll.id,
    jsonb_build_object('transaction_id', v_txn.id, 'amount', v_payroll.amount)
  );

  return v_payroll;
end;
$$;

revoke all on function public.finalize_payroll_payment(uuid) from public;
grant execute on function public.finalize_payroll_payment(uuid) to authenticated;

comment on function public.finalize_payroll_payment is
  'Creates the linked Finance expense (pending approval per PRD §9 tiers). payroll_payments.status becomes ''finalized'' only once the linked transaction is approved via approve_finance_transaction — call this function again afterward to sync status, or drive it from a trigger on transactions if preferred.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0009_publish_notice.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0009_publish_notice.sql
-- PRD §14, §28: AI may draft a notice, but only a human, explicit action can
-- publish it. This RPC is the only path that sets status = 'published'.

create or replace function public.publish_notice(
  p_notice_id uuid
)
returns public.notices
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id uuid := public.current_mosque_id();
  v_actor_id  uuid := public.current_profile_id();
  v_notice    public.notices;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('notice.manage') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  select * into v_notice
  from public.notices
  where id = p_notice_id and mosque_id = v_mosque_id
  for update;

  if v_notice.id is null then
    raise exception 'VALIDATION_ERROR: notice not found for this mosque' using errcode = '22023';
  end if;

  if v_notice.status = 'published' then
    return v_notice;   -- idempotent no-op
  end if;

  update public.notices
  set status = 'published',
      published_by = v_actor_id
  where id = p_notice_id
  returning * into v_notice;

  perform public.log_audit_event(
    v_mosque_id, 'notice.publish', 'notices', v_notice.id,
    jsonb_build_object('ai_generated', v_notice.ai_generated)
  );

  return v_notice;
end;
$$;

revoke all on function public.publish_notice(uuid) from public;
grant execute on function public.publish_notice(uuid) to authenticated;

comment on function public.publish_notice is
  'The only path that sets notices.status = ''published''. published_by is always the calling human profile — an AI process must never call this directly with its own service credential standing in for a person.';


-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-- SECTION: supabase/functions/0010_complete_ai_request.sql
-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

-- 0010_complete_ai_request.sql
-- Called by the server-side process that talks to Gemini (an Edge Function
-- using a service credential, not a regular user session) to write the
-- draft output back onto the ai_requests row. This is intentionally the
-- only writer of ai_requests.output_draft/status — clients only ever create
-- the initial request (see policies/0004) and read it back.
-- Derived from requirements/PRD_v2.0_FINAL.md §28.

create or replace function public.complete_ai_request(
  p_ai_request_id uuid,
  p_status        text,   -- 'completed' or 'failed'
  p_output_draft  text default null
)
returns public.ai_requests
language plpgsql
security definer
set search_path = public
as $$
declare
  v_row public.ai_requests;
begin
  if p_status not in ('completed','failed') then
    raise exception 'VALIDATION_ERROR: invalid status' using errcode = '22023';
  end if;

  update public.ai_requests
  set status = p_status,
      output_draft = p_output_draft,
      completed_at = now()
  where id = p_ai_request_id and status in ('pending','processing')
  returning * into v_row;

  if v_row.id is null then
    raise exception 'VALIDATION_ERROR: ai_request not found or already completed' using errcode = '22023';
  end if;

  perform public.log_audit_event(
    v_row.mosque_id, 'ai_request.' || p_status, 'ai_requests', v_row.id, null
  );

  return v_row;
end;
$$;

-- Deliberately NOT granted to 'authenticated' — this is invoked only via the
-- service-role connection from the trusted server-side AI integration,
-- never directly by an app user's session.
revoke all on function public.complete_ai_request(uuid, text, text) from public, authenticated;

comment on function public.complete_ai_request is
  'Service-role only. The AI drafting output lands here, never directly in a client-writable column, and never sets a notice to published (see publish_notice.sql, which is the only human-triggered publish path).';

