# Remaining acceptance selection — 8 November 2026

Branch: `docs/legacy-journal-acceptance-selection`. Documentation only; no implementation, merge, push, deploy or CI.

Read AGENTS.md, current handoff, checkpoint isolated live review and PLAN §3.4. Selected next bounded scenario: persisted journal compatibility across app versions, including safe rejection of unsupported versions without rewriting or losing the original envelope. Checkpoint lost-response proof is already complete and should not be repeated without a relevant change.

## Read-only preflight

- Retained backend PIDs 8474/8484 and original PID 42589 exist. Process presence does not establish fresh readiness.
- Separate DB snapshot `output/checkpoint-live-isolated/acceptance-preflight.json` exactly equals retained `final.json` across all eight measured tables. Attempt `f73722ea-b54f-4985-9c7e-85a442c1d0a3` remains active, sequence/floors 1.
- Emulator-5582 is connected; reverse remains device 18443 → host 18445. Only read-only device commands were used; no input, install, restart or clear-data.
- Current journal archive `journal-acceptance-preflight.tar` has exactly the same file payload as retained `journal-final.tar`: 35-byte tombstone. No journal overwrite or gameplay operation.
- DiskAttemptJournal currently accepts storage format 1 and validates command envelopes. Unsupported storage identity/version throws before writing. This source inspection alone is not acceptance proof.

## Exact next unit and acceptance criteria

Use separate temporary JVM journal files and an authentic historical serializer/fixture from Git history; never seed or replace emulator-5582 journal. First establish which historical supported app revision and envelope/rules/engine version the fixture actually represents; do not invent a legacy storage format or label a newly fabricated current command as historical evidence.

For supported historical pending commands, prove exact operation ID, expected revision, route and envelope survive reconstruction; account boundaries remain intact. For unsupported storage/rules/engine versions, prove controlled failure, unchanged original bytes, no backend send and no replacement command. Inspect existing adapter/coordinator tests before adding focused coverage. Synthetic JVM evidence must be labeled as such and cannot close live upgrade acceptance. Any live upgrade exercise needs a separate disposable app/device data directory, leaving this active attempt untouched.

No backend API/schema change is indicated by this selection. No compatibility test suite or live upgrade scenario was run. Full stage acceptance remains open, including account isolation/nonfree unlocks, Google linking, Room/outbox and physical/visual checks.

## Review

Reviewed documentation diff for preservation scope, evidence claims and acceptance boundaries; no production source changes. Whitespace check passed. CI not run (main-only); no merge authorized.
