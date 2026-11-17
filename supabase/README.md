# Supabase

This directory holds the database layer for the P0 scope defined in
`requirements/PRD_v2.0_FINAL.md` and `docs/DATABASE_ARCHITECTURE.md`.

## Layout

- `migrations/` — schema (tables, constraints, indexes), applied in filename order:
  1. `0001_extensions_and_helpers.sql` — extensions only (`uuid-ossp`, `pgcrypto`)
  2. `0002_core_identity_tenant.sql` — `mosques`, `profiles`, `roles`, `permissions`, `role_permissions`, `user_roles`, `user_permission_overrides`
  3. `0003_helper_functions.sql` — `current_profile_id()`, `current_mosque_id()`, `has_permission()`. **Must come after `0002`** (queries `profiles`/`user_roles`/etc.) — this file used to be bundled into `0001` and referenced tables that didn't exist yet; caught by the apply test described in "Verifying" below.
  4. `0004_members_staff.sql` — `members`, `member_preferences`, `staff`, `staff_availability`
  5. `0005_finance_chanda_payroll.sql` — `finance_categories`, `approval_policies`, `transactions`, `financial_reconciliations`, `chanda_schedules`, `chanda_payments`, `payroll_payments`
  6. `0006_notices_notifications_audit.sql` — `notices`, `notice_audiences`, `device_tokens`, `event_outbox`, `notifications`, `notification_deliveries`, `ai_requests`, `audit_logs`, `security_events`
- `policies/` — Row Level Security policies, one file per migration group, applied after all migrations.
- `functions/` — secure RPC (SQL, SECURITY DEFINER), applied after migrations and policies:
  1. `0001_log_audit_event.sql` — internal helper; the only writer of `audit_logs`
  2. `0002_generate_business_code.sql` — atomic per-mosque sequence generator (`TXN-2026-000123` style IDs) + its own `mosque_sequences` table/RLS
  3. `0003_request_chanda_payment.sql` — member self-submits a chanda payment claim (pending, not yet posted)
  4. `0004_receive_chanda_payment.sql` — admin receives/verifies a chanda payment; posts to Finance Income exactly once, generates receipt + QR code
  5. `0005_submit_finance_transaction.sql` — creates a pending discretionary income/expense transaction

  6. `0006_approve_finance_transaction.sql` — approves/rejects a pending transaction; blocks self-approval; enforces tiered authority from `approval_policies`
  7. `0007_reverse_finance_transaction.sql` — corrects a posted transaction via a linked equal-and-opposite reversal (never edits/deletes the original)
  8. `0008_finalize_payroll_payment.sql` — creates the one linked Finance expense for a payroll payment
  9. `0009_publish_notice.sql` — the only path that sets a notice to `published`; always attributes to a human profile
  10. `0010_complete_ai_request.sql` — service-role only; records Gemini draft output on an `ai_requests` row

  Not yet written: Madrasa fee payment posting (P1, mirrors `0004`), guardian-portal RPCs, subscription/entitlement RPCs (P2), and Edge Functions for actual FCM delivery / Gemini calls (these SQL functions are the DB-side half; the Deno Edge Function that calls Gemini/FCM and then calls `complete_ai_request` / inserts notifications is not in this repo yet).
- `seed/` — (empty) target for local development seed data (a demo mosque, roles/permissions catalogue, a few members).

## Verifying

This schema/RLS/RPC stack has been applied and functionally exercised against a
real local PostgreSQL 16 instance (not just checked for SQL syntax). That test
caught three real bugs, now fixed in this repo:

1. **Migration ordering bug**: the original `0001` file defined
   `current_mosque_id()` etc. before `public.profiles` existed (it's created in
   `0002`) — `CREATE FUNCTION` succeeded (Postgres doesn't validate table
   references inside a function body at creation time) but the very first real
   query failed with `relation "public.profiles" does not exist`. Fixed by
   moving the helper functions into `0003_helper_functions.sql`, after `0002`.
2. **Infinite RLS recursion**: `current_profile_id()`/`current_mosque_id()`/
   `has_permission()` were originally `SECURITY INVOKER` and queried
   `public.profiles`/`public.user_roles`/etc., which have RLS enabled and
   whose own SELECT policies call these same functions — causing
   `stack depth limit exceeded` on the very first authenticated query. Fixed
   by making all three `SECURITY DEFINER` (see the comment block at the top
   of `0003_helper_functions.sql` for why this is safe).
