# মসজিদ কমিটি ম্যানেজমেন্ট সিস্টেম
## PRD v2.0 — FINAL PRODUCTION PRODUCT REQUIREMENTS DOCUMENT

**Status:** FINAL / Master Source of Truth  
**Version:** 2.0  
**Platform:** Android  
**Primary language:** বাংলা  
**Backend:** Supabase/PostgreSQL  
**Architecture:** Kotlin + Jetpack Compose + MVVM/Clean Architecture  
**UI:** Custom Islamic Modern UI; Material Design 3 নয়

---

## 1. উদ্দেশ্য ও Product Vision

স্থানীয় মসজিদ কমিটির প্রশাসন, সদস্য, চাঁদা, আয়-ব্যয়, কর্মচারী, মাদরাসা/মক্তব, সভা, নোটিশ, নামাজের সময়, দায়িত্ব বণ্টন, রিপোর্ট ও যোগাযোগকে একটি নিরাপদ, সহজ, বাংলা-first digital platform-এ আনা। এটি social media নয়; এটি mosque/committee operations management system।

মূল লক্ষ্য: হিসাবের স্বচ্ছতা, সদস্য ও কমিটি ব্যবস্থাপনা, মাসিক চাঁদা ও payment history, finance approval/audit, Imam/Muazzin/Staff payroll, Madrasa operations, notification/communication এবং strict mosque-wise tenant isolation।

---

## 2. Product Identity

**নাম:** মসজিদ কমিটি ম্যানেজমেন্ট সিস্টেম

**Brand:** Islamic, Trustworthy, Professional, Clean, Calm, Modern, Bengali-first.

**Colors:** Primary Deep Emerald #1B5E20; Secondary Gold/Amber #FFC107; Accent #4CAF50; Background #F5F5F5/#FFFFFF; Card #F8FAFC; Text #212121/#1E293B.

---

## 3. UI/UX Design System

Material Design 3 ব্যবহার করা যাবে না। Custom design system: `MosqueColors`, `MosqueTypography`, `MosqueShapes`, `MosqueSpacing`, `MosqueElevation`, `MosqueMotion`, `MosqueIcons`, `MosqueComponents`।

Design language: Islamic geometric patterns, subtle mosque motifs, elegant Arabic/Islamic icons, 16dp rounded corners, soft elevation, restrained gradients, smooth micro-interactions, generous whitespace, mobile-first compact layouts, Bengali typography এবং accessibility-friendly contrast।

Typography: H1 32sp ExtraBold; H2 24sp Bold; H3 18sp SemiBold; Body Large 16sp Medium; Body Medium 14sp Normal; Label 12sp Medium; Button 16sp SemiBold.

Global header: left—official app logo, user name, role/designation; right—notification এবং profile/menu. Mosque name/Committee ID Dashboard Hero-তে থাকবে।

Primary bottom navigation: ড্যাশবোর্ড, সদস্য, ফাইন্যান্স, নোটিশ, আরও। Role অনুযায়ী visibility বদলালেও backend permission কখনো UI-এর উপর নির্ভর করবে না।

---

## 4. Authentication & Account

### President Registration
Two-step flow: President information → Mosque information → application. President fields: photo, বাংলা/English name, email, personal monthly subscription, optional NID, password/confirm. Mosque fields: logo, বাংলা/English name, address/location/map, established year, committee start/end year. Submit শেষে Committee ID এবং President ID দেখাতে হবে।

Production model: President application → System Admin review/approval → Active mosque. Development/demo mode production-এ disabled।

### Login
President: Committee ID + email + password. Member: Committee ID + Bangladesh mobile + password. Original onboarding rule-এ mobile-এর শেষ ৬ digit password suggestion ছিল; production security অনুযায়ী এটি suggestion মাত্র এবং first login password change করা উচিত। OTP mandatory নয়।

Account states: pending, active, inactive, suspended, archived.

One user = one mosque tenant. One user multiple roles পেতে পারে।

---

## 5. Roles & Permission Model

