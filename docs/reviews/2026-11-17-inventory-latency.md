# İnventar — ilkin lokal təqdimat və optional şəhər oxusu

Branch `fix/menu-inventory-latency`, baza `de86aa3`. Review edilən diff: `AccountViews.kt`, `InventoryScreen.kt`, `AccountViewsTest.kt`; performans tapşırığının ilk dar vahididir. Bütün menyuların optimizasiyası və istifadəçinin bildirdiyi2+s/10+s gecikmələrin tam həlli iddia edilmir.

## Səbəb və düzəliş

Əvvəl siyahı yalnız `inventory page → cache bracket sync → bütün city pages → city bracket sync` bitəndən sonra Compose state-ə verilirdi. Optional şəhər timeout-u hazır binaları gizlədirdi. İndi siyahı şəhərdən asılı deyil; city ayrıca destination effect-də işləyir, cursor dəyişməsi onu yenidən başlatmır. Bir şəhər səhifəsi üçün ilkin ümumi4sorğu qalır; cursor-a görə city oxusunun yenidən çağırılma yolu çıxarılıb. Exact Ready daxilində completed-cache reuse əvvəldən var idi; onun qənaəti yeni düzəliş kimi təqdim edilmir.

`AccountViews.read` opt-in `onLocal` ilə matching hesab/revision qəbul edilmiş bütöv keşi I/O dispatcher-də oxuyub şəbəkədən əvvəl təqdim edir. İnventar bundan istifadə edir. `onCached` həmin təqdimatı read-only edir, `onFresh` yalnız uğurlu canlı/eyni exact capability-də artıq yoxlanmış nəticədən sonra bunu götürür. Cari sessiya dəyişibsə nəticə rədd edilir. Auth rejection offline uğuruna çevrilmir; lokal təqdimat görünürsə read-only qalır. Revision uyğun gəlmirsə köhnə siyahı göstərilmir. Disk replay/capture və parser işi UI dispatcher-də deyil. Cache, journal, mutation envelope və server authority dəyişməyib.

## Ölçmə və yoxlamalar

Nəzarətli test transport-u250ms saxlayır: son normal suite run-da lokal təqdimat5ms, canlı completion261ms. Bu, köhnə network-first completion nöqtəsi ilə yeni ilkin lokal callback arasındakı müqayisədir; production/cihaz benchmark-ı deyil. Eyni exact Ready daxilində10warm oxu: əlavə0sorğu/0response-body byte. TLS/request/header byte-ları ölçülmür. Sərbəst real hesab/server və ümumi route-to-frame baseline yoxdur.

`AccountViewsTest`:5/5PASS; transient fallback,4xx rəddi, explicit retry, eyni UID-də yeni auth epoch, hesab dəyişməsi, revision mismatch, cancellation və local-before-network/freshness yoxlanıb. Tam app28/28 və core138/138PASS. `:app:assembleDebug :app:lintDebug` PASS; yekun normal yoxlamalar44s. Log `/private/tmp/wobbrik-inventory-latency-checks.log`. `git diff --check` PASS.

Native Robolectric/Compose host5/5PASS:320/font1,320/font2,360/font1,390/font1,390/font2. Production InventoryScreen, synthetic repository/accepted account fixture; şəhər transport-u `awaitCancellation()` ilə heç cavab vermir, ilk bina yenə görünür. Bu test cache callback testindən ayrıdır; canlı server/cihaz sübutu deyil. Helper `/private/tmp/wobbrik-inventory-latency-host/InventoryLatencyRenderTest.kt`, init `/private/tmp/wobbrik-inventory-latency.init.gradle`, log `/private/tmp/wobbrik-inventory-latency-render.log`, PNG-lər `/private/tmp/wobbrik-inventory-latency-renders/`. Emulator açılmayıb.

320/font1/font2 və390/font1/font2 PNG-lər açılıb `design/unified-mobile-v3/01-main.png` ilə müqayisə edildi. Şəhər gəlmədən kartların göründüyü təsdiqləndi. Kompozisiya/tokens dəyişdirilməyib; PNG ilə tam parity iddiası yoxdur: normal kartlarda uzun adlar kəsilir, böyük şriftdə tablar üfüqi scroll tələb edir, yığcam dock-da yazılar gizlənir, illüstrasiya nisbətləri istinaddan fərqlidir. Bunlar bu loading düzəlişində həll edilməyib. İnset/klaviatura/uzun balans/offline cache üzrə ayrıca native matris yoxlanmayıb. Mövcud token kontrastları: Text/Surface13.00, Muted/Surface8.22, Accent/Surface8.99PASS.

## Backend uyğunluğu və məhdudiyyətlər

AppBackend AGENTS/README/handoff və API, `market.go:Inventory`, `city.go:City`, `read.go:Sync` source yoxlandı. Mövcud limit20/cursor müqaviləsi və app+uid filter-ləri saxlanır. Revision bracket sync çıxarılmayıb: əlavə trafikə baxmayaraq bütöv qəbul edilmiş cache sərhədi üçün qalır. Bu dar client scheduling/cache təqdimatı üçün API/DTO/schema/domain dəyişikliyi lazım deyil. Backend faylı dəyişməyib, backend test/load ölçməsi işlədilməyib; production performansı barədə nəticə yoxdur. Owner backend dirty işinə toxunulmayıb.

Lokal diff review-da şəhərin cache fallback-ının mutation availability-ni idarə etməməsi yoxlandı: şəhər burada yalnız kart dekorudur; inventarın öz local callback-ı read-only-ni qoruyur. Optional city cancellation udulmur. Hər effect-də sessiya/current yoxlaması saxlanıb.

Açıq: girişdə yüngül sync gözləmə ekranı və lazım olan warmup; digər menyular/detail üçün cache-first; Ready dəyişməsində təkrar capture; request coalescing/traffic-byte baseline; frame polling; main-thread art decode/bitmap allocation. Kod müşahidəsi: GameAtlas/rememberCosmeticArt hər composable instance-də decode edir; ölçülmüş əsas səbəb deyil. Bunlar növbəti vahiddir. CI, merge, push və deploy yoxdur.
