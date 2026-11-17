# PLAN §1.1 — İnventar/Bazar vizual davamı və Ərazi girişinin çıxarılması

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`; scoped working diff, owner işi saxlanır. Stage/commit/merge/push/deploy/CI yoxdur. Əvvəlki native port təkrarlanmadı.

## Son qərarlar və dəyişiklik

İstifadəçi əvvəl Bazar, sonra İnventar daxilindən Ərazi hissəsini çıxarmağı tapşırdı. Mobil Bazar yalnız Binalar/Dekor, İnventar Binalar/Görünüş/Dekor göstərir. Sahiblikdən genişlənmə alışına keçid çıxarıldı; mobil `land` alış dispatch-i bağlandı. Yeni yer uydurulmur. Server hüquqları, katalog, wallet və alış qaydaları silinmir. Əvvəlki legacy/tablet yolları bu mobil vahiddə yenidən qurulmayıb; onların renderi yoxlanmayıb.

`CommerceStatus` read-only native seal-dir: yerləşdirilmiş dekor outlined cyan, aktiv satış filled cyan nişanı alır; klik semantikası yoxdur. Saxlanılan dekor native inventar ikonu ilə göstərilir. Siyahı qiyməti coin+rəqəm kompozisiyasına keçirildi. Mövcud token/palette istifadə olunur, yeni rəng/ölçü/asset/dependency yoxdur. Ərazi kartına bu turda sınaq olaraq edilən refinements, həmin hissə menyudan çıxarıldığı üçün saxlanılmadı.

Authority `unified-mobile-v3/{README.md,01-main.png,02-market.png,05-owned-and-listings.png}` və son Ərazi qərarıdır. PLAN§1.1, AGENTS və visual-work güzgüsü yeniləndi. Backend request/DTO/schema/domain dəyişmir; AppBackend dəyişikliyi tələb olunmur. Əvvəlki optional decoration-position parse işi saxlanır.

## Yoxlama

Son production source ilə debug/mockDebug APK + lintDebug PASS1m13s. Kontrast7/7 PASS, minimum7.66; diffcheck PASS. İlk render turları son Ərazi qərarından əvvəldir və final sayılmır. Ərazi çıxarılandan sonra commerce5/5 PASS; shell3 test yalnız köhnə `plays=11` gözləntisi ilə FAIL oldu: bir `dock()` route-u silindiyi üçün düzgün say10-dur. Test düzəldildi; final commerce5+shell4=9/9 PASS1m13s, failure/error/skipped0. Son dəyişiklik yalnız test sayıdır; production APK/lint bundan əvvəl son production source ilə keçib. Final PNG-lər `/private/tmp/wobbrik-commerce-no-land-final`, seçilmiş9PNG `commerce-visual-followup/` altındadır.

## Lokal review və qalan fərqlər

Status helper commerce feature daxilindədir; data/repository/mutation/session/revision/cancel axınları dəyişmir. Disabled placement qorunur. Tab siyahısından Ərazi çıxarıldı, inventarın genişlənmə CTA-sı yoxdur. Owner source/baseline işinə reset/stash/stage edilmədi.

Native screenshot-lar açılıb müqayisə edildi: normal İnventar/Bazar, kiçik font2 İnventar, inset+uzun balans öz elanları, dekor statusu. Font2 uzun etiketlər horizontal, body vertical scroll saxlayır; state capture-ları bəzən əvvəlki test scroll mövqeyindədir. Tam ekran screenshot pariteti iddia edilmir.

Bu vahid bütün PLAN§1.1 fərqlərini bağlamır: bina/dekor və Profil artwork-ləri reference-dəki poza/materialla eyni deyil, native relyef/işıqlanma və digər ekranların tam kompozisiya pariteti açıqdır. Artıq çıxarılmış Ərazi dizaynı tamamlanmalı ekran sayılmır; yeni yeri istifadəçi təyin edəcək. Device/account E2E, real IME/inset/TalkBack/API24/GPU/performance/locale/tablet yoxlanmayıb. Emulator açılmayıb; mərhələ qəbulu deyil.
