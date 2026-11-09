# Missing city designs — local review

Branch: `docs/missing-city-designs-v1`, base `72209af`. Reviewed working diff: six image boards, three source atlases, alpha preview, prompt set, catalogue map, README and handoff. Design candidates only; no application or server change. No merge/push/deploy.

Findings corrected: inherited invented wall names replaced with actual catalogue names; achievement wall removed; selected plaster preview changed from purple to cream; six canonical starting styles added; short towers lengthened; detail name corrected to İçərişəhər; top-down/source background extraction repeated. Full exact floor counting still does not pass for every image. Remaining geometry, spelling and alpha/crop/density limitations are recorded in the design README; these are not approved production assets.

Checks: final generated images inspected, transparent sources composited against navy and inspected; PNG dimensions/alpha decoded independently; nominal12 IDs match AppBackend catalogue; existing68 modular sources retained. Token contrast pairs16.57/8.22/11.45 pass4.5. No Android build, actual responsive/device/screenreader/font render or API test; no backend change needed for art-only scope. CI not run on branch, and no main merge is authorized in this design review unit.

Next: user design review and bounded corrections, then production source/crop/density verification. A concept approval alone does not accept the stage or prove runtime parity.
