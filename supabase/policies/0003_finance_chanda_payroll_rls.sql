-- 0003_finance_chanda_payroll_rls.sql
-- RLS for finance_categories, approval_policies, transactions,
-- financial_reconciliations, chanda_schedules, chanda_payments, payroll_payments.

alter table public.finance_categories enable row level security;
alter table public.approval_policies enable row level security;
alter table public.transactions enable row level security;
alter table public.financial_reconciliations enable row level security;
alter table public.chanda_schedules enable row level security;
alter table public.chanda_payments enable row level security;
alter table public.payroll_payments enable row level security;

-- finance_categories / approval_policies: read for anyone with finance.view,
-- write only for finance.create-capable roles managing config (President/Treasurer).
create policy finance_categories_select on public.finance_categories
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy finance_categories_write on public.finance_categories
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.create'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('finance.create'));

create policy approval_policies_select on public.approval_policies
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy approval_policies_write on public.approval_policies
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'));

-- transactions
create policy transactions_select on public.transactions
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy transactions_insert on public.transactions
  for insert
  with check (
    mosque_id = public.current_mosque_id()
    and public.has_permission('finance.create')
    and created_by = public.current_profile_id()
  );

-- Approval is a distinct action from creation and is enforced again here:
-- self-approval is blocked both by the table CHECK constraint and this policy.
create policy transactions_approve on public.transactions
  for update
  using (
    mosque_id = public.current_mosque_id()
    and public.has_permission('finance.approve')
    and created_by <> public.current_profile_id()
  )
  with check (
    mosque_id = public.current_mosque_id()
    and approved_by = public.current_profile_id()
  );

-- No update/delete policy is defined for posted (approval_status = 'approved')
-- transactions beyond the approve action above: corrections must be modeled
-- as a new reversal/adjustment transaction (docs/DATABASE_ARCHITECTURE.md),
-- created through the standard insert policy with is_reversal = true.

create policy financial_reconciliations_select on public.financial_reconciliations
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.view'));

create policy financial_reconciliations_write on public.financial_reconciliations
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('finance.approve'));

-- chanda
create policy chanda_schedules_select on public.chanda_schedules
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (
      public.has_permission('chanda.view')
      or member_id in (select id from public.members where profile_id = public.current_profile_id())
    )
  );

create policy chanda_schedules_write on public.chanda_schedules
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.update'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.update'));

create policy chanda_payments_select on public.chanda_payments
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (
      public.has_permission('chanda.view')
      or member_id in (select id from public.members where profile_id = public.current_profile_id())
    )
  );

-- Members can create their own pending payment record ("চাঁদা পরিশোধ করুন");
-- admins with chanda.receive can create/verify on behalf of a member.
create policy chanda_payments_insert_self on public.chanda_payments
  for insert
  with check (
    mosque_id = public.current_mosque_id()
    and member_id in (select id from public.members where profile_id = public.current_profile_id())
  );

create policy chanda_payments_insert_admin on public.chanda_payments
  for insert
  with check (mosque_id = public.current_mosque_id() and public.has_permission('chanda.receive'));

create policy chanda_payments_update_admin on public.chanda_payments
  for update
  using (mosque_id = public.current_mosque_id() and public.has_permission('chanda.receive'))
  with check (mosque_id = public.current_mosque_id());

-- payroll
create policy payroll_payments_select on public.payroll_payments
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.salary.view'));

create policy payroll_payments_write on public.payroll_payments
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.salary.pay'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('staff.salary.pay'));
