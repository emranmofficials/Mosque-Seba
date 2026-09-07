-- 0004_finance_chanda_payroll.sql
-- Finance, chanda (monthly subscription), and payroll. Derived from
-- requirements/PRD_v2.0_FINAL.md §8, §9, §10, §30.

create table if not exists public.finance_categories (
  id          uuid primary key default gen_random_uuid(),
  mosque_id   uuid not null references public.mosques(id) on delete cascade,
  type        text not null check (type in ('income','expense')),
  code        text not null,   -- e.g. 'chanda','donation','madrasa_fee','jummah','electricity','salary'
  name_bn     text not null,
  name_en     text,
  is_active   boolean not null default true,
  created_at  timestamptz not null default now(),
  unique (mosque_id, type, code)
);

create table if not exists public.approval_policies (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  max_amount        numeric(12,2) not null,   -- inclusive ceiling for this tier
  required_role_code text not null references public.roles(code),
  tier_label        text,                     -- e.g. 'assigned_authority','president','emergency','higher_committee'
  created_at        timestamptz not null default now(),
  check (max_amount >= 0)
);

create index if not exists idx_approval_policies_mosque_amount
  on public.approval_policies(mosque_id, max_amount);

create table if not exists public.transactions (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  transaction_code  text not null,             -- human-facing transaction ID
  category_id       uuid not null references public.finance_categories(id),
  type              text not null check (type in ('income','expense')),
  amount            numeric(12,2) not null check (amount > 0),
  txn_date          date not null default current_date,
  description       text,
  source_type       text,                      -- e.g. 'chanda_payment','fee_payment','manual','payroll'
  source_id         uuid,                      -- id of the originating record, when applicable
  attachment_url    text,
  created_by        uuid not null references public.profiles(id),
  approved_by       uuid references public.profiles(id),
  approval_status   text not null default 'pending'
                      check (approval_status in ('pending','approved','rejected')),
  reversed_by_id    uuid references public.transactions(id), -- links a reversal to the original
  is_reversal       boolean not null default false,
  idempotency_key   text,
  created_at        timestamptz not null default now(),
  approved_at       timestamptz,
  unique (mosque_id, transaction_code),
  -- Self-approval prohibition (PRD §9, docs/SECURITY_MODEL.md):
  check (approved_by is null or approved_by <> created_by),
  -- Double-post prevention for anything originating from another module (PRD §8, §20):
  unique (mosque_id, source_type, source_id)
);

create index if not exists idx_transactions_mosque_id on public.transactions(mosque_id, txn_date);
create index if not exists idx_transactions_idempotency on public.transactions(mosque_id, idempotency_key);

comment on constraint transactions_mosque_id_source_type_source_id_key on public.transactions is
  'PRD §8/§20: prevents duplicate posting of the same source record (chanda/fee payment, payroll, etc.) to Finance.';

create table if not exists public.financial_reconciliations (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  period_start  date not null,
  period_end    date not null,
  is_locked     boolean not null default false,   -- PRD §9: period lock mandatory
  locked_by     uuid references public.profiles(id),
  locked_at     timestamptz,
  notes         text,
  created_at    timestamptz not null default now(),
  check (period_end >= period_start)
);

create index if not exists idx_financial_reconciliations_mosque
  on public.financial_reconciliations(mosque_id, period_start, period_end);

-- ---------------------------------------------------------------------------
-- Chanda (monthly subscription)
-- ---------------------------------------------------------------------------

create table if not exists public.chanda_schedules (
  id            uuid primary key default gen_random_uuid(),
  mosque_id     uuid not null references public.mosques(id) on delete cascade,
  member_id     uuid not null references public.members(id) on delete cascade,
  amount        numeric(12,2) not null check (amount >= 0),
  effective_from date not null default current_date,
  effective_to   date,
  created_at    timestamptz not null default now(),
  check (effective_to is null or effective_to >= effective_from)
);

create index if not exists idx_chanda_schedules_member on public.chanda_schedules(member_id, effective_from);

create table if not exists public.chanda_payments (
  id                uuid primary key default gen_random_uuid(),
  mosque_id         uuid not null references public.mosques(id) on delete cascade,
  member_id         uuid not null references public.members(id) on delete cascade,
  month             date not null,              -- first day of the covered month
  amount_due        numeric(12,2) not null check (amount_due >= 0),
  amount_paid       numeric(12,2) not null default 0 check (amount_paid >= 0),
  method            text not null check (method in ('cash','mobile_banking','online')),
  status            text not null default 'pending'
                      check (status in ('pending','paid','partial','verified')),
  receipt_code      text unique,                -- human-facing receipt ID
  qr_verification_code text unique,
  received_by       uuid references public.profiles(id),
  verified_by       uuid references public.profiles(id),
  paid_at           timestamptz,
  created_at        timestamptz not null default now(),
  unique (mosque_id, member_id, month)           -- one ledger row per member per month
);

create index if not exists idx_chanda_payments_mosque_month on public.chanda_payments(mosque_id, month);

comment on table public.chanda_payments is
  'PRD §8: successful cash/mobile-banking payment is posted exactly once to transactions via source_type=''chanda_payment''.';

-- ---------------------------------------------------------------------------
-- Payroll (PRD §10 describes this flow; table added here to fulfil the
-- one-payment -> one-payroll-row -> one-expense-row invariant it specifies)
-- ---------------------------------------------------------------------------

create table if not exists public.payroll_payments (
  id             uuid primary key default gen_random_uuid(),
  mosque_id      uuid not null references public.mosques(id) on delete cascade,
  staff_id       uuid not null references public.staff(id) on delete cascade,
  period_start   date not null,
  period_end     date not null,
  amount         numeric(12,2) not null check (amount >= 0),
  status         text not null default 'pending'
                   check (status in ('pending','finance_pending','approved','finalized','rejected')),
  transaction_id uuid references public.transactions(id), -- the linked Finance expense
  created_by     uuid not null references public.profiles(id),
  created_at     timestamptz not null default now(),
  finalized_at   timestamptz,
  check (period_end >= period_start),
  unique (staff_id, period_start, period_end)
);

create index if not exists idx_payroll_payments_mosque on public.payroll_payments(mosque_id, period_start);

comment on column public.payroll_payments.transaction_id is
  'PRD §10: one salary payment creates exactly one linked payroll payment and one Finance expense.';
