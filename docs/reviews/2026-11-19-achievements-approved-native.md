# Uğurlar — təsdiqli kolleksiya dizaynının native tətbiqi

19 noyabr 2026. Branch `feat/achievements-approved-native`, baza `82e6ca0`; working diff. İstifadəçi `docs/design/achievements-collection-v1/proposal.png` dizaynını açıq təsdiqləyib implementasiyaya başlamağı tapşırdı; PLAN §1.1-də qeyd edildi. Owner Şəhər işi, release.dm və əvvəlki review düzəlişləri saxlanır. Bu vahid üçün commit/merge/CI/push/deploy yoxdur; Bazarın əvvəlki commit-i `82e6ca0`-dır.

## Native UI və real axın

Nailiyyətlər/Rekordlar artıq mövcud collection header-i, cyan alt xəttli tabları və Uğurlar aktiv7slotnavbarı istifadə edir. Kart, panel, progress, düymə, tab, balans, səviyyə, rekord, podium səthi və rank/score yazıları native Compose/Canvas-dır. Yeni trophy/shield/medal rasteri yalnız ayrıca illüstrasiyadır; board və sadə UI skin kimi işlənmir. Mənbə/prompt `docs/design/achievements-collection-v1/{asset-provenance.md,illustration-prompt.txt}`-dədir; built-in ImageGen RGBA1024×1536sheet-i dəyişdirilmədən layihəyə saxlanıb. Text/number/control yoxdur. Mövcud seçilmiş builder/pilot/architect avatar identifikatorları saxlanır; nümunədəki üzlər real profil seçimini əvəz etmir.

Nailiyyət kartları yalnız accepted snapshot `achievementProgress` və `achievements`-dən gəlir. Threshold-a çatmaq completion yaratmır; completion açıq server award ID-si ilədir. Unknown additive award və köhnə progress-siz snapshot fallback-i saxlanır. Group/tier sırası mövcud axındır. Snapshot üzrə memoization və award ID set-i təkrarlanan quadratik membership yoxlamasını çıxarır. Sabit ID-key-li LazyColumn, son kartdan sonra dock clearance və böyük şrift scroll-u var. Font2-də səviyyə ayrıca oxunaqlı mətndir; sözün ortadan qırılması əvəzinə uyğun sətir/horizontal scroll istifadə olunur.

Rekordlar mobil girişdə Ümumi scope ilə açılır, Həftəlik ayrıca scope-dur. Əvvəlki rank/score/acceptedAt/isSelf, immutable cursor snapshot, təkrar sorğu və explicit refresh davranışı saxlanır. Podiumdakı3oyunçu cəmə bir dəfə daxildir; sətirlər rank4-dən başlayır. Böyük şrift/uzun xal/az hündürlükdə mövcud measured-fit list fallback-i işləyir. Sabit7/8oyunçu uydurulmur, yalnız tam sığan sətirlər göstərilir; minimum4read büdcəsi minimum4visible demək deyil. Leaderboard scroll və Daha çox yoxdur. Font2 scope barı sözləri bölmədən horizontal scroll edir; seçilmiş scope görünən tərəfə gətirilir. Öz sətir cyan outline ilə vurğulanır. Loading/error/retry və accepted səhifə saxlanaraq refresh failure görünür.

## Oxu, cache və authority

Nailiyyətlərə giriş0əlavəHTTP-dir. Rekordlar parent authenticated host-da session/login epoch+account revision üzrə yaddaş saxlayır; maksimum2scope×20oyunçu/traversal. Remount və scope-a qayıdış eyni accepted səhifəni səbəbsiz yenidən oxumur. İlk giriş cache-miss loading shell-dir, art metadata-nı bloklamır. Optional sprite/portrait IO mövcud `rememberResourceArt` vasitəsilə background-da edilir; sheet hər mounted screen üçün birdir, kart/recomposition başına decode və bitmap allocation yoxdur. BitmapPainter source rect-lərdən istifadə edir. Compositiondən çıxdıqda worker cancel olunur; yeni eager başqa-screen prewarm/process cache yoxdur.

Server `expires_at` və `week_start` artıq mövcuddur. Core DTO-ya onların derived primitive `expiresAtMillis`/`weekStartKey` metadata-sı əlavə edildi; wire sorğusu/cavabı dəyişmədi. Native UI expiry/current-Baku-week müqayisəsini API24-də mövcud clock/Calendar ilə edir. Expired/keçmiş həftə səhifəsi reentry-də accepted fresh kimi göstərilmir; refresh lazımdır. DTO-nun əvvəlki əl ilə qurulmuş/default metadata-sı cache miss-dir. Mövcud core parserin java.time istifadəsi bu vahiddə dəyişdirilmədi; tam API24 runtime təsdiqi yoxdur. Hesab dəyişəndə parent memory yenilənir, screen də memory session-unu yoxlayır; in-flight result üçün coroutine cancellation/session və canonical Ready reference guard saxlanır. Read display cache mutasiya capability-si deyil. Wallet/journal/reward yolu dəyişmir.

AppBackend `internal/wobbrik/read.go`, `leaderboard.go` və `docs/wobbrik-game-api.md` read-only yoxlanıb: accepted achievement projection, owner/scoped cursor, expires_at/week_start və public profile fields mövcuddur. Yeni server API/schema/domain/SQL/index tələb edilmir. Backend-də kod/test/deploy edilməyib. Yeni HTTP wrapper/payload/cache-disk formatı yoxdur.

