You are an expert senior Flutter developer. Build "Mosque Seba" (মসজিদ সেবা) — a full-featured mosque management mobile app for Android and iOS. This is a 1:1 FEATURE-PARITY REBUILD of an existing Kotlin/Jetpack Compose + Supabase app. Every feature, screen, and workflow listed below must be reproduced exactly — do NOT omit, simplify, or add features beyond what is specified here.

=== 1. TECH STACK ===
- Framework: Flutter (Dart), Material 3 design language
- Platforms: Android + iOS
- Backend: Supabase (PostgreSQL, multi-tenant Row Level Security isolated by mosque_id, Edge Functions / RPC) — reuse the EXISTING backend and schema as-is (see supabase_reference/), do not redesign the database
- AI integration: Google Gemini API (notice drafting, Islamic poster generation)
- Offline-first: pick an appropriate local cache/database (e.g. Drift, Isar, or Hive) that syncs bidirectionally with Supabase and acts as source of truth when offline
- State management: propose Riverpod or Bloc with reasoning before starting, then use it consistently across the whole app

=== 2. MANDATORY ARCHITECTURE RULE (highest priority constraint) ===
The original app became unmaintainable because single files mixed dozens of unrelated screens/dialogs (Dialogs.kt had 4,098 lines; DashboardScreen.kt 3,578 lines; FinanceScreen.kt 2,444 lines, etc). This must NOT happen again.

Use a strict feature-first folder structure (already scaffolded in lib/ in this repo):
lib/core/ (theme, constants, router, utils, shared widgets, navigation/role_shells)
lib/features/<module_name>/screens/  — one screen = one file
lib/features/<module_name>/widgets/  — one widget = one file
lib/features/<module_name>/dialogs/  — one dialog = one file
lib/features/<module_name>/models/
lib/features/<module_name>/services/  — Supabase/RPC calls for that module
lib/features/<module_name>/providers/ — state management for that module
lib/shared/ (cross-feature buttons, cards, inputs)

Every one of the 37 modules below has its own folder under lib/features/ (already created). No file may contain more than one screen or more than one unrelated dialog.

=== 3. ROLE & PERMISSION STRUCTURE — SEPARATE INTERFACE PER ROLE (16 committee positions, 3 tiers) ===
This is a hard requirement, not just permission-gating: each role/duty must get its OWN interface — a distinct navigation shell (its own home dashboard, its own menu/nav items, its own visible feature set) built for that role's actual duties. Do NOT implement this as one shared screen with buttons conditionally hidden/shown; build separate role-based navigation graphs/shells under lib/core/navigation/role_shells/ (folders already scaffolded: president, vice_president, general_secretary, assistant_secretary, treasurer, assistant_treasurer, organizational_secretary, publicity_secretary, office_secretary, social_welfare_secretary, executive_member, khatib_imam, advisory_board, staff_imam, staff_muazzin, staff_khadem, madrasa_teacher, general_member), each composed from only the screens/widgets that role needs.

Tier 1 — Executive Committee:
- President: chairs all meetings, final approval on major decisions, resolves internal disputes
- Vice President: covers for President, oversees sub-committees
- General Secretary: daily admin, calls meetings, writes agenda/minutes/resolutions, supervises Imam/Muazzin/Khadem
- Assistant Secretary: supports General Secretary, acts in their absence
- Treasurer: full fund custody, attends cash-box counting, keeps daily/monthly ledger, presents financial reports
- Assistant Treasurer: supports Treasurer with ledger & banking

Tier 2 — Editorial posts:
- Organizational Secretary: unity among musallis, community liaison
- Publicity Secretary: promotes religious events (Eid Milad, Shab-e-Barat, Shab-e-Qadr, Waz Mahfil), manages notice boards
- Office Secretary: document/contract/correspondence custodian
- Social Welfare & Religious Education Secretary: oversees maktab/hifz madrasa, zakat/donation distribution, medical camps, disaster relief

