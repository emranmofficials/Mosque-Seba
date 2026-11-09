# Changelog

All notable changes to the **মসজিদ সেবা (Mosque Seba)** project will be documented in this file.

## [1.0.6] - 2026-11-09
### Added & Production-Ready (Splash, Auth & Multi-Step Registration)
- **Islamic Modern Animated Splash Screen (`SplashScreen.kt`)**:
  - ডিপ ইসলামিক এমারেল্ড গ্র্যাডিয়েন্ট ব্যাকগ্রাউন্ড, ৩ডি এম্বলেম লোগোর স্মুথ স্কেল ও আলফা এন্ট্রি অ্যানিমেশন।
  - গোল্ডেন শাইন রিং, বিসমিল্লাহ ক্যালিগ্রাফি এবং পবিত্র কুরআনের আয়াত উদ্ধৃতি (*"নিশ্চয়ই মসজিদসমূহ আল্লাহরই জন্য" - সূরা আল-জিন: ১৮*)।
  - অফিসিয়াল স্লোগান ও সংস্করণ ব্যাজ সহ স্বয়ংক্রিয় ২.২ সেকেন্ড টাইমার শেষে সেশন যাচাইপূর্বক সরাসরি ড্যাশবোর্ড বা লগইন স্ক্রিনে ট্রানজিশন।
- **প্রডাকশন-গ্রেড লগইন স্ক্রিন (`LoginScreen.kt`)**:
  - ডুয়েল মোড ট্যাব: "কমিটি ও স্টাফ প্রবেশ" (পাসওয়ার্ড প্রটেক্টেড) এবং "মুসুল্লি প্রবেশ" (পাসওয়ার্ডহীন উন্মুক্ত সেবা পোর্টাল)।
  - কমিটি আইডি, ১১ ডিজিটের বাংলাদেশি মোবাইল ভ্যালিডেশন, শো/হাইড পাসওয়ার্ড টগল এবং "আমাকে মনে রাখুন" (Remember Me) সেশন সেভ।
  - এক ক্লিকে টেস্টিংয়ের জন্য কুইক ডেমো রোল চিপস (👑 সভাপতি, 📝 সাধারণ সম্পাদক, 💰 কোষাধ্যক্ষ, 🕌 পেশ ইমাম, 👥 সাধারণ মুসুল্লি)।
  - বিস্তারিত লগইন সহায়তা ও ডেমো ক্রেডেনশিয়াল ডায়ালগ।
- **ডেডিকেটেড ৩-ধাপ বিশিষ্ট রেজিস্ট্রেশন উইজার্ড (`RegistrationScreen.kt`)**:
  - **ধাপ ১ (সভাপতির তথ্য):** পুরো নাম (বাংলা/ইংরেজি), মোবাইল, ইমেইল, NID ও পাসওয়ার্ড নিশ্চিতকরণ।
  - **ধাপ ২ (মসজিদ বিবরণ):** মসজিদের নাম, পূর্ণ ঠিকানা, প্রতিষ্ঠা সাল ও কমিটির মেয়াদকাল।
  - **ধাপ ৩ (যাচাই ও সম্মতি):** সারসংক্ষেপ রিভিউ এবং শর্তাবলীতে সম্মতি দিয়ে এক ক্লিকে ডাটাবেজে সংরক্ষণ।
  - নতুন মসজিদ ও সভাপতি এন্ট্রি সফল হলে তাৎক্ষণিকভাবে সভাপতি হিসেবে লগইন করে ড্যাশবোর্ডে প্রবেশের সুবিধা।
- **সেশন পারসিসটেন্স ও সিকিউর সেশন ম্যানেজার (`SessionManager.kt`)**:
  - `SharedPreferences` ব্যবহার করে লগইন সেশন, সক্রিয় রোল ও ইউজারের তথ্য ডিভাইস মেমরিতে নিরাপদে সংরক্ষণ।
  - গ্লোবাল হেডার মেন্যু থেকে "🚪 লগআউট করুন" ট্যাপ করলে সেশন ক্লিয়ার হয়ে মসৃণভাবে লগইন স্ক্রিনে প্রত্যাবর্তন।