## Native render, fərqlər və məhdudiyyətlər

`comparison.jpg`, `responsive.jpg`, `states.jpg` son native host renderlərindən qurulub və açılıb baxılıb. Board müqayisəsi referensi öz nisbətində390pxenə kiçildir; native390×844eyni ölçülü reference viewport olduğunu iddia etmir. Synthetic accepted transport/account fixture-dir, real hesab/device screenshot-u deyil. Nümunənin1/8award sayı, score/ad/avatar və viewport-a uyğun sətir sayı domain məlumatı deyil və sabit kopyalanmır.

Native trophy/medallar həmin vizual dildə yeni ayrıca art-dır. Podiumun dekorativ daş/kol/flower bazası hələ PNG-dəki kompozisiyada yoxdur; pedestal gradient/laurel və badge silueti, header/tab tipoqrafiya/ikon detalları da tam piksel pariteti deyil. Source avatarlar real seçimi göstərir; sample üzlərlə eynilik gözlənilmir. Native pedestal score ink-i kontrast üçün tünddür. Kiçik ekranın mövcud collection navbar wrap/icon-only adaptasiyası saxlanır. Font2 scroll/stack/horizontal-scroll adaptasiyaları və long-wallet horizontal scroll screenshot-da bütün rəqəmləri birdəfəlik göstərmir; tam mətn semantikadadır. Bu fərqlər gizlədilmir və tam dizayn/mərhələ qəbulu iddia edilmir.

Emulator/canlı cihaz açılmayıb. Host API34 native graphics-dir; real ART/GPU/frame/production performance, API24 runtime, IME/TalkBack və bütün locale matrisi yoxlanmayıb. Planşet/landşaft dəyişdirilmir; portret kilidi saxlanır. Yeni latency büdcəsi yoxdur.

## Yoxlama və lokal review

Final yoxlama nəticəsi `achievements-approved-native/checks.log` və `test-results/`-dədir. Native matrix:390×844və320×568fontScale1/2;360×640font2+simulated24/48dpinset+uzun ad/Long.MAX_VALUE. Host flow tab/scope/back-to-tab read reuse, fresh refresh, offline retained/error/retry, expired-cache miss və lazy son karta scroll-u yoxlayır. Core focused suite auth epoch/cancellation/cursor/growth/request-bound/response parsing sərhədlərini yoxlayır. Palette checker11/11PASS, minimum7.41. `git diff --check`PASS.

Lokal source review: mobil route/tablet ayrımı, real award authority, accepted snapshot keys, entry budget/dedup, derived expiry metadata, session/revision/cancellation və optional art IO yoxlanıb. Aralıq compile-da role import çatışmazlığı, lint-də UI java.time API26 çağırışı və host testdə hələ compose edilməmiş lazy node-a performScrollTo istifadəsi tapıldı; import, primitive read metadata/Calendar və list-index scroll harness ilə düzəldildi. İlkin scope-return assert-i davam edən bounded growth sorğusundan əvvəl sample götürürdü; host harness settled read-dən sonra sayır. Bu aralıq hallar PASS sayılmayıb.

Təkrar (`android-billing/`):

```sh
./gradlew --offline -I ../docs/reviews/achievements-approved-native/render/render.init.gradle :app:testDebugUnitTest --tests 'com.aladdin.wobbrik.Achievements*Test' :core:test --tests 'appbackend.wobbrik.game.LeaderboardWindowTest' --tests 'appbackend.wobbrik.game.PublicProfilesLeaderboardTest' --tests 'appbackend.wobbrik.game.RecoveryDtosTest' :app:assembleDebug :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-achievements-native
```

Bu fokuslu yoxlamalardır, CI deyil. Görünüş tamamlanmasının qalan sərhədi yuxarıda açıqdır.

Son-source nəticə:5/5native host test,21/21fokuslu core test, `assembleDebug` və `lintDebug`PASS. Son-circle və font2scope-scroll düzəlişindən sonra host/build/lint yenidən işlədildi; core mənbəyi həmin son21PASS-dan sonra dəyişməyib. Son comparison/responsive/states yenidən açılıb baxıldı;320font2və360font2+inset-də ilk rekord görünür, son nailiyyət kartı scroll sonrasında dock-un üstündə tam görünür. Sərt scheduler millisecond assertion yoxdur.

Fixture ölçüləri: Nailiyyətlər0HTTP;390font1records cold2request/1391response-bodybyte (7+yalnız çatışmayan1oyunçu), digər normal/kiçikfont2və320font1cold1request/696byte, long fixture1request/713byte. Warm tab/scope return0əlavəHTTP/byte. Bunlar eyni synthetic fixture-də cold/warm müqayisəsidir, əvvəlki APK ilə production latency benchmark deyil. İlk accepted metadata host21–199ms, təkrar route host30–39ms; IO settle/capture və bütün flow müddəti ayrıca log-dadır və frame/ART/GPU ölçməsi deyil. Yeni sprite faylı təxminən2MiB,1024×1536ARGB üçün nəzəri6MiBpixel payload-dır; GPU allocation/APK compression ölçülməyib. Sheet yalnız mounted award screen-də decode olunur, metadata/naviqasiyanı bloklamır. Backend SQL dəyişmədiyi üçün yeni query-plan/scan benchmark aparılmayıb.
