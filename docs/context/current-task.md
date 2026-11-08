# Home cutout + tam şəhər scope-u — 8 noyabr 2026

## Completed
- Home server rekord/XP binding və əvvəlki adaptiv home işi saxlanır (`69bc4fd` əvvəlki başlıq).
- Bu davamda logo/planşet panel və ikon miqyası, başlıq/statistika/düymə ölçüləri, yığcam XP kartı və CTA işıq gradienti düzəldi. Yeni 4dp/12sp tokenləri canonical tokens.css-dəki mövcud dəyərlərdir.
- Home şəhəri orijinal mobile-primary-v2 lövhədən piksel qoruyan maska ilə ayrıldı: 415×451 RGBA. RGB fərqi sıfır; yalnız alpha dəyişir. HomeScreen `home_city_cutout_v2` oxuyur; portret ölçüsü painter aspect ratio-sundan hesablanır. Köhnə assetlər qorunur.

## Current state
- Branch `feat/home-design-alignment`, son implementasiya `44946d8`, online baza `a230b3b`; bütün online tarix review edilməyib, main/online merge yoxdur. CI işlədilməyib.
- Home tam dizayn qəbulu deyil: generativ logo/ikon detalları, bütün səth/bevel effektləri və əlavə real XP sətrinin kart nisbəti hələ birəbir deyil. Mənbə cutout-un aşağı resolution-u planşetdə yumşaq görünür; upscale edilməyib.
- Şəhərim/İnventar/Bazar hazır route olmadığı üçün deaktivdir; bu yekun scope deyil. Tam şəhər yerləşdirməsi **təsdiqlənib və icra edilməyib**. Bu davamda AppBackend dəyişdirilməyib.
- Əvvəlki backend baxışı: GET city membership/population/revision/pagination və return var; placement_enabled:false. Yeni backend yoxlaması aparılmayıb.

## Decisions
- Hazır mobile/tablet dizaynları əsasdır; sistem fontu əvvəlcədən təsdiqlənmiş istisnadır, sualı yenidən açma.
- İstifadəçi əvvəl home şəhərini lövhədən ayırmanı, sonra ayrıca **imagegen əvəzinə lokal Python ilə piksel qoruyan maskalamanı** təsdiqlədi. Built-in imagegen namizədi rekonstruksiya olduğu üçün runtime-a bağlanmadı. Son PNG orijinal crop + committed alpha maskasıdır.
- Canonical planın cari icra göstərişi ölçüləri versiyalı oyun parametrləri kimi seçməyə icazə verir; köhnə “ölçülər açıqdır” statusu yeni icazə gözləməsi yaratmır. City geometry modeli hələ seçilməyib.
- Server authority qalır; home_city dekorativdir, real hesab şəhəri deyil. Opaque structural-v1-dən etibarlı footprint çıxarılmır. Legacy bina geometry etibarlı əldə edilənədək inventory-only qalır.
- Yeni scope/placement icazəsi lazım deyil. Real SKU/Console/push/deploy əhatədən kənardır. Online diff bütöv review edilmədən main-ə dolayı merge etmə.

## Remaining work
- Home qalan logo/ikon/səth uyğunluğu və canlı sync/finish e2e, real cihaz/TalkBack; tam qəbul verilməyib.
- Tam şəhər: versiyalı typed floor geometry/provenance; integer-tick dəqiqlik, dönmə/normalization; bina/torpaq/12 dekor footprint-ləri; union/transform/containment/overlap; atomik place/move/replace/return və dekor copy ownership; Android renderer/naviqasiya və backend+mobile testləri.
- Inventory pagination, Parametrlər, Room/outbox/fault proof ayrıca açıqdır. Read-only şəhəri tam yerləşdirmənin əvəzi kimi təqdim etmə.

## Relevant files
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{HomeScreen,OnlineGame,GameTokens}.kt
- docs/design/account-assets-v1/{home-city-cutout-v2.png,home-city-cutout-v2-mask.png,extract-home-city.py,README.md}
- docs/reviews/2026-11-08-home-design-alignment.md (son iki davam bölməsi); docs/reviews/2026-11-08-home-record-xp-ui.md
- docs/design/mobile-v1/{mobile-primary-v2.png,tokens.css}; docs/design/tablet-v1/{primary-landscape.png,portrait.png,README.md}
- docs/kotlin-compose-plan.md (Cari icra göstərişi, §5, §6); docs/stage-three-online-plan.md; docs/game-plan.md §7
- /Users/mac/My Workspace/AppBackend/{AGENTS.md,docs/wobbrik-game-api.md}; backend kodundan əvvəl öz AGENTS.md-ni oxu.
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{GameplayHttpBackend,RecoveryDtos}.kt

## Checks performed
- Son cutout/aspect ratio kodu :app:installDebug --offline PASS; /tmp/wobbrik-home-refinement-build.log. CI deyil. Ara named/positional Kotlin xətası və ilk cutout-un intrinsic kiçik render problemi düzəldilib son build təkrarlanıb.
- git diff --check PASS. Text/Surface 13.00, Muted/Surface 8.22, Background/Accent 11.45 PASS.
- extract-home-city.py PASS: source SHA256 yoxlanır; RGB difference zero, alpha (0,255); runtime/dizayn PNG eynidir. Tünd/açıq alpha QA baxılıb.
- Son cutout/aspect kodu ilə phone/landscape/portrait, böyük şrift, MaxInt64, qısa landşaft, error/loading fixture renderlərinə baxılıb; large/max scroll sonunda köməkçi düymələr görünür. Normal portretin köməkçi düymələri scroll altındadır. Dəqiq nəticə və əvvəlki short-bottom sübutunun sərhədi home review-un son sətirlərindədir. Frozen fixture canlı auth/backend/start/finish sübutu deyil. CI/main merge/push/deploy yoxdur.

## Exact next step
- Bu handoff və home review-un son bölməsini oxu, branch işini qoru. Qalan home fərqlərini və yeni cutout-un mənbə resolution məhdudiyyətini nəzərə al; sistem fontunu yenidən bloklayıcı etmə. Tam home qəbulunu iddia etmə.
- Sonra istifadəçinin artıq təsdiqlədiyi **tam şəhər yerləşdirməsini** AppBackend + Android-də icra et. AppBackend AGENTS.md və yalnız şəhər/engine geometry fayllarını yoxla; geometry qərarını mövcud canonical plana qeyd et, yeni paralel master plan yaratma. Scope-u read-only-a endirmə, yeni scope icazəsi istəmə.
- Main worktree /Users/mac/My Workspace/Wobbrik-rail-icons; bütün online diff review olunandan sonra main inteqrasiyası və yalnız main-də ./scripts/local-ci.sh.
