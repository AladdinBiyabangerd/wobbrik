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

- Catalog HTTP loading and other domain routes; RecoveryDtos now decodes full catalog,
  account recovery, attempts and typed attempt receipts. Production sync requires all
  current recovery fields. GameSnapshot keeps account data alongside its coordinator
  projection; synthetic test backends may omit it. This is not a complete domain adapter.
- Compose input gating while
  `ready()` is false, lifecycle recovery, and account/auth bootstrap.

The current PracticeEngine is not yet bound to these contracts. No local coin,
XP, building ownership or checkpoint is promoted to authoritative server state.
The API has no abandon endpoint: do not reinterpret pause/restart/menu as finish
or grant a reward. Resolve that lifecycle contract before wiring those actions.

Reference: AppBackend `docs/wobbrik-game-api.md`, Wobbrik
`docs/PLAN.md` §2. Backend contract/schema/rules are unchanged.

EngineCheckpointCodec in :engine carries practice-checkpoint-v1 and pinned local
rules, including direction, fall/sway velocities and fractional tick units. Decode
and restore reject unsupported versions, invalid numeric/phase/floor state and
oversize JSON. decodeAccepted additionally matches server attempt counters. It is
opaque structural-v1 payload, not physics proof or trusted city geometry. The host
must explicitly pause/reset its frame clock after lifecycle restore. The 16 KiB
limit is fail-closed; unlimited-height compact encoding is not implemented. Only
strictly increasing successful-floor checkpoints are accepted by the current API;
zero-floor, pause/restart/abandon and continuation stabilization remain host work.

Host lifecycle constraints and the selected auth/recovery ownership unit are in
[host-lifecycle-contract](../../../../../../../../docs/host-lifecycle-contract.md).
Online restart/abandon remains unbound pending a product/server contract.
