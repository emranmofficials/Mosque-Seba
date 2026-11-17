# Architecture

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 32–35). Any change here must trace back to an approved PRD requirement.

## High-level shape

```
Android App (Kotlin, Compose)  <-->  Supabase (PostgreSQL, RLS, RPC/Edge Functions, Storage, Auth, FCM)
```

PostgreSQL/Supabase is the **single source of truth (SSoT)**. The Android client is a presentation/interaction layer and is **never** a security boundary — it must never be trusted to enforce authorization, tenant scoping, or business invariants on its own.

## Android layers (PRD §33)

```
UI (Compose) → ViewModel → UseCase → Repository → Remote/Local Data Source
```

- **UI (Jetpack Compose):** renders state, dispatches user intents. Contains no authoritative authorization logic — it may hide/show affordances for UX, but the backend is what actually allows or denies an action.
- **ViewModel:** holds UI state via `StateFlow`/`SharedFlow`, exposes intents/events.
- **UseCase:** business/orchestration logic per feature.
- **Repository:** single point of access per domain, mediates between remote (Supabase) and local (Room) sources.
- **Remote Data Source:** Supabase SDK / RPC calls, OkHttp.
- **Local Data Source:** Room (cache) + DataStore (preferences/session), Android Keystore (secrets).

### Stack

Kotlin, Jetpack Compose, MVVM, Clean Architecture, Navigation Compose, Hilt (DI), Coroutines, StateFlow/SharedFlow, Coil (images), Room (local cache), DataStore, WorkManager (background sync), Android Keystore, Kotlin Serialization, OkHttp, Supabase SDK/RPC, Firebase Cloud Messaging (FCM).

> Local cache (Room) is **not** SSoT — it is a read cache only. Every authoritative decision is re-validated server-side.

## API contract (PRD §32)

**Request envelope**
```json
{"request_id": "req_...", "idempotency_key": "idem_...", "data": {}}
```

**Success envelope**
```json
{"success": true, "data": {}, "error": null, "meta": {"request_id": "...", "api_version": "v1"}}
```

**Error envelope**
```json
{"success": false, "data": null, "error": {"code": "PERMISSION_DENIED", "message": "আপনার এই কাজটি করার অনুমতি নেই।", "details": null}, "meta": {"request_id": "...", "api_version": "v1"}}
```

### Error code families

`AUTH_REQUIRED`, `AUTH_INVALID`, `SESSION_EXPIRED`, `ACCOUNT_INACTIVE`, `ACCOUNT_SUSPENDED`, `PASSWORD_RESET_REQUIRED`, `PERMISSION_DENIED`, `ROLE_NOT_ALLOWED`, `RELATIONSHIP_DENIED`, `TENANT_ACCESS_DENIED`, `SUBSCRIPTION_REQUIRED`, `FEATURE_NOT_ENTITLED`, `USAGE_LIMIT_REACHED`, `SUBSCRIPTION_SUSPENDED`, `VALIDATION_ERROR`, `INVALID_AMOUNT`, `INVALID_DATE`, `INVALID_STATUS`, `DUPLICATE_RECORD`, `FINANCE_APPROVAL_REQUIRED`, `SELF_APPROVAL_NOT_ALLOWED`, `PERIOD_LOCKED`, `PAYMENT_ALREADY_PROCESSED`, `PAYMENT_VERIFICATION_FAILED`, `SYNC_CONFLICT`, `SYNC_VERSION_MISMATCH`, `SYNC_OPERATION_EXPIRED`, `RATE_LIMITED`, `SERVICE_UNAVAILABLE`, `TIMEOUT`, `INTERNAL_ERROR`.

### HTTP status mapping

| Status | Meaning |
|---|---|
| 401 | Auth required/invalid |
| 403 | Permission denied |
| 402 | Subscription required |
| 422 | Validation error |
| 409 | Conflict / duplicate / period locked |
| 429 | Rate limited |
| 503 | Retryable service error |
| 504 | Timeout |
| 500 | Internal error |

## Offline & sync (PRD §34)

- Read models may be cached locally.
- Safe (idempotent) operations can be queued and retried with an `idempotency_key`.
- Version conflicts must be detected (`SYNC_VERSION_MISMATCH`, `SYNC_CONFLICT`).
- Offline mode must **never** bypass permission checks, finance approval, payment verification, or tenant isolation.
- Sensitive operations (payments, approvals, salary actions) require an online round-trip/confirmation — they are not queued blindly.

## Storage (PRD §35)

Supabase Storage holds profile photos, mosque logos, notice attachments, meeting documents, receipts, and Madrasa attachments.

- Private by default; access via **signed URLs** only.
- Every object access is checked against tenant ownership.
- File validation on upload (type/size).
- No client secrets are ever bundled in the Android app.

## Production v1.1.1 Architecture Highlights

- **Room Database v10:** Progressive migrations (`MIGRATION_1_2` to `MIGRATION_9_10`) providing offline-first caching for 28 tables, touch-drawn vector signatures, official committee seals, notice poster attachments, and election entities.
- **Native Android Printing:** `MosquePrintHelper.kt` provides native 1-click printing and PDF generation via `android.print.PrintManager` and custom HTML templates with embedded Base64 stamps and signatures.
- **On-Device Gemini AI Logic:** Firebase AI Logic (`Firebase.ai.generativeModel("gemini-3.8-flash")`) executes client-side via `google-services.json`, eliminating the need to expose API keys in Supabase.
- **Live Supabase PostgreSQL 17 Cloud Backend:** Project `donglyxtrfyzwgtrskhz` hosted in `ap-northeast-1` with 28 tables, 4 RLS policies, 10 server-side RPCs, and automated bidirectional client sync via `SupabaseSyncManager.kt`.

## Related documents

- `docs/DATABASE_ARCHITECTURE.md` — schema and table groups
- `docs/DATA_FLOW.md` — end-to-end flows for money and academic data
- `docs/SECURITY_MODEL.md` — RLS, tenancy, and RPC security rules
- `docs/PERMISSION_MATRIX.md` — role/permission mapping

