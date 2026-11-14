# Mərhələ 4 — yekun regression və qəbul qeydi

13 noyabr 2026. Branch: `chore/phase4-acceptance`; baza: `a2ebcce` (mərhələ 4 və fix-lər main/remote main-də). Yeganə plan: `../../PLAN.md`. Bu qeyd yeni plan və ya mərhələ 5-ə başlama icazəsi deyil.

## Qəbul nəticəsi

Yoxlanmış mobil portret funksional regression dəsti PASS-dır. Aşağıdakı bir responsive problem düzəldildi, son renderlərinə baxıldı. Yeni domain/API problemi aşkarlanmadı. **Tam birəbir vizual uyğunluq və bütöv məhsul qəbulu PASS kimi bağlanmır**: aşağıda konkret fərqlər və yoxlama sərhədləri göstərilir. İstifadəçinin mərhələ qəbulu ayrıca qərardır; emulator/canlı yoxlama gate deyil.

## Tapıntılar və düzəlişlər

1. Dar bina detalında etiket/dəyər bərabər iki sütuna bölünürdü; 360dp/font1-də “Hər mərtəbəyə” və “Yaratmır” sözləri ortadan qırılırdı. `InventoryScreen.kt/DetailRow` yalnız sahə dar olduqda fontScale və mövcud Target tokenindən hesablanan həddə görə etiket/dəyəri alt-alta göstərir. Art, rəng, radius, font scale, preview və ekranın əsas əməliyyatları saxlanır. 360/font1,390/font1,320/font2 testində real text line sərhədləri söz ortasından qırılmır. Mətn uzun olduqda mövcud secondary scroll saxlanır.
2. Köhnə city host testi əvvəlki Home “Şəhərim” mətn düyməsinə/scroll-a bağlı idi; təsdiqli navbar “Şəhər” label-i və “Şəhərim” accessibility adını istifadə edir. Test contentDescription və görünən navbar action-u ilə düzəldi. Production navigation dəyişmədi.
3. Silmə dialoqunun UI regression əhatəsi çatmırdı. Mövcud real InventoryScreen fixture-inə dialoq açılışı, Ləğv et, sıfır mutation dispatch və bina seçiminin saxlanması əlavə edildi; native dialog renderləri çəkildi. Təsdiqlənmiş silmənin exact replay/refund rəddi və hesab izolyasiyası core testlərdədir; UI-dan real serverə silmə E2E-si aparılmayıb.

## Yoxlamalar

- Mövcud iki opt-in init harness birlikdə: **69/69 PASS**, failure/error/skipped0, son production kodunda 66s. Home6 + compact flow3 + purchases6 + parts preview5 + city49. Sinif üzrə nəticə: [host-results.json](evidence/host-results.json).
- Bütün adi core/engine/app unit + assembleDebug + lintDebug: **BUILD SUCCESSFUL,13s**. Core104,engine67,app23; cəmi194test, failure/error/skipped0. Core source dəyişmədiyi üçün Gradle core:test UP-TO-DATE idi; engine/app taskları icra edildi. [Unit nəticələri](evidence/unit-results.json).
- Host fixture-dən əlaqəsiz struktur assertion çıxarıldıqdan sonra son CompactFlowRenderTest **3/3PASS18s**; eyni production kodu. Dialoq/ləğv/söz-qırılması yoxlamaları qüvvədədir.
- Kontrast: Text/Surface13.00, Muted/Surface8.22, Background/Accent11.45, Background/Success11.82;4/4PASS. Mənbə `GameTokens.kt`; yeni rəng/stack pini yoxdur. `git diff --check` PASS.
- Funksional əhatə: inventory/detail read və pagination validasiyası; buy→unlock→apply→fresh detail; stale Ready və eyni UID login epoch rəddi; rejection və lost-response/durable recovery; land XP/daimi hüquq və repeatable dekor copy; şəhər revision bracket/geometry/position və decorations transport allowlist. Şəhərdə focus/return/nav, torpaq union/dekor, liveness frame/background/motion setting yoxlamaları da keçir.
- Bunlar sintetik host və unit testlərdir; real hesab/server E2E nəticəsi deyil. AppBackend müqaviləsi dəyişməyib, əvvəlki read-only tutuşdurma qüvvədədir; backend dəyişdirilməyib və backend testləri bu vahiddə işə salınmayıb.

## Dizayn müqayisəsi

Aktual prioritet: son təsdiqli navbar/yığcamlaşdırma, modular art və balkon qərarları köhnə board-dakı uyğun hissələri əvəz edir. Əvvəlki board-lar qalan vizual detallar üçün istinaddır. Eyni bina sayını/qiymətini zorla çəkmək üçün server fixture-i məhsula əlavə edilməyib.

