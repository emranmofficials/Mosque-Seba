# MOSQUE SEBA (মসজিদ সেবা) — FEATURE PARITY MATRIX (ALL 37 MODULES)

**Project:** Mosque Seba (`com.mosqueseba.app`)  
**Strategy:** 1:1 Strict Feature-Parity Rebuild  
**Status Legend:** `NOT STARTED` | `IN PROGRESS` | `IMPLEMENTED` | `TESTING` | `VERIFIED` | `BLOCKED`  

---

## Complete 37-Module Parity Mapping

### Group A: Core & Authentication

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **01** | **Authentication & Account** | §4 | `SplashScreen.kt` (160L), `LoginScreen.kt` (674L), `RegistrationScreen.kt` (1790L), `OnboardingScreen.kt` (374L), `SessionManager.kt`, `SupabaseAuthClient.kt` | `features/authentication/`<br>• `screens/splash_screen.dart`<br>• `screens/login_screen.dart`<br>• `screens/registration_screen.dart`<br>• `screens/onboarding_screen.dart`<br>• `widgets/committee_login_form.dart`<br>• `widgets/musalli_login_form.dart`<br>• `services/auth_service.dart`<br>• `providers/auth_provider.dart` | `VERIFIED` |
| **02** | **Role & Permission** | §5 | `UserRole` & `CommitteeTier` in `Enums.kt` (750L), `RoleUiUtils.kt`, `RoleResponsibilityDashboard.kt` (334L), `PERMISSION_MATRIX.md` | `features/role_permissions/`<br>• `models/committee_role.dart`<br>• `models/permission_engine.dart`<br>• `providers/permission_provider.dart`<br>• `widgets/role_badge.dart`<br>• `widgets/permission_guard.dart`<br>• `dialogs/role_responsibilities_dialog.dart` | `VERIFIED` |

---

### Group B: Dashboard & Islamic Hub

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **03** | **Smart Dashboard** | §6 | `DashboardScreen.kt` (3578L), `HeroCard.kt` (868L), `SmartBoardCard.kt` (653L), `KpiCards.kt` (734L), `RoyalHomeDashboardComponents.kt` (675L) | `features/dashboard/`<br>• `screens/dashboard_screen.dart`<br>• `widgets/hero_card.dart`<br>• `widgets/smart_board_card.dart`<br>• `widgets/prayer_countdown_card.dart`<br>• `widgets/kpi_summary_grid.dart`<br>• `widgets/quick_actions_grid.dart`<br>• `providers/dashboard_provider.dart` | `VERIFIED` |
| **04** | **Sahih Hadith Digital Board** | §6, §14 | `SmartBoardCard.kt` (embedded rotation), `smart_board_quotes` table | `features/hadith/`<br>• `screens/hadith_board_screen.dart`<br>• `widgets/hadith_card.dart`<br>• `models/hadith_item.dart`<br>• `services/hadith_service.dart`<br>• `providers/hadith_provider.dart` | `VERIFIED` |
| **05** | **Islamic Calendar & Event Hub** | §6, §13 | `IslamicCalendarModels.kt`, `IslamicEventCard.kt` (1110L), 13 annual occasions logic | `features/islamic_calendar/`<br>• `screens/islamic_calendar_screen.dart`<br>• `widgets/islamic_event_card.dart`<br>• `models/islamic_event.dart`<br>• `utils/hijri_calculator.dart`<br>• `providers/calendar_provider.dart` | `VERIFIED` |
| **06** | **Moon-Sighting Offset** | §6, §13 | `ChandaMonthUtils.kt`, `SessionManager.KEY_HIJRI_DAY_OFFSET` (-1, 0, +1 day offset) | `features/moon_sighting/`<br>• `widgets/moon_offset_selector.dart`<br>• `providers/moon_offset_provider.dart`<br>• `services/moon_sighting_service.dart` | `VERIFIED` |
| **07** | **Ramadan Special Hub** | §13 | `RamadanCard.kt` (1453L), `RamadanModels.kt`, Sehri/Iftar countdowns, Taraweeh & Itikaf logs | `features/ramadan/`<br>• `screens/ramadan_hub_screen.dart`<br>• `widgets/sehri_iftar_countdown.dart`<br>• `widgets/taraweeh_tracker_card.dart`<br>• `widgets/itikaf_log_card.dart`<br>• `widgets/khatme_quran_progress.dart`<br>• `providers/ramadan_provider.dart` | `VERIFIED` |

