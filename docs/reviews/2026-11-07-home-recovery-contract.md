# Home read DTO contract — 7 November 2026

- Branch: `feat/home-recovery-contract`, based on main; reviewed diff: RecoveryDtos.kt and RecoveryDtosTest.kt in this commit. Unreviewed online feature history is excluded.
- Change: nullable server bestFloors and BigInteger XP interval in AccountRecovery; missing older-server fields remain unavailable. Present invalid type/null/negative/partial/noncanonical/inconsistent interval fails closed.
- Review: additive defaults preserve callers; required recovery fields and historical receipts unchanged. Exact integer interval validation uses wobbrik-v1 rule, grants nothing, and accepts thresholds beyond Long.MAX_VALUE. No UI change.
- Tests added: legacy missing fields, valid record, XP 0/49/50/149/150/MaxInt64, malformed fields and mismatched intervals.
- CI: branch CI not run per AGENTS.md. After merge `8c1a0d7`, `./scripts/local-ci.sh` on main PASS (1m43s, 103 tasks; core/engine tests, Android/app assemble and lint). Log: `/private/tmp/wobbrik-home-contract-ci.log`.
- Backend: separately scoped `feat/home-read-contract` in AppBackend supplies these read fields; backend checks are independent.
- Limitations: no runtime UI binding, device render or deployment; this unit only defines/parses the contract.
- Handoff review: `docs/home-contract-completion` updates only audit status, this result and current-task continuation. Diff checked against actual commits/logs; no further code change. Context/branch ownership and unfinished UI/online work are explicit.
