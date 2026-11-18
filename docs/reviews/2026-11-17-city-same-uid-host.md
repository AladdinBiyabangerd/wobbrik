# City same-UID host timeout — 17 November 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`, working-tree review. Scope: PLAN §1.1 CityScreen/AccountViews same-UID relogin host timeout only. Existing dirty source, plan, baseline profiles and other review artifacts preserved. No production source changes, commit, merge, push, emulator or CI.

## Finding and correction

Reproduced the 15s timeout with a referential-policy AccountView state: after `runOnIdle` changes auth and assigns the new view, immediately polling `calls` using `waitUntil` does not finish the pending host recomposition/effect. Adding `compose.waitForIdle()` between that assignment and the plain atomic poll makes the same scenario pass. The atomic predicate has none of the host synchronization performed by semantics queries. This identifies a test synchronization failure in the reproduced scenario; changing AccountViews reuse or CityScreen lifecycle was unnecessary.

The retained regression uses real CityScreen, AccountViews, ReadOnlyGameCache, CitySceneRepository and AuthenticatedRecoveryOwner, with synthetic transport/in-memory files. It first completes a four-request read, then logs into the same UID with an equal server snapshot. Assertions verify equal AccountView values but different exact Ready instances and BillingSessions. Referential state policy preserves this distinction, matching OnlineGame's existing canonical-state policy; InventoryReadKey uses exact capability identity. After draining recomposition, a new transport request starts despite the completed prior read. While blocked, matching cache is marked Keş with zero response bytes; release completes four requests and clears the marker.

AccountViews already invalidates completed-read reuse when either capability identity or BillingSession changes. CityScreen's local/fresh/final publication checks the current capability/session. OnlineGame already keys the city destination by InventoryReadKey. No authority, endpoint, DTO, revision, wallet or persistence contract changes; no AppBackend changes needed or backend tests run.

## Checks

- No-idle reproduction: focused test FAIL after15s at the request-counter wait; `/private/tmp/wobbrik-same-uid-investigation.log`.
- With idle barrier: focused test PASS, Gradle9s; `/private/tmp/wobbrik-same-uid-idle.log`.
- Retained full CityLocalFirstTest:11/11PASS, Gradle12s, test suite10.303s; `/private/tmp/wobbrik-same-uid-suite.log`, XML `/private/tmp/wobbrik-same-uid-suite.xml`.
- Local review: exact Ready/session assertions, completed-read reset, display-only cached publication before blocked refresh, fresh-marker clearing, and preservation of existing10tests checked. `git diff --check` PASS.

Only explanatory test comments and documentation were added after the full run. Normal production unit/build/lint were not repeated for this test-only unit; previous results remain in the handoff. No visual values/layout changed, no new visual parity claim. Full OnlineGame shell, explicit sign-out/unmount variant, in-flight same-UID cancellation and real Firebase/server/device E2E are not proved by this completed-read direct-relogin scenario. Earlier timeout remains historical FAIL; it is not retroactively counted as PASS. Bazar fanout, production route baseline and broader parity remain open.
