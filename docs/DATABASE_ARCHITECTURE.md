# Database Architecture

> Derived from `requirements/PRD_v2.0_FINAL.md` (Section 30). PostgreSQL/Supabase is the authoritative source of truth (SSoT).

## Global rules

- **UUID primary keys** on all tables; separate human-facing business IDs (e.g. `STU-YYYY-0001`, Committee ID, Member ID, Receipt ID) are distinct from the UUID PK.
- **`mosque_id` tenant isolation** on every tenant-owned row, either directly or derivable securely through a parent record.
- **PostgreSQL is SSoT** — the Android client and any cache are downstream, never authoritative.
- **UTC timestamps** everywhere; display conversion happens client-side.
- **Numeric money** — all monetary columns use `NUMERIC`/`DECIMAL`, never floating point.
- **Soft-delete / archive** pattern instead of hard deletes for auditable entities.
- Appropriate **indexes**, **unique constraints**, and **check constraints** on every table.
- **Row Level Security (RLS)** enabled and enforced on all tenant-owned tables.
- **Secure RPC / Edge Functions** for any multi-step or privileged operation (see `docs/SECURITY_MODEL.md`).
- **Outbox pattern** (`event_outbox`) and **idempotency keys** for anything that triggers notifications or financial side-effects.

## Table groups

### Core identity / tenant
`mosques`, `profiles`, `roles`, `permissions`, `role_permissions`, `user_roles`, `user_permission_overrides`

### Members / staff
`members`, `member_preferences`, `staff`, `staff_availability`

### Madrasa
`academic_sessions`, `students`, `guardians`, `guardian_profiles`, `student_guardians`, `madrasa_classes`, `subjects`, `teacher_assignments`, `madrasa_attendance`, `hifz_progress`, `hifz_milestones`, `madrasa_exams`, `madrasa_exam_results`, `madrasa_result_reviews`, `fee_types`, `student_fees`, `fee_payments`, `madrasa_timetables`, `madrasa_homework`, `madrasa_homework_submissions`

### Finance
`finance_categories`, `transactions`, `approval_policies`, `financial_reconciliations`, `chanda_schedules`, `chanda_payments`

### Operations
`prayer_settings`, `prayer_times`, `iqamah_settings`, `prayer_overrides`, `duty_roster`, `food_schedules`, `food_assignments`, `meetings`, `meeting_invitees`, `meeting_agenda_items`, `meeting_decisions`, `tasks`, `notices`, `notice_audiences`, `notifications`, `notification_deliveries`, `device_tokens`, `events`

### Procurement / assets
`purchase_requests`, `purchase_request_items`, `rfqs`, `suppliers`, `supplier_quotations`, `purchase_orders`, `goods_receipts`, `inventory_items`, `inventory_transactions`, `assets`

### Projects
`projects`, `project_phases`, `project_boq_items`, `project_site_reports`, `project_change_requests`

### Platform
`subscription_plans`, `mosque_subscriptions`, `subscription_entitlements`, `subscription_usage`, `subscription_invoices`, `subscription_payments`, `ai_requests`, `audit_logs`, `security_events`, `privacy_consents`, `documents`, `event_outbox`, `workflow_instances`, `workflow_history`, `sync_operations`

## Key invariants that must hold at the schema level

- **Double-post prevention (chanda):** uniqueness on `(mosque_id, source_type, source_id)` or an equivalent invariant, so a cash payment can never be posted to Finance Income twice.
- **Payroll linkage:** one salary payment creates exactly one linked payroll payment row and one Finance expense row — never more, never orphaned.
- **Madrasa fee linkage:** one successful fee payment creates exactly one linked Finance Income row.
- **Attendance uniqueness:** one attendance record per `(student_id, date)`; future dates blocked at the write layer.
- **Hifz milestone uniqueness:** one milestone per `(student_id, para)`.
- **Timetable conflict prevention:** active-slot uniqueness plus teacher/class conflict detection.
- **Self-approval prohibition:** the same user cannot both create and approve a finance transaction above their own authority.
- **Posted transactions are immutable:** corrections happen via reversal/adjustment rows, never direct delete/update of a posted transaction.

## Migrations & Policies Status (Production v1.1.1)

- **Android Room Database v10:**
  - Migrated across 10 progressive revisions (`MIGRATION_1_2` through `MIGRATION_9_10`).
  - `MIGRATION_8_9`: Added `imageUrl` to `notices` table for photo/poster attachments.
  - `MIGRATION_9_10`: Added `sealUrl` to `mosques`; `signatureUrl`, `stampUrl`, `designationBn` to `members`; and `signatureUrl`, `stampUrl` to `staff`.
  - Offline-first resilience with reactive StateFlow DAOs and transactions.
- **Supabase Cloud Backend (PostgreSQL 17):**
  - **Live Project:** `donglyxtrfyzwgtrskhz` (Organization: `MosqueSeba`, Region: `ap-northeast-1`).
  - **Schema:** 28 core production tables deployed and verified via `supabase/migrations/0001` to `0006`.
  - **Row Level Security (RLS):** 4 active policy sets under `supabase/policies/` enforcing tenant isolation (`mosque_id`) and authenticated role-based access.
  - **Server-Side RPCs:** 10 stored procedures under `supabase/functions/` for idempotent double-entry finance, cashless reversals, audit event logging, business code generation, and payroll finalization.
  - **Client Sync:** Integrated bidirectional `SupabaseSyncManager` connecting Room DB with Supabase PostgREST endpoints.
