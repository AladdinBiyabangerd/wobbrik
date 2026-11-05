# Compose practice host

`./gradlew :app:installDebug` builds the offline game (`com.aladdin.wobbrik`).
Minimum API 24; target/compile API 36. `./scripts/local-ci.sh` at repository root
includes app assembly and Android lint alongside engine/billing checks.

The Canvas reads engine snapshots in its draw phase. A monotonic frame loop
feeds the engine's bounded fixed-step accumulator. HUD state changes only on
floor/phase changes. Scene taps call `drop`; pause, overlays and lifecycle stop
input and simulation. Resume resets the frame baseline. A ViewModel retains the
attempt during activity recreation; process death starts a fresh practice.

The camera follows the tower top and culls floors below the viewport. The active
HUD has only pause and noninteractive counters; pause has resume/restart/menu.
The small menu is a practice entry point, not the future full product menu.
The palette is ported from mobile-v1, and composition/input follows game-tap-v2.
Canvas buildings are schematic artwork, not extracted concept PNG assets.

No account, rewards, XP, wallet, owned building, checkpoint API or online claim
is created. Best score is session/saved-instance practice state, not a durable
account record. Process restore, audio/haptics, final illustration assets,
TalkBack device validation and real-device performance measurements remain.