3. **RLS/RPC permission mismatch**: `chanda_payments_select` only checked
   `chanda.view`, but `receive_chanda_payment()` checks `chanda.receive` — a
   user granted only `chanda.receive` couldn't even see the row to receive it.
   Fixed by adding `chanda.receive` to the policy's `OR` condition.
4. **Reversed guard-clause bug**: `reverse_finance_transaction()`'s
   double-reversal check queried
   `exists (select 1 from transactions where reversed_by_id = v_original.id)`,
   which can never match (that column is set *on* `v_original`, not on some
   other row pointing at it) — a transaction could be reversed twice. Fixed by
   checking `v_original.reversed_by_id is not null` directly.

To re-run an equivalent check locally: apply `migrations/` → `policies/` →
`functions/` in order against a real Postgres, stub `auth.uid()`/`auth.role()`
as a `auth` schema (Supabase provides these for real; a plain local Postgres
does not), seed a mosque/roles/profiles, then connect as a **non-superuser**
role granted `authenticated` (RLS is not enforced for superusers or table
owners, so testing as `postgres` gives false confidence) and exercise each RPC
end-to-end, including retrying with the same idempotency key and attempting
cross-tenant/cross-person actions that should be denied.



Apply after migrations and policies, in filename order:

```bash
for f in supabase/functions/*.sql; do psql "$DATABASE_URL" -f "$f"; done
```

