# MOSQUE SEBA (মসজিদ সেবা) — FLUTTER ARCHITECTURE PLAN

**Project:** Mosque Seba (`com.mosqueseba.app`)  
**Architecture Pattern:** Feature-First Clean Architecture + Reactive Streams  
**Primary Language:** Dart 3.x / Flutter 3.x  
**Target Platforms:** Android & iOS  
**Core Invariant:** Zero Monolithic Files (<400 lines per file target). Strict separation of UI, business logic, persistence, and network.  

---

## 1. Technology Stack Selection & Technical Justification

### 1.1 State Management: Flutter Riverpod 2.5+
- **Evaluation:** Evaluated `flutter_riverpod` against `flutter_bloc`.
- **Decision:** **Flutter Riverpod** is selected.
- **Justification:**
  1. *Granular Reactivity:* Riverpod allows granular rebuilds using `ref.watch(provider.select((s) => s.property))`. This is essential for live prayer countdowns, cashbox denomination sums, and unread notification badges without causing whole-screen recomposition.
  2. *Compile-Time Safety & Dependency Injection:* Eliminates runtime `ProviderNotFoundException` and avoids external Service Locators (like `GetIt`).
  3. *Reactive Database Integration:* Effortlessly consumes Drift reactive streams (`StreamProvider`) and Supabase Realtime channels.
  4. *Boilerplate Reduction:* Unlike Bloc, which requires Event, State, and Bloc classes for each of the 37 modules (hundreds of repetitive files), Riverpod Notifiers keep business logic concise and testable in isolation using `ProviderContainer`.

### 1.2 Local Database: Drift 2.20+ (Relational SQLite)
- **Evaluation:** Evaluated `Drift` against `Isar`, `Hive`, and raw `sqflite`.
- **Decision:** **Drift** is selected.
- **Justification:**
  1. *Relational Integrity & ACID Transactions:* Mosque accounting requires double-entry cross-posting, cashbox witness signatures, and unique constraints `(source_type, source_id)`. Drift guarantees transactional consistency.
  2. *1:1 Mirror of Android Room v10:* The legacy Android app used Room with 24 tables, complex indices, and DAOs. Drift is the direct Flutter equivalent, offering compile-time type-safe Dart queries and schema migration hooks (`MigrationStrategy`).
  3. *NoSQL Disqualification:* NoSQL engines like Hive or Isar lack relational foreign-key cascade checks, composite unique constraints, and strict decimal/numeric arithmetic needed for financial audit logs.

### 1.3 Navigation & Routing: GoRouter 14+
- **Decision:** **GoRouter** is selected.
- **Justification:**
  - Provides declarative URL routing (`/auth/login`, `/dashboard`, `/finance/transactions/:id`).
  - `ShellRoute` and `StatefulShellRoute` provide persistent Bottom Navigation and Adaptive NavigationRail without reloading tabs.
  - Centralized `redirect` guard evaluates authentication state, active mosque ID, and role permissions before entering any protected route.

### 1.4 Native PDF & Printing: `pdf` & `printing`
- Replaces Android-only `WebView` + `PrintManager` with cross-platform native rendering on both Android and iOS. Generates vector-sharp A4 receipts, salary slips, and financial statements.

---

## 2. Directory & Package Structure

```
lib/
├── main.dart                          # Application entry point with ProviderScope
├── app.dart                           # MosqueSebaApp MaterialApp configuration
│
├── bootstrap/                         # App initialization lifecycle
│   ├── app_bootstrap.dart             # Async initialization (Supabase, Drift, System UI)
│   └── dependency_injection.dart      # Global provider overrides
│
├── core/                              # Cross-cutting foundational modules
│   ├── config/                        # App constants, environment configs
│   ├── constants/                     # Prayer time constants, asset paths
│   ├── database/                      # Drift Database definition, tables, DAOs
│   │   ├── app_database.dart
│   │   ├── tables/                    # Modular table definitions (1 per file)
│   │   └── daos/                      # Typed DAOs
│   ├── errors/                        # Failure and exception models
│   ├── network/                       # Connectivity listener, HTTP helpers
│   ├── router/                        # GoRouter configuration & route guards
│   ├── security/                      # Encryption, secure token storage, root check
│   ├── services/                      # Audio, notifications, date formatters
│   ├── storage/                       # FlutterSecureStorage & SharedPreferences
│   ├── sync/                          # Offline-first queue & Supabase sync engine
│   ├── theme/                         # Islamic Modern Design System tokens
│   │   ├── app_colors.dart
│   │   ├── app_typography.dart
│   │   ├── app_dimens.dart
│   │   ├── app_gradients.dart
│   │   └── app_theme.dart
│   ├── utils/                         # Bengali numerals, Hijri conversions
│   └── widgets/                       # Core atomic UI primitives
│
├── shared/                            # Reusable domain-agnostic UI widgets
│   ├── widgets/                       # AdaptiveScaffold, AppHeader, AppButton
│   ├── components/                    # StatusBadge, EmptyStateView, ErrorStateView
│   ├── dialogs/                       # ConfirmationDialog, InputModalShell
│   └── extensions/                    # BuildContext, DateTime, String extensions
│
├── features/                          # 37 Independent Feature Modules
│   ├── authentication/
│   ├── role_permissions/
│   ├── dashboard/
│   ├── hadith/
│   ├── islamic_calendar/
│   ├── moon_sighting/
│   ├── ramadan/
│   ├── membership/
│   ├── waqf_assets/
│   ├── subscriptions/
│   ├── finance/
│   ├── cashbox/
│   ├── staff/
│   ├── duty_roster/
│   ├── meal_schedule/
│   ├── prayer_times/
│   ├── notices/
│   ├── notifications/
│   ├── meetings/
│   ├── janaza_alerts/
│   ├── elections/
│   ├── madrasa/
│   ├── madrasa_attendance/
│   ├── hifz_tracking/
│   ├── examinations/
│   ├── madrasa_fees/
│   ├── events/
│   ├── poster_ai/
│   ├── welfare_zakat/
│   ├── ai_assistant/
│   ├── official_signature/
│   ├── printing_pdf/
│   ├── app_security/
│   ├── dark_mode/
│   ├── responsive_ui/
│   ├── cloud_sync/
│   └── header/
│
└── l10n/                              # Localization files (Bengali & English)
```

