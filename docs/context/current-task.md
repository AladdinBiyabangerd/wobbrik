# Atomic city placement handoff — 8 November 2026

## Completed
- Backend `feat/city-footprint-placement`: implementation `34c922b` completes migration 012, owned free parcel placement and atomic building/decor-copy place/move/replace/return. Existing BuyShop buys independent rights/copies; placement never charges again.
- Read/HTTP/catalog contract and listing/sale/delete/purge cleanup implemented. Backend review: `/Users/mac/My Workspace/AppBackend/docs/reviews/2026-11-08-city-footprint-placement.md`; Wobbrik review: `docs/reviews/2026-11-08-city-footprint-placement.md`.
- Wobbrik counterpart `feat/city-footprint-placement`, contract/PLAN commit `8c0a958`; no Android/UI code change.

## Current state
- User authorized both main merges on 8 November. Wobbrik `cad922b` and backend `1eec2ef` preserve feature and inherited history. No push/deploy. Wobbrik main lives in `/Users/mac/My Workspace/Wobbrik-rail-icons`; original worktree remains on feature branch.
- Backend main returns placement_enabled:true; mobile remains without placement integration. Stage 3 is still incomplete; this is a backend stage-4 subunit, not phase acceptance.

## Decisions
- Existing paid land right → later free placement. Return/move preserves ownership, no refund. Base fixed at (0,0), quarter turn 0.
- One final-state batch (1–100 changes), account lock/revision/dedup/receipt; connected/no-overlap parcel union, complete object containment and 10000-tick clearance. Position uses canonical decimal strings and required quarter turn 0–3.
- Decoration IDs identify owned copies; buildings require typed accepted provenance/current content revision. No legacy geometry/backfill; return geometry-less membership before new layout validation. No new physics proof.

## Remaining work
- Authorized main merges and local checks completed. Android placement client/renderer/navigation and device/live HTTP integration remain separate requested units; UI needs concrete approved design.
- Stage 3 still needs live account/network/process recovery, final landing→checkpoint→finish checks, cosmetic selection linkage and Room/outbox; no completion claim.

## Relevant files
- AppBackend internal/wobbrik/city_{placement,catalog,footprints,geometry}.go; city.go; shop.go; internal/platform/wobbrik_{http,placement_test}.go; deletion.go.
- AppBackend migrations/012_wobbrik_city_placement.sql; docs/wobbrik-game-api.md final placement section; scoped review above.
- Wobbrik docs/PLAN.md, docs/kotlin-compose-plan.md, docs/context/current-task.md.

## Checks performed
- Wobbrik main `cad922b`: ./scripts/local-ci.sh PASS (1m 52s, 103 tasks). Backend main `1eec2ef`: full real-PostgreSQL race suite, vet and make build PASS. Documentation diff --check PASS. No fresh device/render/live Firebase check.
- Real disposable PostgreSQL 18 / loopback 55439 / placement_test: full `go test -race -count=1 ./...` PASS; after final test/index additions focused city/parcel/return/read/HTTP suite PASS. `go vet ./...`, server build and diff --check PASS.
- Local scoped diff review complete; no main CI. Wobbrik docs-only diff review; no Android build/render/device test in this unit.

## Exact next step
- Both authorized main merges and checks are complete. Await a separately scoped task for stage 3 live validation or Android placement integration; present the approved design before UI implementation. No phase acceptance, push or deploy is implied. Read the handoff from main in Wobbrik-rail-icons; the original feature worktree retains its historical handoff.
