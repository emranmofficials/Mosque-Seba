# MOSQUE SEBA (মসজিদ সেবা) — DATABASE COMPATIBILITY REPORT

**Project:** Mosque Seba (`com.mosqueseba.app`)  
**Backend:** Supabase Cloud (PostgreSQL 17) — Project `donglyxtrfyzwgtrskhz`  
**Client Database:** Drift (Relational SQLite) migrating from Android Room DB (Schema Version 10)  
**Strict Safety Invariant:** Zero destructive operations against production (`DROP TABLE`, `TRUNCATE`, `DROP COLUMN` are strictly prohibited).  

---

## 1. Supabase Cloud Backend Architecture

The Supabase PostgreSQL database serves as the authoritative Single Source of Truth (SSoT). The schema is divided into 28 core production tables and 17 operational/extension tables.

### 1.1 Core Production Tables (28 Tables)

| # | Table Name | Primary Key | Tenant Scoping | Description & Key Invariants |
|---|---|---|---|---|
| 1 | `public.mosques` | `id` (UUID) | Self | Root tenant table. Stores institution name (BN/EN), committee ID, established year, seal/logo URLs, and bank details. |
| 2 | `public.roles` | `code` (TEXT) | System | Catalog of 16 roles (`president`, `treasurer`, etc.) across 4 committee tiers. |
| 3 | `public.permissions` | `code` (TEXT) | System | Granular permission catalog (`finance.approve`, `member.create`, `staff.salary.view`, etc.). |
| 4 | `public.role_permissions` | `(role_code, permission_code)` | System | Default permission grants mapped to each role. |
| 5 | `public.profiles` | `id` (UUID) | `mosque_id` | User profile linked to `auth.users(id)` and `mosques(id)`. |
| 6 | `public.user_roles` | `(profile_id, role_code, mosque_id)` | `mosque_id` | Many-to-many role assignments per user per mosque. |
| 7 | `public.user_permission_overrides` | `(profile_id, permission_code, mosque_id)` | `mosque_id` | Individual user permission overrides (`grant` / `revoke`). |
| 8 | `public.members` | `id` (UUID) | `mosque_id` | Mosque members directory, mobile, NID, monthly Chanda commitment, signature/stamp URLs. |
| 9 | `public.member_preferences` | `member_id` (UUID) | Inherited | Notification and privacy preferences per member. |
| 10 | `public.staff` | `id` (UUID) | `mosque_id` | Operational employees (Imam, Muazzin, Khadem, Teacher). Role code, employment type, salary. |
| 11 | `public.staff_availability` | `id` (UUID) | `mosque_id` | Staff leave, shift availability, and duty logs. |
| 12 | `public.finance_categories` | `id` (UUID) | `mosque_id` | Income and expense categories (e.g., "জুমার দানবাক্স", "মাসিক চাঁদা", "বিদ্যুৎ বিল"). |
| 13 | `public.approval_policies` | `id` (UUID) | `mosque_id` | Threshold tiers (`max_amount`, `required_role_code`, `tier_label`). |
| 14 | `public.transactions` | `id` (UUID) | `mosque_id` | General ledger. Amount, type (`income`/`expense`), category, creator, approver, status. Self-approval prohibited. |
| 15 | `public.financial_reconciliations` | `id` (UUID) | `mosque_id` | Monthly closing, cash/bank reconciliation, period locks. |
| 16 | `public.chanda_schedules` | `id` (UUID) | `mosque_id` | Monthly Chanda collection cycles per fiscal year. |
| 17 | `public.chanda_payments` | `id` (UUID) | `mosque_id` | Individual member subscription receipts with QR verification codes and cross-post link. |
| 18 | `public.payroll_payments` | `id` (UUID) | `mosque_id` | Monthly salary vouchers linked one-to-one with Finance expense transactions. |
| 19 | `public.notices` | `id` (UUID) | `mosque_id` | Rich administrative notices, priority (`normal`, `important`, `urgent`), image attachment, pin status. |
| 20 | `public.notice_audiences` | `id` (UUID) | `mosque_id` | Audience targeting for notices (all, committee, specific roles). |
| 21 | `public.device_tokens` | `id` (UUID) | Inherited | FCM push notification tokens with device metadata. |
| 22 | `public.event_outbox` | `id` (UUID) | `mosque_id` | Outbox pattern queue for reliable asynchronous event delivery. |
| 23 | `public.notifications` | `id` (UUID) | `mosque_id` | Generated notifications with title, body, entity references. |
| 24 | `public.notification_deliveries`| `id` (UUID) | Inherited | Delivery tracking per recipient and channel. |
| 25 | `public.ai_requests` | `id` (UUID) | `mosque_id` | Audit log of all Gemini AI prompts, generated outputs, and human review status. |
| 26 | `public.audit_logs` | `id` (BIGINT/UUID) | `mosque_id` | Immutable security audit trail of sensitive mutations and approvals. |
| 27 | `public.security_events` | `id` (UUID) | `mosque_id` | Security alerts, failed logins, root detections, and rate limit triggers. |
| 28 | `public.mosque_sequences` | `(mosque_id, sequence_type)`| `mosque_id` | Atomic sequence counters for human-readable IDs (`MEM-001`, `RCP-2026-0001`, etc.). |

