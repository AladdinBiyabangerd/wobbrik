# Backend placement contract handoff — 8 November 2026

Branch: `feat/city-footprint-placement`; reviewed documentation diff `835f15b..8c0a958`. Android source/UI is unchanged. Backend counterpart implements placement in `34c922b`; its scoped review is `/Users/mac/My Workspace/AppBackend/docs/reviews/2026-11-08-city-footprint-placement.md`.

PLAN records only the completed backend subunit, preserving stage 3/4 incomplete status. Canonical technical contract documents separate paid purchase rights and later free placement, owned decoration copy IDs, final-state atomic place/move/replace/return, strict integer-string positions, typed building provenance, parcel connectivity/containment/clearance, additive reads/catalog and backend-first migration ordering. Local review matched these fields to backend migration 012, domain requests and HTTP/read code. No unresolved documentation finding. Historical disabled-placement statements describe earlier units; the final contract section explicitly supersedes them on the feature branch.

Checks: docs diff review and diff --check PASS. Backend disposable PostgreSQL full race suite PASS; after final deletion-coverage/index additions focused city suite PASS; go vet and server build PASS. These backend checks do not establish Android integration. No Android build, UI render, device/network test or phase acceptance in this documentation unit.

Main merge still needs separate Aladdin OK. No main merge, local main CI, push, remote CI, Console or deploy. Mobile placement DTO/renderer/navigation remains unimplemented; approved design must be presented before a future UI implementation. Stage 3 live recovery/retry/finish, account verification and Room/outbox remain open.
