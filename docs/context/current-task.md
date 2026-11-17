# Cari tapşırıq — bütün app performansı / Android + AppBackend

## Completed

- Yeni vahid: idle snapshot reuse + APK atlas IO scheduling. `AcceptedEngineHost.snapshot` unchanged-state copy-ni, `LocalGameHost.snapshot` idle object yaradılmasını çıxarır; canonical frame polling saxlanır. `ApprovedArt.prepare` IO-da işləyir, root hazır olmayan atlas istifadəçilərinə native loading/retry göstərir. Home/profil/achievements/leaderboard/settings bu atlasları gözləmir. Warm-root volatile completion fast path var. Review `docs/reviews/2026-11-17-idle-art-scheduling.md`, evidence `docs/reviews/idle-art/`.
- Android Bazar bundled previews `2f81349`; paired backend `9f0faf9`:12kart14→2sorğu, köhnə server4paralel detail fallback, köhnə complete cache offline replay, fresh detail-before-command qorunub. Review `docs/reviews/2026-11-17-market-bundled-previews.md`.
- Cancelled-timeout fallback `c579e22`; əvvəlki dirty işlər `9cac996`-də qorunub. İnventar local-first `083f870`.
- Əvvəlki vahidlər/evidence: `docs/reviews/2026-11-17-{initial-account-sync,art-reuse,city-read-scheduling,city-local-first,city-same-uid-host,market-local-first,readonly-single-replay,production-overlap,https-overlap}.md`.

## Current state

Android `fix/menu-inventory-latency`, yeni vahidin bazası `2f81349`; vahid ayrıca commit-də saxlanır. AppBackend təmiz, `feat/wobbrik`, `9f0faf9`; bu vahiddə dəyişməyib. Əvvəlki DailyPlate işi tarixdə saxlanıb. Main merge, push, deploy, CI yoxdur. Bütün app performansı və redesign tamamlanmayıb.

## Decisions

- Yeganə plan `docs/PLAN.md` §1.1/§2–3. İstifadəçi Android və AppBackend performans düzəlişlərinə icazə verib; backend üçün təkrar icazə soruşma. Push/main merge/deploy ayrıca göstəriş tələb edir. CI yalnız ayrıca tələb olduqda main-də lokaldır; emulator açılmır, canlı yoxlama gate deyil.
- Exact Ready/login epoch, synchronous host revocation, sole journal və server-authoritative wallet/mülkiyyət dəyişmir. İlk sync recovery/reconcile+catalog-dur; art hazırlığı həmin gate-i tutmur. Home dərhal öz resource cutout-u ilə açılır; atlaslar auth entry-də arxa planda hazırlanır. Yalnız game/city/inventory/parts/collection/market/market-own hazır atlasları gözləyir; navbar aktiv, Play müvəqqəti disabled, failure retry var.
- Art qrupları hesabdan asılı deyil. Hazırlıq10bitmap/~60MiB retention-u auth entry-də daha erkən yaradır; Home-only istifadəçidə də tutulacaq. Activity/UID saxlanmır. Heap/battery/device speedup iddiası yoxdur. Raw art getter-ləri sinxrondur; root-dan kənar standalone consumer öz readiness sərhədinə cavabdehdir.
- Frame dövrəsi hələ canonical recovery/host lifecycle üçün saxlanır; yalnız idle snapshot allocation azalıb. Identity ilə immutable scene və bütün dəyişən presentation field-lər yoxlanır. Physics/finish/abandon/pause-clock qaydaları dəyişməyib.
- Bazar public query hər mount-da background refresh edir. Complete cache exact UID/revision/scope/filter/cursor ilədir; final revision sync çıxarılmır. Yeni include=previews representation köhnə complete bundle-in exact query+detail-ləri ilə replay ola bilər. Keş display-only, mutation üçün live detail+Ready lazımdır. Live cursor server-wide frozen snapshot deyil.

## Checks performed

