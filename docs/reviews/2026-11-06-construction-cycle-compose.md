# Construction-cycle Compose alignment + FPS — 6 November 2026

Branch: `feat/construction-cycle-compose`, based on `main` (`9945b76`).
Reviewed scope: GameScene framing/guide/cables, GameTokens HUD/pause polish,
FrameTiming helper, PracticeGame host loop, debug overlay, captures and contrast.
No AppBackend / online 3.4 changes.

## Changes

- Framing: dropped the 50u horizontal gutter and tightened the vertical span
  (`455f`) so the tower/crane read closer to game-tap-v2 hero scale. Physics
  units (`PracticeRulesV1`) unchanged. Crane art slightly larger; hook anchor
  nudged with the new ground placement.
- Guide: three-layer dashed Accent/Danger stroke for a soft glow without new art.
- Cables: thicker Text underlay + Muted core.
- HUD: bold score/hint (`Hint` 18sp), Medium record; pause circle uses Text-tint
  border + elevation. Pause panel: Accentsuccess primary gradient, Raised→Surface
  secondary, Accent-tint border, higher elevation. Hint hidden while paused.
- `FrameTiming`: ring buffer with avg FPS, P50/P95/P99, jank>16.7ms and >33ms.
  Records in the practice frame loop; logcat on pause; debug overlay while running.
- Captures: `docs/reviews/construction-cycle-compose/`. Contrast text pairs OK
  (`contrast.txt`).

## Visual comparison

References: `docs/design/game-tap-v2/{phone,tablet-portrait,tablet-landscape}.png`.
Inspected: phone active/pause, tablet portrait/landscape active/pause, 40-floor
crane-limit, animate-30. Phone/tablet pause panels show › Fasilə ‹, cyan
Davam et, two secondary actions. Active framing is larger than the 5 Nov asset
board; still not a pixel-identical generative concept (garden lighting / painted
sky remain sprite-limited; no new art without approval).

## Frame timing (emulator only)

Pixel_6 AVD, `-gpu swiftshader_indirect`, ~10s animate at 30 floors then HOME:

```text
frames=189 avg=19fps p50=50.0ms p95=133.3ms p99=183.3ms jank16=143 jank33=143
```

This is software-GPU emulator evidence, not a 60 FPS pass on weak/mid physical
hardware. Release-type physical measurement remains open. Harness and log tag
`WobbrikFrameTiming` are ready for that run.

## Checks

- `:app:assembleDebug` / `:app:installDebug` with Android Studio JBR — SUCCESS.
- Contrast `--pairs Text:Raised,…` all ≥4.5.
- Capture + hierarchy: pause fixture exposes Fasilə / Davam et / Yenidən başla /
  Menyuya qayıt; cyan band present in phone-pause PNG.
- Backend: visual/host-only; no API/schema/wallet change; no backend CI required.

## Remaining limitations

- Online AcceptedEngineHost wiring (project-progress 3.4) out of scope.
- Physical-device 60 Hz P95/P99, RAM, startup not measured.
- Exact concept lighting/garden still differs without new sprites.
