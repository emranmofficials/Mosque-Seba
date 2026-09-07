# Data Flow

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 8–10, 14, 20, 22, 27, 28, 34). Every flow below must satisfy the invariants in `docs/DATABASE_ARCHITECTURE.md` and the security rules in `docs/SECURITY_MODEL.md`.

## 1. Chanda (monthly subscription) payment

```
Member/Admin initiates payment
  → Cash / Mobile banking / (future) online gateway
  → chanda_payments row created (idempotent, keyed by mosque_id + source_type + source_id)
  → On success: automatically posted to Finance Income (transactions)
  → Official receipt generated (mosque, Committee ID, member, member ID, month,
     amount, method, transaction ID, receipt ID, date/time, received/verified by, QR code)
  → Ledger updated (month, due, paid, balance, method, date, status)
```

- Manual duplicate deposit is forbidden by the double-post invariant.
- Corrections never edit a posted receipt directly: **request → approval → reversal/adjustment → audit**.
- Receipt verification is QR-only.

## 2. Finance income / expense

```
Creator submits transaction (category, amount, date, description, source, attachment)
  → Approval policy evaluated (₮5,000 / ₮20,000 / ₮50,000 authority tiers, configurable per mosque)
  → Self-approval blocked (creator ≠ approver)
  → Approved → transaction posted (immutable)
  → Correction needed → reversal/adjustment transaction created, linked, and audited
```

Money fields are `NUMERIC`, never floating point. Posting is idempotent and period-lockable.

## 3. Staff payroll

```
Payroll record created for a pay period
  → Linked Finance expense created
  → Finance approval flow (same as §2)
  → Payroll finalized
```

One salary payment ⇒ exactly one payroll payment row ⇒ exactly one Finance expense row. Salary figures are only visible to holders of `staff.salary.view`.

## 4. Madrasa fees

```
Fee Setup → Student Fee assigned → Payment → Receipt
  → Finance Income (auto-posted, one-to-one with the payment)
  → Verification → Ledger
```

Distinct from mosque chanda; same idempotency/duplicate-prevention discipline applies via secure RPC + RLS.

## 5. Exam / result publication

```
Teacher submits results → Review → Approve → Publish
```

Only **published** results are exposed to guardian/student-facing views; intermediate states are internal to staff/teacher roles.

## 6. Notice publication (including AI-assisted drafting)

```
(Optional) AI draft via Gemini from a short instruction
  → Human edit/review
  → Publish (title, rich content, date, expiry, attachment, priority, audience, pin)
```

AI may draft; it can never autonomously publish a notice or mutate an authoritative record. All AI requests are logged (`ai_requests`) with lifecycle `Pending → Processing → Completed/Failed/Rejected`.

## 7. Notifications

```
Domain event occurs (payment, approval, notice, meeting, prayer, duty, leave,
  attendance, exam/result, homework, fee, guardian academic update)
  → event_outbox row written
  → Delivery worker sends via FCM (+ future email/SMS)
  → notification_deliveries tracks delivery state per device_token
```

Device tokens support register/refresh/deactivate/logout lifecycle.

## 8. Offline queueing

```
Client performs a safe (idempotent) write while offline
  → Queued locally with an idempotency_key
  → On reconnect: replayed against the server
  → Server detects version conflicts (SYNC_VERSION_MISMATCH / SYNC_CONFLICT) if applicable
```

Sensitive operations (payments, approvals, salary actions, tenant-changing actions) are **not** queued offline — they require an online round-trip.
