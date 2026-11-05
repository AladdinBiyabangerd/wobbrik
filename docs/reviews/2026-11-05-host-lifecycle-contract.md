# Host lifecycle contract review

- Branch: docs/host-lifecycle-contract, based on main 18d1825.
- Reviewed diff: lifecycle contract, core README reference and current-task handoff.
- Findings corrected: finish creates a result/building but does not credit wallet;
  checkpoint rewards cannot be silently undone by a mobile restart.
- AppBackend attempts.go/finish.go and HTTP route registration checked; no server
  implementation, migration or version change needed for the selected owner unit.
- Scope: documentation/next-unit selection only; no UI or auth implementation claim.
- Checks: git diff --check; CI not run on branch. Post-merge main CI pending.
- Limitations: restart/abandon product semantics unresolved; no device/live HTTP tests.
