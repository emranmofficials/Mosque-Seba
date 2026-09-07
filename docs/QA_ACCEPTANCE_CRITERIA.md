# QA & Acceptance Criteria

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 38, 39, 41).

## Testing strategy (PRD §38)

- **Unit tests:** business rules, permission logic, money/date calculations, state management, serialization.
- **Integration tests:** repository/RPC behavior, RLS enforcement, finance/chanda/payroll/fee linkage correctness.
- **Security tests:** cross-mosque access attempts, unauthorized role use, direct DML bypass attempts, self-approval attempts, IDOR, forged tenant/role/subscription claims.
- **E2E critical journeys:** President/member login, member CRUD/profile, chanda payment/receipt, finance transaction/approval, staff payroll/leave/duty, student/guardian management, attendance, Hifz, exam workflow (submit → review → approve → publish), fees, timetable, homework, notice, meeting, prayer, notification delivery, AI draft assistance, and cross-tenant access rejection.

## Definition of complete

See `docs/DEVELOPMENT_RULES.md` — a feature is not "done" just because a screen/table/RPC exists; all thirteen completion criteria must hold, including audit logging, idempotency, tests, and cross-tenant safety.

## Release gates (PRD §41)

| Area | Gate |
|---|---|
| **Security** | RLS audit passed; cross-tenant tests passed; sensitive RPCs reviewed; no client secret in build; unsafe direct DML removed |
| **Data** | Duplicate finance posting provably impossible; payment idempotency verified; receipt verification (QR) working; payroll/fee finance linkage verified one-to-one |
| **Android** | Clean build; release build succeeds; critical paths tested; offline/online behavior verified; navigation complete |
| **UX** | Custom design system in place; no Material Design 3 present anywhere; Bengali typography correct; accessibility checks pass; loading/error/empty states implemented everywhere |
| **E2E** | Critical journeys (listed above) pass with real authenticated users across **at least two mosque tenants**, proving isolation as well as function |

## Acceptance is evidence-based, not assumed

Per PRD §40, the "current implementation snapshot" in the PRD is a planning document, not a verification report. Before marking any P0/P1/P2 item as accepted:
1. Confirm the corresponding requirement in `requirements/PRD_v2.0_FINAL.md` and, if UI-related, `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`.
2. Confirm the DB schema, RLS policy, and RPC exist and match `docs/DATABASE_ARCHITECTURE.md` / `docs/SECURITY_MODEL.md`.
3. Confirm Android implementation matches `docs/ARCHITECTURE.md`.
4. Run the relevant unit/integration/security/E2E tests under `testing/`.
5. Update `requirements/REQUIREMENT_TRACEABILITY.md` and `requirements/FEATURE_MATRIX.md` with the verified status.
