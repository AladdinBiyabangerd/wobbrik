# Home read DTO contract — 7 November 2026

- Branch: `feat/home-recovery-contract`, based on main; reviewed diff: RecoveryDtos.kt and RecoveryDtosTest.kt in this commit. Unreviewed online feature history is excluded.
- Change: nullable server bestFloors and BigInteger XP interval in AccountRecovery; missing older-server fields remain unavailable. Present invalid type/null/negative/partial/noncanonical/inconsistent interval fails closed.
- Review: additive defaults preserve callers; required recovery fields and historical receipts unchanged. Exact integer interval validation uses wobbrik-v1 rule, grants nothing, and accepts thresholds beyond Long.MAX_VALUE. No UI change.
- Tests added: legacy missing fields, valid record, XP 0/49/50/149/150/MaxInt64, malformed fields and mismatched intervals.
- CI: not run on branch per AGENTS.md. Run `./scripts/local-ci.sh` on main after reviewed merge; outcome will be recorded after execution.
- Backend: separately scoped `feat/home-read-contract` in AppBackend supplies these read fields; backend checks are independent.
- Limitations: no runtime UI binding, device render or deployment; this unit only defines/parses the contract.
