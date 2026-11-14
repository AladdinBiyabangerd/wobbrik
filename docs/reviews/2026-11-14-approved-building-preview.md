# Approved purple building in inventory/detail

- Branch: `fix/mobile-consistency-performance`; reviewed working diff against `4ae6f87`, with existing owner changes retained. No commit/merge/push/CI.
- Scope: InventoryScreen.kt, approved art resource/design provenance, opt-in host output destination, PLAN/decision mirrors/handoff. Older BuildingArt/GameTokens changes predate this unit and remain uncommitted.
- User approved the generated purple building and requested its application. Production BuildingPreview uses the transparent asset with ContentScale.Fit for complete base_1 sets on owned buildings with at least10floors and available geometry/assets. Its fixed representative silhouette is deliberate; real floors and server geometry are not rewritten. Other cosmetic selections retain the modular renderer instead of being hidden by one baked illustration.
- Local review: checked eligibility guard, missing-art/geometry behavior, real floor accessibility metadata, shared inventory/detail/parts-edit caller, aspect-ratio fit, unchanged gameplay/city/domain. No additional source issue found in this scoped diff. AppBackend change unnecessary: no API/domain/schema changes.
- Native host: CompactFlowRenderTest3/3PASS25s,360×640font1,390×844font1,320×568font2. Real production composables; synthetic HTTP/account fixtures, not device/real-account E2E. Captures under `approved-building-visual/`. Looked at390inventory/detail and320font2detail; approved roof/balconies/entrance/grass are visible and unclipped. Large-font metadata remains secondary scroll content; horizontal tabs retain existing scroll behavior.
- Normal build: app unit24/24,assembleDebug,lintDebugPASS13s. DiffcheckPASS. Palette/typography/layout tokens unchanged; no new contrast pair.
- Limits: only this one complete cosmetic set has the approved new art. Other/mixed sets, city, dynamic silhouette and full catalog visual parity remain open. No device/emulator/API24runtime/TalkBack/GPU/tablet validation. The previous maximum8floor renderer passing checks did not constitute user visual acceptance; this approved illustration supersedes it only for the guarded set.

Reproduce from android-billing with Java21:

```sh
./gradlew --offline -Dwobbrik.render.output='/Users/mac/My Workspace/Wobbrik/docs/reviews/approved-building-visual' -I ../docs/reviews/mobile-sizing-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*CompactFlowRenderTest'
./gradlew --offline :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
