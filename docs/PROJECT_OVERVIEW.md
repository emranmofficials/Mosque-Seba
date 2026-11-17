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
| Name | মসজিদ সেবা — Mosque Seba |
| Platform | Android (minSdk 26, targetSdk 35) |
| Current Version | v1.1.1 (Production Ready) |
| Primary language | বাংলা (Bengali-first) |
| Backend | Supabase / PostgreSQL 17 (Multi-tenant RLS) + Room DB v10 |
| Client architecture | Kotlin 2.0 + Jetpack Compose + MVVM / Clean Architecture |
| UI system | Custom "Islamic Modern" design system (Pan-Islamic Emerald `#0D5C3A`, Royal Gold `#D4AF37`, Mehrab Arches, Rub-El-Hizb dividers, Zero Emojis) |
| Brand tone | Islamic, trustworthy, professional, clean, calm, modern |

## Source of truth hierarchy

1. `requirements/PRD_v2.0_FINAL.md`
2. `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`
3. Security, permission, and tenant isolation rules (`docs/SECURITY_MODEL.md`, `docs/PERMISSION_MATRIX.md`)
4. Architecture and database documentation (`docs/ARCHITECTURE.md`, `docs/DATABASE_ARCHITECTURE.md`)
5. Implementation code

Any conflict is resolved by this order. Code never overrides the PRD; a PRD change requires a Change Request (see `docs/DEVELOPMENT_RULES.md`).

## Roadmap & Milestone Status (v1.1.1 Production Release)

- **P0 — Production Foundation (Complete):** Multi-role authentication, tenant isolation, dynamic role catalogue, Member management, Chanda subscription cycle, 2-tier financial approval, staff & payroll, dashboard Smart Board, notices, critical notifications, Room v10 offline-first cache.
- **P1 — Core Expansion (Complete):** Madrasa/Maktab core, attendance tracking, Hifz progress scoring, exams & 360 evaluation, student fees, duty rosters, food schedules, committee meetings & resolutions archive, prayer times calculation.
- **P2 — Executive Operations & Modern AI (Complete):** 
  - Google Gemini AI notice drafter & Dawatnama Mahfil poster creator.
  - Interactive touch-screen digital signature pad & official seal manager.
  - 3-witness Jummah cashbox counter & voucher posting.
  - Emergency Janaza alert broadcast via WhatsApp/SMS.
  - Native 1-click printing & PDF generation with embedded Base64 signatures/seals.
  - Election schedule, nomination management & official gazette publisher.
  - Ramadan Special Hub with Sahri/Iftar countdown, Taraweeh & Itikaf tracking.
  - Supabase live cloud backend (`donglyxtrfyzwgtrskhz`) with 28 PostgreSQL tables and automated synchronization.

## Final product definition (PRD §44)

The finished application must be a secure, production-ready digital operating system for mosque committees — Islamic, modern, trustworthy, fast, simple, professional, and Bengali-first, while behaving securely, consistently, auditably, tenant-isolated, permission-aware, financially reliable, and resilient to duplicate requests. **PostgreSQL/Supabase is the authoritative source of truth; Android is a presentation/interaction layer, not a security boundary.**
