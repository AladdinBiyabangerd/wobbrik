# Finish cosmetics integration — 8 November 2026

Branch: `feat/finish-cosmetics`, rooted at `6e22f69`, retaining restore fix
`e0af061`. Asset commit: `17f8201`. Reviewed the complete local integration diff: core catalog/selection,
engine host/draft, Android storage/UI/application binding, atlas metadata and
assets, regressions and these plan/lifecycle/handoff updates. No merge, push,
deploy, Console change or branch CI. Live tests are explicitly user-owned.

## Behavior and boundary review

- Authenticated GET catalog pins `rules_version=wobbrik-v1`; response also checks
  `wobbrik-structural-v1`. Catalog and current UID are published with the exact
  Ready capability only after recovery drains the existing wire journal. Delayed
  catalog responses cannot cross an account or same-UID auth epoch.
- Five categories, free implicit, otherwise current account `unlocks` only.
  Decorations, prices, grants and purchase/reward actions are excluded. Atlas
  metadata supplies pixel locations, never inventory or permissions. Unknown art
  is reported and omitted rather than replaced with placeholders. Confirmation
  requires art for the selected IDs; additive unknown locked items do not block
  finishing with supported parts.
- Removed automatic base_6 finish. Terminal accepted checkpoints open the picker;
  confirm revalidates all five IDs, and only then calls the existing typed finish
  command. Back/menu retains the attempt. Waiting/recovery/error lock physics.
- Synchronous app-private SharedPreferences draft is keyed by SHA-256 of UID and
  attempt ID, with a codec version, terminal physics and selected IDs. It contains
  no credentials, rewards or wire command and never replaces AttemptJournal.
  A no-added-floor terminal miss restores only when version/rules, full accepted
  floor prefix and precise/rescue counters match. Stale progress is ignored.
  Failed draft writes keep the previous selection and block finish dispatch.
- Exact operation ID, revision, route and envelope still belong to the existing
  AtomicFile journal/coordinator. Pending finish (including old base_6 envelopes)
  drains before new selection. Lost response retry never rebuilds selected parts.
  Definitive revision conflict still requires a fresh explicit decision.
- A crash before an additional-floor checkpoint reaches the wire journal may
  discard that unaccepted floor and restore the accepted boundary. Local draft
  is intentionally not promoted to server progress. No attempt reset/abandon.

AppBackend inspected read-only: `internal/wobbrik/catalog-v1.json`, `finish.go`,
`docs/wobbrik-game-api.md`. All 68 cosmetic ID/category pairs match; 12 decorations
are excluded. Existing finish already checks the exact five categories, UID
ownership/free status, accepted sequence, revision and deduplication. No API,
DTO, rule version, migration, wallet/ledger or backend code change is required.
No backend test PASS is inferred from the mobile checks.

## Visual and asset review

Compared actual build renders against
`docs/design/finish-cosmetics-v1/phone-tablet.png` and its approved README:
phone has preview, five scrolling category tabs, two-column scrolling cards and
Back/Confirm; wide tablet has left preview, right three-column grid and footer.
A narrow landscape phone uses the same split with two columns. All colours,
spacing, radii and type reuse GameTokens, whose source is mobile-v1/tokens.css.
Cards expose server names to accessibility, selected semantics and disabled
locked state; controls meet the existing 48dp minimum.

Found/fixed during review: original diamond roofs did not join facade camera;
user authorized compatible variants and built-in imagegen generated a versioned
14-roof atlas with front rectangular eaves. Full PNGs remain in design sources;
only five lossless WebP atlases and verified crop metadata ship. Facade/roof join
now uses a shared eave baseline. Initial phone preview was too narrow, cards too
large, and landscape stacking left too little grid space; reviewed renders drove
compact cards, joined courses and split composition at the existing 600dp token.

These are the prepared modular illustrations, not the concept's literal flower
island/full decorative tower. Preview uses at most six representative courses
and the real terminal floor-count label; it neither changes nor claims to replay
accepted physics/geometry. Unlocked status reuses the existing success colour
and an explicit label rather than adding a new gold token. Full pixel-for-pixel
concept parity/visual acceptance is NOT claimed; those differences need the
user's visual review. All 463,736 combinations were not individually rendered.

Final source/runtime art: `docs/design/finish-cosmetics-v1/assets/roof-atlas-v2.png`,
`assets/prompts.json` (`roof-v2` exact built-in prompt), and
`android-billing/app/src/main/assets/finish/`. Original sources remain unchanged.
68 nonempty alpha-body crops checked; runtime map exactly matches server cosmetic
IDs/categories. Native dark-background QA and runtime alpha edges inspected.
WebP encoded with cwebp 1.6.0 `-lossless -exact -m 6`; dwebp output compared with
source RGBA byte-for-byte for all five 1254×1254 atlases: PASS.

Runtime image payload is 6,279,700 bytes (5.99 MiB), 33.4% smaller than its PNG
sources. APK ZIP art/metadata occupies about 6.28 MB. Decoded ARGB pixels alone
are 31,450,320 bytes (30.0 MiB); this is arithmetic, not measured total heap/GPU
use or a performance acceptance result.

## Checks and remaining limits

- `:core:test` PASS: 51 tests, no failures/errors/skips.
- `:engine:test` PASS: 55 tests, no failures/errors/skips. Regressions cover explicit
  confirmation, terminal server recovery, zero-floor miss/draft restoration,
  account isolation/stale capability, locked parts, failed storage, stale local
  progress and identical finish envelope after lost response/disk recovery.
- `:app:assembleDebug` and `:app:lintDebug` PASS after final Kotlin changes. Lint
  has warnings (including stylistic UseKtx for deliberately synchronous commit),
  no errors. No new pins/framework or aggregate CI task.
- Actual UI renders use an ignored isolated debug activity with explicit fixture
  data in a private read-only emulator on port 5580. No shared emulator-5554,
  real account, attempt journal, server process or live DB was controlled.
  Fixture source/manifest are not committed; final APK DEX excludes the activity.
- Render matrix in `finish-cosmetics/`: phone 1080×2400 @420dpi and 2400×1080;
  tablet 1600×2560 @320dpi and 2560×1600; portrait phone and wide tablet also at
  font scale 1.3. Safe drawing insets and scrolling checked. Screenshots are UI
  fixtures, NOT proof of live catalog/finish/retry/process-death behavior.
- Existing token contrast checker with actual foreground/background pairs PASS:
  Text on Background/Surface/Raised, Muted on Background, Success on Surface,
  Background on Accent all exceed 4.5:1. Generic auto-role detection also compares
  decorative Raised to surfaces and reports low ratios; no palette was changed.
- `git diff --check` PASS. No live network/gameplay tests, physical-device proof,
  TalkBack session, font scales above 1.3, every window size or full combination
  matrix tested. No editable field/keyboard flow was added. Azerbaijani labels
  follow the existing app; no new locale list was invented.
- Local test APK uses the existing ignored localhost TLS overlay and configured
  `https://localhost:18443`, not a new endpoint or release trust relaxation.
  User must test live finish, back/return, process death before confirm, pending
  response-loss retry, old journals, current-account unlocks and reward dedup.
  Stage 3 and visual acceptance remain open; next stage is not authorized.