Tier 3 — General members & advisors:
- Executive Members (3–9): implement committee decisions, maintain discipline at Jumu'ah/Eid prayers, witness cash-box counting
- Ex-officio Member (Khatib/Imam): sharia guidance, flags un-Islamic decisions
- Advisory Board (3–5): senior/respected community figures, consulted on major crises or construction/renovation

Plus operational staff roles (Imam, Muazzin, Khadem, Madrasa teacher) and the general member/musalli, each with their own fully separate, restricted dashboard/interface.

=== 4. FULL FEATURE LIST — 37 MODULES, 8 GROUPS (implement ALL, unchanged from original) ===
Folder names already scaffolded under lib/features/ are shown in parentheses.

A. Core/Auth
1. Authentication & Account (01_auth_account) — President registration, mosque onboarding (mosque code generation), login (separate flows for committee vs general musalli), encrypted session manager, splash screen
2. Role & Permission (02_role_permission) — role catalog above, dynamic role-based navigation shells (see section 3)

B. Dashboard & Islamic Hub
3. Smart Dashboard (03_smart_dashboard) — hero card (mosque name/logo), smart board, prayer-time card (5 waqt + Jumu'ah azan/jamaat times), live countdown to next waqt, Hijri+Bangla calendar banner, KPI cards, quick actions, financial summary
4. Sahih Hadith digital board (04_hadith_board) — daily auto-rotating Bukhari/Muslim hadith (100% offline dataset)
5. Islamic Calendar & Event Hub (05_islamic_calendar_hub) — 13 annual occasions (Ashura, Seerat-un-Nabi, Shab-e-Meraj, Shab-e-Barat, Ramadan, Laylatul Qadr, Eid-ul-Fitr, Arafah, Eid-ul-Adha, etc) with real-time tracking/countdown
6. Moon-sighting offset adjustment (06_moon_sighting_adjustment) — 1-click -1 day / normal / +1 day persistent setting
7. Ramadan Special Hub (07_ramadan_hub) — Sehri/Iftar live countdown, Tarawih & Itikaf tracking, Khatme Quran progress log

C. Membership & Finance
8. Member Management (08_member_management) — list/search/filter, add/edit, profile (blood group, mobile, NID), subscription ledger history
9. Waqf Property Directory (09_waqf_property_directory) — digital register of movable/immovable mosque assets
10. Subscription/Donation collection (10_subscription_donation) — payment collection, monthly cycle tracking, QR-verified digital receipts (with social share), auto double-entry finance posting
11. Finance Management (11_finance_management) — income/expense ledger, two-tier approval, cash/bank balance, cent-accurate accounting, audit trail log, PDF export/share, President's pending-voucher approval card
12. Jumu'ah cashbox 3-witness counter (12_jumma_cashbox_counter) — real-time denomination counting (1000 down to 5 taka notes), 3 independent witness signature verification, auto-voucher posting

D. Staff & Operations
13. Staff & Payroll (13_staff_payroll) — staff profiles, attendance/leave, monthly salary & Eid bonus distribution, salary-visibility control, auto finance-expense cross-posting
14. Duty Roster (14_duty_roster) — 5 waqt + Jumu'ah Imam/Muazzin duty assignment, daily/weekly/monthly calendar
15. Meal Schedule (15_meal_schedule) — family rota for Imam/Khadem lunch-dinner, fair-distribution tracker
16. Prayer Times (16_prayer_times) — 5 waqt + Jumu'ah auto-calculation, next-waqt countdown

E. Communication & Admin
17. Notices & Communication (17_notices_communication) — rich-text notices, audience targeting, pinned notices, AI draft assist (Gemini)
18. Notification Center (18_notification_center) — role-based notifications, unread badge count, delivery tracking
19. Meeting Management (19_meeting_management) — scheduling, agenda, invitations, minutes, permanent resolution archive
20. Emergency Janaza Alert (20_emergency_janaza_alert) — direct SMS & WhatsApp broadcast with time/place/graveyard details
21. Election & Nomination Gazette (21_election_nomination_gazette) — election schedule, candidate nomination, symbol allocation, ballot voting, real-time results, gazette publication

F. Madrasa/Maktab
22. Madrasa/Maktab Core (22_madrasa_core) — student list/admission, daily attendance, guardian contact info
23. Madrasa Attendance (23_madrasa_attendance) — daily attendance record & history
24. Quran/Hifz Tracking (24_quran_hifz_tracking) — session recording, para/page star-rating, milestones, teacher evaluation
25. Exams & Results (25_exams_results) — progress review, 360° performance evaluation, student report cards
26. Madrasa Fees (26_madrasa_fees) — student fee collection, digital receipt, auto cross-posting to finance income

G. Events & Welfare
27. Events & Programs (27_events_programs) — Waz Mahfil, Shab-e-Qadr, Iftar event planning, budget & volunteer tracking
28. Islamic Dawat Poster AI (28_islamic_poster_ai) — Gemini-generated poster prompts/visual layout for Waz Mahfil/discussion sessions, image attachment
29. Welfare & Zakat Fund (29_welfare_zakat_fund) — medical grants, widow/orphan allowance, education aid, disaster relief, application approval workflow

H. Documents, Security & Platform
30. AI Assistant (30_ai_assistant) — Gemini-powered smart notice/khutbah-point draft generator, human-in-the-loop review
31. Official Seal & Signature (31_official_seal_signature) — touchscreen signature canvas pad, official stamp image upload, title storage, attached to all receipts/vouchers
32. Native 1-click Print & PDF (32_print_pdf) — professional receipts, expense vouchers, salary slips, notices — direct print/PDF with seal
33. App security / API abuse-prevention layer (33_app_security) — attestation/CAPTCHA protection, debug fallback
34. Dark Mode (34_dark_mode) — full Islamic emerald-dark palette, high contrast, runtime toggle
35. Tablet & Responsive UI (35_responsive_ui) — adaptive navigation rail for 600dp+, bottom bar on mobile
36. Supabase cloud backend & sync (36_supabase_sync) — multi-tenant PostgreSQL, 28 tables, RLS isolation by mosque_id, 10 RPC functions, bidirectional client sync, 100% offline-first
37. Header cleanliness (37_header_cleanliness) — official logo, user's name & title in header, institution name on hero card

=== 5. NON-NEGOTIABLE CONSTRAINTS ===
- ALL 37 features above must be implemented — none dropped, none added.
- Flows, fields, and business logic (two-tier finance approval, 3-witness cashbox counting, QR receipt verification, etc.) must match the original exactly.
- Supabase backend/schema stays as-is; only the client is rewritten in Flutter.
- NO monolithic files — strictly one screen/dialog/widget per file, organized by feature folder as in section 2.
- Each role's home screen, navigation, and menu must be a DISTINCT interface tailored to that role's duties — separate nav shells per role/tier (lib/core/navigation/role_shells/), not one shared screen with conditionally hidden buttons.
- Maintain ONE shared design system (colors, typography, spacing) in core/theme so UI stays visually consistent across all role-shells and all 37 modules — visual inconsistency was the main flaw in the original app.

=== 6. HOW TO PROCEED ===
1. Propose the state-management choice and local-database choice (with reasoning) before writing code.
2. Confirm/fill in the already-scaffolded folder structure (lib/core, lib/features/<37 modules>, lib/core/navigation/role_shells/<18 roles>, lib/shared).
3. Build module by module in this order: Auth & Role → Role Shells/Navigation → Dashboard → Member Management → Finance → remaining modules.
4. For each module, implement screens, dialogs, services, and providers as separate files, wired to Supabase.
5. After each module, confirm it compiles/runs and ask before moving to the next, unless told to proceed continuously.