- **সিস্টেম ব্যাক হ্যান্ডলার ও ফ্রেম-পারফেক্ট অ্যানিমেশন**:
  - Jetpack Compose `BackHandler` ইন্টিগ্রেশন এবং `AnimatedContent` ট্রানজিশন (Splash -> Login <-> Registration -> Main)।

## [1.0.5] - 2026-11-09
### Added & Refined (Role-Responsibility Isolation & Security)
- **Strict Role-Responsibility Feature Isolation (কমিটি ও স্টাফদের দায়িত্ব অনুযায়ী ফিচার বিভাজন)**:
  - **কোষাধ্যক্ষ (Treasurer)**:
    - সম্পূর্ণ অর্থ ব্যবস্থাপনা কেন্দ্রীভূত: আয় এন্ট্রি, ব্যয় ভাউচার তৈরি, নিয়মিত মাসিক চাঁদা আদায়/রসিদ প্রদান, স্টাফ বেতন ও ভাতা প্রদান, মাদরাসা ফি কালেকশন, এবং আর্থিক অডিট নিরীক্ষা রিপোর্ট।
    - কোষাধ্যক্ষের ইন্টারফেস থেকে অপপ্রাসঙ্গিক ফিচার (যেমন: মিটিং ডাকা, হিফজ মূল্যায়ন, খাবার শিডিউল, মেম্বার অ্যাড ইত্যাদি) সম্পূর্ণ অপসারণ।
  - **সাধারণ সম্পাদক ও সাংগঠনিক সম্পাদক (Secretariat)**:
    - সার্বিক প্রশাসনিক সচিবালয় ও সমন্বয়: নতুন সদস্য নিবন্ধন, জরুরি ও মাসিক কার্যনির্বাহী সভা আহ্বান ও কার্যবিবরণী লিপিবদ্ধকরণ, ডিজিটাল নোটিশ জারি ও AI ড্রাফটিং, সমাজকল্যাণ ও যাকাত আবেদন গ্রহণ এবং দ্বীনি মাহফিল/ইভেন্ট ব্যবস্থাপনা।
    - সচিবালয়ের ইন্টারফেস থেকে ক্যাশ হ্যান্ডলিং ও ভাউচার পেমেন্ট অপশন বাদ দিয়ে প্রশাসনিক শৃঙ্খলায় কেন্দ্রীভূত।
  - **সভাপতি ও সহ-সভাপতি (President & Vice President)**:
    - নির্বাহী তত্ত্বাবধান ও চূড়ান্ত অনুমোদন: কেন্দ্রীয় ড্যাশবোর্ড কেপিআই, পেন্ডিং ব্যয় ভাউচার অনুমোদন/বাতিল, যাকাত ও সমাজকল্যাণ অনুদান মঞ্জুরি, সভা আহ্বান ও রেজুলেশন অনুমোদন, এবং কেন্দ্রীয় অডিট নিরীক্ষা।
  - **ইমাম, মুয়াজ্জিন ও খাদেম (Religious Staff)**:
    - ধর্মীয় খেদমত, শিক্ষার্থী তালিম ও ব্যক্তিগত প্রাপ্তি: ৫ ওয়াক্ত নামাজের সঠিক সূচি, ব্যক্তিগত ওয়াক্ত ডিউটি রোস্টার, খাবার প্রদানকারী পরিবার তালিকা, মাদরাসা শিক্ষার্থীদের হিফজ ও মক্তব মূল্যায়ন (সবক এন্ট্রি), এবং ব্যক্তিগত বেতন ও ভাতা স্লিপ।
    - তহবিলের ক্যাশ ব্যালেন্স ও ডোনার ডাটাবেস থেকে নিরাপদ দূরত্ব বজায় রাখা হয়েছে।
  - **সাধারণ সদস্য ও মুসুল্লি (General Member / Musalli)**:
    - মুসুল্লি পোর্টাল ও স্বচ্ছ তহবিল: ওয়াক্ত সময় ও জামাত ট্র্যাকিং, ব্যক্তিগত চাঁদা প্রদানের ইতিহাস ও ডিজিটাল ভেরিফায়েড QR রসিদ, মসজিদের উন্মুক্ত মোট আয়-ব্যয় সারাংশ ও সার্বিক স্থিতি, নোটিশ বোর্ড, কমিটি ডিরেক্টরি এবং আপৎকালীন সমাজকল্যাণ আবেদন।