---

### 1.2 Extended Operational Tables (17 Tables)

The legacy client and `full_schema_setup.sql` include these extended operational tables, which are 100% supported in the Flutter Drift schema:

| # | Table Name | Related Feature Module | Key Columns & Responsibilities |
|---|---|---|---|
| 29 | `public.meetings` | 19. Meeting Management | `title`, `meeting_date`, `location`, `agenda`, `status`, `resolution`. |
| 30 | `public.madrasa_students` | 22. Madrasa/Maktab Core | `student_code`, `name_bn`, `class_name`, `guardian_name`, `guardian_mobile`, `monthly_fee`. |
| 31 | `public.madrasa_attendance` | 23. Madrasa Attendance | `student_id`, `date`, `status` (`present`, `absent`, `late`), `remarks`. Unique per `(student_id, date)`. |
| 32 | `public.hifz_progress` | 24. Quran & Hifz Tracking | `student_id`, `para_number`, `page_number`, `sabaq_rating` (1–5 stars), `teacher_remarks`. |
| 33 | `public.madrasa_fee_payments` | 26. Madrasa Fees | `student_id`, `month`, `amount_paid`, `receipt_code`, `payment_method`. Auto-posts to Finance. |
| 34 | `public.staff_payroll` | 13. Staff & Payroll | `staff_id`, `month`, `base_salary`, `bonus_amount`, `deductions`, `net_paid`, `status`. |
| 35 | `public.duty_roster` | 14. Duty Roster | `waqt_name`, `duty_date`, `imam_staff_id`, `muazzin_staff_id`, `substitute_notes`. |
| 36 | `public.food_schedules` | 15. Meal Schedule | `meal_type` (lunch/dinner), `schedule_date`, `provider_member_id`, `receiver_staff_id`, `status`. |
| 37 | `public.welfare_cases` | 29. Welfare & Zakat Fund | `applicant_name`, `category` (medical, widow, orphan, disaster), `grant_amount`, `status`. |
| 38 | `public.mosque_events` | 27. Events & Programs | `title`, `event_type` (Waz, Shab-e-Qadr, Iftar), `event_date`, `budget`, `venue`. |
| 39 | `public.cashbox_records` | 12. Jumu'ah Cashbox | Banknote denomination breakdown (৳1000–৳5), total, 3 witness names/signatures, voucher ID. |
| 40 | `public.office_documents` | 19. Administration | Document register, incoming/outgoing letters, contracts, resolution files. |
| 41 | `public.advisory_notes` | 02. Role & Permission | Sharia reviews by Imam and strategic guidance from Advisory Board. |
| 42 | `public.smart_board_quotes` | 04. Sahih Hadith & Quotes | Wall quotes, Friday instructions, Sahih Hadith rotations. |
| 43 | `public.election_schedules` | 21. Election & Nomination | Term title (e.g. 2026–2028), Chief Election Commissioner, schedule dates, gazette notes. |
| 44 | `public.election_candidates`| 21. Election & Nomination | Target role, candidate name, proposer, seconder, symbol/panel, votes received, is_elected. |
| 45 | `public.mosque_properties` | 09. Waqf Asset Directory | Waqf land, commercial shops, market, ponds, deed/registration numbers, estimated valuation. |

---

## 2. Server-Side RPC Functions Audit (10 Procedures)

All multi-step financial, payment, and security actions MUST execute through the pre-deployed PostgreSQL RPC stored procedures to guarantee transactional ACID compliance and enforce server-side security:

