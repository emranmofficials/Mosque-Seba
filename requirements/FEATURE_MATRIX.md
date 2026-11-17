# Feature Matrix

> Derived from `requirements/PRD_v2.0_FINAL.md` (all sections) and `docs/DEVELOPMENT_PHASES.md`. `Status` must be updated only after verification per `docs/QA_ACCEPTANCE_CRITERIA.md` — do not mark items Done from memory or assumption.

Status legend: `Not started` · `In progress` · `Needs verification` · `Done`

| # | Module | Key capabilities | PRD §§ | Priority | Status |
|---|---|---|---|---|---|
| 1 | Authentication & Account | President registration, mosque onboarding, login (Committee/Musalli), session manager, splash | 4 | P0 | Done — `SplashScreen.kt`, `LoginScreen.kt`, `RegistrationScreen.kt`, `SessionManager.kt`, Supabase schema & RLS (`0002_core_identity_tenant.sql`) |
| 2 | Roles & Permissions | Role catalogue (9 roles), permission engine, strict role-responsibility UI isolation | 5 | P0 | Done — `UserRole` enum, `RoleUiUtils.kt`, dynamic navigation per role, Supabase RLS (`0001`–`0004_policies`) |
| 3 | Dashboard | Hero card, Smart Board, Prayer card, KPIs, Quick Actions, Devotional/Approval cards | 6 | P0 | Done — `DashboardScreen.kt`, `HeroCard.kt`, `SmartBoardCard.kt`, `PrayerCard.kt`, `KpiCards.kt` |
| 4 | Member Management | List/search/filter, add/edit member, member profile, chanda ledger | 7 | P0 | Done — `MembersScreen.kt`, `MemberProfileScreen.kt`, `showAddMemberDialog`, Room `MemberEntity` + DAO |
| 5 | Chanda / Subscription | Payment collection, cycle month, receipt + QR verification, auto double-entry posting | 8 | P0 | Done — `showReceiveChandaDialog`, `ChandaMonthUtils.kt`, digital receipt preview with QR, cross-post to Finance |
| 6 | Finance Management | Income/expense, 2-tier approval, cash/bank balance, audit log, PDF export/share | 9 | P0 | Done — `FinanceScreen.kt`, `PresidentPendingVouchersCard`, `TransactionEntity` + DAO, audit trail |
| 7 | Staff & Payroll | Staff profile, availability/leave, payroll flow, salary visibility, cross-posting | 10 | P0 | Done — `paySalaryTargetStaff`, `StaffEntity` + DAO, automatic finance expense linking |
| 8 | Duty Roster | Daily/weekly/monthly waqt roster, imam/muazzin duty assignment | 11 | P1 | Done — `DutyRoster` management, `allDutyRosters` in `MoreOperationsHub.kt` |
| 9 | Food Schedule | Provider/receiver assignment, meal status tracking for staff | 12 | P1 | Done — `FoodSchedule` module, `allFoodSchedules` in `MoreOperationsHub.kt` |
| 10 | Prayer Times | 5 daily prayers + Jummah, next waqt countdown, auto calculation | 13 | P1 | Done — `PrayerCard.kt`, `IslamicDecorations.kt`, dynamic countdown timer |
| 11 | Notice & Communication | Rich notice, audience targeting, pinned notices, AI draft assist | 14 | P0/P2 | Done — `NoticesScreen.kt`, `showPublishNoticeDialog`, `NoticeEntity` + DAO |
| 12 | Meeting Management | Schedule, agenda, invite, minutes, permanent resolutions record | 15 | P1 | Done — `showScheduleMeetingDialog`, `MeetingEntity` + DAO, resolutions archive |
| 13 | Madrasa / Maktab core | Students list, attendance, guardian contact, academic progress | 16 | P1 | Done — `MadrasaScreen.kt`, `StudentEntity` + DAO |
| 14 | Madrasa Attendance | Daily student attendance tracking and records | 17 | P1 | Done — integrated within `MadrasaScreen.kt` |
| 15 | Quran / Hifz | Session recording, para/page rating, milestones, teacher evaluation | 18 | P1 | Done — interactive star rating and progress in `MadrasaScreen.kt` |
| 16 | Exam & Result | Progress review, performance evaluation, student report card | 19 | P1 | Done — student 360 evaluation in `MadrasaScreen.kt` |
| 17 | Madrasa Fees | Student fee collection, digital receipt, Finance Income cross-posting | 20 | P1 | Done — fee payment workflow and finance integration |
| 18 | Events & Programs | Waz mahfil, Shab-e-Qadr, Iftar planning, budget & volunteer tracking | 25 | P2 | Done — `allEvents` tracking in `MoreOperationsHub.kt` |
| 19 | Welfare & Zakat | Medical grant, widow/orphan stipend, disaster relief, application approval | 26 | P2 | Done — `allWelfareCases` management in `MoreOperationsHub.kt` |
| 20 | Notifications | Role-based notification center, unread count badges, delivery tracking | 27 | P0/P1 | Done — `NotificationsScreen.kt`, unread badge in `GlobalHeader.kt` |
| 21 | AI Assistant | Gemini AI smart notice draft generator, human-in-the-loop review | 28 | P2 | Done — Gemini integration via `aiNoticeState`, draft assist dialog |
| 22 | Dark Mode | Full Islamic emerald-dark color palette, high contrast, runtime toggle | N/A | P0 | Done — `Theme.kt`, `Color.kt`, `AccountHubScreen.kt` |
| 23 | Tablet & Responsive | Adaptive NavigationRail for tablets/foldables, NavigationBottomBar for mobile | N/A | P0 | Done — `MosqueNavigationRail` (>= 600dp) in `MainActivity.kt` |
| 24 | Firebase App Check | API abuse prevention, reCAPTCHA Enterprise, debug provider fallback | N/A | P0 | Done — `AppCheckHelper.kt` in `app/src/main/` |
| 25 | Islamic Dawatnama Poster AI | Gemini 3.8 Flash poster prompt generator, visual design layout, image attachments | 14, 28 | P2 | Done — `IslamicDawatnamaDialog.kt`, Room `imageUrl` column (`MIGRATION_8_9`) |
| 26 | Jummah Cashbox 3-Witness Counter | Real-time denomination counting, 3-witness signature verification, auto-voucher posting | 9 | P0 | Done — `CashboxCounterDialog.kt`, auto cross-post to Finance with audit log |
| 27 | Emergency Janaza Alert | Community SMS/WhatsApp deep link, janaza prayer time & location broadcast | 14, 27 | P1 | Done — `JanazaAlertDialog.kt`, direct Android intent broadcaster |
| 28 | Official Seals & Signatures | Touch-screen signature canvas pad, official stamp image upload, designation storage | 7, 9 | P0 | Done — `OfficialSealSignatureDialog.kt`, Room DB v10 (`MIGRATION_9_10`) |
| 29 | Native 1-Click Print & PDF | Professional receipts, expense vouchers, salary slips, notices with embedded seals | 8, 9, 10 | P0 | Done — `MosquePrintHelper.kt` utilizing Android `PrintManager` and HTML layout |
| 30 | Election & Nomination Gazette | Election schedule, candidate nomination, symbol allocation, gazette publishing | 29 | P2 | Done — `ElectionScheduleEntity`, `ElectionCandidateEntity` + DAOs, `MoreOperationsHub.kt` |
| 31 | Ramadan Special Hub | Live Sehri/Iftar countdown timer, Taraweeh & Itikaf tracking, Khatam-ul-Quran logs | 13 | P1 | Done — `RamadanHubDialog.kt`, Islamic prayer calculator sync |
| 32 | Supabase Cloud Backend & Sync | Multi-tenant PostgreSQL 17, 28 tables, RLS isolation, 10 RPCs, bidirectional client sync | 32, 34 | P0 | Done — `SupabaseConfig.kt`, `SupabaseHttpClient.kt`, `SupabaseSyncManager.kt` |
| 33 | Header Cleanliness & Hardening | Official logo, user name and designation in header, institution name on Hero card | 3 | P0 | Done — `GlobalHeader.kt`, `ChandaMonthUtils.kt`, zero-emoji design compliance |

## How to use this table

1. Before implementation of a module, confirm the PRD section(s) it maps to.
2. While implementing, keep status at `In progress`.
3. Do **not** move to `Done` without passing the checklist in `docs/QA_ACCEPTANCE_CRITERIA.md` and updating `requirements/REQUIREMENT_TRACEABILITY.md` with the specific tables/RPCs/screens delivered.
