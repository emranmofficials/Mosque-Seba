# MOSQUE SEBA (মসজিদ সেবা) — PRE-DEVELOPMENT AUDIT REPORT
**Package:** `com.mosqueseba.app`  
**Current Legacy Version:** 1.2.5 (Android Kotlin / Jetpack Compose)  
**Target Version:** 1.2.5+1 (Flutter / Dart iOS & Android)  
**Date of Audit:** November 2026  
**Auditor:** Senior Flutter Architect & Autonomous Rebuild Agent  
**Audit Scope:** 100% Comprehensive Codebase, Database, Security, and UI/UX Inspection  

---

## 1. Executive Summary

A comprehensive pre-development audit was conducted on the legacy Mosque Seba codebase (`Mosque-Seba-main`), requirements specifications (`PRD_v2.0_FINAL.md`, `UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`, `FEATURE_MATRIX.md`), database architecture (`full_schema_setup.sql`, `DATABASE_ARCHITECTURE.md`), and security configurations (`SECURITY_MODEL.md`, `PERMISSION_MATRIX.md`).

The audit concludes that while the core business logic, Islamic domain rules, and Supabase cloud infrastructure are exceptionally well-conceived and mature, the existing Android Kotlin/Jetpack Compose client suffers from severe architectural decay caused by massive monolithic source files (several exceeding 2,500 to 4,000 lines). This has created an unmaintainable state with high defect risks, UI rendering freezes, and synchronization bottlenecks.

A clean, 1:1 feature-parity Flutter rebuild is completely feasible and will solve all legacy limitations while preserving 100% of the Supabase backend schema (all 28 core tables, 17 extended tables, 10 RPC procedures, and RLS policies) with zero breaking changes.

---

## 2. Legacy Codebase Metrics & Anti-Pattern Analysis

### 2.1 File Size & Monolithic Inventory
The inspection of `app/src/main/java/com/mosqueseba/app/` identified extreme violations of single-responsibility and modular design:

| Component / File | Legacy Lines of Code | Identified Issues & Risks |
|---|---|---|
| `Dialogs.kt` | **4,098 lines** | Crammed 7+ completely unrelated modal dialogs (Add Member, Receive Chanda, Add Transaction, Publish Notice, Schedule Meeting, Resolution) with mixed business logic, state mutations, and canvas drawing. |
| `DashboardScreen.kt` | **3,578 lines** | Massive Composable containing prayer timers, smart board ticker, Hadith rotation, quick action grids, financial summaries, and committee cards in one file. |
| `MoreScreen.kt` | **2,751 lines** | Unbounded aggregation of navigation targets, operational launchers, and settings dialogs without sub-routing. |
| `MosqueViewModel.kt` | **2,735 lines** | Giant god-object ViewModel holding dozens of `StateFlow` instances, Supabase sync calls, Gemini AI API calls, date conversions, and printer triggers. |
| `FinanceScreen.kt` | **2,444 lines** | Income/Expense tabs, two-tier approval card, bank/cash fund balances, audit history, PDF statement generation, and filtering in one file. |
| `MadrasaScreen.kt` | **2,371 lines** | Student registration, 360-degree exam reviews, daily attendance, star-based Hifz evaluation, and fee collection merged into a single Composable. |
| `CommitteeOperationsScreens.kt` | **2,070 lines** | Combined Cashbox, Office Docs, Sharia Advisory, Smart Board, and Committee Charter. |
| `ElectionScreen.kt` | **1,854 lines** | 4 sub-tabs (Schedule, Candidates, Voters, Results) in one screen file. |
| `RegistrationScreen.kt` | **1,790 lines** | Multi-step onboarding, mosque creation, verification, and credentials management. |
| `MosqueProfileScreen.kt` | **1,692 lines** | Profile editing, seal upload, bank account configuration, and waqf data. |
| `MembersScreen.kt` | **1,592 lines** | Member list, filtering, CSV export, and deletion logic. |
| `MemberProfileScreen.kt` | **1,473 lines** | Profile display, Chanda history, QR verification, and signature canvas. |
| `NoticesScreen.kt` | **1,461 lines** | Notice feed, audience filtering, pinning, and Gemini drafting. |
| `RamadanCard.kt` | **1,453 lines** | Sehri/Iftar countdowns, Taraweeh tracker, Itikaf log, and Khatam progress. |
| `MoreDialogs.kt` | **1,407 lines** | Duty roster, food schedules, welfare grants, and event forms. |
| `AccountHubScreen.kt` | **1,374 lines** | Dark mode toggle, password change, offline sync status, and cache wiping. |
| `NotificationsScreen.kt` | **1,342 lines** | Notification list, delivery state, read/unread management. |

### 2.2 Root Architectural Defects
1. **Lack of Domain Layer:** Screens directly call `MosqueViewModel` methods, which directly call Room DAOs and `SupabaseHttpClient`.
2. **Duplicated Formatting Logic:** Bengali digit conversion (`toBengaliDigits`), ISO date formatting, and Hijri calendar calculations are duplicated in at least 14 separate files.
3. **Fragile Sync Mapping:** `SupabaseSyncManager.kt` manually mapped JSON objects to Room entities using raw strings and index lookups without type-safe DTOs, leading to case-sensitivity bugs (e.g., uppercase `INCOME` vs cloud lowercase `income`).
4. **Heavy Re-renders:** A single state change in `MosqueViewModel` triggered recomposition cascades across thousands of UI widgets due to coarse-grained `collectAsState()`.