- Yeni normal engine97/core147/app37unit, assembleDebug/lintDebugPASS26s; `/private/tmp/wobbrik-idle-art-complete.log`.
- JVM21 actual idle host path10000warmup+3600iteration: before701592allocated byte/2460µs/online3600+local3600distinct snapshot; after48byte/1301µs/1+1identity. Bu, JVM loop sample-dır; display-frame wakeup/device FPS/battery ölçüsü deyil və saniyəlik gecikməni izah etmir.
- Native cold prior3atlas helper375ms main-thread. Final isolated7/7nativePASS10.761s/Gradle16s; cold preparation285msIO, ready helpers91µs, workerblocked navigation47ms. Small/normal portrait,font1/font2,synthetic insets/Long.MAX balance, failure retry, cancelled-producer stale completion yoxlanıb.7PNG `docs/reviews/idle-art/rendered/` altında saxlanıb və açılıb. Pixel/crop2native+resource1nativePASS(Gradle9s), native7loading/IO ayrıcaPASS; nəticələr son review-da.
- Contrast Text/Surface13.00,Accent/Surface8.99PASS. Font2Long.MAX shared header clipping qalır; loadedHome fixture minimaldır, XPaccount yoxdur, PNGparity/mərhələqəbulu deyil. Tablet/device/Firebase/TalkBack/locale matrisi yoxlanmayıb.
- Digər native resource helper ölçüsü (ilk sample): Home30ms/6.29MiB,Profile97ms/25.17MiB,Achievements20ms/6.29MiB,Leaderboard29ms/6.29MiB.İlk sample2remount0newbitmap olsa da final repeat cold63/112/21/28ms; firstremountHome31ms/1newbitmap vəProfile51ms/2newbitmap göstərdi, secondremount0newbitmap. Resource cache variable-dır və repeatUIdecode qalır; bunlar bütün route/server timing deyil. Əlavə resource cache/dizayn uydurulmayıb.
- Bu vahiddə API/DTO/domain/schema/network query dəyişmir, backend dəyişikliyi/testi lazım olmayıb. Əvvəlki cross-stack vahiddə full backend race+disposablePostgreSQL18/vet/buildPASS(platform23.791s), Android147/37unit+18nativePASS. DBdayandırılıb, deploy yoxdur.

## Remaining work

- Bütün route üçün deployed/device2+s/10+s baseline və tam ekran composition/layout/network ölçüsü yoxdur. Resource helper qiymətləri bunun əvəzi deyil. Şəhər building detail fanout və başqa City/Purple art helper-ləri hələ yoxlanmalıdır.
- Real HTTPS cancellation blocking read-i kəsmir: controlled public→own→public16request/1same-path overlap; timeout exit~8s. Tapıntı/evidence `docs/reviews/2026-11-17-https-overlap.md`. Fallback cancellation qorunub, socket cancellation/coalescing implementasiya edilməyib. Scope/session/subscriber cancellation sərhədləri vacibdir.
- Android və AppBackend City query/request/byte/latency ölçülərini birlikdə yoxla. Bazar bundled endpoint backend-də hazırdır, deploy edilməyib; köhnə serverdə fanout qalır.
- Home/Profile resource cache dəyişkəndir: final təkrarda remountHome31ms/1bitmap vəProfile51ms/2bitmap UIdecode var. Configuration/density-ə uyğun background resource loading ayrıca vahiddə düzəlməlidir.
- Durable disk cost, main-thread custom art/map allocations, heap/GPU/device FPS və tam vizual parity qalır. Mövcud font2tab/navbar/scroll və320grid priceclipping borcları review-lardadır.

## Relevant files

- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{OnlineGame,ApprovedArt,ApprovedArtLoading,GameAtlas,FinishCosmeticScreen,CityScreen,ProfilePresentation,MobileHomeScreen,MobileAchievementsScreen,LeaderboardScreen}.kt`.
- `android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/{AcceptedEngineHost,LocalGameHost}.kt`; tests `IdleHostPollingTest.kt`, `LocalGameHostTest.kt`.
- Host `docs/reviews/idle-art/render/{OffMainArtTest,RemainingResourceDecodeTest,ColdArtBaselineTest}.kt`, init script; əvvəlki pixel test `docs/reviews/art-reuse/render/ArtReuseTest.kt`.
- Backend `/Users/mac/My Workspace/AppBackend`: `AGENTS.md`, `docs/context/current-task.md`, `internal/wobbrik/{city,market}.go`, `docs/wobbrik-game-api.md`. Mövcud persistent branch `feat/wobbrik`.

## Exact next step

PLAN §1.1, bu handoff, idle-art və HTTPS overlap review-larını oxu. Növbəti dar vahid üçün ölçülmüş Home/Profile remount resource decode-un configuration/density-aware background loading-ni hazırla (MobileHomeScreen/ProfilePresentation; final31/51ms). Müvafiq hazır native renderləri saxla, əlavə60MiB atlas retention-una ikinci kor process cache qatma. Alternativ ayrıca vahiddə production UrlConnectionGameplayTransport cancellation-a scoped socket disconnect tətbiqinin mümkünlüyünü yoxla və real loopbackTLS fixture ilə before/after ölç; UID/session publication, cachewrite və callback regression-larını saxla. Yaxud City fanout-u Android+AppBackend query ölçüsü ilə növbəti unit et. İnfrastruktur/coalescing kor-koranə əlavə edilmir. Eyni source render/test sübutları saxlanır; bütün app bitmiş kimi bildirilməsin.
