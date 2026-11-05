# Practice engine foundation

Pure Kotlin module in the existing Gradle workspace. PracticeRulesV1 names provisional simulation units, not city footprints or server geometry. The host supplies monotonic elapsed time, pauses on background entry and resumes with a fresh frame timestamp. Resume and drop are separate commands.

Current scope: bounded 60 Hz stepping, moving crane, falling floor, support overlap, miss, pause/resume and restart. No currency or account effects. This is a foundation for the final engine: camera, persistence and Android host remain pending. practice-balance-v1 adds persistent lean, danger hysteresis, one rescue per danger episode, perfect landings, a damped shared sway signal, first-five forgiveness and capped difficulty. Sway is cosmetic and cannot cause a loss or rescue; only a landing changes balance. No physical validation or device balancing is claimed. Local state is not an online checkpoint or proof of rewards.

Run ./scripts/local-ci.sh from the repository root.

EngineCheckpointCodec provides versioned bounded JSON roundtrip of public and hidden
simulation state. PracticeEngine.restore validates before mutation. decodeAccepted
matches floors/perfect/rescue counters from a server attempt. This is local recovery,
not server physics verification or online host binding. Call pause and reset the host
frame timestamp for lifecycle recovery; do not pass background elapsed time. Snapshots
over the server's 16 KiB limit fail rather than truncate floors.


## Motion revision (5 November 2026)

New attempts use inner checkpoint rules `practice-pendulum-v2`: crane x follows a
smooth sinusoidal pendulum phase, slowing continuously at each turn. Drop x is
still the actual engine x; presentation never substitutes a different horizontal
aiming position. Step rate, range, gravity, overlap, balance, scoring and difficulty
are unchanged. The slower ends increase the swing period versus v1; this is an
intentional timing change requiring device playtesting.

`practice-checkpoint-v1` accepts both inner rule IDs. Restoring
`practice-balance-v1` retains its exact constant-speed trajectory and re-encodes
that rule ID; restart creates a new v2 attempt. Unknown rule IDs still fail closed.
An older v1-only client cannot resume a v2 snapshot and must retain its existing
unsupported-checkpoint barrier rather than replaying it as v1.

The server's outer `wobbrik-v1` / `wobbrik-structural-v1` envelope and <=16 KiB
opaque JSON contract do not change. Revision, sequence, dedup, UID/login epoch,
wallet and accepted-restore gates are unchanged. AppBackend `attempts.go` checks
opaque JSON and structural counters, not the inner trajectory; no backend code,
DTO or migration is required. This does not add server physics verification or
unlock the unfinished online game host.

Android presentation adds fixed center-suspension arc, suspended-load angular
inertia, a connected stack rotating about its foundation with impact-driven damped
motion, and smoothed camera following. These visual springs are not persisted and
cannot change collision or rewards. They freeze on pause and reset on restart;
process recreation resumes the authoritative/local checkpoint with fresh visual
springs. This remains a stylized 2D single-body presentation, not a validated
structural or full rigid-body simulation (release has no horizontal ballistic drift).

## Horizontal camera revision (5 November 2026)

New attempts/restart now use `practice-camera-v3`. The horizontal camera and the
fixed-screen crane share a world origin at the last landed floor's center (zero
before the first landing). `craneX` remains the local pendulum displacement with
the same +/-120 range and 155-unit presentation suspension. Drop stores the world
x `cameraX + craneX`; overlap and balance use actual world-space floor positions.
The view subtracts exactly that origin from the island, stack, guide and falling
module. Recentring is atomic on landing, with no independent horizontal view lag;
vertical camera smoothing remains. The crane/HUD/background do not pan.

World floor x can exceed the former +/-120 boundary, while every adjacent floor
must still overlap. Both sides can still miss or reach the existing balance-loss
threshold; camera tracking is not a new stability model. This does not add a
mass-distribution solver or collapse animation.

The JSON keys and outer checkpoint/structural-v1 contracts are unchanged. The
inner rule ID distinguishes local swing x from world falling/floor x. Both
`practice-balance-v1` and `practice-pendulum-v2` restore and re-encode their original
fixed-origin behavior; old clients reject unknown v3 rules rather than replaying
with incorrect coordinates. No wallet/reward/domain changes or online-host unlock.
AppBackend `internal/wobbrik/attempts.go` and `docs/wobbrik-game-api.md` were checked:
the server still carries opaque valid JSON <=16 KiB and authoritative counters;
no backend code, DTO, migration or test change is required for this revision.

## Level swing speed revision (5 November 2026)

New attempts/restart use `practice-speed-v4`. In-attempt level is the upcoming
floor number (landed count + 1), not backend account XP level. Swing phase speed
is base speed times `1 + min(landed floors, 25) * 0.08`: 1x at floor 1, 1.8x at
floor 11, 3x at floor 26 and above. Only the SWINGING phase uses this multiplier;
range, suspension, gravity, balance difficulty, camera and perfect scoring retain
their previous rules. Speed advances after a successful landing, not while waiting.

Restoring v1/v2/v3 preserves each original speed curve and original camera mode;
v3 and v4 both follow the tower. Unknown inner rule IDs still fail closed. JSON
keys, opaque <=16 KiB outer structural-v1 snapshots, server counters, retry/dedup,
account isolation and wallet remain unchanged. AppBackend attempts.go was checked;
no backend DTO/schema/migration or code change is required. This does not connect
the unfinished accepted online host or make local state authoritative rewards.
