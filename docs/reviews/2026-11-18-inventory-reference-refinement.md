# İnventar — header, kart və navbar uyğunlaşdırması

Branch `feat/inventory-art-led-redesign`, diff bazası `f8beac0`. İstifadəçi ilk native nəticəni qəbul etməyib, yalnız həmin səhifənin header/kart/navbar hissələrinin `docs/design/inventory-art-led-v1/reference.png` ilə uyğunlaşdırılmasını istəyib. Yeganə plan `docs/PLAN.md` §1.1. Digər səhifələrin redesign-ı başlanmayıb. Əvvəlki review ilkin keçidin tarixi sübutudur, son vizual vəziyyət bu qeyddir.

## Dəyişikliklər və müqayisə

- Header: nümunədəki gecə şəhəri/bulud/yarpaq fonu ayrıca mətnsiz panorama asset-i kimi hazırlandı. Native logo üçün mövcud PNG-nin şəffaf kənarları ölçüldü, BitmapPainter source rect ilə göstərildi; yeni bitmap allocation yoxdur. Native wallet daha yığcamdır və sağdadır. Scenery logo/wallet/UI daşımır. Fon optional IO decode-dur, səhifənin mətnini/oxusunu bloklamır.
- Tab:48dp input row daxilində36dp görünən gradient səth, nazik native rim, separatorlar və aktiv firuzəyi underline. Normal şriftdə bütün kateqoriyalar görünür; böyük şriftdə selected-tab horizontal scroll qalır. Rənglər nümunənin piksel nöqtələrindən InventoryCollection tokenlərinə köçürüldü.
- Kart:14dp kənar inset,8dp sütun/sətir boşluğu, normal portretdə160dp art sahəsi, native yumşaq gradient/kontur və16sp/12sp metadata iyerarxiyası. İllüstrasiya kartın bütün eninə yayılır. Ayrı mətnsiz plaza fonu və real modular bina canvas-ı çəkilir; giriş/ad/mərtəbə/status rasterə bişirilmir. Bina art miqyası tam garden/roof görünməsi üçün renderlə yoxlanıb. Font2 ad söz üzrə wrap olur, list vertical scroll saxlanır.
- Navbar: native64dp səth,40dp görünən Play + ağ highlight arc (48dp input),20dp native ikonlar, kiçik label-lər, aktiv İnventar çərçivəsi. Bottom-safe dock listin üzərindədir; üçüncü sıra arxadan davam edir. Dar en üçün iki-sıralı dock və48dp sahələr saxlanır. Son kart üçün scroll bottom padding faktiki safe viewport enindən hesablanır; loading/error/empty footer-i də dock boşluğu alır.
- Inventory root/detail eyni content slotunda qalır. Scenery bitmap-ləri route lifecycle-da paylaşılır və detail/back chrome dəyişməsi onları təkrar decode etmir. Detail əvvəlki native header/navbar, background və narrow dock clearance-ni saxlayır. Digər page route-ları əvvəlki shell-dən istifadə edir.

İlk keçidin render fixture-sində adlar material ID-si ilə səhv qoşulmuşdu (Suvaq üçün tam teal dəst). Fixture indi kataloqla uyğun mixed seçimlər göstərir: wall2=Kərpic hörgü, wall3=Suvaq fasad; qalan native purple hissələr real ID-lərdir. Bu screenshot dekorunu production state-ə köçürmür, server DTO/real inventory seçimini dəyişmir.

## Lokal review, performans və sərhədlər

Yeni kod Inventory feature chrome/presentation daxilindədir; tokens mərkəzləşdirilib. Press/disabled, selected tab və navigation semantics saxlanır. Native ikonların heç biri PNG crop deyil. Bina renderer-i və domain/data/cache/revision/UID/cancellation/fresh mutation gate dəyişmir. Backend müqaviləsi/API/schema/economy eynidir, AppBackend dəyişikliyi tələb olunmur; bu vizual refinement üçün backend testləri/SQL ölçməsi təkrar işlədilməyib.

