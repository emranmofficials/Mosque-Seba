# Requirement Traceability

> Purpose: map every PRD requirement to its concrete implementation artifacts (DB tables/RLS/RPC, Android screens, tests) so completion claims are verifiable rather than assumed — per the Definition of Complete in `docs/DEVELOPMENT_RULES.md`.

This file should be filled in **as each module is actually implemented in this repository** — it starts empty of implementation evidence because, per the production scaffold audit, no application code exists yet (`app/` and `supabase/` are currently empty directory scaffolds).

## Template (copy per module)

```
### <Module name> (PRD §<section>)

- PRD requirement summary: <one line>
- DB tables: <list, or "not yet created">
- RLS policies: <file(s) under supabase/policies/, or "not yet created">
- RPC/Edge functions: <file(s) under supabase/functions/, or "not yet created">
- Android screens/ViewModels: <path(s) under app/, or "not yet created">
- Tests: <path(s) under testing/, or "not yet created">
- Status: Not started | In progress | Needs verification | Done
- Last verified: <date, or "never">
```

## Modules to trace (from `requirements/FEATURE_MATRIX.md`)

1. Authentication & Account — PRD §4
2. Roles & Permissions — PRD §5
3. Dashboard — PRD §6
4. Member Management — PRD §7
5. Chanda / Subscription — PRD §8
6. Finance Management — PRD §9
7. Staff & Payroll — PRD §10
8. Duty Roster — PRD §11
9. Food Schedule — PRD §12
10. Prayer Times — PRD §13
11. Notice & Communication — PRD §14
12. Meeting Management — PRD §15
13. Madrasa / Maktab core — PRD §16
14. Madrasa Attendance — PRD §17
15. Quran / Hifz — PRD §18
16. Exam & Result — PRD §19
17. Madrasa Fees — PRD §20
18. Timetable — PRD §21
19. Homework — PRD §22
20. Madrasa Reports / Academic 360 — PRD §23
21. Guardian Portal — PRD §24
22. Events & Programs — PRD §25
23. Welfare — PRD §26
24. Notifications — PRD §27
25. AI Assistant — PRD §28
26. Subscription & Entitlement — PRD §29
27. Procurement / Inventory / Assets — PRD §30 (schema-level)
28. Projects — PRD §30 (schema-level)
29. Multi-Tenant Security & RLS (cross-cutting) — PRD §31
30. API Contract (cross-cutting) — PRD §32
31. Android Architecture (cross-cutting) — PRD §33
32. Offline & Sync (cross-cutting) — PRD §34
33. Storage (cross-cutting) — PRD §35
34. Audit, Privacy & Security Events (cross-cutting) — PRD §36
35. Performance & Accessibility (cross-cutting) — PRD §37

> Fill in each module using the template above as implementation begins. Do not pre-fill status as "Done" — every entry must be backed by an actual file path in this repository.
