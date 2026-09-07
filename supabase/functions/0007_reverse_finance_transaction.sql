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

  if exists (select 1 from public.transactions where reversed_by_id = v_original.id) then
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
