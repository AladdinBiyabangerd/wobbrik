# İnventar art-led dizayn sınağı — 18 noyabr 2026

Branch: `feat/inventory-art-led-redesign`, baza `81e0705`. Lokal diff review: bu vahidin aşağıda göstərilən production, design, render-harness və handoff faylları. Owner baselineProfiles/{0,1}/app-release.dm və `2026-11-18-inventory-city-detail-costs.md` əlavəsi scope xaricindədir, saxlanır.

## İstifadəçi qərarı və scope

Yeni chat maketinin yalnız İnventar/Binalar kök ekranında native implementasiyası təsdiqlənib. Digər səhifələr istifadəçinin bu nəticəni bəyənməsindən sonra ayrıca tapşırıqdır. Yeganə plan `docs/PLAN.md` §1.1; istinad `docs/design/inventory-art-led-v1/reference.png`. Detail və digər ekranlar əvvəlki dizaynda qalır. Yeni shared chrome yalnız `page == inventory && !commerceDetail` olduqda seçilir; digər ekranlara yayılmır.

## Dəyişiklik və lokal review

- GameTokens.InventoryCollection mövcud palitra və ölçü pillələrindən istifadə edir. Native Surface/tab/underline, bir yığcam wallet, kiçik refresh gövdəsi və 48dp input target, 7 girişli navbar və aktiv İnventar vurğusu. İkonlar mövcud native Canvas ikonlarıdır, raster crop deyil.
- Kartlarda real seçilmiş material və geometry ilə mövcud BuildingPreview daha böyük çəkilir. Metadata native Text-dir, ad uzun olduqda söz üzrə wrap olur. Şəhər nişanı artıq damı örtmür: aşağı sağda native city icon və `Şəhərdə` semantics var, yeni action deyil.
- Yeni bina/coin/state uydurulmur. Kart/detail/tab/nav əvvəlki real route callback-lərini çağırır. Press/hold/release/cancel native interaction animasiyası yalnız bu sınağın idarələrinə qoşulub; disabled refresh/Play scale reaksiyası vermir. Bütün app düymələrinin animasiyası tamamlanmış sayılmır.
- Raster yalnız yeni mətnsiz plaza/shrub backdrop illüstrasiyasıdır. Built-in ImageGen provenance və tam prompt design README-dədir. 512×512, təxminən 256KiB paket/1MiB RGBA. Optional IO decode root kompozisiyasında bir dəfə edilir və eyni bitmap bütün görünən kartlarla paylaşılır. Kart başına decode/allocation və yeni şəbəkə sorğusu yoxdur. Root scope bitəndə bitmap state buraxılır; detail/back root dəyişməsində yenidən decode mümkündür. UI/səth/ikon/mətn native olaraq qalır.
- Domain/data/network/cache koduna toxunulmayıb. Mövcud limit20 display inventory oxusu, bounded pagination/lazy keyed rows, UID/login/revision/cancellation/fresh mutation gate saxlanır. AppBackend `market.go` InventoryDisplay/ReadBuildingDisplay və optional in_city müqaviləsi yoxlanıldı; API/DTO/schema/economy dəyişmədiyi üçün backend dəyişikliyi lazım deyil. SQL və backend testləri bu sırf vizual vahiddə təkrar işlədilməyib.

## Yoxlama və düzəlişlər

İlk assembleDebug/lintDebug PASS. İlk host run-larda fixture `buildings/b-1?view=display` yolundakı query-ni rəqəm kimi parse edirdi və təkrar mərtəbə mətnində çoxlu node selector-u lazım idi; fixture düzəldildi. Sonra yalnız font2-də offscreen Dekor üçün scroll, font1-də isə artıq görünən Dekor üçün scroll-suz klik tətbiq edildi. Bu ilkin run-lar PASS sayılmır.

Son native run: `:app:testDebugUnitTest --tests com.aladdin.wobbrik.InventoryRedesign*Test` + assembleDebug/lintDebug, offline host init, **5/5 PASS**, build successful 52s. Son production mənbə ilə 390×844 font1/font2, 320×568 font1/font2 və 360×640 font2 + simulated top/bottom inset + uzun ad/Long.MAX_VALUE balans renderləri açılıb baxıldı və maketlə müqayisə edildi. Qalın çərçivələr çıxıb, bina böyükdür, kartlar bərabərdir, normal enlərdə yeddi giriş bir sıradadır. Kiçik enlərdə 48dp sahələri qorumaq üçün navbar tərəfləri iki sıraya keçir, font2-də label-lər gizlənir, accessibility adları qalır. Böyük şriftdə tablar üfüqi scroll olunur; aktiv underline qalır, uzun ad söz üzrə wrap olur, siyahı vertical scroll qalır.

Native test axını hər ölçüdə 3 inventory read (ilk giriş, detail-dən geri, explicit refresh), 1 detail read; ümumi 13833 response byte, 0 city read, 0 mutation submission. Fixture 9 binadır, server/account deyil. Bu performans yaxşılaşması ölçməsi deyil; cache-li warm/offline/refresh/account boundary suite bu vizual diff üçün təkrar işlədilməyib. Əvvəlki cache/revision qaydalarını dəyişən kod yoxdur. ART/frame/GPU/battery və production latency ölçülməyib.

Kontrast ui-craft: Text/Surface 13.00, Muted/Surface 8.22, Text/Raised 10.24, Accent/Raised 7.08, Background/Accent 11.45 — hamısı 4.5-dən yuxarıdır. Touch target semantics yoxlamaları altı nav girişi, Play və Refresh üçün ən azı48dp PASS. `git diff --check` PASS.

Sübutlar: `inventory-redesign/evidence/inventory-{regular,small,regularfont,smallfont,insets}.png`, native-build.txt, regular.xml, contrast.txt; render init və test həmin qovluğun render/ altındadır. Native şəkillər repo qaydasına görə ignored lokal review artefaktlarıdır. İstinad design/ altında saxlanır.

## Vizual fərqlər və məhdudiyyətlər

Bu tam piksel pariteti deyil. Maket yeni generativ bina illüstrasiyasıdır; production screenshot-da mövcud purple/teal modular art və həqiqi seçimlər qalır. Mövcud dekorativ preview 6–8 representative mərtəbə göstərir, label serverdəki tam mərtəbə sayıdır. Bu əvvəlki davranış dəyişməyib. Maketin header-dəki ayrıca şəhər/yarpaq fonu və bəzi işıqlandırma detalları yoxdur. Native ikonlar əvvəlki native siluetlərdir; generativ ikonla eyni piksel deyil. Şəhər statusu maketdə olmayan qəbul edilmiş real metadata-dır. Native üst status/navigation bar host renderdə telefon screenshot-u kimi çəkilmir.

ADB device siyahısı boşdur. Emulator açılmayıb. Buna görə şəkil **production Compose komponentlərinin synthetic HTTP məlumatlı Robolectric NATIVE host renderidir**, real cihaz/real hesab screenshot-u deyil. Fiziki cihaz, TalkBack, real OS inset/IME, başqa lokallar, tablet və GPU yoxlanmayıb. İstifadəçi qəbulunu və digər səhifələrin redesign-ına icazəni əvəz etmir. CI/main merge/push/deploy işə salınmır.

Renderer əlavəsi olmadan son normal yoxlama: app unit 41 test, failures/errors 0; assembleDebug/lintDebug **PASS25s**. Native screenshot-lar yuxarıdakı son5/5run-a aiddir, adi APK renderer init-siz yenidən quruldu. Lokal review-dan sonra yeni problem qalmadı; sadalanan vizual fərqlər və cihaz məhdudiyyəti açıqdır.