### 2.1 Internal Feature Architecture
Every feature under `lib/features/<feature_name>/` follows a strict internal structure:
```
finance/
├── screens/         # Screen-level Composables (1 per file, max 300L)
├── widgets/         # Feature-specific UI components
├── dialogs/         # Modal popups & input sheets (1 per file)
├── models/          # Pure Dart data entities & DTOs
├── repositories/    # Data mediation between Drift DB and Supabase RPC
├── services/        # Specialized calculations or helpers
├── providers/       # Riverpod Notifiers and StateProviders
└── utils/           # Formatters, local validators
```

---

## 3. Global Islamic Modern UI/UX Design System

In compliance with `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`, the UI is custom-tailored with an Islamic Modern visual identity.

### 3.1 Color Tokens (`app_colors.dart`)
- **Primary Islamic Emerald:** `0xFF0D5C3A`
- **Deep Sanctuary Green:** `0xFF022C22`
- **Vibrant Prayer Green:** `0xFF047857`
- **Soft Tinted Emerald:** `0xFFE6F4EA`
- **Royal Islamic Gold:** `0xFFD4AF37`
- **Antique Bronze / Minaret Gold:** `0xFFD97706`
- **Warm Gold Shimmer:** `0xFFFEF3C7`
- **Soft Creamy Ivory Gold:** `0xFFFFFBEB`
- **Light Background Canvas:** `0xFFF6F8F6`
- **Night Surface (Fajr/Isha Comfort):** `0xFF0E231D`
- **Night Background:** `0xFF071511`

### 3.2 Typography Tokens (`app_typography.dart`)
Configured with Google Font `Hind Siliguri` (Bengali-first):
- `headlineLarge`: 28sp / ExtraBold
- `headlineMedium`: 22sp / Bold
- `headlineSmall`: 18sp / Bold
- `titleMedium`: 16sp / SemiBold
- `bodyLarge`: 15sp / Regular
- `bodyMedium`: 13sp / Regular
- `labelLarge`: 14sp / SemiBold

### 3.3 Spacing & Radius Tokens
- **Grid Scale:** 8pt base grid with 4pt sub-grid (`MosqueSpace.xs` = 4dp, `sm` = 8dp, `md` = 12dp, `lg` = 16dp, `xl` = 24dp, `xxl` = 32dp).
- **Corner Radii:** Card = 16dp, Dialog = 20dp, Pill = 999dp.

---

## 4. Offline-First Sync Architecture

```
[UI Screen]
    │
    ▼ (ref.watch)
[Drift Local Database] ◄─── (Reactive Stream)
    ▲
    │ (Local writes complete instantly with UUID)
[Offline Sync Queue]
    │
    ▼ (When network active)
[Supabase PostgREST & RPC]
    │
    ▼ (Server confirms & updates sync status)
[Conflict Resolution Engine]
```

1. **Local-First Reads & Writes:** Every user action persists to Drift SQLite immediately, providing instant zero-latency UI response.
2. **Pending Outbox Queue:** Mutations record an `outbox` task with a unique `idempotency_key`.
3. **Background Sync Worker:** Connects to Supabase PostgREST endpoints and RPC functions when internet connectivity is detected.
4. **Conflict Resolution:** Server PostgreSQL timestamps dictate authoritative state; non-conflicting field updates merge seamlessly.
