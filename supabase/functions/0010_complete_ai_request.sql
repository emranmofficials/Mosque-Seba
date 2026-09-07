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
