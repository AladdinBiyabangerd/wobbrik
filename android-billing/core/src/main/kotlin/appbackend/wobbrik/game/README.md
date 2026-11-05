# Attempt recovery core

This is the transport-independent recovery unit, not a connected Android host.
`AttemptCoordinator` shares the existing UID/login-epoch session boundary and
serializes start/checkpoint/finish submissions. Persist the exact command before
I/O. An uncertain response retains that command, including its original revision;
recovery submits it again using the server's operation deduplication. It never
treats a missing operation receipt as permission to create a replacement command.
A definitive revision conflict discards the rejected command, refreshes state and
reports the conflict to the caller. Historical receipts are followed by sync;
equal-revision wallet refreshes replace the balance, never add a delta.

Required Android adapters are still absent:

- An atomic, durable, per-UID `AttemptJournal` (exclude from device backup).
  Save must reject overwriting a pending command; remove must compare the exact
  command. Keep pending commands across sign-out, without exposing them to another
  UID. Only one coordinator/journal writer may own the host session.
- An authenticated `AttemptBackend` with strict DTO/envelope serialization,
  bounded HTTP, fresh Firebase tokens and exact error-code mapping. Construct
  command metadata and its envelope together; they must describe the same command.
  Only the explicit `revision_conflict` code maps to `RevisionConflict`.
- Complete catalog/account/attempt DTOs; `GameSnapshot` here is only the recovery
  projection, not the full account model or a complete domain adapter.
- Versioned engine checkpoint codec and validation, Compose input gating while
  `ready()` is false, lifecycle recovery, and account/auth bootstrap.

The current PracticeEngine is not yet bound to these contracts. No local coin,
XP, building ownership or checkpoint is promoted to authoritative server state.
The API has no abandon endpoint: do not reinterpret pause/restart/menu as finish
or grant a reward. Resolve that lifecycle contract before wiring those actions.

Reference: AppBackend `docs/wobbrik-game-api.md`, Wobbrik
`docs/kotlin-compose-plan.md` section 5. Backend contract/schema/rules are unchanged.