Committee roles: President, Vice-President, General Secretary, Assistant Secretary, Treasurer, Assistant Treasurer, Organizing Secretary, Publicity Secretary, Welfare Secretary, Executive Member, General Member.

Staff: Imam, Muazzin, Khadem, Other Staff. Platform: System Super Admin.

Role ≠ permission ≠ subscription ≠ relationship. Backend decision formula:

`Authenticated AND Correct Mosque AND Role Permission AND Individual Override AND Relationship Permission AND Subscription Entitlement AND Privacy Policy → Allow`

Representative permissions: member.view/create/update/archive; chanda.view/pay/receive; finance.view/create/approve/report; staff.view/manage/salary.view/salary.pay; madrasa.view/manage; student.view/create/update/archive; guardian.view/manage/portal; attendance.view/manage; hifz.view/manage; exam.view/manage/review/approve/publish; fee.view/manage/pay; timetable.view/manage; homework.view/manage/submit/review; madrasa.report.view; madrasa.ai.use; notice.view/manage; meeting.view/manage; prayer.view/manage; duty.view/manage; welfare.view/manage; subscription.view/manage.

---

## 6. Dashboard

### Hero Card
Mosque name + address + Committee ID + mosque logo/avatar.

### Smart Board
Bangla, English ও Hijri date; current time; সকাল/দুপুর/বিকেল/সন্ধ্যা/রাত; জরুরি নোটিশ; Islamic/Hajj information; upcoming meeting; prayer information. প্রায় ৫ সেকেন্ড পরপর subtle animation-এ পরিবর্তন।

### Prayer Card
ফজর, যোহর, আসর, মাগরিব, ইশা; current/next prayer highlight, countdown, Jummah/special prayer যেখানে প্রযোজ্য।

### Committee KPIs
মোট ব্যালেন্স, মোট সদস্য, বকেয়া চাঁদা, গুরুত্বপূর্ণ/জরুরি নোটিশ।

### Quick Actions
Committee: নতুন সদস্য, আয় যুক্ত করুন, ব্যয় যুক্ত করুন, নোটিশ প্রকাশ করুন, মিটিং সৃষ্টি করুন। Member: চাঁদা পরিশোধ করুন, আমার চাঁদা, নোটিশ, নামাজের সময়।

### Other
Recent Activity, Upcoming Meetings, Latest Notices, Relevant Alerts.

---

## 7. Member Management

Member list-এ search placeholder: “সদস্য খুঁজুন (নাম, মোবাইল, আইডি)”. Filters: role, status, subscription, area, join date. Compact KPI exactly 3: মোট সদস্য, সক্রিয় সদস্য, বকেয়া চাঁদা।

Add member: বাংলা/English name, mobile, email, NID, address, village/area, role, monthly chanda, join date, photo, status. Imam/Muazzin/Staff হলে conditional staff/salary fields।

Member profile: personal info, member ID, role, join date, chanda plan, ledger, payment/receipt history এবং authorized activity। নির্দিষ্ট সদস্যে tap করলে detail profile page খুলবে।

---

## 8. Chanda / Monthly Subscription

General/member users monthly chanda দিতে পারবে; staff category monthly donation-এর জন্য বাধ্যতামূলক member নয়। Admin action: “চাঁদা গ্রহণ করুন”; member action: “চাঁদা পরিশোধ করুন”. Cash payment সফল হলে Finance Income-এ automatically post হবে; manual duplicate deposit নিষিদ্ধ। Madrasa fee ও mosque chanda আলাদা source.

Methods: cash, mobile banking, future approved online gateway.

Ledger: month, due, paid, balance, method, date, status.

Official receipt: mosque, Committee ID, member, member ID, month, amount, method, transaction ID, receipt ID, date/time, received/verified by, QR verification code. QR verification only; correction = request → approval → reversal/adjustment → audit.

Double-post prevention: `(mosque_id, source_type, source_id)` uniqueness / equivalent invariant.

---

## 9. Finance Management

