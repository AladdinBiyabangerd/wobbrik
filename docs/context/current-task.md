# Cari tapşırıq — Home/Profil resources + City/HTTPS / 17 noyabr 2026

## Completed

- Home city cutout, köhnə Profil4board və cari native Profil avatar sheet-i composition-owned, Configuration/resource/density-aware Dispatchers.IO loading-ə keçirildi. Process cache yoxdur; retry/stale completion qorunur, avatar grid və overview daxilində1producer paylaşılır. Home navbar/stats açıqdır, hero ölçüsü loading zamanı saxlanır.
- City Android ardıcıllığı real AppBackend HTTP handler+disposable PostgreSQL ilə yoxlandı:12bina,15request,175SQLstatement,6282JSONbyte; lokal73.565/63.367/61.744ms.21domainSELECT, qalanı middleware+BEGIN/COMMIT. Backend production source dəyişməyib.
- Real HTTPS fixture təkrarı4/4PASS:cancel→exit522msheaders/755msbody/7998mstimeout;public→own→public567/533ms, eyni-path overlap qalır. Socket cancellation düzəldilməyib.
- Əvvəlki atlas IO/idle snapshot vahidi `7f24bfc`; Bazar bundled previews Android`2f81349`/backend`9f0faf9`. Əvvəlki sübutlar tarixdə/review-larda saxlanır.

## Current state

Android `fix/menu-inventory-latency`, yeni vahid bazası`7f24bfc`. AppBackend təmiz `feat/wobbrik`/`9f0faf9`. Main merge/push/deploy/CI yoxdur. Bütün app performansı/redesign bitməyib.

## Decisions

- Yeganə plan `docs/PLAN.md` §1.1/§2–3. Android+AppBackend performans işi icazəlidir; backend üçün təkrar icazə soruşma. Main merge/push/deploy ayrıca göstərişdir. CI yalnız ayrıca tələb ilə main-də lokaldır. Emulator açılmır; canlı yoxlama gate deyil.
- Exact Ready/login epoch, sole journal, server wallet/mülkiyyət authority-si dəyişmir. İlk sync recovery/reconcile+catalog olaraq qalır.
- Əvvəlki atlas10bitmap/~60MiBprocessretention saxlanır. Yeni resource loader ikinci process cache yaratmır; remount decode yenə mümkündür, IO-da. Home logo/font və Profil foreground/skin CPUişi bu vahidin scope-u deyil.

## Checks performed

- Backend focused3raceintegrationPASS2.364s; real disposable PostgreSQL18. DB dayandırılıb. Full backend suite/vet bu production-code-unchanged vahiddə təkrarlanmayıb.
- HTTPS4nativePASS/Gradle19s. İlk paralel Gradle test-output collision-u uğursuzdur; ardıcıl təkrar PASS.
- Native10/10PASS11.035stest/Gradle27s:8loadedportretPNG(320/360,font1/2) və2resource/cancellationregression. PNG-lər açılıb; Home/Profil authorityPNG-lərlə müqayisə edilib. Əlavə inset/klaviatura yoxlanmayıb. Son normal core147/appDebug37unit+assembleDebug/lintDebugPASS; log `/private/tmp/wobbrik-resource-checks-shared.log`.
- Kontrast Text/Surface13.00,Accent/Surface8.99PASS. Native nəticələr/render matrisi son review-da saxlanır; device/Firebase/TalkBack/locale/runtimeproduction ölçüsü yoxdur.

## Remaining work

- HTTPS cancellation aktiv blocking socketread-i kəsmir; timeout-a qədər8s davam edə bilir. Scoped disconnect tətbiqi ayrıca correctness+realTLSbefore/after yoxlaması ilə edilməlidir; subscriber/session sərhədlərini pozma.
- City hər bina üçün detail+middlewarefanout saxlayır. Batching düşünülərsə Android+AppBackend additiveDTO/legacyfallback, geometry payload/response limit, revisionbracket/cache/accountisolation birlikdə hazırlanmalıdır. Təhlükəsizlik yoxlamalarını çıxarma.
- Production/device2+s/10+sbaseline, real payloadbytes/SQLcontention, durableIO, heap/GPU/FPS və tam vizualparity yoxdur. Home fixture XPaccount-suz ola bilər; font2Long.MAXbalanceclipping və əvvəlki navbar/scroll/grid borcları qalır.

## Relevant files

- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{ResourceArtLoading,MobileHomeScreen,ProfilePresentation,NativeProfileEditor,NativeProfileOverview}.kt`.
- `docs/reviews/2026-11-17-resource-background-city-https.md`, `docs/reviews/resource-background/` (native/TLS/backendprobe evidence).
- Android `core/.../game/{CitySceneReads,GameplayHttpBackend,ReadOnlyGameCache}.kt`; AppBackend `internal/{wobbrik/city.go,wobbrik/market.go,platform/auth.go}` və `docs/wobbrik-game-api.md`.
- Əvvəlki `docs/reviews/2026-11-17-{idle-art-scheduling,https-overlap,market-bundled-previews}.md`.

## Exact next step

Bu handoff+PLAN§1.1+son review-u oxu. Növbəti dar vahid HTTPS scoped socket cancellation-u realTLSfixture ilə before/after yoxlayıb düzəltməkdir; callback/cachewrite/login-isolation/cancellationregression-ları saxla. City batching alternativ növbəti vahiddir; paralel əsas plan/cacheframework uydurma. Yeni böyük dəyişiklik ayrıca chat-də davam etdirilsin.
