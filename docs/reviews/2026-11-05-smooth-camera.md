# Smooth horizontal camera — 5 November 2026

Branch: `fix/smooth-camera-follow`; reviewed diff: branch base through implementation commit (see git history).

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
Visual verification and main CI: pending, to be recorded after execution.

AppBackend attempts.go and game API contract checked: camera is presentation-only,
not serialized. No backend API/DTO/schema/migration or test change needed.
Visible app remains offline practice; broader approved design parity and online
integration remain unfinished. No physical-device, remote push/deploy or Console work.
