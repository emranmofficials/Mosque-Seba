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