- **Home:** v3 navbar ilə cari360font1 renderi tutuşduruldu: art, logo, balans, stats, nav sırası və mərkəzi Play kompozisiyası saxlanır. Font2-də art/yan label-lərin yığılması əvvəlki təsdiqli adaptasiyadır; minimum48dp/nav callback testləri keçir. [Müqayisə](evidence/home-comparison.png).
- **Detail/parts:** `04-building-detail.png`, `02-parts-flow-v2.png` və son compact production contact-lərinə baxıldı. Real10mərtəbə/accepted centers və seçilmiş modular hissələr saxlanır. Compact inventory toolbar/grid və azaldılmış ölçülər son qərardır. **Qalan fərq:** ilkin board-dakı ikonlu tab/metadata, parlaq çərçivələrin bütün detalları və parts kateqoriya ikonları cari ekranla birəbir deyil; bunlar bu acceptance işində özbaşına redesign edilməyib və tam vizual PASS sayılmır. Reklam/real pul action-ları mərhələ6, bazar mərhələ5-dir. [Detail müqayisəsi](evidence/detail-comparison.png), [font2 contact](evidence/production-contact-320-font2.png).
- **Silmə:** təsdiqli dialoqla native render tutuşduruldu: mətn, navy surface, pink destructive və secondary cancel saxlanır. Font2 düymələri şaqulidir; hər ikisi görünür. Native dialog-only capture fon dim/backdrop-un tam ekran sübutu deyil. [Müqayisə](evidence/delete-comparison.png), [font2](evidence/production-delete-320-font2.png).
- **Dekor/torpaq:** təsdiqli `03-decoration-flow.png` alış hissəsi və `05-land-purchase.png` list/confirmation hissəsi cari purchase renderləri ilə tutuşduruldu. Art/cyan selection/pink price, qiymət/XP və Al/Ləğv et qalır. Mətn/ölçü və normal/stacked düzülüş compact qərarına uyğundur. Board-dakı map selection/placement bağlı scope-dur; tamamlanmış kimi təqdim edilmir. [Dekor](evidence/purchase-decor-360-font1-confirmation.png), [torpaq font2](evidence/purchase-land-360-font2-confirmation.png). Listdə aşağı kartların scroll ilə görünməsi və confirmation daxilində secondary mətn scroll-u saxlanır; Al/Ləğv et sabit görünür.
- **Şəhər:** city-view və son modular balkon renderləri yanaşı yoxlandı. Header/view selector/Bax/nav, accepted crooked geometry, seçilmiş art və real parcel union saxlanır. **Qalan fərq:** konseptin yumru dekorativ sahil kompozisiyası, fontan/kafe mebeli, geniş terraslar və neon detalı tam eyni deyil; serverin vermədiyi mülkiyyət yaradılmır. Sabit kamera istifadəçi tərəfindən qəbul edilib. Bu fərqlər tam city-view pariteti kimi bağlanmır. [Müqayisə](evidence/city-comparison.png), [320/font2](evidence/representative-320-font2.png).

PNG-lərin mənbə və SHA256-ları: [images.json](evidence/images.json). Müqayisə lövhələri yalnız QA üçün native AppKit kompozitidir; məhsul asset-ləri redaktə olunmayıb. Bütün renderlər API34/density3 synthetic host-dur. 360×640/font1,390×844/font1,320×568/font2; ayrıca Home font2 reserved-space24/48dp,offline,long-balance/XP əhatəsi mövcuddur.

## Məhdudiyyət və sonrakı iş

Real cihaz/API24runtime/TalkBack/GPU/performance, həqiqi klaviatura/inset və bütün locale/state kombinasiyaları yoxlanmayıb. Emulatordan istifadə edilmədi. Planşet/landşaft ayrıca scope-dur. Bunlar PASS sayılmır; canlı yoxlama qəbul üçün tələb edilmir. Tam vizual parity fərqlərinin istifadəçi tərəfindən qəbul edildiyi iddia edilmir.

İstifadəçinin son yeni tapşırığı: zəif/kəsilmiş internet oyun və digər mümkün işləri bloklamamalıdır. Bu ayrıca davam vahididir, bu sənəddə həll edilmiş sayılmır. Server wallet/ownership authority, pending command exact replay və məlumatın qəbul statusu qorunmalıdır; mövcud pause/offline məşq qaydasının dəyişən hissəsi dəqiqləşdirilməlidir.

Lokal diff review: dəyişiklik yalnız dar DetailRow və opt-in host testlərindədir; domain/data/server/physics dəyişmir. Owner release/live-check/XML/QA və əvvəlki handoff qeydləri qorunur. Bu vahiddə commit/merge/push/CI yoxdur.


## 14 noyabr — detail/parts ikon və çərçivə davamı

İstifadəçinin açıq göstərişi ilə yuxarıdakı detail/parts ikon və çərçivə fərqləri `fix/phase4-detail-parts-parity` working diff-də tamamlandı. Son compact/navbar/modular art qərarları saxlanır. Final8/8native host və adi unit/APK/lint PASS; nəticə, yanaşı render müqayisəsi və məhdudiyyətlər [ayrıca review-da](../2026-11-14-phase4-detail-parts-parity.md). Əvvəlki acceptance nəticələri tarixidir; bu əlavə şəhər parity fərqlərini, tam piksel uyğunluğunu və mərhələ4qəbulunu bağlamır. Merge/push/CI yoxdur.
