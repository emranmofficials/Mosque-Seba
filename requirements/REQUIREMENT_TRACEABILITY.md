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

### 12. Islamic Dawatnama Poster AI (PRD §14, §28)
- **PRD requirement summary:** Waz mahfil invitation generation, AI visual prompt crafting, event details formatting, and notice image attachment.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/dialogs/IslamicDawatnamaDialog.kt`, `NoticesScreen.kt`
- **Database / DAOs:** Room `NoticeEntity` (`imageUrl` via `MIGRATION_8_9`), Supabase `notices` table.
- **Status:** Done

### 13. Jummah Cashbox 3-Witness Counter (PRD §9)
- **PRD requirement summary:** Real-time physical banknote counting (৳১০০০ down to ৳৫), three independent witness sign-offs, and automatic voucher posting to Finance.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/dialogs/CashboxCounterDialog.kt`, `FinanceScreen.kt`
- **Database / DAOs:** `TransactionEntity` + `TransactionDao`, `AuditLogEntity` + `AuditLogDao`.
- **Status:** Done

### 14. Emergency Janaza Alert Broadcaster (PRD §14, §27)
- **PRD requirement summary:** Immediate broadcast of deceased member details, namaz-e-janaza time, location, and grave site via SMS, WhatsApp, and in-app notice.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/dialogs/JanazaAlertDialog.kt`
- **Status:** Done

### 15. Official Seals & Digital Canvas Signatures (PRD §7, §9, §10)
- **PRD requirement summary:** Touch-screen interactive signature drawing pad, official stamp image upload, designation assignment, and document embedding.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/dialogs/OfficialSealSignatureDialog.kt`
- **Database / DAOs:** Room `MIGRATION_9_10` (`sealUrl` on `mosques`, `signatureUrl`, `stampUrl`, `designationBn` on `members`, `signatureUrl`, `stampUrl` on `staff`).
- **Status:** Done

### 16. Native 1-Click Print & PDF Document Engine (PRD §8, §9, §10)
- **PRD requirement summary:** Printing formal chanda receipts, expense payment vouchers, staff monthly salary slips, and circular notices with embedded seals & signatures.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/components/MosquePrintHelper.kt` using Android `PrintManager` and clean responsive printable HTML layouts.
- **Status:** Done

### 17. Election Schedule, Nomination & Gazette Publisher (PRD §29)
- **PRD requirement summary:** Comprehensive committee election management: nomination phases, scrutiny, symbol allotment, voting schedule, and formal election gazette publishing.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/screens/MoreOperationsHub.kt`, `MoreDialogs.kt`
- **Database / DAOs:** `ElectionScheduleEntity`, `ElectionCandidateEntity`, `ElectionDao` in Room DB.
- **Status:** Done

### 18. Ramadan Special Hub & Quran Tracker (PRD §13)
- **PRD requirement summary:** Live Sahri and Iftar countdown timer, daily Taraweeh prayer rak'ah tracking, Itikaf participant registration, and Quran Khatam progress logging.
- **Android Screens / Components:** `app/src/main/java/com/mosqueseba/app/ui/dialogs/RamadanHubDialog.kt`, `DashboardScreen.kt`
- **Status:** Done

### 19. Supabase Cloud Backend, Multi-tenant RLS & Sync (PRD §32, §34, §35)
- **PRD requirement summary:** Multi-tenant PostgreSQL 17 cloud database (`donglyxtrfyzwgtrskhz`), 28 normalized tables, 4 RLS policies isolating data by `mosque_id`, 10 server-side stored procedures (RPCs), and bidirectional client-side sync.
- **Backend Artifacts:** `supabase/migrations/` (0001–0006), `supabase/policies/` (0001–0004), `supabase/functions/` (0001–0010), `supabase/full_schema_setup.sql`.
- **Client Synchronization:** `SupabaseConfig.kt`, `SupabaseHttpClient.kt`, `SupabaseSyncManager.kt`.
- **Status:** Done