- **Architecture & Navigation Improvements**:
  - `overlaySubModule` আর্কিটেকচার: যেকোনো ডিপ সাব-মডিউল (নামাজের সূচি, অডিট লগ, ইভেন্ট মাহফিল, মাদরাসা ফি) বটম বার স্টেট অক্ষুণ্ণ রেখে ফুল-স্ক্রিন ওভারলে এবং ব্যাক বাটন দিয়ে সহজে ব্যবহারের সুবিধা।
  - কঠোর এক্সেস কন্ট্রোল গার্ডস: মেম্বার অ্যাড, আয়/ব্যয় এন্ট্রি, চাঁদা গ্রহণ, সভা আহ্বান, নোটিশ প্রকাশের প্রতিটি ডায়ালগ ও বাটন রোল-পারমিশন দ্বারা সুরক্ষিত।

## [1.0.4] - 2026-11-09
### Improved & Optimized (UI & Performance)
- **Islamic Modern Visual Refinement**:
  - Implemented multi-stop Islamic emerald gradients (`MosqueEmeraldGradient`, `MosqueEmeraldDarkGradient`) and gold metallic accents.
  - Added subtle card borders (`MosqueCardBorderSubtle`) and elevated depth (`elevation = 2.dp` / `4.dp`, `RoundedCornerShape(16.dp)` / `18.dp` / `20.dp`).
  - Polished Global Header with rich vertical gradient, gold trim accent divider, and refined logo container.
  - Upgraded HeroCard, Net Balance Card, and Prayer Waqt card with glowing next-waqt badges and gold callout banners.
- **Typography & Bengali Script Legibility**:
  - Optimized typography line-heights (24sp–38sp) to prevent clipping of complex Bengali diacritics and conjuncts.
- **Buttery-Smooth Tab Transitions & Elevation**:
  - Added `AnimatedContent` crossfade/slide transitions on bottom bar destination switches for 60/120fps smooth navigation.
  - Migrated KPI cards and Member cards to native `Card(onClick = ...)` for bounded ripple effects and unclipped drop shadows.
  - Added subtle top border and elevation to `NavigationBottomBar`.

## [1.0.3] - 2026-11-09
### Added
- **Dedicated Role-Based Interface Profiles (PRD v2.0)**:
  - **Treasurer (কোষাধ্যক্ষ)**: Distinct 5-tab financial command navigation (Dashboard, Income/Expense, Chanda Register, Staff Payroll & Madrasa Fees, Audit Reports). Custom cash/bank KPI and quick actions.
  - **General Secretary (সাধারণ সম্পাদক)**: Dedicated secretariat navigation (Dashboard, Member Council, Committee Meetings & Resolutions, Notice Board, Welfare Fund).
  - **President & Executive**: Administrative command center, voucher approval workflows, member directory, circulars and full mosque operations.
  - **Imam & Religious Staff**: Daily waqt duty roster, meal provider schedules, maktab/hifz progress tracking and salary status.
  - **General Member**: Musalli portal for personal chanda payment/history, verified digital QR receipts, transparent fund balance and notices.
- **Repository & DAO Enhancements**:
  - Implemented `getTransactionById` in `TransactionDao` and completed `approveTransaction` in `MosqueRepository`.
  - Generalized audit log actor attribution.

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
