# Modular purple building assembly study

- Branch `fix/mobile-consistency-performance`, base4ae6f87. This unit adds only design atlas/metadata/provenance and an opt-in visual harness. Existing uncommitted production/owner work remains intact. No commit/merge/push/CI.
- User authorization: start modular reconstruction of the approved purple house and show how it assembles. Follow-up clarified that selected inventory parts must work on a suitable new template as they already do in the current game. User agreed with that approach. No second inventory/domain system is introduced.
- Nine atlas cells: blank repeat shell, roof, blank ground shell, arched window, rectangular window, entrance, balcony, garden and a pair of lanterns. The harness samples one lantern and places it twice. Existing GameTokens and sprite primitive are reused. All numeric layout measurements in the renderer are atlas coordinates, not new UI design tokens.
- Assembly draws independent layers, projects front/side windows, stacks4or6floors with slight offsets, and swaps window layers without replacing the complete building. These are study choices, not actual inventory IDs or an accepted server building.
- Initial native render exposed roof join gap and a lamp/window collision. Adjusted roof baseline and entrance/lamp placement. Final native2/2PASS10s,6captures:390×844font1 and320×568font2, each arch/square/6floors. Inspected final arch-font1.0, square-font1.0 and six-floors-font2.0. Roof gap closed and lamp overlap resolved. DiffcheckPASS.
- Results: separate modules are feasible and visibly retain the approved purple material/volume. Repeat joints are more regular than the original bespoke reference, the cornice is visually heavy, and the new template is not pixel identical to that reference. Font2 rendering is a study page, not product layout certification. No blanket visual acceptance claim.
- Remaining: align exact catalog part mapping, wall material/roof/entrance/light replacements and mixed-set checks; integrate the proven template through the existing production selected-part renderer; show actual inventory/detail renders. Other styles are not rebuilt yet. Do not present these captures as production inventory screens.
- No AppBackend change needed: no domain/API/DTO/schema/ownership mutation. No device/emulator, CI, tablet or live account checks. Normal APK/lint not rerun because this unit changes no production code/resources; previous production results remain historical.

Run from android-billing with Java21:

```sh
./gradlew --offline -I ../docs/reviews/mobile-sizing-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*ModularBuildingStudyTest'
```

Atlas/provenance: docs/design/building-purple-v1/modular/. Captures: docs/reviews/modular-building-visual/. Test success means captures were produced; it does not mean catalog integration is complete.
