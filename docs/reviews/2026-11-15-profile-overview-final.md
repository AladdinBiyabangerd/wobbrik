# PLAN §1.1 — Profil overview: crop, relyef və kompozisiya

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Bu tur yalnız `NativeProfileOverview.kt`, `PublicAccountScreen.kt` və mövcud `MobileProfileProductionRenderTest.kt` dəyişdirildi. Başlanğıcdakı owner dirty işi saxlanıldı; stage/commit/merge/push/deploy/CI yoxdur.

Authority: `docs/design/unified-mobile-v3/03-account.png`, README-də son Home chrome qərarı və Profil başlıq sətrinin silinməsi barədə son istifadəçi qərarı. Yeganə plan `docs/PLAN.md` §1.1-dir.

## Tamamlanan uyğunlaşdırma

Mövcud transparent artwork baş/çiyin kompozisiyasına daha sıx crop olunur; mətn/çərçivə asset-ə daxil deyil. Lent panelin üst kənarı ilə üst-üstə oturur; yan quyruqlar gradientlə dərinlik alır, mərkəzi tac yuvası yığcamdır. Lent və rekord üçün yumru uclu native qızılı tac istifadə olunur. Panelin ağ selected konturu cyan konturla əvəz edildi; səviyyə shield-i ikiqat qızıl bevel və kontrastlı tünd mərkəz alır. Normal mobil ölçüdə Parametrlər/Rekordlar panelin ardınca body-dədir; kiçik ekran/font2-də əsas Dəyiş və qısayollar pinned qalır. Legacy/tablet qısayol yerləşməsi `LocalMobileMenuShell` sərhədi ilə saxlanır.

Yeni rəng, ölçü tokeni, raster və dependency yoxdur. Real profil load/save/conflict/exact retry/session/XP/rekord mənbəyi dəyişmir. AppBackend müqaviləsi/domain/schema dəyişmir; backend işi lazım deyil, backend testləri işə salınmadı. Press animasiyası ayrıca tapşırıq olaraq qalır.

## Yoxlamalar

- Son source: dedicated Profil5 + real shell4 = **9/9 PASS**, failure/error/skipped0; Gradle1m3s.
- Son debug/mockDebug APK + lintDebug **PASS56s**; CI deyil.
- Son5 native render açılıb03-account ilə müqayisə edildi:390/font1,320/font1,320/font2,390/font2,360/font2+24/48dp simulated inset+uzun ad/balans. Şəkillər `profile-overview-final/` altındadır. Əsas Dəyiş görünür; aşağı body scroll, uzun ad və font2 qısayollar horizontal scroll-dadır.
- Kontrast4/4 PASSminimum8.22: Muted/Surface8.22, Text/Raised10.24, Background/Accent11.45, Text/Surface13.00. Shield-in ən açıq mərkəzi Raised-dir; qızıl/cyan dekorun içində səviyyə mətni çəkilmir.
- İlk native tur9/9 PASS idi; açılıb baxılan renderdə tac gradientinin yaşıl çaları düzəldildi və shield-in parlaq mərkəzi kontrastlı tünd tokenlərə qaytarıldı. Son tur yuxarıdakı source-u yoxlayır. Yanlış relative edit yolu ilə başlayan aralıq tur dayandırıldı, PASS sayılmır.
- Scoped review: crop source bounds sheet daxilindədir; native geometry/dekor, font2 scroll/48dp action, mobile-only qısayol yerləşməsi və domain sərhədi yoxlandı. `git diff --check` PASS; index boşdur.

## Açıq sərhəd

Bu tur overview-dəki konkret crop/relyef/yerləşmə fərqlərini yaxınlaşdırır; tam piksel pariteti iddia edilmir. Approved portretdəki personajın eyni ayrıca artwork-ü yoxdur; mövcud records personajının üzü/pozası fərqlənir. Native lent/shield hələ reference-in bütün işıq/kölgə detallarını təkrarlamır. Editor/avatar picker legacy raster-dir və ayrıca növbəti vahiddir. Bütün PLAN§1.1 və Profil tamamlanmış sayılmır.

Emulator açılmadı. Device/account E2E, TalkBack/IME/API24/GPU/performance/locale/tablet yoxlanmayıb. Host render runtime sübutu deyil.
