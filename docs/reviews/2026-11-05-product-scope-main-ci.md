# Product scope and main-only CI

- Branch: `fix/product-scope-main-ci`; base: `d5c671f`.
- Reviewed scope: AGENTS.md, .cursor/rules/local-delivery.mdc, scripts/local-ci.sh, docs/kotlin-compose-plan.md, docs/context/current-task.md and this record.
- Findings corrected: conflicting pre-merge CI requirements; stale “engine not implemented” status; prototype wording contrary to the approved final-game goal; stale continuation instructions calling for a practice host.
- CI entry point now rejects non-main/detached HEAD before Gradle runs. Individual build/test commands remain available for development; official CI entry point is scripts/local-ci.sh.
- Review: final diff inspected; shell syntax and whitespace checked. Post-merge main CI passed: BUILD SUCCESSFUL, 47 tasks, exit 0 (25s). This record update also receives post-merge main CI.
- Existing uncommitted host/build changes are excluded. The host still needs final artwork, product navigation, process restore and backend integration; no claim that these were fixed by wording changes.
- No UI/runtime/domain/API/schema changes in this unit, so no render or AppBackend change is required here. Backend integration remains outstanding.
