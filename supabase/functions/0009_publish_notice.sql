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
