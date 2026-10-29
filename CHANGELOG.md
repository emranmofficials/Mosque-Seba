# Changelog

All notable changes to the **মসজিদ সেবা (Mosque Seba)** project will be documented in this file.

## [1.0.2] - 2026-10-29
### Added
- **Official 3D Launcher Icons & Brand Logo**:
  - Integrated official 3D emerald-and-gold emblem icon into `res/mipmap-*` (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) and round variants.
  - Configured adaptive icon in `res/mipmap-anydpi-v26/ic_launcher.xml` with `@color/mosque_emerald_dark` background for Android 8.0+ devices.
  - Added official brand logo (`app_logo.png`) in `GlobalHeader.kt` and `LoginScreen.kt`.
- **Role-Based Dynamic Navigation & Interfaces (PRD v2.0)**:
  - **Admin (President, VP, Treasurer)**: Dashboard, Member Management, Financial Approvals & Vouchers, Circulars/Notices, Full Operations (all 11 sub-modules).
  - **Secretariat (General Secretary, Organizing Secretary, Executive)**: Dashboard, Member Directory, Notices & Committee Meetings, Welfare/Zakat Fund, Operations.
  - **Religious Staff (Imam, Muazzin, Khadem)**: Dashboard, Waqt Duty Roster, Food Schedule, Madrasa/Maktab, Staff Salary & Profile.
  - **General Member & Musalli**: Dashboard with Waqt countdown, My Chanda (Receipts & QR status), Transparent Financial Audit, Notice Board, Committee Directory.
- **Dynamic Context Routing**:
  - Auto-reset bottom tab index on role switch.
  - Dashboard Quick Actions and KPIs seamlessly navigate directly to their dedicated sub-module or action dialog.
  - Read-only transparency mode for General Member finance view with full audit report access.

## [1.0.1] - 2026-10-28
### Fixed
- **Critical Launch Crash (`ClassNotFoundException`)**: Added missing `alias(libs.plugins.kotlin.android)` to `build.gradle.kts` and `app/build.gradle.kts`, enabling full Kotlin compilation for all 25 source files and Room DAOs.
- **Bytecode Compatibility**: Aligned JVM bytecode target to `Java 17` (`JavaVersion.VERSION_17` and `jvmTarget = "17"`) for seamless ART compatibility across Android versions.
- **Workflow Automation**: Made release asset and release name dynamic based on tag name.

## [1.0.0] - 2026-10-28
### Added
- **Official Branding**: Set app name to "মসজিদ সেবা (Mosque Seba)" and package name to `com.mosqueseba.app`.
- **Slogan**: Added official slogan: *"মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা"*.
- **Branding Assets**: Integrated official vector logo, 512x512 app icon, and Google Play Store feature banner into `design/branding/`.
- **Madrasa & Maktab Module** (`MadrasaScreen.kt`): Student directory, Hifz & Quran tracker with star ratings, Fee collection with auto-posting to Finance, and Guardian Portal.
- **Staff Operations & Payroll**: Staff directory, salary disbursement with auto-posting to Finance, 5-waqt Duty Roster, and Khadem/Imam Food Schedule.
- **Welfare & Zakat Fund**: Case assistance tracker for medical, education, widow/orphan, and food relief grants.
- **Events & Programs**: Waz Mahfil, Milad, Shab-e-Qadr, and Iftar program manager with speaker, venue, and budget tracking.
- **Committee Meetings**: Scheduled meetings directory, agendas, and resolutions viewer.
- **AI Notice Drafter**: 1-click Gemini AI powered notice drafting within Publish Notice dialog.
- **Financial Reports**: Monthly income/expense summary and shareable report dialog.
- **Authentication**: Committee ID login, 2-step President/Mosque onboarding, and Header logout.
- **Database Architecture**: Upgraded Room Database to version 3 with 17 entities and comprehensive seed data.
