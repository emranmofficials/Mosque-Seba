# Security Model

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 5, 31, 35, 36, 38). This is a hard constraint document — nothing here may be weakened without a Change Request per `docs/DEVELOPMENT_RULES.md`.

## Authorization decision formula (PRD §5)

Every action is only allowed when **all** of the following hold:

```
Authenticated
  AND Correct Mosque (tenant match)
  AND Role Permission
  AND Individual Override
  AND Relationship Permission
  AND Subscription Entitlement
  AND Privacy Policy
→ Allow
```

Role, permission, subscription state, and relationship are **independent axes** — none of them substitutes for another. A user can hold a role without automatically having every permission normally associated with it (individual overrides exist), and a paid subscription never grants a permission the role/relationship model denies.

## Multi-tenant isolation & RLS (PRD §31)

- Every tenant-owned record carries `mosque_id` directly, or derives it securely through a parent record.
- **Row Level Security (RLS) is mandatory** on every tenant-owned table.
- The server **never trusts** client-provided `mosque_id`, role, permission, or subscription state, and never trusts a hidden/disabled UI element as an authorization mechanism.
- Tenant context is derived only from the authenticated identity (JWT/session), never from a request parameter the client controls.

## Secure RPC / Edge Functions

- Default to **`SECURITY INVOKER`**.
- Use **`SECURITY DEFINER`** only when strictly necessary, and when used it must:
  - set a fixed `search_path`,
  - restrict execution to `authenticated` where appropriate,
  - apply the principle of least privilege,
  - perform explicit tenant, permission, and input validation checks inside the function body.
- Sensitive direct client DML (e.g. writing straight to `transactions`, `chanda_payments`, `staff` salary fields) is revoked at the grant level; such writes must go through a secure RPC.

## Roles (PRD §5)

**Committee:** President, Vice-President, General Secretary, Assistant Secretary, Treasurer, Assistant Treasurer, Organizing Secretary, Publicity Secretary, Welfare Secretary, Executive Member, General Member.
**Staff:** Imam, Muazzin, Khadem, Other Staff.
**Platform:** System Super Admin.

See `docs/PERMISSION_MATRIX.md` for the full role → permission mapping.

## Sensitive-data handling (PRD §36)

NID, bank details, salary, welfare case data, and guardian/student data are sensitive categories requiring:
- least-privilege access,
- masking where full values aren't required for the task,
- secure storage,
- auditable access (every read/write of these fields is attributable to an actor).

## Audit logging (PRD §36)

The following categories must be recorded in `audit_logs` with actor, mosque, action, entity, entity ID, timestamp, and metadata/request ID:
- login and security-relevant account changes
- member mutations
- role/permission changes
- finance transactions
- payment/receipt actions
- payroll actions
- student/guardian record changes
- exam workflow transitions
- notice publication/edits
- meeting records
- subscription changes
- AI requests

## Security-critical invariants

- **Self-approval is prohibited** — a creator cannot approve their own finance transaction.
- **Posted financial transactions cannot be hard-deleted** — only reversed/adjusted, preserving the audit trail.
- **Payment idempotency** — duplicate submissions of the same payment must not create duplicate Finance Income/Expense rows.
- **AI cannot self-authorize** — Gemini-assisted drafts can never autonomously approve finance, publish notices, bypass permissions, or mutate authoritative records (PRD §28).
- **No client secrets in the Android build** — API keys for privileged operations stay server-side.
- **Offline mode never bypasses** permission checks, finance approval, payment verification, or tenant isolation (PRD §34).

## Testing requirements (PRD §38, security track)

Security test coverage must specifically include: cross-mosque access attempts, unauthorized role escalation, direct DML bypass attempts, self-approval attempts, IDOR (insecure direct object reference), and forged tenant/role/subscription claims in requests.

## Release gate (PRD §41, security)

Before any release: RLS audit passed, cross-tenant tests passed, sensitive RPCs reviewed, no client secret present in the build artifact, and unsafe direct DML paths removed.
