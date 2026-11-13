# Mərhələ 4 — bina silmə adapteri

- Branch `feat/phase-4`, baza HEAD `09185dc`; əvvəlki working diff və istifadəçi `app/release/` faylları qorunur. Commit/merge/push yoxdur.
- Son istifadəçi qərarı: şəhərə yerləşdirmə bəndi bağlı sayılır; qalan işlərə davam edilir. Bağlanma istifadəçi qərarıdır, implementasiya/test PASS iddiası deyil. `docs/PLAN.md` §1 yeniləndi.
- `BuildingDeletion.confirmedDelete` yalnız exact Ready ilə mövcud owner/coordinator/journal üzərindən işləyir. `BUILDING_DELETE` → `buildings/delete`; operation ID/account revision/building revision və bütün envelope diskdə saxlanır, unknown outcome eyni sorğu ilə retry edilir. Yeni outbox və wallet yoxdur.
- Receipt bina ID-si, artmış account/content revision, sıfır price/fee tələb edir; nəticədən sonra mövcud sync authority-si saxlanır. Aktiv elan/building conflict/not-found/invalid request definitive rəddləri mövcud reconciliation yolundadır. Revision conflict avtomatik rebase edilmir.
- Backend `docs/wobbrik-game-api.md` silmə müqaviləsi və `internal/wobbrik/market.go:DeleteBuilding` read-only yoxlandı: tombstone, city membership removal, content revision +1, price/fee 0. Backend yazısı, müqavilə/schema dəyişikliyi, test və deploy yoxdur; bu adapter üçün backend dəyişikliyi lazım deyil.
- Lokal review: route exhaustive branch-ləri və historical fixture yeniləndi; historical wire yeni əmri rədd edir. Mövcud same-UID stale Ready testinə deletion rəddi əlavə edildi. Yeni 3 regression: lost response + recreated disk journal/exact retry/account isolation/no wallet delta; receipt identity/revision/refund/extra fields; definitive-vs-ambiguous errors. UI-dan real silmə edilmir.
- Java 21: `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` BUILD SUCCESSFUL (1m1s; 29 executed/79 up-to-date). Son test əlavələrindən sonra `:core:test` yenidən BUILD SUCCESSFUL (4s, compileTestKotlin/test executed). `git diff --check` PASS.
- Silmə UI-si hələ qoşulmayıb: təsdiqli `04-building-detail.png`-də delete təsdiqi yoxdur. `ui-craft` oxundu, `GameTokens.kt` tapıldı; yeni token yoxdur. Hazırlanan confirmation nümunəsi yalnız təklifdir, Android renderi deyil; istifadəçi konkret UI-ni təsdiqləməlidir. Kontrast Text/Surface 13.00, Muted/Surface 8.22, Background/Danger 10.67 PASS.
- Qalan: silmə girişinin və confirmation görünüşünün təsdiqi → UI integration → uyğun fokuslu yoxlamalar; dekor/torpaq və real şəhər renderer-i sonrakı vahidlərdir. Android runtime/render/emulator/CI yoxdur. Canlı yoxlama gate deyil.

- Confirmation təklifi `docs/design/city-completion-v1/delete-confirmation-proposal.png` AppKit ilə mövcud təsdiqli detail fonu və GameTokens palette/scale əsasında render edildi və görüntüsünə baxıldı. Mətn/düymələr bu nümunədə görünür; bu, Android adaptiv/a11y yoxlaması deyil. Nümunə hələ təsdiqlənməyib.


## Təsdiqdən sonra UI inteqrasiyası
- 13 noyabr istifadəçi “okeydir” ilə nümunəni və detail sonundakı girişi təsdiqlədi. `InventoryScreen`-də explicit confirmation qoşuldu. Dismiss və Ləğv et yalnız dialoqu bağlayır; təsdiq exact Ready ilə `BuildingDeletion.confirmedDelete` çağırır. Submit true olduqda seçim/cursor sıfırlanır; owner Ready-ni revoke edir, yeni Ready ilə inventory destination serverdən ilk səhifəni oxuyur. Commit sübutu olmadan lokal inventory və wallet dəyişdirilmir.
- Köhnə Ready və bina seçimi dəyişdikdə confirmation namizədi atılır. Mövcud listing conflict statusu server rəddini bildirir; elan geri götürmə mərhələ 5-in işidir. Böyük şriftdə dialoq düymələri şaqulidir; izahat scroll sahəsidir. Yeni token yoxdur.
- Lokal diff review: təsdiqsiz dispatch, stale capability və optimistic deletion yolları yoxlandı. Kontrast Text/Surface13.00, Text/Raised10.24, Muted/Surface8.22, Background/Danger10.67 PASS. Android runtime/render edilmədi; yalnız təsdiqli dizayn nümunəsinə baxılmışdı. Uyğunluq və adaptiv davranışın runtime PASS iddiası yoxdur.

- Son UI kodunda Checks performed: Java21 `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` BUILD SUCCESSFUL (52s; 15 executed,91 up-to-date), `git diff --check` PASS. CI/merge/push yoxdur.
