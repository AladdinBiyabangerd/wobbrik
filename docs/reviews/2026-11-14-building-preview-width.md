# Bina preview eni və torpaq — 14 noyabr 2026

Əvvəlki stretch nəticəsi aşağıda tarixçədir; son qəbul edilmiş yanaşma sənədin son bölməsindədir.

Branch: `fix/mobile-consistency-performance`, reviewed working diff against `4ae6f87`; no commit/merge/push/CI.

User explicitly chose full building visibility, wider preview and wider ground; active gameplay width must stay unchanged. Existing GameTokens and modular renderer were inspected first. Owned previews (garden supplied) now have an independent horizontal scale capped at3× vertical scale and constrained to available width. Ground spans260 instead of180 scene units; floor width remains100. All floors/accepted offsets and selected parts are retained; proportions intentionally widen in presentation. Low-floor previews may scale down to fit the wider ground. Picker without garden retains uniform scaling. No palette/assets/domain/API change; AppBackend change unnecessary.

Local diff review: only `buildingPreview` presentation and its named tokens change production behavior. Active game rendering, `buildingFloor`, physics and server geometry remain unchanged. Accepted coordinates are inputs only. No additional bug found in scoped review.

Checks: Java21 offline native5/5 PASS22s: dedicated1/16/40-floor straight/offset fixtures at390×844font1 and320×568font2, plus existing production screen flow at360/390font1 and320font2. Viewed before/after regular, after compact, and production360 contact. Local images: `building-width-visual/{before,after}-font{1.0,2.0}.png`; PNGs follow repository local-render ignore policy. Existing tracked production contact sheets refreshed. Ordinary `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS19s; `git diff --check` PASS. First fixture compile failed on Int/Long floor count and was corrected before passing runs.

Limitations: synthetic native host, no emulator/device/TalkBack/GPU/API24runtime/tablet certification. No claim of pixel-perfect parity or full release acceptance. Contrast palette unchanged. The user's subsequent aesthetic-retouch instruction is recorded in PLAN §7 and handoff; this diff does not smooth/rewrite offsets. Supplied attachment exactly matches existing `docs/design/city-view.png` (SHA25611ca8ffcbae85d8d3510e27b6b6cf5f7f6f1094166f36bec4c02546a62fdaf81).


## Final — təbii nisbətli nümayəndə mərtəbələr

User rejected the flattened appearance and explicitly approved40 real floors represented by6–8 natural-proportion visible floors, retaining real count in metadata. Removed horizontal stretching. Owned building previews now sample at most8floors including first/last, normalize presentation origin and cap overall offset range at30scene units. Source centers remain immutable; selected parts stay intact. This is display-only retouch, never a persistence/API transform. Empty/single-floor inputs remain safe. Garden scales uniformly from its previous180×120 reference to260×173.33, so foliage/ground are no longer flattened. Existing no-garden picker keeps its floor count and uniform proportions. Active GameScene/engine/buildingFloor unchanged.

Final native5/5PASS26s: same1/16/40straight+offset scenarios and production flow3sizes. Viewed final regular+compact images and production360contact; visible full representative building, natural window/floor/garden proportions, softened offset and true10floor metadata next to8-floor art. Actual city-view reference informs proportions/retouch; no claim of asset/pixel parity. City renderer not changed by this scoped fix.

Final ordinary app unit/APK/lint PASS16s; diffcheck PASS. Same synthetic/device/a11y/tablet limitations as above. No backend change, CI, emulator, commit, merge or push. PLAN §7, decisions mirror and handoff record the new approved policy. Previous stretch review/checks remain historical only.
