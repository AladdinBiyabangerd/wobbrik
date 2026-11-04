# Practice engine foundation

Pure Kotlin module in the existing Gradle workspace. PracticeRulesV1 names provisional simulation units, not city footprints or server geometry. The host supplies monotonic elapsed time, pauses on background entry and resumes with a fresh frame timestamp. Resume and drop are separate commands.

Current scope: bounded 60 Hz stepping, moving crane, falling floor, support overlap, miss, pause/resume and restart. No currency or account effects. This is a foundation for the final engine: linked sway, balance, rescue, first-five forgiveness, difficulty, camera, persistence and Android host remain pending. Local state is not an online checkpoint or proof of rewards.

Run ./scripts/local-ci.sh from the repository root.
