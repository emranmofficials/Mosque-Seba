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