---

### Group C: Membership & Finance

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **08** | **Member Management** | §7 | `MembersScreen.kt` (1592L), `MemberProfileScreen.kt` (1473L), `Dialogs.kt` (`AddMemberDialog` 580L), `MemberEntity` + DAO | `features/membership/`<br>• `screens/member_list_screen.dart`<br>• `screens/member_profile_screen.dart`<br>• `dialogs/add_member_dialog.dart`<br>• `dialogs/edit_member_dialog.dart`<br>• `repositories/member_repository.dart`<br>• `providers/member_provider.dart` | `VERIFIED` |
| **09** | **Waqf Asset Directory** | §7, §30 | `CommitteeOperationsScreens.kt` (`mosque_properties`), `MosquePropertyEntity` + DAO | `features/waqf_assets/`<br>• `screens/waqf_asset_list_screen.dart`<br>• `screens/waqf_asset_detail_screen.dart`<br>• `dialogs/add_waqf_property_dialog.dart`<br>• `repositories/waqf_repository.dart`<br>• `providers/waqf_provider.dart` | `VERIFIED` |
| **10** | **Subscription & Donation** | §8 | `Dialogs.kt` (`ReceiveChandaDialog` 620L, `OfficialReceiptDialog` 450L), `ChandaPaymentEntity` + DAO | `features/subscriptions/`<br>• `screens/chanda_ledger_screen.dart`<br>• `dialogs/receive_chanda_dialog.dart`<br>• `widgets/qr_receipt_widget.dart`<br>• `repositories/chanda_repository.dart`<br>• `providers/chanda_provider.dart` | `VERIFIED` |
| **11** | **Finance Management** | §9 | `FinanceScreen.kt` (2444L), `Dialogs.kt` (`AddTransactionDialog` 610L), `PresidentPendingVouchersCard`, `TransactionEntity` + DAO | `features/finance/`<br>• `screens/finance_dashboard_screen.dart`<br>• `screens/transaction_list_screen.dart`<br>• `screens/pending_approval_screen.dart`<br>• `dialogs/add_transaction_dialog.dart`<br>• `widgets/balance_summary_cards.dart`<br>• `repositories/finance_repository.dart`<br>• `providers/finance_provider.dart` | `VERIFIED` |
| **12** | **Jumu'ah Cashbox** | §9 | `ui/dialogs/JummahCashboxDialog.kt` (610L), `CashboxRecordEntity` + DAO, 3-witness signature canvas | `features/cashbox/`<br>• `screens/cashbox_history_screen.dart`<br>• `dialogs/cashbox_counter_dialog.dart`<br>• `widgets/denomination_counter_row.dart`<br>• `widgets/witness_signature_panel.dart`<br>• `repositories/cashbox_repository.dart`<br>• `providers/cashbox_provider.dart` | `VERIFIED` |

---

