# Project Overview

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 1, 2, 43, 44). This document must stay consistent with the PRD; the PRD remains the Master Source of Truth.

## What this is

মসজিদ কমিটি ম্যানেজমেন্ট সিস্টেম একটি Bengali-first, Android-native operating system for local mosque committees. It is **not** a social app — it is an operations management system covering committee administration, membership, monthly subscription (chanda), income/expense, staff, Madrasa/Maktab, meetings, notices, prayer times, duty rosters, reporting, and communication, built with strict mosque-wise (multi-tenant) isolation.

## Core goals

- Financial transparency (income/expense, approvals, audit trail)
- Member and committee management
- Monthly chanda collection and payment history
- Staff (Imam/Muazzin/Khadem) payroll
- Madrasa operations (students, guardians, attendance, Hifz, exams, fees)
- Notification and communication
- Strict per-mosque tenant isolation — no cross-mosque data leakage under any condition

## Product identity

| Attribute | Value |
|---|---|
| Name | মসজিদ কমিটি ম্যানেজমেন্ট সিস্টেম |
| Platform | Android |
| Primary language | বাংলা (Bengali-first) |
| Backend | Supabase / PostgreSQL |
| Client architecture | Kotlin + Jetpack Compose + MVVM / Clean Architecture |
| UI system | Custom "Islamic Modern" design system — **Material Design 3 is prohibited** |
| Brand tone | Islamic, trustworthy, professional, clean, calm, modern |

## Source of truth hierarchy

1. `requirements/PRD_v2.0_FINAL.md`
2. `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`
3. Security, permission, and tenant isolation rules (`docs/SECURITY_MODEL.md`, `docs/PERMISSION_MATRIX.md`)
4. Architecture and database documentation (`docs/ARCHITECTURE.md`, `docs/DATABASE_ARCHITECTURE.md`)
5. Implementation code

Any conflict is resolved by this order. Code never overrides the PRD; a PRD change requires a Change Request (see `docs/DEVELOPMENT_RULES.md`).

## Roadmap snapshot (PRD §43)

- **P0 — Production foundation:** Authentication, tenant isolation, roles/permissions, Member, Chanda, Finance, Staff/payroll, core Dashboard, Notices, critical Notifications, backup/recovery, security testing.
- **P1 — Core expansion:** Madrasa, Guardian, Attendance, Hifz, Exam/Result, Fees, Timetable, Homework, Reports, Duty, Meetings, Prayer.
- **P2 — Advanced:** AI assistant, advanced analytics, procurement, inventory, assets, projects, advanced welfare, subscription automation.

## Final product definition (PRD §44)

The finished application must be a secure, production-ready digital operating system for mosque committees — Islamic, modern, trustworthy, fast, simple, professional, and Bengali-first, while behaving securely, consistently, auditably, tenant-isolated, permission-aware, financially reliable, and resilient to duplicate requests. **PostgreSQL/Supabase is the authoritative source of truth; Android is a presentation/interaction layer, not a security boundary.**
