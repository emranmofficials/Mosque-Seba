# Mosque Committee Management System

Production repository foundation for the Mosque Committee Management System.

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