### Group D: Staff & Operations

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **13** | **Staff & Payroll** | §10 | `MoreOperationsHub.kt` (Staff list), `MoreDialogs.kt` (`PaySalaryFormDialog`), `StaffEntity` + `StaffPayrollEntity` + DAOs | `features/staff/`<br>• `screens/staff_list_screen.dart`<br>• `screens/staff_profile_screen.dart`<br>• `screens/payroll_ledger_screen.dart`<br>• `dialogs/add_staff_dialog.dart`<br>• `dialogs/pay_salary_dialog.dart`<br>• `repositories/staff_repository.dart`<br>• `providers/staff_provider.dart` | `VERIFIED` |
| **14** | **Duty Roster** | §11 | `MoreOperationsHub.kt` (`allDutyRosters`), `MoreDialogs.kt` (`AddRosterFormDialog`), `DutyRosterEntity` + DAO | `features/duty_roster/`<br>• `screens/duty_roster_screen.dart`<br>• `dialogs/add_roster_slot_dialog.dart`<br>• `widgets/roster_calendar_grid.dart`<br>• `repositories/duty_roster_repository.dart`<br>• `providers/duty_roster_provider.dart` | `VERIFIED` |
| **15** | **Meal Schedule** | §12 | `MoreOperationsHub.kt` (`allFoodSchedules`), `MoreDialogs.kt` (`AddFoodScheduleFormDialog`, `AutoFoodScheduleDialog`), `FoodScheduleEntity` + DAO | `features/meal_schedule/`<br>• `screens/meal_schedule_screen.dart`<br>• `dialogs/assign_family_meal_dialog.dart`<br>• `dialogs/auto_schedule_generator_dialog.dart`<br>• `repositories/meal_repository.dart`<br>• `providers/meal_provider.dart` | `VERIFIED` |
| **16** | **Prayer Times** | §13 | `PrayerCard.kt` (520L), `IslamicDecorations.kt`, dynamic waqt calculations | `features/prayer_times/`<br>• `screens/prayer_schedule_screen.dart`<br>• `widgets/prayer_time_table.dart`<br>• `widgets/next_prayer_banner.dart`<br>• `services/prayer_calculator_service.dart`<br>• `providers/prayer_times_provider.dart` | `VERIFIED` |

---

### Group E: Communication & Administration

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **17** | **Notices & Communication** | §14 | `NoticesScreen.kt` (1461L), `Dialogs.kt` (`PublishNoticeDialog` 530L), `NoticeEntity` + DAO, audience targeting | `features/notices/`<br>• `screens/notice_feed_screen.dart`<br>• `screens/notice_detail_screen.dart`<br>• `dialogs/publish_notice_dialog.dart`<br>• `repositories/notice_repository.dart`<br>• `providers/notice_provider.dart` | `VERIFIED` |
| **18** | **Notification Center** | §27 | `NotificationsScreen.kt` (1342L), `GlobalHeader.kt` (unread badge), `NotificationEntity` | `features/notifications/`<br>• `screens/notification_center_screen.dart`<br>• `widgets/notification_item_tile.dart`<br>• `services/notification_service.dart`<br>• `providers/notification_provider.dart` | `VERIFIED` |
| **19** | **Meeting Management** | §15 | `Dialogs.kt` (`ScheduleMeetingDialog` 410L, `MeetingResolutionDialog` 380L), `MeetingEntity` + DAO | `features/meetings/`<br>• `screens/meeting_list_screen.dart`<br>• `screens/meeting_detail_screen.dart`<br>• `dialogs/schedule_meeting_dialog.dart`<br>• `dialogs/record_resolution_dialog.dart`<br>• `repositories/meeting_repository.dart`<br>• `providers/meeting_provider.dart` | `VERIFIED` |
| **20** | **Emergency Janaza Alert** | §14, §27 | `ui/dialogs/JanazaAlertDialog.kt` (420L), SMS & WhatsApp deep-link broadcaster | `features/janaza_alerts/`<br>• `screens/janaza_alert_screen.dart`<br>• `dialogs/create_janaza_alert_dialog.dart`<br>• `services/sms_whatsapp_broadcaster.dart`<br>• `providers/janaza_provider.dart` | `VERIFIED` |
| **21** | **Election & Nomination Gazette** | §29 | `ElectionScreen.kt` (1854L), `ElectionScheduleEntity` + `ElectionCandidateEntity` + DAOs | `features/elections/`<br>• `screens/election_dashboard_screen.dart`<br>• `screens/candidate_nomination_screen.dart`<br>• `screens/ballot_voting_screen.dart`<br>• `screens/gazette_publication_screen.dart`<br>• `repositories/election_repository.dart`<br>• `providers/election_provider.dart` | `VERIFIED` |

