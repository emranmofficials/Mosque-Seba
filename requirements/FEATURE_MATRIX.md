# Feature Matrix

> Derived from `requirements/PRD_v2.0_FINAL.md` (all sections) and `docs/DEVELOPMENT_PHASES.md`. `Status` must be updated only after verification per `docs/QA_ACCEPTANCE_CRITERIA.md` — do not mark items Done from memory or assumption.

Status legend: `Not started` · `In progress` · `Needs verification` · `Done`

| # | Module | Key capabilities | PRD §§ | Priority | Status |
|---|---|---|---|---|---|
| 1 | Authentication & Account | President registration, mosque application/approval, login (President/Member), account states | 4 | P0 | In progress — DB schema + RLS drafted (`supabase/migrations/0002`, `supabase/policies/0001`); registration/login RPC and Android UI not started |
| 2 | Roles & Permissions | Role catalogue, permission engine, authorization formula, individual overrides | 5 | P0 | In progress — schema, `has_permission()` helper, and RLS drafted; seed data and Android enforcement not started |
| 3 | Dashboard | Hero card, Smart Board, Prayer card, KPIs, Quick Actions, Recent Activity | 6 | P0 | Not started |
| 4 | Member Management | List/search/filter, add/edit member, member profile, ledger | 7 | P0 | In progress — `members`/`member_preferences` schema + RLS drafted; RPC and Android UI not started |
| 5 | Chanda / Subscription | Payment collection, ledger, receipt + QR, double-post prevention | 8 | P0 | In progress — `chanda_schedules`/`chanda_payments` schema + RLS drafted; posting RPC, receipt/QR generation, and Android UI not started |
| 6 | Finance Management | Income/expense, approval tiers, self-approval block, audit | 9 | P0 | In progress — `transactions`/`approval_policies`/`financial_reconciliations` schema + RLS drafted (self-approval blocked at DB level); approval RPC and Android UI not started |
| 7 | Staff & Payroll | Staff profile, availability/leave, payroll flow, salary visibility | 10 | P0 | In progress — `staff`/`staff_availability`/`payroll_payments` schema + RLS drafted (overlap exclusion constraint in place); payroll finalize RPC and Android UI not started |
| 8 | Duty Roster | Daily/weekly/monthly roster, assignment validation, PDF export | 11 | P1 | Not started |
| 9 | Food Schedule | Provider/receiver assignment, meal status | 12 | P1 | Not started |
| 10 | Prayer Times | 5 daily prayers, Jummah/Eid/Taraweeh, manual/auto, overrides | 13 | P1 | Not started |
| 11 | Notice & Communication | Rich notice, audience targeting, AI draft assist | 14 | P0 (notice) / P2 (AI) | In progress — `notices`/`notice_audiences` schema + RLS drafted (human-publish enforced at RLS level); publish RPC and Android UI not started |
| 12 | Meeting Management | Schedule, agenda, invite, minutes, action items | 15 | P1 | Not started |
| 13 | Madrasa / Maktab core | Academic sessions, classes, subjects, teachers, students, guardians | 16 | P1 | Needs verification |
| 14 | Madrasa Attendance | Status tracking, uniqueness, reports | 17 | P1 | Needs verification |
| 15 | Quran / Hifz | Session recording, milestones, teacher feedback | 18 | P1 | Needs verification |
| 16 | Exam & Result | Submit → Review → Approve → Publish, report card | 19 | P1 | Needs verification |
| 17 | Madrasa Fees | Fee setup → payment → receipt → Finance Income | 20 | P1 | Needs verification |
| 18 | Timetable | Slot scheduling, conflict detection | 21 | P1 | Not started |
| 19 | Homework | Assignment, submission, review/feedback | 22 | P1 | Not started |
| 20 | Madrasa Reports / Academic 360 | Aggregate reports, student 360 view | 23 | P1 | Needs verification |
| 21 | Guardian Portal | Linked-children view, academic summary | 24 | P1 | Needs verification |
| 22 | Events & Programs | Eid/Milad/Waz/Iftar planning and reporting | 25 | P2 | Not started |
| 23 | Welfare | Case management, approval, disbursement | 26 | P2 | Not started |
| 24 | Notifications | In-app + FCM, event_outbox, delivery tracking | 27 | P0 (critical types) / P1 (rest) | In progress — `notifications`/`notification_deliveries`/`device_tokens`/`event_outbox` schema + RLS drafted; delivery worker and FCM integration not started |
| 25 | AI Assistant | Draft notices/minutes/summaries, human-in-the-loop | 28 | P2 | In progress — `ai_requests` schema + RLS drafted (draft-only, no autonomous publish); Gemini integration not started |
| 26 | Subscription & Entitlement | Plan, mosque subscription, entitlement checks | 29 | P2 | Not started |
| 27 | Procurement / Inventory / Assets | Purchase requests through assets | (schema only, §30) | P2 | Not started |
| 28 | Projects | Project/phase/BOQ/site reports/change requests | (schema only, §30) | P2 | Not started |

## How to use this table

1. Before implementation of a module, confirm the PRD section(s) it maps to.
2. While implementing, keep status at `In progress`.
3. Do **not** move to `Done` without passing the checklist in `docs/QA_ACCEPTANCE_CRITERIA.md` and updating `requirements/REQUIREMENT_TRACEABILITY.md` with the specific tables/RPCs/screens delivered.
