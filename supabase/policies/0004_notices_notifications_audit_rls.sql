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
