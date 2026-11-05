# Host lifecycle contract review

- Branch: docs/host-lifecycle-contract, based on main 18d1825.
- Reviewed diff: lifecycle contract, core README reference and current-task handoff.
- Findings corrected: finish creates a result/building but does not credit wallet;
  checkpoint rewards cannot be silently undone by a mobile restart.
- AppBackend attempts.go/finish.go and HTTP route registration checked; no server
  implementation, migration or version change needed for the selected owner unit.
- Scope: documentation/next-unit selection only; no UI or auth implementation claim.
- Checks: git diff --check; CI not run on branch. Post-merge main CI passed: 95 tasks, 59 executed/36 up-to-date; Android/app assemble and lint, core/engine tests included (tests up-to-date). Verification-record merge receives a final main rerun.
- Limitations: restart/abandon product semantics unresolved; no device/live HTTP tests.
