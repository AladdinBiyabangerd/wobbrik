# Şəhər başlığı, naviqasiya və baxış paneli

- Branch: `feat/city-chrome`; review bazası: `8b4a6a4`. Working diff, commit/merge/push/CI yoxdur.
- Mənbə: `docs/design/city-view.png`; cari istifadəçi bu dizayn əsasında vahidi açıq tapşırıb. Sabit kamera qərarı qüvvədədir. Kamera illüstrasiyasını həmin istinaddan şəffaf cutout kimi çıxarmağa ayrıca açıq icazə verildi.

## Dəyişiklik

`CityChrome.kt` başlıq çərçivəsi, aktiv cyan şəhər görünüşü, əhali, baxış paneli və aşağı Şəhər/İnventar/Bazar naviqasiyasını ayırır. Mövcud rəng, ölçü, radius və tipoqrafiya tokenləri istifadə olunur. `CityScreen.kt` server scene oxusu və Canvas qatını saxlayır; kamera bucağı, accepted geometry, painter order, terrain/dekor proyeksiyası dəyişmir.

“Bax” seçilmiş binanı (seçim yoxdursa mövcud son render edilən binanı) mövcud focus görünüşündə açır. “Geri” overview-u qaytarır. Android Geri focus-dan overview-a, sonra Ana ekrana qayıdır. İnventar real mövcud destination-a gedir. Bazar deaktivdir; Plan yalnız deaktiv istinad elementidir, placement/market endpoint-i uydurulmur. Sabit kamera olduğuna görə orbit vədi verilmir. Bina olmayan/loading/error hallarında Bax deaktivdir.

Böyük şriftdə panel şaqulidir; 320dp dar pəncərədə İnventar yazısı kəsildiyi üçün yalnız böyüdülmüş şrift+dar pəncərə halında həmin elementə əlavə en ayrılır. Rəng palitrası dəyişmir. Kamera `city_view_camera_v1.png`: built-in imagegen vasitəsilə təsdiqli istinaddan çıxarılıb, alpha saxlanılıb; orijinal dizayn dəyişdirilməyib. Çıxarılmış raster orijinalın bit-identik piksel crop-u deyil.

## Yoxlama

- Kontrast: Text/Surface 13.00; Muted/Surface 8.22; Background/Accent 11.45; Text/Raised 10.24 — PASS.
- İlkin yeni mətn-layout yoxlaması 7 halda uğursuz oldu: `softWrap=false` paraqraf eni görünən sətirdən geniş saxlanıldığı üçün `hasVisualOverflow` yanlış siqnal verdi. Yoxlama unmerged Text node-da real sətir sərhədlərini komponent eni ilə tutuşdurur. Həqiqi 320dp/2× İnventar kəsilməsi əvvəl renderdə tapılıb düzəldildi. Finalda bu sətir-sərhədi yoxlamaları PASS.
- Final host/native Compose və normal build/lint/unit nəticələri aşağıda qeyd edilir.
- Render evidence: `docs/reviews/city-chrome-visual/`; harness mövcud `city-scene-visual/render/` daxilindədir. Əvvəlki tracked PNG evidence bərpa edildi, yeni renderlər ayrıca qovluqdadır.

## Sərhədlər

Bu vahid tam şəhər konseptinə və mərhələ 4-ə qəbul deyil. Mövcud bina artı, rect/L parcel sərhədi və V1 canlılıq əvvəlki sərhədlərdə qalır. Mobil adaptasiyada konseptin logo/konsept etiketi ayrıca üst sətir kimi əlavə edilməyib; real Ana ekran girişi başlıq yanındadır. Real inset, TalkBack, cihaz/hesab runtime yoxlanmır; emulator açılmır. Planşet və landşaft bu vahiddə deyil.

Backend müqaviləsi və authority dəyişmədiyi üçün AppBackend dəyişikliyi lazım deyil; serverə yazı və backend testləri edilməyib. Owner `app/release/` və əvvəlcədən untracked release review faylı qorunur.

## Final nəticə

- Son host run: 18/18 test PASS, 22 PNG; 320×568dp/font2, 360×640dp/font1/2, 390×844dp/font1, overview/focus/return, loading/error/empty, Home/inventory callbacks və deaktiv market. Final contact və yan-yana design comparison açılıb baxıldı. Dar focus-da uzun geri etiketi “Geri” ilə yığcamlaşdırıldı; əsas idarəetmələr görünür, inventar sözü kəsilmir. Normal şriftdə naviqasiya Body, böyüdülmüş şriftdə Caption token pilləsidir.
- Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` son kodda PASS (14s). Bu fokuslu yoxlamadır, CI deyil.
- Son token kontrastı 4/4 PASS, min 8.22. Lokal diff review: scene reader/viewport/projection və backend dəyişikliyi yoxdur; yeni təsdiqlənmiş production bug tapılmadı. `git diff --check` PASS.
- Real device/account/inset/TalkBack yoxlaması və tam piksel uyğunluğu iddiası yoxdur. Başlıqdakı Home, kompakt mobil tablar, mövcud UI icon atlasları, panel ölçüləri və glow/divider fərqləri comparison-da görünür; yalnız sabit kamera qərarı ilə orbit mətni dəyişdirilib. Bu nəticə tam konseptin vizual qəbulu deyil.

## Lokal commit review

İstifadəçi “commit” göstərişi verdi. Son diff yenidən review edildi; production kodu dəyişmədi, əvvəlki final yoxlamalar qüvvədədir. Şəhər vahidi və onun handoff hissəsi commit-ə daxil edilir; paralel emulator handoff qeydi, release faylları, live-check review və QA patch xaricdə saxlanır. Merge/push/CI yoxdur.
