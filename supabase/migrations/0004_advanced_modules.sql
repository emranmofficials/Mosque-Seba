-- ============================================================================
-- 0004_advanced_modules.sql
-- Mosque Seba (মসজিদ সেবা) Advanced Modules Tables & RLS Policies
-- Covers: Meetings, Madrasa, Payroll & Rosters, Welfare, Events, Cashbox,
--         Documents, Sharia Advisory, Smart Board, Elections, and Properties.
-- ============================================================================

-- 1. Meetings
CREATE TABLE IF NOT EXISTS public.meetings (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    meeting_date TEXT NOT NULL,
    location TEXT NOT NULL,
    agenda TEXT NOT NULL,
    status TEXT DEFAULT 'upcoming',
    resolution TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Madrasa Students
CREATE TABLE IF NOT EXISTS public.madrasa_students (
    id TEXT PRIMARY KEY,
    student_code TEXT NOT NULL,
    name_bn TEXT NOT NULL,
    name_en TEXT DEFAULT '',
    class_name TEXT NOT NULL,
    guardian_name TEXT NOT NULL,
    guardian_mobile TEXT NOT NULL,
    monthly_fee NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    join_date TEXT NOT NULL,
    status TEXT DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 3. Madrasa Attendance
CREATE TABLE IF NOT EXISTS public.madrasa_attendance (
    id TEXT PRIMARY KEY,
    student_id TEXT NOT NULL,
    student_name_bn TEXT NOT NULL,
    date TEXT NOT NULL,
    status TEXT DEFAULT 'present',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Hifz Progress
CREATE TABLE IF NOT EXISTS public.hifz_progress (
    id TEXT PRIMARY KEY,
    student_id TEXT NOT NULL,
    student_name_bn TEXT NOT NULL,
    date TEXT NOT NULL,
    para_number INT NOT NULL,
    page_or_ayah TEXT NOT NULL,
    quality_score INT DEFAULT 5,
    mistake_count INT DEFAULT 0,
    teacher_feedback TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 5. Madrasa Fee Payments
CREATE TABLE IF NOT EXISTS public.madrasa_fee_payments (
    id TEXT PRIMARY KEY,
    student_id TEXT NOT NULL,
    student_name_bn TEXT NOT NULL,
    month TEXT NOT NULL,
    amount_paid NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    receipt_code TEXT NOT NULL,
    payment_method TEXT DEFAULT 'cash',
    status TEXT DEFAULT 'verified',
    paid_at TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 6. Staff Payroll
CREATE TABLE IF NOT EXISTS public.staff_payroll (
    id TEXT PRIMARY KEY,
    staff_id TEXT NOT NULL,
    staff_name_bn TEXT NOT NULL,
    month TEXT NOT NULL,
    salary_amount NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    bonus_amount NUMERIC(10,2) DEFAULT 0.0,
    net_salary NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    payment_status TEXT DEFAULT 'pending',
    paid_date TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 7. Duty Roster
CREATE TABLE IF NOT EXISTS public.duty_roster (
    id TEXT PRIMARY KEY,
    staff_id TEXT NOT NULL,
    staff_name_bn TEXT NOT NULL,
    day_of_week TEXT NOT NULL,
    prayer_name TEXT NOT NULL,
    role_title TEXT NOT NULL,
    notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 8. Food Schedules
CREATE TABLE IF NOT EXISTS public.food_schedules (
    id TEXT PRIMARY KEY,
    date_str TEXT NOT NULL,
    meal_type TEXT NOT NULL,
    provider_name TEXT NOT NULL,
    provider_mobile TEXT NOT NULL,
    receiver_staff_name TEXT NOT NULL,
    status TEXT DEFAULT 'scheduled',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 9. Welfare Cases
CREATE TABLE IF NOT EXISTS public.welfare_cases (
    id TEXT PRIMARY KEY,
    beneficiary_name TEXT NOT NULL,
    category TEXT NOT NULL,
    mobile TEXT NOT NULL,
    amount_requested NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    amount_approved NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    status TEXT DEFAULT 'approved',
    application_date TEXT NOT NULL,
    remarks TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 10. Mosque Events
CREATE TABLE IF NOT EXISTS public.mosque_events (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    date_str TEXT NOT NULL,
    time_str TEXT NOT NULL,
    speaker_or_guest TEXT NOT NULL,
    venue TEXT NOT NULL,
    budget NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    volunteer_lead TEXT NOT NULL,
    status TEXT DEFAULT 'upcoming',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 11. Cashbox Records
CREATE TABLE IF NOT EXISTS public.cashbox_records (
    id TEXT PRIMARY KEY,
    collection_date TEXT NOT NULL,
    box_type TEXT NOT NULL,
    counted_amount NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    counted_by TEXT NOT NULL,
    witness_names TEXT NOT NULL,
    witness_verified BOOLEAN DEFAULT false,
    verified_by TEXT,
    bank_name TEXT DEFAULT '',
    deposit_slip_no TEXT DEFAULT '',
    deposit_status TEXT DEFAULT 'counted',
    notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 12. Office Documents
CREATE TABLE IF NOT EXISTS public.office_documents (
    id TEXT PRIMARY KEY,
    doc_code TEXT NOT NULL,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    counterparty TEXT NOT NULL,
    doc_date TEXT NOT NULL,
    summary TEXT NOT NULL,
    reply_note TEXT DEFAULT '',
    status TEXT DEFAULT 'সংরক্ষিত',
    recorded_by TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 13. Advisory Notes
CREATE TABLE IF NOT EXISTS public.advisory_notes (
    id TEXT PRIMARY KEY,
    note_type TEXT NOT NULL,
    topic TEXT NOT NULL,
    recommendation TEXT NOT NULL,
    author_name TEXT NOT NULL,
    author_role_title TEXT NOT NULL,
    sharia_compliance TEXT DEFAULT 'শরিয়াহ সম্মত',
    status TEXT DEFAULT 'পর্যালোচনাধীন',
    created_date TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 14. Smart Board Quotes
CREATE TABLE IF NOT EXISTS public.smart_board_quotes (
    id TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT true,
    created_by TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 15. Election Schedules
CREATE TABLE IF NOT EXISTS public.election_schedules (
    id TEXT PRIMARY KEY,
    term_title TEXT NOT NULL,
    term_start_year INT DEFAULT 2026,
    term_end_year INT DEFAULT 2028,
    chief_election_commissioner TEXT NOT NULL,
    commissioner_mobile TEXT DEFAULT '',
    schedule_date TEXT NOT NULL,
    nomination_deadline TEXT NOT NULL,
    voting_date TEXT NOT NULL,
    status TEXT DEFAULT 'nomination_open',
    election_type TEXT DEFAULT 'সর্বসম্মত ও ব্যালট নির্বাচন',
    gazette_notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 16. Election Candidates
CREATE TABLE IF NOT EXISTS public.election_candidates (
    id TEXT PRIMARY KEY,
    election_id TEXT NOT NULL,
    target_role_code TEXT NOT NULL,
    target_role_title_bn TEXT NOT NULL,
    candidate_name_bn TEXT NOT NULL,
    candidate_mobile TEXT DEFAULT '',
    member_code TEXT DEFAULT '',
    proposer_name_bn TEXT DEFAULT '',
    seconder_name_bn TEXT DEFAULT '',
    status TEXT DEFAULT 'valid',
    symbol_or_panel TEXT DEFAULT 'ঐক্য প্যানেল',
    votes_received INT DEFAULT 0,
    is_elected BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 17. Mosque Properties
CREATE TABLE IF NOT EXISTS public.mosque_properties (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    area_or_quantity TEXT NOT NULL,
    location_or_dag_no TEXT NOT NULL,
    deed_or_reg_no TEXT DEFAULT '',
    estimated_value_or_income TEXT DEFAULT '',
    photo_url TEXT,
    status TEXT DEFAULT 'সংরক্ষিত',
    description TEXT DEFAULT '',
    recorded_date TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Enable RLS and setup permissive policies for application synchronization
DO $$
DECLARE
    tbl_name text;
    new_tables text[] := ARRAY[
        'meetings', 'madrasa_students', 'madrasa_attendance',
        'hifz_progress', 'madrasa_fee_payments', 'staff_payroll',
        'duty_roster', 'food_schedules', 'welfare_cases',
        'mosque_events', 'cashbox_records', 'office_documents',
        'advisory_notes', 'smart_board_quotes', 'election_schedules',
        'election_candidates', 'mosque_properties'
    ];
BEGIN
    FOREACH tbl_name IN ARRAY new_tables LOOP
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY;', tbl_name);
        EXECUTE format('DROP POLICY IF EXISTS allow_all ON public.%I;', tbl_name);
        EXECUTE format('CREATE POLICY allow_all ON public.%I FOR ALL USING (true) WITH CHECK (true);', tbl_name);
        EXECUTE format('GRANT ALL ON public.%I TO anon, authenticated, service_role;', tbl_name);
    END LOOP;
END $$;