Yeni header1024×341 bitmap-i təxminən319KiB paket/1.33MiB RGBA-dır; əvvəlki512×512 fonla birlikdə təxminən2.33MiB RGBA. Decode bir IO çağırışında route/config/density scope-da, görünən kartlar üçün paylaşılan ImageBitmap-lərlədir. Eyni header bitmap-i aşağı kənar foliage-si üçün də təkrar istifadə edilir, əlavə bitmap decode yoxdur. Gizli prewarm, əlavə HTTP, per-card disk/bitmap decode və yeni polling yoxdur. Art cache mutation icazəsi deyil.

Naviqasiya fixture-si9binadır: ilk inventory + detail + back inventory + explicit refresh =4HTTP/13833response byte,0city request və0mutation submission. Müqayisə edilən əvvəlki eyni9bina fixture-də də4HTTP/13833byte idi (hissə ID-lərinin ölçüsü eynidir); bu dəyişiklik latency yaxşılaşması iddiası deyil. Cache-hit/offline/account suite və production RTT/ART/frame/GPU/battery ölçülməyib.

## Yoxlamalar

Native host:390×844 və320×568 font1/2,360×640 font2 + simulated top/bottom inset + uzun ad/Long.MAX_VALUE balans. Son5render açılıb istinad PNG ilə müqayisə edilib. Testlər kart→detail→back/refresh/Dekor,48dp target və son kartın dock-un üzərinə scroll olunmasını yoxlayır. Native nəticə və adi build/lint son run-dan aşağıda qeyd edilir. İlkin renderdə header crop/logo alpha boşluğu və kart fon keçidi düzəldilib. Son mənbə ilə təkrar yoxlama əvvəlki run-ları əvəz edir.

Kontrast: Metadata/PanelBottom8.24, Accent/Selected8.78, Background/Accent10.85, Text/PanelTop12.51, Text/Selected12.70, Text/PanelBottom13.91 —4.5tələbiPASS. `git diff --check`PASS. Sübutlar `inventory-redesign/evidence/refinement/` altındadır; PNG-lər repo qaydasına görə lokal ignored artefaktlardır. Header asset/prompt provenance `docs/design/inventory-art-led-v1/{README.md,header-prompt.txt}`.

## Dürüst vizual status

Header/card/navbar kompozisiyası, rəngləri və nisbətləri yeni nümunəyə görə yenilənib. Bu generativ maketin piksel eyniliyi üzrə avtomatik diff PASS iddiası deyil: yeni panorama ayrıca rekonstruksiya olunub; native bina real seçilmiş material/accepted geometry ilə mövcud modular renderer-dən gəlir. Mövcud decorative preview6–8representative mərtəbə, metadata isə serverin həqiqi sayını göstərir. Telefon OS status/navigation bar native host şəkilində çəkilmir. Bu şəkillər synthetic HTTP data ilə real production Compose ekranının Robolectric NATIVE renderidir, real hesab/device screenshot-u deyil. Emulator açılmayıb. Physical device, TalkBack, başqa lokallar, IME, tablet və real GPU yoxlanmayıb. İstifadəçi qəbulu və digər ekranlara keçid ayrıca qalır. CI/main merge/push/deploy yoxdur.


Son mənbə nəticələri: native wildcard5test **5/5PASS28s**, son kart scroll/dock yoxlaması daxil. Son5PNG yenidən açılıb reference ilə baxıldı. Renderer əlavəsiz adi app unit41test, failures/errors0; assembleDebug/lintDebug **PASS30s**. Normal APK init-siz yenidən quruldu. Native əvvəlki mərhələ run-u da PASS idi, amma son miqyas/viewport düzəlişindən əvvəlki nəticə yekun sübut sayılmır. Son diff review və diffcheckPASS; scope xarici owner faylları commitə daxil edilmir.