Every RPC here validates its own inputs and re-checks permissions with `public.has_permission(...)` even though RLS also applies — this "belt and suspenders" pattern matches the authorization formula in `docs/SECURITY_MODEL.md` and gives clearer, PRD-aligned error codes (`SELF_APPROVAL_NOT_ALLOWED`, `PAYMENT_ALREADY_PROCESSED`, etc. from `docs/ARCHITECTURE.md`'s error-code table) instead of a generic RLS denial.

## What's covered vs. not yet

Covered here: **P0 scope** — auth/tenant identity, roles/permissions, members, staff, finance, chanda, payroll, notices, notifications, audit. See `requirements/FEATURE_MATRIX.md`.

**Not yet in this repo:** Madrasa/academic tables (P1), procurement/inventory/assets/projects (P2), subscription/entitlement platform tables (P2). These should be added as their own numbered migrations (`0006_...`, `0007_...`) when that phase starts, following the same pattern: schema first, then a matching file in `policies/`.

## How this satisfies the PRD's non-negotiables

- Every tenant-owned table carries `mosque_id`; RLS is enabled on every table in `policies/`.
- `current_mosque_id()` derives tenant context only from the authenticated session — never from a client-supplied value (PRD §31).
- Money columns are `NUMERIC`, never floating point.
- Self-approval on `transactions` is blocked both by a `CHECK` constraint and by the `transactions_approve` RLS policy (PRD §9).
- Duplicate posting from chanda/fees/payroll into `transactions` is blocked by the `unique (mosque_id, source_type, source_id)` constraint (PRD §8, §20).
- `staff_availability` has a `no_overlapping_availability` exclusion constraint (PRD §10).
- AI-authored notices cannot self-publish: `published_by` must be a human profile before `status = 'published'` (PRD §14, §28).
- `event_outbox`, `audit_logs`, and `security_events` are not directly writable by any client role — only by SECURITY DEFINER functions/Edge Functions running with service credentials, matching `docs/SECURITY_MODEL.md`.
- Every money-moving RPC in `functions/` is idempotent (replay-safe via `idempotency_key` or a source-row lock) and writes to `audit_logs` via `log_audit_event()` before returning.

## Applying locally

```bash
supabase db reset          # applies migrations/ in order against a local Supabase instance
# then apply policies (or move them into migrations/ once stable, per your team's convention)
psql "$DATABASE_URL" -f supabase/policies/0001_core_identity_tenant_rls.sql
psql "$DATABASE_URL" -f supabase/policies/0002_members_staff_rls.sql
psql "$DATABASE_URL" -f supabase/policies/0003_finance_chanda_payroll_rls.sql
psql "$DATABASE_URL" -f supabase/policies/0004_notices_notifications_audit_rls.sql
```

Before applying to any real project, review every policy against `docs/PERMISSION_MATRIX.md` and run the security test suite described in `docs/QA_ACCEPTANCE_CRITERIA.md`.

## Live backend ↔ Android sync mapping

The currently configured production backend (`https://donglyxtrfyzwgtrskhz.supabase.co`)
uses a **flattened, text-ID schema** that is optimised for the offline-first Room
client, not the UUID/RLS schema described above. `SupabaseSyncManager` maps the
local Room field names to the live column names. Two mappings are easy to miss:

| Room entity field | Live column | Table |
| --- | --- | --- |
| `TransactionEntity.categoryNameBn` | `category_id` (text) | `transactions` |
| `NoticeEntity.publishDate` | `publish_at` (date) | `notices` |
| `StaffEntity.nameBn` / `.mobile` | `name_bn` / `mobile` (added to live schema) | `staff` |
| `AuditLogEntity.details` / `.timestamp` | `details` / `timestamp` (added to live schema) | `audit_logs` |
| `AuditLogEntity.id` (`Long`) | `id` text as `audit-<n>` | `audit_logs` |

The sync engine now covers **all 24 Room entities plus `profiles`** (25 tables).
It normalizes values to satisfy live `CHECK` constraints on push:
`transactions.type` → `income|expense`, `amount > 0` (zero rows skipped);
`notices.priority` → `normal|important|urgent`, `status` → `draft|published|archived`,
date strings (Bengali `dd MMMM yyyy`) → ISO `yyyy-MM-dd`, `expiry_at >= publish_at`;
`staff.role_code` → `imam|muazzin|khadem|other_staff`, `employment_type` →
`full_time|part_time|contract`, `salary_frequency` → `monthly|weekly|one_time`,
negative money clamped to `0`. Every write is locked to the active `mosque_id`.
When pointing the app at a different Supabase project, run the in-app **Test
connection** first: it validates all 25 tables used by the sync engine instead
of reporting success on a bare `200`/`404`.

## Supabase Auth (live project)

The live project uses **real Supabase Auth** (`migrations/0007_auth_rls.sql`),
not the old local SHA-256 demo credentials:

- `SupabaseAuthClient` calls `/auth/v1/signup`, `/auth/v1/token?grant_type=password`,
  the refresh-token endpoint and `PUT /auth/v1/user` for password changes.
  `mailer_autoconfirm` is enabled so signup returns a usable JWT immediately.
- `SessionManager` stores `access_token` / `refresh_token` / `expires_at` /
  `auth_user_id` and refreshes the token before every sync. `isLoggedIn()`
  requires a valid Auth session, so stale demo sessions are forced to re-sign in.
- `SupabaseHttpClient` sends `Authorization: Bearer <JWT>` on all REST calls;
  `SupabaseRealtimeClient` joins the channel with the same JWT (fresh on every
  reconnect). The anon key is used only for `apikey`, the connection test, and
  the two bootstrap RPCs.
- `profiles.auth_user_id` now holds the **real Auth UID**. RLS replaces the old
  `public / using true / check true` rules with: own profile by
  `auth.uid() = auth_user_id`, `mosques` by `id = seba_current_mosque_id()`,
  and every tenant table by `mosque_id = seba_current_mosque_id()`.
  `mosques` INSERT stays open to authenticated users so registration works.
- UI is unchanged. The president signs in with the email used at signup;
  mobile-only members are mapped to a deterministic synthetic email
  (`m-<memberId>@mosqueseba.app`) with the last six mobile digits as the initial
  password. `seba_lookup_committee` / `seba_lookup_member_by_mobile` are
  narrow `SECURITY DEFINER` RPCs that let a new device resolve those IDs before
  it has a JWT; `seba_create_member_profile` links a new member's Auth UID to
  the caller's own mosque only.

End-to-end verification against the live project covered registration, new-device
president login, adding a member, member login with the last six digits, Auth
password change, REST tenant isolation (cross-tenant INSERT returns `403`, anon
reads zero rows), and a JWT Realtime join with all 25/25 subscriptions `ok`.

## Realtime

The app uses `SupabaseRealtimeClient` (OkHttp WebSocket, Phoenix v1 JSON
protocol) for true live updates instead of relying on polling. It opens
`wss://<project>/realtime/v1/websocket?apikey=<anon>&vsn=1.0.0`, joins one
channel with `postgres_changes` subscriptions (`event: "*"`) for the same 25
tables, filtered by `mosque_id=eq.<activeMosqueId>` (`id=eq.<activeMosqueId>`
for `mosques`). Every committed INSERT/UPDATE/DELETE triggers an immediate
`SupabaseSyncManager.performSync()`; heartbeats (20 s) and reconnects with
exponential backoff keep the channel alive. A 15 s fallback poll runs only while
disconnected (plus a 5 min refresh), and a 5 s cooldown prevents the app's own
upserts from echoing into a sync loop.

All 25 synced tables are members of the `supabase_realtime` publication with
`REPLICA IDENTITY FULL` (`staff` was added during this setup). Realtime delivery
was verified live against the configured project (join reply `ok` and a test
INSERT produced a `postgres_changes` event with `payload.data.table`).

