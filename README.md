# মসজিদ সেবা — Mosque Seba

<p align="center">
  <img src="design/branding/banner_playstore.png" alt="Mosque Seba Banner" width="100%" />
</p>

> **অফিসিয়াল স্লোগান:** *মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা*  
> **Application Package:** `com.mosqueseba.app`

Production repository foundation for **মসজিদ সেবা (Mosque Seba)** — Comprehensive Islamic mosque, committee, madrasa, and finance operations platform.

## Source of Truth
1. `requirements/PRD_v2.0_FINAL.md`
2. `requirements/UI_UX_DESIGN_SYSTEM_v2.0_FINAL.md`
3. Security, permission and tenant rules
4. Architecture and database documentation
5. Implementation code

## Repository Structure
- `requirements/` — product requirements and traceability
- `docs/` — architecture, security, development and QA documentation
- `design/` — design tokens, components, screens and assets
- `app/` — Android application source
- `supabase/` — database, policies, functions and seed data
- `testing/` — unit, integration, security, UI and acceptance tests
- `.github/` — CI/CD and GitHub templates

## Development Rule
Do not remove or weaken PRD requirements, security/RLS controls, tenant isolation, auditability, or the custom Islamic Modern UI/UX system. Material Design 3 must not be introduced.
