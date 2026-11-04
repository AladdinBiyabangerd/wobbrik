# Practice engine foundation

Pure Kotlin module in the existing Gradle workspace. PracticeRulesV1 names provisional simulation units, not city footprints or server geometry. The host supplies monotonic elapsed time, pauses on background entry and resumes with a fresh frame timestamp. Resume and drop are separate commands.

Current scope: bounded 60 Hz stepping, moving crane, falling floor, support overlap, miss, pause/resume and restart. No currency or account effects. This is a foundation for the final engine: camera, persistence and Android host remain pending. practice-balance-v1 adds persistent lean, danger hysteresis, one rescue per danger episode, perfect landings, a damped shared sway signal, first-five forgiveness and capped difficulty. Sway is cosmetic and cannot cause a loss or rescue; only a landing changes balance. No physical validation or device balancing is claimed. Local state is not an online checkpoint or proof of rewards.

Run ./scripts/local-ci.sh from the repository root.