---

### Group F: Madrasa & Maktab

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **22** | **Madrasa/Maktab Core** | §16 | `MadrasaScreen.kt` (tab 0: student list & admission), `StudentEntity` + DAO | `features/madrasa/`<br>• `screens/madrasa_dashboard_screen.dart`<br>• `screens/student_directory_screen.dart`<br>• `dialogs/admit_student_dialog.dart`<br>• `repositories/madrasa_repository.dart`<br>• `providers/madrasa_provider.dart` | `VERIFIED` |
| **23** | **Madrasa Attendance** | §17 | `MadrasaScreen.kt` (tab 1: daily attendance sheet), `MadrasaAttendanceEntity` + DAO | `features/madrasa_attendance/`<br>• `screens/madrasa_attendance_screen.dart`<br>• `widgets/attendance_toggle_row.dart`<br>• `widgets/attendance_summary_chart.dart`<br>• `providers/madrasa_attendance_provider.dart` | `VERIFIED` |
| **24** | **Quran & Hifz Tracking** | §18 | `MadrasaScreen.kt` (tab 2: para/page rating, 5-star evaluation), `HifzProgressEntity` + DAO | `features/hifz_tracking/`<br>• `screens/hifz_tracking_screen.dart`<br>• `widgets/para_milestone_card.dart`<br>• `widgets/star_rating_bar.dart`<br>• `dialogs/log_hifz_session_dialog.dart`<br>• `providers/hifz_provider.dart` | `VERIFIED` |
| **25** | **Exams & Results** | §19 | `MadrasaScreen.kt` (tab 3: 360 review & report card), `StudentDetailsScreen` | `features/examinations/`<br>• `screens/exam_list_screen.dart`<br>• `screens/student_report_card_screen.dart`<br>• `dialogs/enter_exam_marks_dialog.dart`<br>• `dialogs/review_publish_exam_dialog.dart`<br>• `providers/exam_provider.dart` | `VERIFIED` |
| **26** | **Madrasa Fees** | §20 | `MadrasaScreen.kt` (tab 4: student fee collection & receipt), `MadrasaFeePaymentEntity` + DAO | `features/madrasa_fees/`<br>• `screens/fee_collection_screen.dart`<br>• `dialogs/collect_student_fee_dialog.dart`<br>• `widgets/student_fee_receipt_widget.dart`<br>• `providers/madrasa_fee_provider.dart` | `VERIFIED` |

---

### Group G: Events & Welfare

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **27** | **Events & Programs** | §25 | `MoreOperationsHub.kt` (`allEvents`), `MoreDialogs.kt` (`AddEventDialog`), `MosqueEventEntity` + DAO | `features/events/`<br>• `screens/event_list_screen.dart`<br>• `screens/event_detail_screen.dart`<br>• `dialogs/add_event_dialog.dart`<br>• `repositories/event_repository.dart`<br>• `providers/event_provider.dart` | `VERIFIED` |
| **28** | **Islamic Dawat Poster AI** | §14, §28 | `ui/dialogs/IslamicDawatnamaDialog.kt` (680L), Gemini 3.8 Flash prompt generator | `features/poster_ai/`<br>• `screens/poster_designer_screen.dart`<br>• `dialogs/islamic_dawatnama_dialog.dart`<br>• `widgets/poster_preview_canvas.dart`<br>• `services/poster_ai_service.dart`<br>• `providers/poster_provider.dart` | `VERIFIED` |
| **29** | **Welfare & Zakat Fund** | §26 | `MoreOperationsHub.kt` (`allWelfareCases`), `MoreDialogs.kt` (`AddWelfareDialog`), `WelfareCaseEntity` + DAO | `features/welfare_zakat/`<br>• `screens/welfare_dashboard_screen.dart`<br>• `screens/welfare_case_detail_screen.dart`<br>• `dialogs/add_welfare_case_dialog.dart`<br>• `dialogs/approve_welfare_grant_dialog.dart`<br>• `repositories/welfare_repository.dart`<br>• `providers/welfare_provider.dart` | `VERIFIED` |

