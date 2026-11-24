# MOSQUE SEBA (মসজিদ সেবা) — PHASED MIGRATION ROADMAP

**Project:** Mosque Seba (`com.mosqueseba.app`)  
**Execution Strategy:** Sequential, Phased, Verifiable Feature-First Rebuild  
**Governing Rule:** Never claim a feature is complete merely because UI exists. A feature is complete only when interface, business logic, persistence, permissions, validation, and integrations are verified.  

---

## Migration Phase Overview

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7 ──► Phase 8 ──► Phase 9 ──► Phase 10
 Audit      Found-     Auth &       Dashboard   Member &    Staff &     Comms &     Madrasa     Events &    Docs,       Final
 & Plan     ation      RBAC         Islamic     Finance     Ops         Admin       Education   Welfare     Security    Release
```

---

## Detailed Phase Breakdown

### Phase 0 — Pre-Development Audit & Strategy (Completed & Verified)
- **Objectives:**
  - Complete codebase, database, security, and PRD inspection.
  - Formulate Drift schema, Riverpod architecture, and GoRouter layout.
  - Publish all 6 mandatory audit deliverables.
- **Deliverables:**
  - `docs/AUDIT_REPORT.md`
  - `docs/FEATURE_PARITY_MATRIX.md`
  - `docs/DATABASE_COMPATIBILITY_REPORT.md`
  - `docs/ROLE_PERMISSION_AUDIT.md`
  - `docs/FLUTTER_ARCHITECTURE_PLAN.md`
  - `docs/MIGRATION_ROADMAP.md`
- **Gate:** Formal approval from user before writing code.

---

### Phase 1 — Architecture, Foundation & Design System
- **Modules Covered:**
  - Module 34: Dark Mode
  - Module 35: Responsive UI (AdaptiveScaffold)
  - Module 37: Header & Branding
- **Tasks:**
  1. Configure `pubspec.yaml` with Riverpod, Drift, Supabase Flutter, GoRouter, PDF, and printing.
  2. Implement `core/theme/` (colors, typography, dimensions, gradients, theme mode).
  3. Implement `core/database/` (Drift setup, tables, and migration manager).
  4. Implement `core/router/` (GoRouter setup, routes, error pages).
  5. Implement `core/supabase/` (Supabase initialization, client singleton).
  6. Create `shared/` widgets (AdaptiveScaffold, AppHeader, AppButton, AppTextField, CardShell).
- **Verification Gate:** Clean build, running app showing themed shell with responsive navigation on phone and tablet.

---

### Phase 2 — Authentication, Onboarding & Role Permissions
- **Modules Covered:**
  - Module 01: Authentication & Account
  - Module 02: Role & Permission Engine
- **Tasks:**
  1. Port `SessionManager` to `flutter_secure_storage` + `shared_preferences`.
  2. Implement `SplashScreen` with session recovery and token refresh.
  3. Build `LoginScreen` (Committee email/password and Musalli mobile login).
  4. Build `RegistrationScreen` (President signup, mosque onboarding, code generation).
  5. Build `OnboardingScreen` (institution setup wizard).
  6. Implement RBAC Permission Engine with route guards in GoRouter.
- **Verification Gate:** Successful login, role-based menu filtering, session persistence across restart.

---

### Phase 3 — Dashboard & Islamic Devotional Hub
- **Modules Covered:**
  - Module 03: Smart Dashboard
  - Module 04: Sahih Hadith Digital Board
  - Module 05: Islamic Calendar & Event Hub
  - Module 06: Moon-Sighting Offset
  - Module 07: Ramadan Special Hub
  - Module 16: Prayer Times
- **Tasks:**
  1. Implement `HeroCard` with mosque name, logo, date, and user designation.
  2. Build dynamic Prayer Time Calculator (5 waqts, Jummah, live countdown timer).
  3. Build Sahih Hadith rotation board with offline storage.
  4. Build Islamic Calendar with 13 annual occasions and countdowns.
  5. Implement Moon-Sighting (-1, 0, +1 day) persistent selector.
  6. Implement Ramadan Hub (Sehri/Iftar countdown, Taraweeh tracker, Itikaf log, Khatam Quran).
- **Verification Gate:** Timers count down smoothly, Hadith rotates correctly, moon offset adjusts Hijri calendar accurately.

---

### Phase 4 — Membership, Subscriptions & Financial Core
- **Modules Covered:**
  - Module 08: Member Management
  - Module 09: Waqf Asset Directory
  - Module 10: Subscription & Donation (Chanda)
  - Module 11: Finance Management
  - Module 12: Jumu'ah Cashbox (3-Witness)
- **Tasks:**
  1. Member directory with search, filter, profile detail, and add/edit modals.
  2. Waqf asset inventory (movable/immovable property, deed registration).
  3. Chanda collection with month selection, QR-verified receipts, and auto Finance cross-post.
  4. Finance income/expense ledgers, bank/cash balances, and audit history.
  5. Two-tier voucher approval workflow (enforcing self-approval prohibition).
  6. Jummah Cashbox counter (৳1000–৳5 denominations, 3-witness signature panel, auto-voucher post).
- **Verification Gate:** Double-post prevention passes, cashbox balances sum accurately, self-approval is blocked.

---

### Phase 5 — Staff & Operational Management
- **Modules Covered:**
  - Module 13: Staff & Payroll
  - Module 14: Duty Roster
  - Module 15: Meal Schedule
- **Tasks:**
  1. Staff directory with salary visibility gating (`staff.salary.view`).
  2. Monthly salary voucher generation with Finance expense cross-posting.
  3. Five waqt and Jummah duty rosters with Imam/Muazzin assignments.
  4. Family-based meal rota for Imam and Khadem with lunch/dinner tracking.
- **Verification Gate:** Non-treasurer roles cannot see salary figures, rosters display cleanly on monthly grid.

---

### Phase 6 — Communication & Administration
- **Modules Covered:**
  - Module 17: Notices & Communication
  - Module 18: Notification Center
  - Module 19: Meeting Management
  - Module 20: Emergency Janaza Alert
  - Module 21: Election & Nomination Gazette
- **Tasks:**
  1. Rich notice feed with audience targeting, priority badges, and image attachments.
  2. In-app notification center with unread badges.
  3. Committee meeting scheduler, agenda builder, and permanent resolution archive.
  4. Emergency Janaza alert with WhatsApp/SMS deep-link broadcasting.
  5. Committee election module (schedules, candidates, symbol allocation, ballot results, gazette).
- **Verification Gate:** Notice pinning works, Janaza broadcaster generates correct URL intents, election votes tally properly.

---

### Phase 7 — Madrasa & Islamic Education
- **Modules Covered:**
  - Module 22: Madrasa/Maktab Core
  - Module 23: Madrasa Attendance
  - Module 24: Quran & Hifz Tracking
  - Module 25: Exams & Results
  - Module 26: Madrasa Fees
- **Tasks:**
  1. Student admission, directory, and guardian contact records.
  2. Daily student attendance sheet with percentage summaries.
  3. Interactive 5-star Hifz session logger and Para completion milestones.
  4. Exam result entry, 360-degree evaluation, and printable student report cards.
  5. Student fee collection with receipt generation and Finance income posting.
- **Verification Gate:** Attendance uniqueness per date holds, Hifz milestones update correctly, fee payments reflect in Finance ledger.

---

### Phase 8 — Events, Welfare & Creative AI
- **Modules Covered:**
  - Module 27: Events & Programs
  - Module 28: Islamic Dawat Poster AI
  - Module 29: Welfare & Zakat Fund
- **Tasks:**
  1. Event manager for Waz Mahfil, Shab-e-Qadr, and Iftar gatherings with budget tracking.
  2. Islamic Dawat Poster AI tool using Gemini 3.8 Flash to generate event banners and copy.
  3. Zakat & Welfare application registry, review workflow, and disbursement tracking.
- **Verification Gate:** Poster prompts generate authentic Bengali Islamic copy, welfare grants require proper approval.

---

### Phase 9 — Documents, Platform Security & Cloud Sync
- **Modules Covered:**
  - Module 30: AI Assistant
  - Module 31: Official Seal & Signature
  - Module 32: Native Print & PDF
  - Module 33: Application Security
  - Module 36: Supabase Synchronization
- **Tasks:**
  1. Gemini AI assistant for Khutbah ideas and formal Bengali notice drafting.
  2. Touchscreen signature canvas pad and official seal image upload.
  3. Cross-platform vector PDF generator and native printer integration for receipts and vouchers.
  4. App security hardening (root detection heuristics, token encryption).
  5. Bidirectional Supabase sync manager with offline outbox queue and conflict resolution.
- **Verification Gate:** PDF prints cleanly with digital signature and seal, offline writes sync seamlessly upon reconnect.

---

### Phase 10 — Integration, Regression & Production Readiness (Completed & Verified)
- **Status:** `VERIFIED` (100% Complete)
- **Tasks:**
  1. End-to-end integration testing across all 37 modules.
  2. Feature parity verification against legacy APK and PRD checklist.
  3. Multi-tenant security audit (ensuring zero data leakage across mosques).
  4. Android and iOS release build testing.
  5. Prepare final `RELEASE_READINESS.md` report.
- **Verification Gate:** 100% test pass rate, zero compilation warnings, verified Supabase compatibility, and all 18 Acceptance Gates approved.
