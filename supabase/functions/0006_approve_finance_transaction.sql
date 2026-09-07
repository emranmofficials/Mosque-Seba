-- 0006_approve_finance_transaction.sql
-- Approves a pending transaction according to the mosque's approval_policies
-- tiers, blocking self-approval. Derived from requirements/PRD_v2.0_FINAL.md §9.

create or replace function public.approve_finance_transaction(
  p_transaction_id uuid,
  p_decision       text   -- 'approved' or 'rejected'
)
returns public.transactions
language plpgsql
security definer
set search_path = public
as $$
declare
  v_mosque_id      uuid := public.current_mosque_id();
  v_actor_id       uuid := public.current_profile_id();
  v_txn            public.transactions;
  v_required_role  text;
  v_actor_has_role boolean;
begin
  if v_mosque_id is null or v_actor_id is null then
    raise exception 'AUTH_REQUIRED' using errcode = '28000';
  end if;

  if not public.has_permission('finance.approve') then
    raise exception 'PERMISSION_DENIED' using errcode = '42501';
  end if;

  if p_decision not in ('approved','rejected') then
    raise exception 'VALIDATION_ERROR: invalid decision' using errcode = '22023';
  end if;

  select * into v_txn
  from public.transactions
  where id = p_transaction_id and mosque_id = v_mosque_id
  for update;

  if v_txn.id is null then
    raise exception 'VALIDATION_ERROR: transaction not found for this mosque' using errcode = '22023';
  end if;

  if v_txn.approval_status <> 'pending' then
    raise exception 'PAYMENT_ALREADY_PROCESSED' using errcode = '23505';
  end if;

  -- Self-approval prohibition (also enforced at the RLS + CHECK-constraint layer).
  if v_txn.created_by = v_actor_id then
    raise exception 'SELF_APPROVAL_NOT_ALLOWED' using errcode = '42501';
  end if;

  if p_decision = 'approved' then
    -- Determine the minimum-authority tier that covers this amount.
    select required_role_code into v_required_role
    from public.approval_policies
    where mosque_id = v_mosque_id and max_amount >= v_txn.amount
    order by max_amount asc
    limit 1;

    if v_required_role is not null then
      select exists (
        select 1 from public.user_roles ur
        join public.roles r on r.id = ur.role_id
        where ur.profile_id = v_actor_id and r.code = v_required_role
      ) into v_actor_has_role;

      if not coalesce(v_actor_has_role, false) then
        raise exception 'FINANCE_APPROVAL_REQUIRED: requires role %', v_required_role using errcode = '42501';
      end if;
    end if;
  end if;

  update public.transactions
  set approval_status = p_decision,
      approved_by = v_actor_id,
      approved_at = now()
  where id = p_transaction_id
  returning * into v_txn;

  perform public.log_audit_event(
    v_mosque_id, 'finance.' || p_decision, 'transactions', v_txn.id,
    jsonb_build_object('amount', v_txn.amount, 'required_role', v_required_role)
  );

  return v_txn;
end;
$$;

revoke all on function public.approve_finance_transaction(uuid, text) from public;
grant execute on function public.approve_finance_transaction(uuid, text) to authenticated;

comment on function public.approve_finance_transaction is
  'PRD §9: self-approval blocked; approval authority tier resolved from approval_policies by amount. Rejects with FINANCE_APPROVAL_REQUIRED if the caller lacks the required role for this amount tier, even if they generically hold finance.approve.';
