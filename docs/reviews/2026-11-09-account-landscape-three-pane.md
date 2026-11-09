# Account page three-pane landscape (design C2) — local review (9 November 2026)

Branch `feat/account-landscape-three-pane` from main `f53eab3`; merges the approved design branch
`design/account-landscape-actions` (C2 approval `28d7602`). `docs/room-cache-main-ci` stays unmerged.

## Change
- `GameTokens.CompactHeight = 480.dp` (window height class boundary).
- `AccountScreen`: when the window is wide and shorter than 480dp, three equal panes with 16dp gaps:
  hero (page title + illustration) | info (name, card, link/count/attempt text) | actions. Info/action
  panes are vertically centred and scroll only when content is taller than the pane.
- In the action pane, Kolleksiya and Nailiyyətlər are stacked full width (8dp apart) so no label wraps;
  the status text is centred there only (it wraps at a word boundary on long statuses).
- Other layouts call the same content in the same order inside the old single column; no new art,
  colour, icon or copy.

## Renders (emulator-5586, AccountPreviewActivity; compared with c2 mockups)
- Phone landscape 2400×1080@420 (≈914×411dp): live (Oyna, Kolleksiya, Nailiyyətlər, Ana ekran),
  offline (status, Yenilə, Ana ekran), recovering (two-line centred status + spinner), achievements:
  all fit without scrolling, labels on one line, buttons 278×48dp, matches C2.
- Unchanged check: phone portrait live/offline and tablet 1280×800@160 landscape/portrait offline are
  pixel-identical (0 px outside system bars) to the pre-change renders in the design `source/`.
  Tablet live renders viewed (same layout as before).
- Small phone landscape 1280×720@320 (640×360dp): three panes, labels on one line, but the action pane
  is ~2dp too short: "Ana ekran" is below the fold (reachable by scroll); card text
  "Hələ nailiyyət yoxdur" wraps at a word boundary.
- Font scale 2.0 phone landscape: no button label wraps (one line, 51–53dp buttons); live "Ana ekran" is
  partly below the fold and the card scrolls; offline fits.
- Safe drawing insets respected (content within status/navigation bars); all buttons ≥ 48dp tall.

## Checks
`:app:assembleDebug`, `:app:lintDebug` (0 errors, 17 warnings, unchanged), `:core:test` 70 + `:engine:test` 55,
0 failures; app has no unit tests. No branch CI. `git diff --check` clean.

## Open / not verified
- Small-phone and font 2.0 overflow need a decision (see report options); not changed without approval.
- Google link block (guest + configured only) lands in the info pane; not rendered (preview has no link).
- No real-device render; only emulator API 34.

## Update 9 November — v3 responsive tier and side-menu Home (`e55f7b2`, `3d5e998`)
User decisions: v3 renders approved ("bunlar okeydir implement edə bilərsən"); Home layouts with the side menu
show only Oyna in the centre ("Yan menyu olan bütün ekranlarda çıxart, planşet landşaft da daxil").
Rule followed: responsive sizing rule (branch `docs/responsive-rule`, `f70da31`, not merged yet).
- `e55f7b2` (v3, applied unchanged from the approved mockup patch):
  - Account three-pane: header row removed; logo at the top of the hero pane. Compact tier: 16dp outer
    padding, 18sp (Hint) titles, tighter pane/card spacing.
  - Home, wide and shorter than 480dp: own layout (`ShortHome`). Logo + coin above the art, controls start
    at the top of the right pane (0.8 : 1.2 weights), full-height side menu with items sized from height
    (40–56dp), heading 18sp, Oyna label 24sp.
- `3d5e998`:
  - Home with side menu (tablet portrait, tablet landscape, short landscape): centre Kolleksiya/Nailiyyətlər
    removed; Oyna (and Şəhərim on tablet) remain. Phone portrait (no side menu) keeps both buttons.
  - Tablet portrait: the illustration takes only the height the controls leave; controls scroll only past
    65% of the window height. Fixes the old cut-off at 800×1280.
  - Note: the side menu has Nailiyyətlər but not Kolleksiya; on side-menu layouts Kolleksiya is reachable from
    Hesab (account page) only.

### Renders (emulator-5586, all viewed; `/workspace/yenile-design/v4-*.png` on the agent box)
- Home: phone/small/font 2.0 landscape, phone landscape offline, tablet portrait 1.0/2.0, tablet landscape
  1.0/2.0 — everything visible without scrolling, no label wraps.
- Account: phone/small/font 2.0 landscape live+offline fit; font 2.0 card text wraps at a word boundary.
- Pixel diff outside the status bar = 0: Home phone portrait; account phone portrait and tablet landscape
  (vs pre-change); all six account landscape renders and Home phone landscape offline (vs approved v3).
  Expected differences only where Kolleksiya/Nailiyyətlər were removed (Home tablet landscape, Home phone landscape).

### Checks
`:app:assembleDebug`, `test` (core 70 + engine 55, 0 failures), `:app:lintDebug` 0 errors / 17 warnings.

### Open
- Kolleksiya has no side-menu entry (design decision for the user).
- Emulator 5586 was stopped twice during rendering by something outside the render scripts (one qemu gRPC
  abort, one clean shutdown); renders were recaptured and every PNG checked non-empty.
