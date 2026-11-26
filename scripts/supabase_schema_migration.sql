-- =============================================================================
-- Mosque Seba (মসজিদ সেবা) — Safe Non-Destructive Supabase Schema Migration
-- Target Project: donglyxtrfyzwgtrskhz
-- Purpose: Align live PostgreSQL tables with Flutter app v1.2.5 models/repositories
-- Guarantee: Zero DROP TABLE / DROP COLUMN / TRUNCATE statements. Safe to re-run.
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. profiles (Email Login & President/Mosque Registration)
-- -----------------------------------------------------------------------------
ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS full_name_bn TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS full_name_en TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS phone TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;

DO $$
BEGIN
  ALTER TABLE public.profiles ALTER COLUMN email DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 2. transactions (Finance General Ledger)
-- -----------------------------------------------------------------------------
ALTER TABLE public.transactions
  ADD COLUMN IF NOT EXISTS category_name_bn TEXT DEFAULT '';

DO $$
BEGIN
  ALTER TABLE public.transactions ALTER COLUMN category_id DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 3. chanda_payments (Member Subscriptions)
-- -----------------------------------------------------------------------------
ALTER TABLE public.chanda_payments
  ADD COLUMN IF NOT EXISTS member_name_bn TEXT DEFAULT '';

-- -----------------------------------------------------------------------------
-- 4. cashbox_records (Jumu'ah 3-Witness Denomination Counter)
-- -----------------------------------------------------------------------------
ALTER TABLE public.cashbox_records
  ADD COLUMN IF NOT EXISTS waqt_date TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS counted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS note_1000 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_500 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_200 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_100 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_50 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_20 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_10 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_5 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_2 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS note_1 INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS coins_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS witness_1_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS witness_2_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS witness_3_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS witness_1_signature_url TEXT,
  ADD COLUMN IF NOT EXISTS witness_2_signature_url TEXT,
  ADD COLUMN IF NOT EXISTS witness_3_signature_url TEXT,
  ADD COLUMN IF NOT EXISTS voucher_code TEXT;

DO $$
BEGIN
  ALTER TABLE public.cashbox_records ALTER COLUMN collection_date SET DEFAULT CURRENT_DATE;
  ALTER TABLE public.cashbox_records ALTER COLUMN collection_date DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.cashbox_records ALTER COLUMN counted_by SET DEFAULT '';
  ALTER TABLE public.cashbox_records ALTER COLUMN counted_by DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 5. notices (Notice Board & Janaza Broadcasts)
-- -----------------------------------------------------------------------------
ALTER TABLE public.notices
  ADD COLUMN IF NOT EXISTS publish_date TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS expiry_date TEXT;

DO $$
BEGIN
  ALTER TABLE public.notices ALTER COLUMN publish_at SET DEFAULT now();
  ALTER TABLE public.notices ALTER COLUMN publish_at DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 6. notifications (In-App Notification Center)
-- -----------------------------------------------------------------------------
ALTER TABLE public.notifications
  ADD COLUMN IF NOT EXISTS entity_type TEXT,
  ADD COLUMN IF NOT EXISTS entity_id TEXT,
  ADD COLUMN IF NOT EXISTS priority TEXT DEFAULT 'normal',
  ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT false;

-- -----------------------------------------------------------------------------
-- 7. election_schedules & election_candidates
-- -----------------------------------------------------------------------------
ALTER TABLE public.election_schedules
  ADD COLUMN IF NOT EXISTS commissioner_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS nomination_start_date TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS nomination_end_date TEXT DEFAULT '';

ALTER TABLE public.election_candidates
  ADD COLUMN IF NOT EXISTS schedule_id TEXT,
  ADD COLUMN IF NOT EXISTS target_role TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS target_role_bn TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS candidate_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS proposer_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS seconder_name TEXT DEFAULT '';

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN mosque_id DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN election_id DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN target_role_code DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN target_role_title_bn DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN candidate_name_bn DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN proposer_name_bn DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.election_candidates ALTER COLUMN seconder_name_bn DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 8. madrasa_students
-- -----------------------------------------------------------------------------
ALTER TABLE public.madrasa_students
  ADD COLUMN IF NOT EXISTS guardian_nid TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS admission_date TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS blood_group TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS photo_url TEXT;

-- -----------------------------------------------------------------------------
-- 9. madrasa_attendance
-- -----------------------------------------------------------------------------
ALTER TABLE public.madrasa_attendance
  ADD COLUMN IF NOT EXISTS class_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS attendance_date TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS remarks TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS recorded_by TEXT DEFAULT '';

DO $$
BEGIN
  ALTER TABLE public.madrasa_attendance ALTER COLUMN "date" SET DEFAULT CURRENT_DATE;
  ALTER TABLE public.madrasa_attendance ALTER COLUMN "date" DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
  ALTER TABLE public.madrasa_attendance ALTER COLUMN student_name_bn DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 10. hifz_progress
-- -----------------------------------------------------------------------------
ALTER TABLE public.hifz_progress
  ADD COLUMN IF NOT EXISTS surah_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS page_number INTEGER NOT NULL DEFAULT 1,
  ADD COLUMN IF NOT EXISTS sabaq_rating INTEGER NOT NULL DEFAULT 3,
  ADD COLUMN IF NOT EXISTS sabaqi_rating INTEGER NOT NULL DEFAULT 3,
  ADD COLUMN IF NOT EXISTS manzil_rating INTEGER NOT NULL DEFAULT 3,
  ADD COLUMN IF NOT EXISTS teacher_remarks TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS recorded_date TEXT DEFAULT '';

-- -----------------------------------------------------------------------------
-- 11. madrasa_fee_payments
-- -----------------------------------------------------------------------------
ALTER TABLE public.madrasa_fee_payments
  ADD COLUMN IF NOT EXISTS class_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS amount_due NUMERIC(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS received_by TEXT DEFAULT '';

DO $$
BEGIN
  ALTER TABLE public.madrasa_fee_payments ALTER COLUMN paid_at SET DEFAULT CURRENT_DATE;
  ALTER TABLE public.madrasa_fee_payments ALTER COLUMN paid_at DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 12. mosque_events
-- -----------------------------------------------------------------------------
ALTER TABLE public.mosque_events
  ADD COLUMN IF NOT EXISTS event_type TEXT DEFAULT 'waz_mahfil',
  ADD COLUMN IF NOT EXISTS event_date TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS event_time TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS chief_guest TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS special_speakers TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS estimated_budget NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS actual_expense NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS description TEXT DEFAULT '';

-- -----------------------------------------------------------------------------
-- 13. welfare_cases
-- -----------------------------------------------------------------------------
ALTER TABLE public.welfare_cases
  ADD COLUMN IF NOT EXISTS case_code TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS applicant_name TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS applicant_mobile TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS applicant_nid TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS address TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS fund_type TEXT DEFAULT 'zakat',
  ADD COLUMN IF NOT EXISTS requested_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS approved_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS verified_by TEXT,
  ADD COLUMN IF NOT EXISTS approved_by TEXT,
  ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT '',
  ADD COLUMN IF NOT EXISTS disbursed_date TEXT;

DO $$
BEGIN
  ALTER TABLE public.welfare_cases ALTER COLUMN application_date SET DEFAULT CURRENT_DATE;
  ALTER TABLE public.welfare_cases ALTER COLUMN application_date DROP NOT NULL;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- -----------------------------------------------------------------------------
-- 14. Missing Tables: examinations & exam_results
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.examinations (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
  mosque_id TEXT NOT NULL,
  exam_name_bn TEXT NOT NULL DEFAULT '',
  exam_type TEXT NOT NULL DEFAULT 'half_yearly',
  academic_year TEXT NOT NULL DEFAULT '',
  start_date TEXT NOT NULL DEFAULT '',
  status TEXT NOT NULL DEFAULT 'published',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.exam_results (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
  mosque_id TEXT NOT NULL,
  exam_id TEXT NOT NULL,
  student_id TEXT NOT NULL,
  student_name_bn TEXT NOT NULL DEFAULT '',
  class_name TEXT NOT NULL DEFAULT '',
  subject_name TEXT NOT NULL DEFAULT '',
  marks_obtained NUMERIC(8,2) NOT NULL DEFAULT 0,
  total_marks NUMERIC(8,2) NOT NULL DEFAULT 100,
  grade TEXT NOT NULL DEFAULT '',
  remarks TEXT DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_examinations_mosque
  ON public.examinations (mosque_id);

CREATE INDEX IF NOT EXISTS idx_exam_results_mosque_exam
  ON public.exam_results (mosque_id, exam_id);

CREATE INDEX IF NOT EXISTS idx_exam_results_student
  ON public.exam_results (student_id);

ALTER TABLE public.examinations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exam_results ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'examinations'
      AND policyname = 'Tenant members manage examinations'
  ) THEN
    CREATE POLICY "Tenant members manage examinations"
      ON public.examinations
      FOR ALL
      TO authenticated
      USING (
        EXISTS (
          SELECT 1 FROM public.profiles p
          WHERE p.id = auth.uid()::text AND p.mosque_id = examinations.mosque_id
        )
        OR EXISTS (
          SELECT 1 FROM public.members m
          WHERE m.id = auth.uid()::text AND m.mosque_id = examinations.mosque_id
        )
      )
      WITH CHECK (
        EXISTS (
          SELECT 1 FROM public.profiles p
          WHERE p.id = auth.uid()::text AND p.mosque_id = examinations.mosque_id
        )
        OR EXISTS (
          SELECT 1 FROM public.members m
          WHERE m.id = auth.uid()::text AND m.mosque_id = examinations.mosque_id
        )
      );
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'exam_results'
      AND policyname = 'Tenant members manage exam_results'
  ) THEN
    CREATE POLICY "Tenant members manage exam_results"
      ON public.exam_results
      FOR ALL
      TO authenticated
      USING (
        EXISTS (
          SELECT 1 FROM public.profiles p
          WHERE p.id = auth.uid()::text AND p.mosque_id = exam_results.mosque_id
        )
        OR EXISTS (
          SELECT 1 FROM public.members m
          WHERE m.id = auth.uid()::text AND m.mosque_id = exam_results.mosque_id
        )
      )
      WITH CHECK (
        EXISTS (
          SELECT 1 FROM public.profiles p
          WHERE p.id = auth.uid()::text AND p.mosque_id = exam_results.mosque_id
        )
        OR EXISTS (
          SELECT 1 FROM public.members m
          WHERE m.id = auth.uid()::text AND m.mosque_id = exam_results.mosque_id
        )
      );
  END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 15. Helpful Performance Indexes (idempotent)
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_election_candidates_schedule
  ON public.election_candidates (schedule_id);

CREATE INDEX IF NOT EXISTS idx_notifications_mosque_unread
  ON public.notifications (mosque_id, is_read);

-- Refresh PostgREST schema cache
NOTIFY pgrst, 'reload schema';

COMMIT;