Income: monthly chanda, donation, Madrasa fee, Jummah collection, other. Expense: electricity, water, maintenance, salary, construction, event, Madrasa, welfare, other.

Transaction fields: transaction ID, category, income/expense, amount, date, description, source, attachment, creator, approval state. Money must use decimal/numeric—not floating point.

Approval policy baseline: ≤৳5,000 assigned authority; ≤৳20,000 President where required; ≤৳50,000 emergency/special authority; >৳50,000 higher committee approval. Exact authority configurable by mosque policy.

Self approval prohibited. Posted transaction direct delete prohibited. Corrections via reversal/adjustment. Audit + idempotency + period lock mandatory.

---

## 10. Staff & Payroll

Staff profile: role, designation বাংলা/English, employment type, joining/end date, salary, salary frequency, meal requirement, active status, notes.

Availability/leave: available range, status, reason, leave application, approved leave, substitute. Server-side overlap checks mandatory.

Payroll flow: Payroll record → Finance expense → Finance approval → Payroll finalized. One salary payment creates exactly one linked payroll payment and finance expense. Salary visibility requires `staff.salary.view`.

---

## 11. Duty Roster

Daily/weekly/monthly roster; prayer assignment; Imam/Muazzin assignment; attendance; leave; substitute; duty history; PDF export. Assignment to unavailable/approved-leave staff must be rejected server-side.

---

## 12. Food Schedule

Food schedule, provider assignment, receiver/beneficiary assignment, date/time, meal status, notes. Access must be role/relationship controlled.

---

## 13. Prayer Times

Five daily prayers, Jummah, Eid, Taraweeh/special prayer, seasonal changes, manual setup, optional location-based calculation, dashboard display এবং optional pre-prayer notification. Approved manual overrides must not be silently overwritten by auto-calculation.

---

## 14. Notice & Communication

Notice: title, rich content, date, expiry, image/PDF, priority Normal/Important/Urgent, audience, pin, edit, history. Audience: all members, committee only, specific roles, authorized targeted users.

AI Notice Generator using Gemini: short instruction → professional Islamic notice → human edit/review → publish/template. AI may draft but cannot autonomously publish or mutate authoritative records.

---

## 15. Meeting Management

Schedule title/date/time/location; agenda; invite all/specific roles; notifications; attendance; discussion points; decisions; action items; responsible person; deadlines; attachments; history; PDF minutes export.

---

## 16. Madrasa / Maktab

First-class module.

Academic structure: academic sessions, classes, subjects, teachers, teacher assignments.

Student: admission, Student ID, personal data, class, status, archive, guardians. Suggested ID `STU-YYYY-0001` or configured equivalent.

Guardian: guardian code, name, mobile, email, address, occupation, emergency contact, relationship, primary guardian, emergency-contact flag. Guardian portal only exposes server-authorized children.

---

## 17. Madrasa Attendance

Statuses: present, absent, late, excused, on_leave, not_applicable. Future date blocked; tenant/class/session/teacher assignment checked; unique student/date attendance; corrections auditable. Reports: daily, monthly, student and class summaries, attendance percentage.

---

## 18. Quran / Hifz

Record session date, Sabaq/Sabqi/Manzil, para, page/ayah range, quality score 1–5, mistake count, revision status, teacher feedback, revision due date. Milestone: student + para unique and completion percentage. Teacher assignment, tenant and archive/future-date validation mandatory.

---

## 19. Exam & Result

Exam: session, subject, exam type, instructions, status. Result: max marks, obtained marks, percentage, grade, status, submitted/reviewed/approved/published metadata. Workflow: Teacher Submit → Review → Approve → Publish. Report card includes subject results and overall result; only published results exposed to guardian/student views.

---

## 20. Madrasa Fees

Flow: Fee Setup → Student Fee → Payment → Receipt → Finance Income → Verification → Ledger. Madrasa fee is distinct from mosque chanda. Successful payment creates exactly one linked Finance Income; duplicate posting prevented; secure RPC, audit, idempotency and RLS mandatory.