```sql
-- 1. Check current authenticated profile & mosque
public.current_profile_id() RETURNS uuid;
public.current_mosque_id()  RETURNS uuid;

-- 2. Verify granular permission for current user
public.has_permission(p_permission text) RETURNS boolean;

-- 3. Human-readable business sequence code generator
public.generate_business_code(p_sequence_type text, p_prefix text) RETURNS text;

-- 4. Financial transaction submission (pending approval evaluation)
public.submit_finance_transaction(
  p_category_id   uuid,
  p_type          text, -- 'income' or 'expense'
  p_amount        numeric,
  p_txn_date      date,
  p_description   text,
  p_source_type   text,
  p_source_id     uuid,
  p_attachment_url text,
  p_idempotency_key text
) RETURNS public.transactions;

-- 5. Financial transaction approval / rejection (with self-approval prevention & tier verification)
public.approve_finance_transaction(
  p_transaction_id uuid,
  p_decision       text -- 'approved' or 'rejected'
) RETURNS public.transactions;

-- 6. Financial transaction reversal (cashless correction preserving audit trail)
public.reverse_finance_transaction(
  p_transaction_id uuid,
  p_reason         text
) RETURNS public.transactions;

-- 7. Request Chanda payment schedule entry
public.request_chanda_payment(
  p_member_id  uuid,
  p_month      text,
  p_amount_due numeric
) RETURNS public.chanda_payments;

-- 8. Receive Chanda payment (auto double-entry posts to Finance Income)
public.receive_chanda_payment(
  p_payment_id uuid,
  p_amount_paid numeric,
  p_method      text,
  p_notes       text
) RETURNS public.chanda_payments;

-- 9. Finalize payroll payment (links to Finance Expense pending tier approval)
public.finalize_payroll_payment(
  p_payroll_payment_id uuid
) RETURNS public.payroll_payments;

-- 10. Audit event logging (immutable log entry)
public.log_audit_event(
  p_action    text,
  p_entity    text,
  p_entity_id uuid,
  p_metadata  jsonb
) RETURNS void;
```

---

## 3. Drift Local Database Mapping (from Android Room DB v10)

The legacy application utilized Android Room DB version 10 with 24 entity classes (`Entities.kt`) and 10 progressive migrations (`MIGRATION_1_2` through `MIGRATION_9_10`).

In the Flutter rebuild, these 24 entities are mapped 1:1 to Drift tables in Dart:

```dart
// Core Drift Tables mapping 1:1 with Room Entities
class MosquesTable extends Table { ... }
class MembersTable extends Table { ... }
class TransactionsTable extends Table { ... }
class ChandaPaymentsTable extends Table { ... }
class StaffTable extends Table { ... }
class NoticesTable extends Table { ... }
class MeetingsTable extends Table { ... }
class AuditLogsTable extends Table { ... }

// Madrasa Drift Tables
class MadrasaStudentsTable extends Table { ... }
class MadrasaAttendanceTable extends Table { ... }
class HifzProgressTable extends Table { ... }
class MadrasaFeePaymentsTable extends Table { ... }

// Operations & Welfare Drift Tables
class StaffPayrollTable extends Table { ... }
class DutyRosterTable extends Table { ... }
class FoodSchedulesTable extends Table { ... }
class WelfareCasesTable extends Table { ... }
class MosqueEventsTable extends Table { ... }
class CashboxRecordsTable extends Table { ... }
class OfficeDocumentsTable extends Table { ... }
class AdvisoryNotesTable extends Table { ... }
class SmartBoardQuotesTable extends Table { ... }
class ElectionSchedulesTable extends Table { ... }
class ElectionCandidatesTable extends Table { ... }
class MosquePropertiesTable extends Table { ... }
```

### 3.1 Type Normalization & Invariant Rules
1. **Transaction Type Case Normalization:**
   - Cloud Supabase stores: `income` / `expense` (lowercase, enforced by Postgres CHECK constraint).
   - Drift Local DB stores: typed `TransactionType.income` / `TransactionType.expense`.
   - Normalizer automatically handles bidirectional conversion on sync.
2. **Date String Normalization:**
   - Bengali display strings (e.g. `১৭ নভেম্বর ২০২৬`) are converted to ISO 8601 `YYYY-MM-DD` before pushing to Postgres `DATE` columns.
3. **Double-Posting Prevention:**
   - Drift table `TransactionsTable` enforces a composite unique index on `(sourceType, sourceId)` when non-null, mirroring Postgres `UNIQUE (mosque_id, source_type, source_id)`.
4. **Tenant Isolation on Logout:**
   - Drift database provides an atomic `clearAllTenantData()` method executed on logout to prevent cross-mosque data pollution.
