# PLAN §1.1 — native mobil Profil editoru və avatar picker

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`; yoxlanmış vahid working diff-dir, commit deyil. Başlanğıcdakı owner dirty işi qorundu; stage/commit/merge/push/deploy/CI yoxdur. Authority: `docs/design/unified-mobile-v3/03-account.png`, README-dəki son Home chrome və PLAN §1.1. Overview bu vahiddə dəyişmir.

## Dəyişiklik

`NativeProfileEditor.kt` mobil editorun gradient fonunu, relyef panelini, portrait konturunu, native geri/ad sahəsi/sayğac/avatar dəyiş/save/cancel idarələrini qurur. Normal portretdə cancel/save yan-yana, kiçik və font2-də şaqulidir; action-lar scroll body-dən kənardadır. Body scroll və IME padding saxlanır. Font2 avatar düyməsi mövcud panel enini tutur, söz ortadan bölünmür.

Picker native modal panel, radio semantikalı 3/2 sütunlu lazy grid, mavi/cyan kontur və seçilmə işarəsi ilə qurulur. Yalnız seçimlər scroll olur, təsdiq/ləğv görünür; seçilmiş ID yalnız təsdiqlə callback-ə ötürülür. Dialog dismiss əvvəlki kimi seçim tətbiq etmir. Mobil dispatch `ProfilePresentation.kt`-dədir; legacy/tablet yolu saxlanır. Mobil editor/picker `ProfileArtProvider`/raster UI skin decode etmir. Raster yalnız əvvəlki transparent `records_portraits_v3` personaj sheet-idir, yeni asset/token/pin/dependency yoxdur. Press animasiyası ayrıca tapşırıq olaraq qalır.

Real `PublicAccountScreen`/`ProfileEditor` load/save/revision/conflict/exact retry/login-epoch qaydaları və avatar ID siyahısı dəyişməyib. API/DTO/domain/schema dəyişmədiyindən AppBackend işi tələb olunmur; backend testləri işlədilmədi. Adın Unicode limiti mövcud route/domain tərəfindən tətbiq olunur. Native editor əvvəlki enabled/canCancel/canSave/status callback-lərini qəbul edir; uncertain vəziyyətdə redaktə bloklanır və retry action-u qalır.

## Yoxlamalar və lokal review

- Son production source ilə dedicated native editor/picker **5/5 PASS**:390×844 və320×568,font1/font2;360×640,font2+24/48dp simulated editor inset+uzun ad. Save/name callbacks,48dp save, local seçim/təsdiq/ləğv, grid scroll, disabled name/avatar/save/cancel yoxlandı. Bu turda həmin5test PASS, genişləndirilən shell assertion-ları3FAIL idi; aşağıdakı final shell turunda düzəldi.
- Son real shell **4/4 PASS40s**, failure/error/skipped0. Profil → editor → avatar picker → pilot təsdiqi → editor ləğvi → əvvəlki builder profile-a qayıdış; digər real menu route-ları/dock və iqtisadi submission=0 saxlanır. `MobileShellRouteRenderTest.kt`-də yalnız əlavə edilən profil axını bu vahidə aiddir; əvvəlki owner diff-i saxlanır.
- Final normal konfiqurasiyada debug/mockDebug APK + lintDebug **PASS46s**. CI deyil. Core PublicProfilesLeaderboard task UP-TO-DATE; mövcud XML **7/7 PASS**, bu tur təkrar icra deyil. Burada conflict/exact retry/account isolation sübutu mövcuddur, yeni server runtime testi iddia edilmir.
- Son10PNG `profile-editor-native/` altında. Editor5 final PNG və picker5 eyni picker source renderi açılıb03-account ilə müqayisə edildi. Kiçik/font2-də ad sahəsi body scroll-u ilə görünür; portrait və name eyni anda sığmaya bilər. Font2-də picker üçüncü avatarı grid scroll-dadır. Picker capture ayrıca dialog pəncərəsidir: şəffaf xarici sahə PNG viewer-də qara görünür, shell overlay screenshot deyil. Simulated inset picker pəncərəsinə süni ötürülmür; onun real safeDrawing insets/runtime-u ayrıca yoxlanmayıb.
- Kontrast **4/4 PASS**, minimum8.22: Text/Surface13.00, Muted/Surface8.22, Background/Accent11.45, Text/Raised10.24. Yeni palitra yoxdur. `git diff --check` PASS; index boşdur.
- Scoped review: mobile/legacy sərhədi, grid bounds/selection, action enabled state, scroll/IME modifier, source sprite bounds və import/feature yerləşməsi yoxlandı. Domain/data dublikatı yoxdur.

İlk yazı əmri yanlış relative cwd səbəbindən fayl yaratmadı; düzəldildi. İlk test hostu picker-i shell-dən kənarda legacy action-a yönəltdiyi üçün5FAIL oldu; native action asılılığı aradan qaldırıldı və real Dialog yolu testə qoşuldu. Sonrakı5FAIL eyni cancel tag-in dialog və arxa editorda olması idi; dialog node-u seçildi. Shell-in3FAIL-i pinned action-a lazımsız scroll və overview-də2builder portrait assertion-u idi; uyğun node/ölçü şərti düzəldi. Bu uğursuz turlar PASS sayılmır.

## Sərhədlər

Native editor/avatar picker vahidi mobil route-a qoşulub; bütün Profil/PLAN §1.1 və mərhələ qəbulu deyil. Personajların approved PNG ilə eyni üz/poza artwork-ü yoxdur; mövcud transparent sheet saxlanır. Native relyef, geri glyph-i, field və panel ritmi reference ilə piksel eyni deyil. Ad sahəsi native single-line input-dur; font2/kiçik ekranda zəruri body scroll saxlanır. Yeni alternativ dizayn və domain qaydası əlavə edilməyib.

Emulator açılmadı. Real hesab/device E2E, canlı IME/inset, TalkBack, API24, GPU/performance, əlavə locale və tablet yoxlanmayıb; host render bunların PASS sübutu deyil. Tam artwork/piksel pariteti barədə əvvəlki overview review məhdudiyyətləri qüvvədədir.
