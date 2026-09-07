# Development Rules

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 39, 42) and the repository `README.md` Development Rule.

## Non-negotiables

Do **not** remove or weaken:
- PRD requirements
- Security / RLS controls
- Tenant isolation
- Auditability
- The custom "Islamic Modern" UI/UX system

**Material Design 3 must never be introduced.**

## Governance workflow (PRD §42)

```
PRD Requirement → Change Request → Design → DB/RPC → Android → Tests → Verification → Status Update
```

- A new requirement that conflicts with the current PRD requires a **Change Request** and a **PRD version update** after approval — features are not silently added or altered outside this flow.
- Bug fixes only trigger a PRD change when the fix changes observable product behavior (not for pure implementation-detail fixes).

## Definition of "complete" (PRD §39)

A feature is **not** complete merely because a UI screen, a database table, or an RPC function exists. It is production-complete only when **all** of the following are satisfied:

1. Traced to a PRD requirement
2. Database schema in place
3. Server-side authorization enforced (not client-side only)
4. RLS policies applied and tested
5. RPC/API implemented and validated
6. Android UI implemented
7. Loading, error, and empty states implemented
8. Audit logging added for sensitive actions
9. Idempotency implemented for money/payment-related actions
10. Unit tests written
11. Critical E2E test(s) passing
12. Cross-tenant safety verified
13. No secrets present in the client build

## Working agreement for this repository

- `requirements/PRD_v2.0_FINAL.md` and `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md` are read-only source-of-truth documents. Do not edit them casually — a substantive change is a PRD version bump via Change Request.
- Derived docs (`docs/*.md`) may be refined/expanded as implementation proceeds, but must never contradict the PRD/UI-UX spec above them.
- `requirements/FEATURE_MATRIX.md` and `requirements/REQUIREMENT_TRACEABILITY.md` should be kept up to date as features are implemented, so status is always verifiable against the actual codebase rather than assumed.
- See `docs/DEVELOPMENT_PHASES.md` for phase sequencing and `docs/QA_ACCEPTANCE_CRITERIA.md` for release gating.
