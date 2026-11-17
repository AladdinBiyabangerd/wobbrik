# PLAN §1.1 — Profil mobil overview native kompozisiyası

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`; scoped uncommitted diff: `ProfilePresentation.kt`, `PublicAccountScreen.kt`, yeni `NativeProfileOverview.kt` və dedicated render host. Əvvəlki owner dirty işi saxlanır; stage/commit/merge/push/deploy/CI yoxdur.

Authority: `docs/design/unified-mobile-v3/03-account.png` Profil kompozisiyası + README-də son Home header/7-slot qərarı. Yalnız mobil overview vahididir; editor/avatar picker və tam Profil/paritet qəbulu deyil.

## Dəyişiklik və diff review

Mobil overview artıq legacy profile/panel/skin board-larını decode etmir. Mövcud records personaj sheet-indən yalnız illüstrasiya götürülür; avatar çərçivəsi, tac lenti, crown/pencil, səviyyə shield-i, panel, rule, stat/mətn, əsas əməliyyat və qısayollar native-dir. Mövcud GameTokens/MenuRelief, rəng və ölçü pillələri istifadə olunur; yeni palitra/pin yoxdur. Shared legacy/tablet təqdimatı saxlanır. Shell daxilində ProfileAction native gradient olur, o cümlədən editor/save/retry düymələri; editor/picker paneli hələ legacy-dir.

PublicAccountScreen real profile load/save, session epoch yoxlaması, revision conflict, uncertain retry və server XP/rekord mənbələrini saxlayır. Artıq XP intervalını təkrarlayan əlavə next-level cümləsi yoxdur; real interval/progress qalır. Kiçik mobil və font2 üçün əsas Dəyiş body scroll-dan kənardadır. Uzun ad/rəqəm və qısayol etiketləri horizontal scroll, body vertical scroll-dur. Parametrlər və Rekordlar mövcud route-lara gedir. Disabled action klik qəbul etmir. Press animation ayrıca tapşırıq olaraq qalır.

AppBackend API/DTO/domain/schema və wallet/progression qaydaları dəyişməyib; backend dəyişikliyi tələb olunmur. Backend testləri bu vahiddə işə salınmadı.

## Vizual fərqlər və məhdudiyyətlər

Kompozisiya hələ PNG ilə piksel pariteti deyil: lentin qat/fold relyefi, səviyyə nişanının laurel/ribbon dekoru, XP nişanının silueti, qısayol ikonları və statların dəqiq horizontal ritmi açıqdır. Mövcud transparent personaj sheet-i approved account portretinin eyni crop-u deyil. Əlavə alternativ dizayn təsdiqi kimi təqdim edilmir; bu fərqlər tamamlanmamış uyğunlaşdırmadır.

Normal/kiçik portret, font1/font2 və simulated inset+uzun ad/balans host renderləri istifadə olunur. Böyük şriftdə aşağı statlar scroll-dadır. Device/account E2E, TalkBack, IME/editor, API24/GPU/performance, lokallar və planşet yoxlanmayıb. Native host screenshot runtime və ya mərhələ qəbulu deyil.

## Yoxlamalar

İlk dedicated tur 5 testdən3 FAIL: scroll-dan kənardakı font2 əsas düyməsinə səhv performScrollTo tətbiq olunurdu; test düzəldildi, növbəti5/5 PASS. Real profil core suite7/7 PASS (revision conflict, uncertain exact retry, session isolation və leaderboard read sərhədləri daxil).

Son nəticələr aşağıda yenilənir.

- Son struktur/input dəyişikliklərindən sonra native dedicated5 + real shell4 = **9/9 PASS**, failure/error/skipped0; debug/mockDebug APK və lintDebug PASS (1m28s).
- Son shield kontrast düzəlişindən sonra dedicated native **5/5 PASS**, debug/mockDebug APK + lintDebug **PASS48s**. Rəng düzəlişi shell/domain davranışını dəyişmədiyindən shell/core təkrarlanmadı.
- Son5 PNG açılıb03-account ilə müqayisə edildi, `profile-native/profile-*.png` altında saxlanıldı:390/font1,320/font1,320/font2,390/font2,360/font2+simulated24/48dp inset+uzun ad/Long.MAX_VALUE balans. Testdə48dp əsas əməliyyat, redaktə callback-i və iki route callback-i yoxlanır. Font2 qısayolların görünən mətn hissəsi horizontal scroll-dadır; kiçik/inset halda overview-nin aşağı hissəsi vertical scroll-dadır.
- Kontrast6/6 PASS: Text/Surface13.00, Muted/Surface8.22, Background/Accent11.45, Text/Background16.57, shield Text/Raised10.24 və Text/PanelTop13.58. Minimum8.22.
- Scoped diff üzrə review: mobile-only dispatch, legacy provider decode sərhədi, enabled click, profile session/save/conflict axını və mövcud paket konvensiyası yoxlandı. `git diff --check` PASS. Index boşdur.
