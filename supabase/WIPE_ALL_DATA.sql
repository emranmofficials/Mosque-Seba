-- ============================================================================
-- ⚠️  DANGER: DESTRUCTIVE — WIPES ALL MOSQUE SEBA DATA
-- ============================================================================
-- This script permanently deletes EVERY row from all 25 app tables AND all
-- Supabase Auth users. There is NO undo. Run it ONLY in the Supabase Dashboard
-- SQL Editor (or with a service_role connection) when you intend a full reset.
--
-- Usage: Supabase Dashboard → SQL Editor → New query → paste → Run
--
-- NOTE: The live backend is a FLAT TEXT-ID schema (see README.md). All public
--       tables are listed here. TRUNCATE ... CASCADE also clears any dependent
--       rows and resets sequences.
-- ============================================================================

-- 1. Wipe every application table (public schema).
TRUNCATE TABLE
    public.mosques,
    public.members,
    public.transactions,
    public.chanda_payments,
    public.notices,
    public.meetings,
    public.staff,
    public.audit_logs,
    public.cashbox_records,
    public.madrasa_students,
    public.madrasa_attendance,
    public.hifz_progress,
    public.madrasa_fee_payments,
    public.staff_payroll,
    public.duty_roster,
    public.food_schedules,
    public.welfare_cases,
    public.mosque_events,
    public.office_documents,
    public.advisory_notes,
    public.smart_board_quotes,
    public.election_schedules,
    public.election_candidates,
    public.mosque_properties,
    public.profiles
CASCADE;

-- 2. Wipe all Supabase Auth accounts (after profiles are gone so FKs don't block).
DELETE FROM auth.users;

-- 3. Verify: every count below must be 0.
SELECT 'mosques' AS table_name, count(*) FROM public.mosques
UNION ALL SELECT 'members', count(*) FROM public.members
UNION ALL SELECT 'transactions', count(*) FROM public.transactions
UNION ALL SELECT 'chanda_payments', count(*) FROM public.chanda_payments
UNION ALL SELECT 'profiles', count(*) FROM public.profiles
UNION ALL SELECT 'auth_users (service_role only)', count(*) FROM auth.users;

-- ⚠️  After running this, uninstall the app or Clear Data on every device.
-- The app is offline-first: local Room data will otherwise re-sync back to cloud.
