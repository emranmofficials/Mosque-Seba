-- 0002_generate_business_code.sql
-- Generates sequential, human-facing business IDs like RCPT-2026-000123,
-- TXN-2026-000045, STU-2026-0001, scoped per mosque + code series + year.
-- Derived from requirements/PRD_v2.0_FINAL.md §16 (STU-YYYY-0001 pattern)
-- and the general "separate business IDs" rule in docs/DATABASE_ARCHITECTURE.md.

create table if not exists public.mosque_sequences (
  mosque_id   uuid not null references public.mosques(id) on delete cascade,
  series      text not null,   -- e.g. 'transaction', 'chanda_receipt', 'student', 'payroll'
  year        smallint not null,
  last_value  integer not null default 0,
  primary key (mosque_id, series, year)
);

alter table public.mosque_sequences enable row level security;

-- Not directly readable/writable by clients; only via generate_business_code().
create policy mosque_sequences_no_client_access on public.mosque_sequences
  for all
  using (false)
  with check (false);

create or replace function public.generate_business_code(
  p_mosque_id uuid,
  p_series    text,
  p_prefix    text,
  p_pad_width int default 6
)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
  v_year  smallint := extract(year from now())::smallint;
  v_value integer;
begin
  insert into public.mosque_sequences (mosque_id, series, year, last_value)
  values (p_mosque_id, p_series, v_year, 1)
  on conflict (mosque_id, series, year)
  do update set last_value = public.mosque_sequences.last_value + 1
  returning last_value into v_value;

  return p_prefix || '-' || v_year::text || '-' || lpad(v_value::text, p_pad_width, '0');
end;
$$;

revoke all on function public.generate_business_code(uuid, text, text, int) from public;
grant execute on function public.generate_business_code(uuid, text, text, int) to authenticated;

comment on function public.generate_business_code is
  'Atomic, per-mosque, per-series, per-year sequence generator for human-facing IDs (e.g. TXN-2026-000123). Concurrency-safe via ON CONFLICT ... DO UPDATE.';
