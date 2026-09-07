# AI Assistant Instructions

> Derived from `requirements/PRD_v2.0_FINAL.md` (Sections 14, 28). This governs any AI feature (currently specified as Gemini-based) built into the product — separate from any AI tooling used to *build* this repository.

## Scope of the in-product AI assistant

The AI assistant (Gemini, per PRD) may:
- Draft mosque notices in Bengali from a short human instruction, in a professional Islamic tone
- Draft meeting minutes
- Draft report summaries and academic summaries
- Offer suggestions (e.g. notice templates, summarized insights)

## Hard limits — the AI assistant may never:

- Autonomously **publish** an official notice — a human must review and explicitly publish
- **Approve finance** transactions
- **Bypass permission** checks of any kind
- **Mutate authoritative records** directly (it drafts; a human/backend workflow commits)
- Ship or require API secrets inside the Android client — any AI call must go through the server side

## Lifecycle (PRD §28)

Every AI request follows: **Pending → Processing → Completed / Failed / Rejected**, and is recorded in `ai_requests` for audit purposes (see `docs/SECURITY_MODEL.md` audit requirements).

## Notice generation flow (PRD §14)

```
Short human instruction
  → AI drafts a professional Islamic-toned notice
  → Human edits/reviews
  → Publish (or save as template)
```

The human-in-the-loop review step is mandatory and cannot be skipped, regardless of how the request is framed in the app.

## Priority

The in-product AI assistant is a **P2 (Advanced)** feature per `docs/DEVELOPMENT_PHASES.md` — it should not be built ahead of P0 (auth, tenancy, roles, finance, chanda) or P1 (Madrasa/academic core) foundations.
