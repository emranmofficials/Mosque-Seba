# Requirement Traceability

> Purpose: map every PRD requirement to its concrete implementation artifacts (DB tables/RLS/RPC, Android screens, tests) so completion claims are verifiable rather than assumed — per the Definition of Complete in `docs/DEVELOPMENT_RULES.md`.

## Implemented Modules & Verification Evidence

### 1. Authentication & Account (PRD §4)
- **PRD requirement summary:** President registration, mosque onboarding, multi-role login, and persistent session state.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/SplashScreen.kt`, `LoginScreen.kt`, `RegistrationScreen.kt`, `SessionManager.kt`
- **Database / Entities:** Room `MosqueDatabase.kt`, `MemberEntity`, `SessionManager` (Encrypted/SharedPreferences), Supabase `0002_core_identity_tenant.sql`
- **Status:** Done

### 2. Roles & Permissions (PRD §5)
- **PRD requirement summary:** Role catalogue (9 user roles) with strict responsibility feature isolation.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/data/model/Enums.kt` (`UserRole`), `app/src/main/java/com/mosqueseba/app/ui/components/RoleUiUtils.kt`, dynamic navigation in `MainActivity.kt`
- **RLS Policies:** `supabase/policies/0001_core_identity_tenant_rls.sql`, `0002_members_staff_rls.sql`
- **Status:** Done

### 3. Dashboard (PRD §6)
- **PRD requirement summary:** Interactive prayer card, waqt countdown, Islamic Hijri date banner, financial KPIs, quick action cards, and role-specific devotional cards.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/DashboardScreen.kt`, `HeroCard.kt`, `PrayerCard.kt`, `SmartBoardCard.kt`, `KpiCards.kt`, `IslamicDecorations.kt`
- **Status:** Done

### 4. Member Management (PRD §7)
- **PRD requirement summary:** Member directory with search, filter, detailed profile, add/edit member, and chanda ledger.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/MembersScreen.kt`, `MemberProfileScreen.kt`, `Dialogs.kt` (`AddMemberDialog`)
- **Database / DAOs:** `app/src/main/java/com/mosqueseba/app/data/local/entity/Entities.kt` (`MemberEntity`), `Daos.kt` (`MemberDao`)
- **Status:** Done

### 5. Chanda / Subscription (PRD §8)
- **PRD requirement summary:** Monthly chanda collection, cycle resolution, verified digital receipt preview with QR code, and automatic posting to Finance.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/components/ChandaMonthUtils.kt`, `Dialogs.kt` (`ReceiveChandaDialog`, `ChandaReceiptDialog`)
- **Database / DAOs:** `ChandaPaymentEntity`, `ChandaPaymentDao`, `receive_chanda_payment.sql`
- **Status:** Done

### 6. Finance Management (PRD §9)
- **PRD requirement summary:** Multi-tier voucher approval, income/expense entries, cash and bank reconciliation, audit log, PDF reports.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/FinanceScreen.kt`, `KpiCards.kt` (`PresidentPendingVouchersCard`), `MoreDialogs.kt`
- **Database / DAOs:** `TransactionEntity`, `TransactionDao`, `AuditLogEntity`, `AuditLogDao`
- **Status:** Done

### 7. Staff & Payroll (PRD §10)
- **PRD requirement summary:** Staff directory, salary disbursement workflow, linking payroll directly to Finance expense.
- **Android Screens / Components:** `Dialogs.kt` (`PaySalaryDialog`), `MemberProfileScreen.kt` (Staff salary history)
- **Database / DAOs:** `StaffEntity`, `StaffDao`, `finalize_payroll_payment.sql`
- **Status:** Done

### 8. Duty Roster & Food Schedule (PRD §11, §12)
- **PRD requirement summary:** 5 waqt prayer duty roster for Imam/Muazzin and staff meals provider tracking.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/MoreOperationsHub.kt`, `MoreDialogs.kt`
- **Database / DAOs:** `DutyRosterEntity`, `FoodScheduleEntity`, respective DAOs in `Daos.kt`
- **Status:** Done

### 9. Madrasa & Maktab (PRD §16, §17, §18, §20)
- **PRD requirement summary:** Student management, daily attendance, Hifz/Quran progress (para/page rating), and fee collection.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/MadrasaScreen.kt`
- **Database / DAOs:** `StudentEntity`, `StudentDao`
- **Status:** Done

### 10. Notices & Gemini AI Assistant (PRD §14, §28)
- **PRD requirement summary:** Official circulars, pinned notices, audience targeting, and 1-click Google Gemini AI notice draft generator.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/NoticesScreen.kt`, `Dialogs.kt` (`PublishNoticeDialog`)
- **Database / DAOs:** `NoticeEntity`, `NoticeDao`, `aiNoticeState` in `MosqueViewModel.kt`
- **Status:** Done

### 11. Dark Mode, Adaptive Layout & Security (PRD §33, §37)
- **PRD requirement summary:** Full Islamic emerald-dark theme, Tablet/Foldable adaptive `NavigationRail`, and Firebase App Check reCAPTCHA Enterprise.
- **Android Components:** `Theme.kt`, `Color.kt`, `MainActivity.kt` (`MosqueNavigationRail`), `AppCheckHelper.kt`
- **Status:** Done
