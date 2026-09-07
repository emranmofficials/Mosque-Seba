# Development Phases

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 40, 43).

## Priority roadmap (PRD §43)

### P0 — Production foundation
Authentication, tenant isolation, roles/permissions, Member, Chanda, Finance, Staff/payroll, core Dashboard, Notices, critical Notifications, backup/recovery, security testing.

### P1 — Core expansion
Madrasa, Guardian, Attendance, Hifz, Exam/Result, Fees, Timetable, Homework, Reports, Duty, Meetings, Prayer.

### P2 — Advanced
AI assistant, advanced analytics, procurement, inventory, assets, projects, advanced welfare, subscription automation.

## Current implementation snapshot (PRD §40)

> ⚠️ This is a **planning snapshot from the PRD**, not a live, verified status of this repository. Every item below must be re-verified against the actual code/database in this repo before being trusted — see `docs/QA_ACCEPTANCE_CRITERIA.md` and the Definition of Complete in `docs/DEVELOPMENT_RULES.md`.

**Reported as implemented / foundation-level (per PRD, unverified here):**
- Core database/security foundation
- Tenant/RLS foundation
- Permission engine
- API envelope
- Member command center, list, search, profile
- Chanda ledger and receipt backend
- Finance transaction/approval/self-approval controls
- Staff CRUD, payroll, availability, duty, food foundations
- Madrasa foundation: students, guardians, classes/subjects/teachers, attendance, Hifz, exams/results, timetable, homework
- Android Madrasa dashboard/navigation foundations
- Backend-connected student profile / Academic 360
- Guardian portal foundation

**Reported as partial / needs verification or hardening:**
- Madrasa Fees — exact current contract
- Reports / Analytics / AI — exact current implementation
- FCM full end-to-end delivery
- QR encoder/scanner UI
- Android full production build
- Authenticated multi-user E2E testing
- Final custom Islamic design-system migration (ensuring no Material Design 3 remnants)
- Release pipeline

## Phase gate

No phase (P0 → P1 → P2) should be considered started in earnest until the prior phase's items pass the Definition of Complete in `docs/DEVELOPMENT_RULES.md` and the release gates in `docs/QA_ACCEPTANCE_CRITERIA.md` — in particular, P1 (Madrasa/Guardian/academic features) should not outpace P0's tenant isolation, security testing, and finance correctness.
