# Supabase

This directory holds the database layer for the P0 scope defined in
`requirements/PRD_v2.0_FINAL.md` and `docs/DATABASE_ARCHITECTURE.md`.

## Layout

- `migrations/` — schema (tables, constraints, indexes, helper functions), applied in filename order:
  1. `0001_extensions_and_helpers.sql` — extensions + `current_profile_id()`, `current_mosque_id()`, `has_permission()`
  2. `0002_core_identity_tenant.sql` — `mosques`, `profiles`, `roles`, `permissions`, `role_permissions`, `user_roles`, `user_permission_overrides`
  3. `0003_members_staff.sql` — `members`, `member_preferences`, `staff`, `staff_availability`
  4. `0004_finance_chanda_payroll.sql` — `finance_categories`, `approval_policies`, `transactions`, `financial_reconciliations`, `chanda_schedules`, `chanda_payments`, `payroll_payments`
  5. `0005_notices_notifications_audit.sql` — `notices`, `notice_audiences`, `device_tokens`, `event_outbox`, `notifications`, `notification_deliveries`, `ai_requests`, `audit_logs`, `security_events`
- `policies/` — Row Level Security policies, one file per migration group, applied after all migrations.
- `functions/` — (empty) target for secure RPC/Edge Functions (e.g. chanda payment posting, payroll finalize, notice publish). Not yet written.
- `seed/` — (empty) target for local development seed data (a demo mosque, roles/permissions catalogue, a few members).

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