---

## 21. Timetable

Academic session, class, subject, teacher, weekday, start/end time, room, status. Active slot uniqueness and teacher/class conflict detection required.

---

## 22. Homework

Teacher creates class/subject homework with assigned date and attachment. Student/guardian can view and submit. Teacher can review, give feedback and change status. Workflow and access server-enforced.

---

## 23. Madrasa Reports & Academic 360

Reports: student count, class distribution, attendance trends, Hifz progress, exam performance, fee collection/outstanding fees, teacher workload, homework completion. Student Academic 360: student summary, guardian, attendance, Hifz, published exams, homework, timetable and verified fee summary where authorized. Export PDF/CSV where appropriate.

---

## 24. Guardian Portal

Guardian sees own profile, authorized children, child academic summary, attendance, Hifz, published results, homework, timetable and relevant notices/notifications. No impersonation, OTP bypass or unrelated-student access.

---

## 25. Events & Programs

Eid, Milad, Waz, Iftar and educational events: date/time/location, description, agenda, budget, volunteers, expenses, gallery, post-event report.

---

## 26. Welfare

Case, application, review, assistance, approval, disbursement, finance linkage and audit. Welfare data is privacy-sensitive and requires stricter role access.

---

## 27. Notifications

In-app + FCM; future email/SMS provider optional. Types: subscription reminder, payment approval, notice, meeting, prayer, duty, leave, attendance, exam/result, homework, fee, guardian academic update. Use event_outbox + delivery tracking. Device tokens must support register/refresh/deactivate/logout lifecycle.

---

## 28. AI Assistant

Gemini may draft notices, meeting minutes, report summaries, academic summaries and suggestions. Lifecycle: Pending → Processing → Completed/Failed/Rejected. AI cannot approve finance, publish official notices automatically, bypass permission or mutate authoritative data. Secrets must never ship in Android client. AI requests auditable.

---

## 29. Subscription & Entitlement

Separate subscription plan, mosque subscription, entitlement, usage, invoice and payment. Entitlement checked server-side. Subscription never replaces role/permission.

---

## 30. Database Architecture

Core identity/tenant: `mosques`, `profiles`, `roles`, `permissions`, `role_permissions`, `user_roles`, `user_permission_overrides`.

Members/staff: `members`, `member_preferences`, `staff`, `staff_availability`.

Madrasa: `academic_sessions`, `students`, `guardians`, `guardian_profiles`, `student_guardians`, `madrasa_classes`, `subjects`, `teacher_assignments`, `madrasa_attendance`, `hifz_progress`, `hifz_milestones`, `madrasa_exams`, `madrasa_exam_results`, `madrasa_result_reviews`, `fee_types`, `student_fees`, `fee_payments`, `madrasa_timetables`, `madrasa_homework`, `madrasa_homework_submissions`.

Finance: `finance_categories`, `transactions`, `approval_policies`, `financial_reconciliations`, `chanda_schedules`, `chanda_payments`.

Operations: `prayer_settings`, `prayer_times`, `iqamah_settings`, `prayer_overrides`, `duty_roster`, `food_schedules`, `food_assignments`, `meetings`, `meeting_invitees`, `meeting_agenda_items`, `meeting_decisions`, `tasks`, `notices`, `notice_audiences`, `notifications`, `notification_deliveries`, `device_tokens`, `events`.

Procurement/assets: `purchase_requests`, `purchase_request_items`, `rfqs`, `suppliers`, `supplier_quotations`, `purchase_orders`, `goods_receipts`, `inventory_items`, `inventory_transactions`, `assets`.

Projects: `projects`, `project_phases`, `project_boq_items`, `project_site_reports`, `project_change_requests`.

Platform: `subscription_plans`, `mosque_subscriptions`, `subscription_entitlements`, `subscription_usage`, `subscription_invoices`, `subscription_payments`, `ai_requests`, `audit_logs`, `security_events`, `privacy_consents`, `documents`, `event_outbox`, `workflow_instances`, `workflow_history`, `sync_operations`.

