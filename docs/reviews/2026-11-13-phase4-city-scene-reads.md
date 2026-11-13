# Şəhər renderer-i — server səhnəsinin oxu təməli

- Branch: `feat/phase-4`, baza `bb02b50`; review edilən diff: `CitySceneReads.kt`, `CitySceneReadsTest.kt`, `WobbrikApplication.kt` və bu işin sənədləri. Commit/merge/push edilməyib.
- İstifadəçi göstərilmiş `docs/design/city-view.png` şəhər görünüşünü və V1 canlılığı “basla” ilə təsdiqlədi. Şəhərdə istifadəçinin tikib saxladığı/yerləşdirdiyi binalar real mərtəbə həndəsəsi və hissələri ilə göstərilməlidir. Placement bağlı qalır.
- Son qərar: AppBackend dəyişikliyinə ehtiyac olsa icazə var; onun main-indən ayrıca branch, worktree yoxdur. Bu vahiddə backend dəyişikliyi lazım deyil: mövcud city/parcels/position və decoration copy müqavilələri kifayətdir. Server kodu yalnız oxundu.

## Dəyişiklik

`CitySceneRepository` application bootstrap-da eyni authenticated HTTP backend-ə qoşuldu. City səhifələri, bina detalları və dekor nüsxələri oxunur; yalnız yerləşdirilmiş dekorlar səhnəyə daxil edilir. Torpaq mövqeləri və bina mövqeləri saxlanır. Koordinatsız legacy bina üçün origin/həndəsə uydurulmur; bina məlumatı və null mövqe saxlanır. Geometry-si olmayan bina sonrakı renderer-də hazır bina şəkli ilə əvəz edilə bilməz.

Canonical decimal string tick, ±1e9 sərhədi, quarter-turn 0–3, geometry/rules versiyaları, base origin, unikal parcel ID-ləri yoxlanır. Bina detalları membership content revision ilə tutuşdurulur. City səhifələrinin revision/population/parcel uyğunsuzluğu, təkrar/geri cursor qeydləri rədd edilir. Bütün canlı oxular ilk və son city səhifəsinin eyniliyi ilə əhatələnir: aradakı domain write səhnəni etibarsız edir; qarışıq snapshot UI-a qaytarılmır. Auth epoch HTTP sərhədində hər cavabdan sonra yoxlanır. Yeni cache, wallet, command journal və placement əmri yoxdur.

## Lokal review

- AppBackend `internal/wobbrik/{city,city_placement,shop}.go` və API sənədi ilə DTO sahələri müqayisə edildi; backend dəyişmədi.
- Əvvəlki city membership parser-i dəyişdirilmədi; mövcud inventar axınının legacy uyğunluğu qorunur. Yeni scene reader müasir geometry müqaviləsini tələb edir.
- Təsdiqlənmiş UI dizaynı yenidən yaradılmadı, ekran/asset/palette dəyişmədi. Bu yalnız renderer-in məlumat təməlidir; işlək şəhər renderer-i və canlılıq iddiası yoxdur.

## Yoxlamalar

- Java21 `:core:test`: PASS; `CitySceneReadsTest` 7/7. Exact/legacy position, malformed coordinate/version/base, placed copy filtering, building revision race, city page race, decoration read zamanı account revision dəyişikliyi və eyni UID yeni login yoxlanır.
- Bootstrap inteqrasiyası sonrası core test/Kotlin compile/lint nəticəsi aşağıda qeyd olunur.
- Vizual/render/runtime/backend test/CI yoxdur. UI dəyişmədiyi üçün screenshot/kontrast nəticəsi iddia edilmir. Emulator açılmadı.

## Qalan iş

Real şəhər projection/depth ordering, accepted floor geometry üzərinə fasad/roof/window atlaslarının oturdulması, server parcel union-una uyğun ada səthi, bina seçimi/baxış və V1 sakin/avtomobil/işıq/ağac animasiyaları hələ implementasiya edilməyib. Mövcud sabit şəhər rasteri real server şəhəri kimi istifadə edilməməlidir. Böyük zoom və kamera dönüşü üçün atlas/səth keyfiyyəti ayrıca qiymətləndirilməlidir; təsdiqli dizaynı sadə bloklarla əvəz etmək olmaz. Növbəti vahid yeni məlumat təməli üzərində bu renderer-dir; yeni stack pinləri və ya 3D mühərrik seçilmir.

- Son yoxlama: Java21 `:core:test :app:compileDebugKotlin :app:lintDebug` PASS (28s); `git diff --check` PASS. UI/render/CI yoxdur.
