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

Implemented adapters (not yet connected to the host):

- `GameplayWire` builds typed start/checkpoint/finish envelopes and validates journal
  metadata against the body. `GameplayHttpBackend` uses the existing `Sessions`
  UID/login epoch and fresh token boundary; `UrlConnectionGameplayTransport` sends
  exact retained UTF-8 bodies with bounded response size, connect/read timeouts,
  HTTPS origin validation and no redirects. Only 409 `revision_conflict` rebases
  through the coordinator. Other failures retain the command.
- `DiskAttemptJournal` rejects pending overwrites and compares exact removals.
  Android `androidAttemptJournal(context)` uses AtomicFile in noBackupFilesDir,
  synced writes and durable tombstones. Keep ONE application-scoped journal and
  coordinator owner; multiple writers/processes are unsupported. Corruption fails
  closed. JVM disk tests cover recreation, not Android process/power-loss testing.

Remaining integration requirements:

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