---

### Group H: Documents, Security & Platform

| # | Module Name | PRD §§ | Legacy Source Implementation | Target Flutter Implementation Architecture | Status |
|---|---|---|---|---|---|
| **30** | **AI Assistant** | §28 | `MosqueViewModel.kt` (`draftNoticeWithAi`, `summarizeFinancesWithAi`), structured JSON schemas | `features/ai_assistant/`<br>• `dialogs/gemini_draft_assistant_dialog.dart`<br>• `screens/khutbah_ideas_screen.dart`<br>• `services/gemini_ai_service.dart`<br>• `providers/ai_assistant_provider.dart` | `VERIFIED` |
| **31** | **Official Seal & Signature** | §7, §9 | `ui/dialogs/OfficialSealSignatureDialog.kt` (720L), `MIGRATION_9_10`, touchscreen signature canvas | `features/official_signature/`<br>• `dialogs/signature_pad_dialog.dart`<br>• `dialogs/seal_stamp_upload_dialog.dart`<br>• `widgets/verified_signature_preview.dart`<br>• `services/signature_storage_service.dart` | `VERIFIED` |
| **32** | **Native Print & PDF** | §8, §9, §10 | `MosquePrintHelper.kt` (Android `PrintManager` + WebView HTML) | `features/printing_pdf/`<br>• `services/pdf_generator_service.dart`<br>• `services/native_printing_service.dart`<br>• `templates/receipt_pdf_template.dart`<br>• `templates/voucher_pdf_template.dart`<br>• `templates/salary_slip_pdf_template.dart`<br>• `templates/notice_pdf_template.dart` | `VERIFIED` |
| **33** | **Application Security** | §31, §36 | `MosqueSecurityManager.kt` (root detection heuristics), `AppCheckHelper.kt` | `features/app_security/`<br>• `services/security_check_service.dart`<br>• `services/secure_token_manager.dart`<br>• `security/encryption_helper.dart` | `VERIFIED` |
| **34** | **Dark Mode** | N/A | `Color.kt`, `Theme.kt`, `AccountHubScreen.kt` (Emerald-dark palette) | `features/dark_mode/`<br>• `providers/theme_mode_provider.dart`<br>• `core/theme/app_theme.dart`<br>• `core/theme/app_colors.dart` | `VERIFIED` |
| **35** | **Responsive UI** | N/A | `MainActivity.kt` (`MosqueNavigationRail` for >=600dp, `NavigationBottomBar` for mobile) | `features/responsive_ui/`<br>• `widgets/adaptive_scaffold.dart`<br>• `widgets/adaptive_navigation_rail.dart`<br>• `widgets/adaptive_bottom_nav.dart` | `VERIFIED` |
| **36** | **Supabase Backend & Sync** | §32, §34 | `SupabaseConfig.kt`, `SupabaseHttpClient.kt`, `SupabaseSyncManager.kt` (11 entity sync blocks) | `features/cloud_sync/`<br>• `services/supabase_sync_service.dart`<br>• `services/offline_queue_manager.dart`<br>• `providers/sync_status_provider.dart`<br>• `dialogs/cloud_sync_dialog.dart` | `VERIFIED` |
| **37** | **Header & Branding** | §3 | `GlobalHeader.kt` (420L), `ChandaMonthUtils.kt`, `app_logo.png`, `hind_siliguri.ttf` | `features/header/`<br>• `widgets/global_app_header.dart`<br>• `widgets/institution_branding_bar.dart` | `VERIFIED` |

---

## Parity Verification Gate Summary
- Total Required Modules: **37 / 37**
- Existing Kotlin Screen Equivalents Identified: **100%**
- Existing Room Entities & Supabase Tables Mapped: **100%**
- De-monolithization Target: Each module broken down into independent `screens/`, `widgets/`, `dialogs/`, `models/`, `repositories/`, `services/`, and `providers/`. Zero files over 400 lines of code.
