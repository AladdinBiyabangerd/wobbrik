# Game scene sprites — 5 November 2026

The user approved `floor.png`, `crane.png`, `island.png` and `roof.png` in chat
before integration. `cloud.png` follows the approved tablet landscape backdrop.
Generated with built-in image_gen, actual PNG alpha preserved. Prompts and
original paths are in `manifest.json` and `cloud-prompt.txt`.

Runtime copies: `android-billing/app/src/main/res/drawable-nodpi/game_*.png`.
No resource density scaling; `GameScene.kt` uses measured source rectangles and
anchors. No source pixels were repainted. Floor/cable/crane sprites are composed
from the same moving coordinates; settled floors are drawn back to front. The
entrance island is decorative foundation, not a rewarded or counted floor. The
pitched roof appears only after FINISHED; active and paused towers stay roofless.

Framing uses window dimensions and leaves space for the full hanging-module
travel. The crane body and upper suspension point remain fixed; only the cable
and hanging module move horizontally. There is no horizontal camera tracking. Empty air above the stack is compressed continuously
through landing; simulation, collision and checkpoint coordinates are unchanged.
These rendering transforms are not server-authoritative building geometry.

Reproduce emulator captures with an installed debug APK:

```sh
python3 scripts/capture-game-scene.py
```

`ScenePreviewActivity` exists only in `src/debug`; its frozen local fixture is
not an online attempt. Release MainActivity uses the real offline practice engine.
No server progression, wallet, checkpoint command or backend contract changes.
