# Permission Matrix

> Derived from `requirements/PRD_v2.0_FINAL.md` (Section 5) and `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md` (Sections 2–3). Authoritative enforcement is server-side (RLS + secure RPC) — this document is the reference mapping, not the enforcement mechanism. See `docs/SECURITY_MODEL.md` for the decision formula.

## Roles → UI interface profile

| Role | Interface Profile |
|---|---|
| President | UI-01 (full administrative) |
| Vice-President | UI-02 (Committee Executive) |
| General Secretary | UI-02 |
| Assistant Secretary | UI-02 |
| Treasurer | UI-02 |
| Assistant Treasurer | UI-02 |
| Organizing Secretary | UI-02 |
| Publicity Secretary | UI-02 |
| Welfare Secretary | UI-02 |
| Executive Member | UI-02 |
| General Member | UI-03 |
| Imam | UI-04 (+ UI-05 if academic permission granted) |
| Muazzin | UI-04 |
| Khadem | UI-04 |
| Other Staff | UI-04 |
| Madrasa Teacher | UI-05 |
| Guardian | UI-06 |
| System Super Admin | UI-07 (platform-level: mosque approval, subscription, support, security) |

A single user may hold multiple roles; the UI merges relevant features according to the union of granted permissions — but the backend still checks every permission independently per action, never assuming UI merge implies grant.

## Permission catalogue (representative, PRD §5)

Grouped by domain — each is an independent grantable permission, not implied by role name alone:

**Members:** `member.view`, `member.create`, `member.update`, `member.archive`
**Chanda:** `chanda.view`, `chanda.pay`, `chanda.receive`
**Finance:** `finance.view`, `finance.create`, `finance.approve`, `finance.report`
**Staff:** `staff.view`, `staff.manage`, `staff.salary.view`, `staff.salary.pay`
**Madrasa (admin):** `madrasa.view`, `madrasa.manage`
**Students:** `student.view`, `student.create`, `student.update`, `student.archive`
**Guardians:** `guardian.view`, `guardian.manage`, `guardian.portal`
**Attendance:** `attendance.view`, `attendance.manage`
**Hifz:** `hifz.view`, `hifz.manage`
**Exam:** `exam.view`, `exam.manage`, `exam.review`, `exam.approve`, `exam.publish`
**Fees:** `fee.view`, `fee.manage`, `fee.pay`
**Timetable:** `timetable.view`, `timetable.manage`
**Homework:** `homework.view`, `homework.manage`, `homework.submit`, `homework.review`
**Reports:** `madrasa.report.view`
**AI:** `madrasa.ai.use`
**Notice:** `notice.view`, `notice.manage`
**Meeting:** `meeting.view`, `meeting.manage`
**Prayer:** `prayer.view`, `prayer.manage`
**Duty:** `duty.view`, `duty.manage`
**Welfare:** `welfare.view`, `welfare.manage`
**Subscription:** `subscription.view`, `subscription.manage`

## Special-case rules

- **`staff.salary.view` / `staff.salary.pay`** are the only gate on payroll amount visibility and payroll actions — not general `staff.view`.
- **`exam.publish`** must follow the workflow Submit → Review → Approve → Publish (PRD §19); no role may skip directly to publish without holding every prior stage's permission or an explicit override.
- **`guardian.portal`** only ever exposes children explicitly and server-verifiably linked to that guardian via `student_guardians` — never a broader student list.
- **Individual permission overrides** (`user_permission_overrides`) can grant or revoke a specific permission for a specific user independent of their role — always checked in addition to role-derived permissions, per the authorization formula in `docs/SECURITY_MODEL.md`.

## Bottom navigation & primary UI rule (UIUX §1–2)

- Maximum 5 primary bottom-navigation items; visibility is role/permission-driven, but this is a UX convenience only.
- **UI permission-hiding is never a substitute for backend authorization.** Every action listed above must be re-checked server-side regardless of what the client renders.