---

## 3. Reference Documentation Audit

All key specifications were cross-checked:

1. **`requirements/PRD_v2.0_FINAL.md`:** Authoritative product blueprint defining 38 sections, 7 UI profiles, multi-tier finance approval, and strict data flow invariants.
2. **`requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`:** Comprehensive design specification. Strictly mandates custom Islamic Modern aesthetic (Emerald & Gold tokens, Hind Siliguri typography, 8pt/4pt grid) and forbids default Material 3 styling from dictating application identity.
3. **`requirements/FEATURE_MATRIX.md`:** Lists 33 major module areas mapping to PRD sections. Reconciled with the user's master list of 37 modules (explained in `FEATURE_PARITY_MATRIX.md`).
4. **`docs/DATABASE_ARCHITECTURE.md`:** Enforces PostgreSQL/Supabase as SSoT, multi-tenant isolation via `mosque_id`, numeric decimal currency, double-entry linkage, and immutable financial ledgers.
5. **`docs/PERMISSION_MATRIX.md` & `SECURITY_MODEL.md`:** Formulates the authorization decision matrix: `Authenticated AND Tenant Match AND Role Permission AND Override AND Relationship -> Allow`.

---

## 4. Reusability & Migration Assessment

### 4.1 What Exists & Can Be Reused
- **Supabase Production Schema:** All 28 core tables + 17 extended tables are live on PostgreSQL 17 (`donglyxtrfyzwgtrskhz.supabase.co`). Fully preserved without schema mutations.
- **Supabase RPC Procedures:** 10 mission-critical stored functions (`approve_finance_transaction`, `submit_finance_transaction`, `request_chanda_payment`, `receive_chanda_payment`, `reverse_finance_transaction`, `finalize_payroll_payment`, `log_audit_event`, `generate_business_code`, `publish_notice`, `complete_ai_request`). Reused 100%.
- **Row Level Security (RLS) Policies:** Tenant isolation and role-based policies in `supabase/policies/` are intact and will be enforced directly via the Flutter Supabase SDK.
- **Design Assets & Branding:** Official app logo (`app_logo.png`, `official_logo.png`), Islamic geometric motifs, and `hind_siliguri.ttf` font asset files are preserved.
- **Business Algorithms:** Exact formulas for Jummah cashbox denominations, Bengali date conversion, Hijri offset calculation, and prayer time algorithms.

### 4.2 What Needs Complete Rebuilding
- **Client Presentation Layer:** Complete rewrite from Jetpack Compose to modular Flutter Widgets following the Islamic Modern Design System.
- **State Management & Dependency Injection:** Replacement of monolithic `MosqueViewModel` with fine-grained Riverpod Notifiers and Providers.
- **Local Persistence Layer:** Migration from Android Room DB (v10) to Drift relational SQLite database with typed tables, DAOs, and reactive streams.
- **Client Sync Engine:** Migration from manual OkHttp/JSON string parsing to `supabase_flutter` with structured offline outbox queues and idempotency keys.
- **Print & PDF Generation:** Replacement of Android `WebView`/`PrintManager` with cross-platform `pdf` and `printing` libraries supporting identical A4 print templates with digital seals and signatures.
- **Gemini AI Client:** Replacement of Android Firebase Vertex AI with `google_generative_ai` Dart client with backend proxying.

---

## 5. Risk Assessment & Mitigation Strategies

| Risk Category | Identified Risk | Impact | Mitigation Strategy |
|---|---|---|---|
| **Data Integrity** | Case-mismatch in transaction types (`INCOME` vs `income`) causing local queries to miss synced data. | High | Enforce strict Dart `@JsonEnum` / Drift Converters that normalize to lowercase on cloud I/O while presenting typed enums to the UI. |
| **Financial Concurrency** | Double-submitting transactions or cashbox counts in offline mode. | Critical | Enforce client-generated UUID `idempotency_key` and Supabase SQL unique constraint `(mosque_id, source_type, source_id)`. Block offline execution of approval actions. |
| **Multi-Tenancy Leakage** | Stale local SQLite records from one mosque leaking when logging into another mosque on the same device. | Critical | Implement an explicit `clearAllTenantData()` trigger on logout/switch that purges tenant tables in Drift before pulling new tenant records. |
| **AI Credential Exposure** | Exposing Gemini API keys in the client-side binary. | High | Route Gemini AI requests through Supabase Edge Functions (`complete_ai_request` RPC) with server-side API keys, falling back to secure storage if user provides their own key. |
| **Screen Fragmentation** | Unresponsive layout on tablets or landscape orientation. | Medium | Implement an adaptive layout engine (`AdaptiveScaffold`) providing `NavigationBar` for mobile (<600dp) and `NavigationRail` for tablets (>=600dp). |

---

## 6. Audit Conclusion & Go/No-Go Recommendation

**Recommendation: GO (Proceed to Phase 1 Architecture & Foundation upon user approval).**

The project has exceptionally clear requirements, an existing production-verified Supabase database, and well-documented business rules. Rebuilding the application in Flutter using a Feature-First modular architecture, Riverpod, Drift, and GoRouter will eliminate all 46,000+ lines of monolithic legacy code and deliver a fast, maintainable, production-ready solution for both Android and iOS.
