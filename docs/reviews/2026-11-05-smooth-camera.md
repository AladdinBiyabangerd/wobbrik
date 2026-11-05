# Smooth horizontal camera — 5 November 2026

Branch: `fix/smooth-camera-follow`; reviewed diff: implementation `1f69672` (reviewed before merge).

User requested removing the abrupt camera transition. Presentation-only horizontal
following now uses the existing vertical camera exponential response (7/s, about
95% in 0.43s), without overshoot. Restore/reset initializes directly to the target.
Authoritative crane origin is separate from the eased camera: hanging and falling
modules share world coordinates, including release before the pan completes.
The crane body/hook remains fixed; suspension endpoints follow the module during
the brief recentering. Finished scenes keep rendering until camera settling ends.
No engine rules, checkpoint format, swing range/speed or rewards change.

Local source review covered initialization, restart, pause/resume, release during
pan, legacy origin zero and finished-frame settling. No unresolved source finding.
GameTokens and approved reference review inspected; no art/palette/type/layout
change. Contrast recheck not applicable because no color pair changed.

Branch assembleDebug passed (9s, 66 tasks, 4 executed); initial invocation failed
because ANDROID_HOME was unset, then rerun with the SDK path. No branch CI run.
Phone 1080x2400/420dpi and tablet landscape 1920x1200/240dpi fixtures at
0/6/30 ticks captured and visually inspected alongside approved game-tap-v2
references (comparison.jpg). Tower recenters progressively; hook/HUD stay fixed.
Tick 0 is an initialized old-origin baseline; ticks 6/30 use the new origin and
also advance swing. These are deterministic frame samples, not a live landing
video or physical-device timing check. Existing art/framing/background/design
parity differences remain, as in the previous camera review; no new asset change.
Other orientations/large fonts were not newly rendered. Device size/density restored.
First emulator disappeared; cold restart initially lacked package service and UI
hierarchy, then installation and all six marker-verified captures succeeded.
Main CI after implementation merge passed: 40s, 106 tasks, 9 executed / 97
up-to-date. Engine regression tests were up-to-date, not freshly executed.
Validation documentation/evidence reviewed locally; main CI repeated after merge.

AppBackend attempts.go and game API contract checked: camera is presentation-only,
not serialized. No backend API/DTO/schema/migration or test change needed.
Visible app remains offline practice; broader approved design parity and online
integration remain unfinished. No physical-device, remote push/deploy or Console work.
