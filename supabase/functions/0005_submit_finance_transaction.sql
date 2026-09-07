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
