# Design fidelity rule review

- Branch: `docs/design-fidelity-rule`; base: `d8ef908`.
- Reviewed diff: four added requirements in `AGENTS.md`, plus this record. Existing uncommitted Compose work is excluded from the commit.
- Findings: reference precedence, asset substitution, adaptive exceptions and screenshot comparison are explicit; no unresolved findings.
- Validation: `./scripts/local-ci.sh` passed before this record (95 tasks); final documentation diff receives the same check before commit. Post-merge CI is run separately.
- Scope: documentation only; no UI rendering or backend changes required. This does not validate the existing host against the designs.