Rules: UUID PKs, separate business IDs, mosque_id tenant isolation, PostgreSQL as SSoT, UTC timestamps, numeric money, soft-delete/archive, indexes, unique/check constraints, RLS, secure RPC/Edge Functions, outbox and idempotency.

---

## 31. Multi-Tenant Security & RLS

Every tenant-owned record must have `mosque_id` directly or derive it securely through a parent. RLS mandatory. Never trust client-provided mosque_id, role, permission, subscription state or hidden UI. Tenant context must come from authenticated identity.

Secure RPC default: SECURITY INVOKER. SECURITY DEFINER only when necessary; fixed search_path, authenticated-only where appropriate, minimal privileges, explicit tenant/permission/validation checks. Sensitive direct client DML revoked.

---

## 32. API Contract

Request:
```json
{"request_id":"req_...","idempotency_key":"idem_...","data":{}}
```
Success:
```json
{"success":true,"data":{},"error":null,"meta":{"request_id":"...","api_version":"v1"}}
```
Error:
```json
{"success":false,"data":null,"error":{"code":"PERMISSION_DENIED","message":"আপনার এই কাজটি করার অনুমতি নেই।","details":null},"meta":{"request_id":"...","api_version":"v1"}}
```

Error families: AUTH_REQUIRED, AUTH_INVALID, SESSION_EXPIRED, ACCOUNT_INACTIVE, ACCOUNT_SUSPENDED, PASSWORD_RESET_REQUIRED, PERMISSION_DENIED, ROLE_NOT_ALLOWED, RELATIONSHIP_DENIED, TENANT_ACCESS_DENIED, SUBSCRIPTION_REQUIRED, FEATURE_NOT_ENTITLED, USAGE_LIMIT_REACHED, SUBSCRIPTION_SUSPENDED, VALIDATION_ERROR, INVALID_AMOUNT, INVALID_DATE, INVALID_STATUS, DUPLICATE_RECORD, FINANCE_APPROVAL_REQUIRED, SELF_APPROVAL_NOT_ALLOWED, PERIOD_LOCKED, PAYMENT_ALREADY_PROCESSED, PAYMENT_VERIFICATION_FAILED, SYNC_CONFLICT, SYNC_VERSION_MISMATCH, SYNC_OPERATION_EXPIRED, RATE_LIMITED, SERVICE_UNAVAILABLE, TIMEOUT, INTERNAL_ERROR.

HTTP: 401 auth; 403 permission; 402 subscription; 422 validation; 409 conflict/duplicate/locked; 429 rate limit; 503 retryable service; 504 timeout; 500 internal.

---

## 33. Android Architecture

Kotlin, Jetpack Compose, MVVM, Clean Architecture, Navigation Compose, Hilt, Coroutines, StateFlow/SharedFlow, Coil, Room, DataStore, WorkManager, Android Keystore, Kotlin Serialization, OkHttp, Supabase SDK/RPC, FCM.

Layers: UI → ViewModel → UseCase → Repository → Remote/Local Data Source. UI contains no authoritative authorization. Local cache is not SSoT. PostgreSQL/Supabase is SSoT.

---

## 34. Offline & Sync

Read models cache করা যাবে। Safe operations idempotency key সহ queue/retry করতে পারবে। Version conflict detect করতে হবে। Offline mode permission, finance approval, payment verification বা tenant isolation bypass করতে পারবে না। Sensitive operations require online confirmation.

---

## 35. Storage

Supabase Storage: profile photos, mosque logo, notice attachments, meeting documents, receipts, Madrasa attachments. Private by default, signed URLs, tenant ownership check, file validation, no client secrets.

---

## 36. Audit, Privacy & Security Events

Audit: login/security changes, member mutations, role/permission changes, finance, payment/receipt, payroll, student/guardian, exam workflow, notices, meetings, subscription and AI requests. Record actor, mosque, action, entity, entity ID, timestamp, metadata/request ID.

