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
