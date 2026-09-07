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