Sensitive data: NID, bank details, salary, welfare, guardian/student data. Apply least privilege, masking, secure storage and auditable access.

---

## 37. Performance & Accessibility

Paginated lists, indexed queries, compressed images, no N+1, smooth loading, compact mobile layouts, clear empty/error states. Bengali readable typography, sufficient contrast, practical touch targets, content descriptions, color-independent status cues, scalable text.

---

## 38. Testing Strategy

Unit: business rules, permissions, money/date calculations, state, serialization. Integration: repository/RPC, RLS, finance/chanda/payroll/fee linkage. Security: cross-mosque access, unauthorized role, direct DML, self approval, IDOR, forged tenant/role/subscription. E2E critical journeys: President/member login, member CRUD/profile, chanda/receipt, finance/approval, staff/payroll/leave/duty, student/guardian, attendance, Hifz, exam workflow, fees, timetable, homework, notice, meeting, prayer, notification, AI draft and cross-tenant rejection.

---

## 39. Production Acceptance Criteria

A feature is not complete merely because UI/table/RPC exists. Production complete only when requirement, database, server authorization, RLS, RPC/API, Android UI, loading/error/empty states, audit for sensitive actions, idempotency for money/payment, tests, critical E2E, cross-tenant safety and secret protection are all satisfied.

---

## 40. Current Implementation Snapshot

**Implemented/Foundation:** core database/security foundation; tenant/RLS foundation; permission engine; API envelope; Member command center/list/search/profile; Chanda ledger and receipt backend; Finance transaction/approval/self-approval controls; Staff CRUD/payroll/availability/duty/food foundations; Madrasa foundation, students, guardians, classes/subjects/teachers, attendance, Hifz, exams/results, timetable, homework; Android Madrasa dashboard/navigation foundations; backend-connected student profile/academic 360; guardian portal foundation.

**Partial / needs verification or hardening:** Madrasa Fees exact current contract; Reports/Analytics/AI exact current implementation; FCM full end-to-end delivery; QR encoder/scanner UI; Android full production build; authenticated multi-user E2E; final custom Islamic design-system migration; release pipeline.

This section is a planning snapshot—not a live verification report. Before release, every partial item must be verified against the actual repository/database.

---

## 41. Release Gates

**Security:** RLS audit, cross-tenant tests, sensitive RPC review, no client secret, unsafe DML removed.  
**Data:** duplicate finance posting impossible, payment idempotency, receipt verification, payroll/fee finance linkage.  
**Android:** clean build, release build, critical paths, offline/online, complete navigation.  
**UX:** custom design system, no prohibited Material 3, Bengali typography, accessibility, loading/error/empty states.  
**E2E:** critical journeys pass with real authenticated users across at least two mosque tenants.

---

## 42. Development Governance

Future work must follow:

**PRD Requirement → Change Request → Design → DB/RPC → Android → Tests → Verification → Status Update**

New requirement that conflicts with this PRD requires a Change Request and PRD version update after approval. Bug fixes only change the PRD when product behavior changes.

---

## 43. Priority Roadmap

### P0 — Production foundation
Authentication, tenant isolation, roles/permissions, Member, Chanda, Finance, Staff/payroll, core Dashboard, Notices, critical Notifications, backup/recovery, security testing.

### P1 — Core expansion
Madrasa, Guardian, Attendance, Hifz, Exam/Result, Fees, Timetable, Homework, Reports, Duty, Meetings, Prayer.

### P2 — Advanced
AI assistant, advanced analytics, procurement, inventory, assets, projects, advanced welfare, subscription automation.

---

## 44. Final Product Definition

The finished application must be a secure, production-ready digital operating system for mosque committees. It must feel Islamic, modern, trustworthy, fast, simple, professional and Bengali-first. It must behave securely, consistently, audibly, tenant-isolated, permission-aware, financially reliable and resilient to duplicate requests.

**PostgreSQL/Supabase is the authoritative source of truth. Android is a presentation/interaction layer, not a security boundary. This PRD is the Master Source of Truth for future development.**
