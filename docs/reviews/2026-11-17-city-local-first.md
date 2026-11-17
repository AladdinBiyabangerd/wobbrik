# CityScreen local-first read — 17 noyabr 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`; reviewed unit: CityScreen.kt working diff and `city-local-first/render/{CityLocalFirstTest.kt,render.init.gradle}`. Earlier dirty owner/initial-sync/art/city-scheduling work preserved. No commit, merge, push, emulator or CI.

## Behavior

CityScreen now passes the existing AccountViews `onLocal`: a complete matching account/revision cache publishes before live capture finishes. `onCached` marks it Keş; `onFresh` clears that marker only after successful capture or already-completed exact-capability reuse. Local publication and freshness callback check the exact current recovery capability/session; final publication keeps the same guard. AccountViews remains unchanged. Cache cannot grant mutation capability.

Failure clears the scene before showing existing error UI (including non-mobile content); a 403 cannot leave previously published cached content visible. Transient transport/server failure retains AccountViews' existing complete-cache fallback. A miss or stale-revision cache waits for live data, and a miss plus failing network shows the existing error/retry. No extra request, new endpoint or altered revision bracket.

## Validation

Native Robolectric SDK34 graphics, real CityScreen + AccountViews + ReadOnlyGameCache + CitySceneRepository, synthetic transport/empty city, in-memory JournalFile.10/10 host tests PASS13s:

- Cached scene appears while first network request is blocked and zero response bytes have completed; release yields fresh scene with Keş removed, four reads total (city start/end, decorations, sync).
- Cache miss and stale revision never publish a local scene; release publishes live scene.
- Cached503 remains marked Keş; miss503 shows error.
- Cached403 clears scene, shows existing error; Yenilə retries successfully.
- Alice→Bob during blocked refresh removes Alice's cached scene, then Bob's live data publishes without the old cache marker. Production's exact capability key is reproduced by harness; it does not mount the full OnlineGame shell.
- Cached presentation rendered at320/font1,320/font2,360/font1,390/font2.

Logs `/private/tmp/wobbrik-city-local-render-final.log`, XML `/private/tmp/wobbrik-city-local-final.xml`; outputs `/private/tmp/wobbrik-city-local-first/`. All7PNG outputs opened and compared with `unified-mobile-v3/01-main.png` and `04-game-states.png`. Cache marker is legible; existing error art/retry composition retained. Fixture is empty city, so populated scene art and full shell/header/navbar parity are not proved. At320/font2 the existing lower Binaya bax text clips at the scroll viewport; main disabled Bax remains visible. This layout debt was observed, not fixed in the cache unit. No new palette/layout values. GameTokens Text/Surface13.00, Muted/Surface8.22, Accent/Surface8.99 PASS.

Initial host attempt failed only on an incorrect retry label in the harness (Yenidən yoxla vs actual Yenilə); corrected. An additional same-UID re-login host experiment timed out waiting for the second transport invocation, even with explicit sign-out/unmount. Root cause is unresolved; removed from the retained focused suite and NOT counted as PASS. Failure XML `/private/tmp/wobbrik-city-local-same-uid-timeout.xml`. Same-UID remount needs separate investigation; generic existing AccountViews epoch-rejection tests do not prove that route scenario.

Native host elapsed values include mounting/art decode/Compose scheduling, not production latency; captured scene was visible before response completion. This unit does not reduce request/byte volume or claim to resolve real2+s/10+s delays. Real Firebase/server/device, filesystem timings, insets, keyboard, TalkBack and locale matrix not run.

Backend: scheduling-only callback integration uses unchanged owner-scoped city/building/decorations/sync contract audited in `2026-11-17-city-read-scheduling.md`; no AppBackend API/domain/schema changes necessary, backend tests not run. Local diff review checks publication guards, cache marker transitions, error clearing, cancellation and existing root capability key. No new authority or journal.

Final normal `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS; log `/private/tmp/wobbrik-city-local-checks.log`. Core141/app28 tests; native harness is opt-in only. `git diff --check` PASS. No CI.

## Same-UID investigation follow-up

The later narrow investigation reproduced the timeout and resolved host synchronization with an explicit post-state-write `waitForIdle()`. A same-UID regression is now retained; full suite11/11PASS. Production CityScreen/AccountViews unchanged. Evidence and limits: `2026-11-17-city-same-uid-host.md`. The original failed experiment above remains a historical failure.
