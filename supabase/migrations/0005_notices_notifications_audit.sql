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
