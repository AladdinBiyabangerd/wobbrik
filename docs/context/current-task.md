# Cari tapşırıq — Profil açılışı / 18 noyabr 2026

## Completed

- Əvvəlki İnventar/core cache/chrome optimizasiyaları saxlanır: disk-kilid gözləməsi621→1ms; real-Home host ilk inventory191→160ms; main header decode çıxarılıb. Əvvəlki review-lar ayrıca qalır.
- Profil təkrar mount-da son server ad/avatarını itirib GET gözləyirdi. Session/epoch-scoped, yalnız yaddaşda display memory authenticated root-a qoşuldu; background GET və fresh editor load saxlanır. Cached/loading/error profil redaktə hüququ vermir.
- Mobil portrait IO profile GET-lə paralel başlayır, overview/editor/picker bir route-owned sheet paylaşır. Tablet və Home eager art dəyişmir.
- Controlled400ms HTTP ilə host təkrar profil mətni451–475→24–38ms, avatar493–516→66–83ms. Hər giriş1GET/61response byte olaraq qalır. Cold ilk mətn529→582ms nümunələrində sürətlənmə sübut olunmayıb; main composition~35ms qalır. Device/percentile deyil.

## Current state

`fix/profile-open-latency`, baza54621a7. Əvvəlki owner inventory/cache/chrome dirty işi + bu vahiddə4production UI faylı, QA/docs dəyişiklikləri var. İki release baseline-profile binary-si iş ərzində ayrıca dirty göründü, toxunulmadı. Profil vahidi ayrıca commit-lə saxlanır; əvvəlki owner diff-i bu commit-ə daxil deyil. CI/merge/push/deploy yoxdur. AppBackend dəyişməyib.

## Decisions

- Yeganə plan docs/PLAN.md§1.1/§2–3; yeni stack/dizayn/prewarm yaradılmır.
- Profil memory-si yalnız son server identity-nin display-i üçündür, editor revision/capability deyil. Hər login epoch, eyni UID re-login daxil, yeni boş memory alır. Fresh GET uğursuzsa cached view qalır, Dəyiş disabled-dır.
- HTTP/public profile revision, conflict/exact retry, iqtisadi wallet/journal/authority dəyişmir. İlk proses girişi üçün saxta profil göstərilmir; durable profil cache əlavə edilməyib.
- CI yalnız ayrıca tələb ilə main-də lokal. Emulator avtomatik açılmır; ölçülməmiş runtime/render PASS deyil.

## Remaining work

- Digər menyuların konkret call path-ləri; production2+s/İnventar10+s şikayətinin real əvvəl/sonra ölçüsü. ART/frame/GPU/HTTP/payload/yaddaş/battery və authenticated Android→GoE2E ölçülməyib.
- Profil cold ilk GET və~35ms ilkin composition xərci qalır; route remount portrait IO hələ var. First-open/app-wide sürətlənmə iddiası yoxdur.
- Əvvəlki font2/iki sətir dock/PNG parity/real inset/IME/locale/TalkBack borcları saxlanır. Yeni alternativ dizayn tətbiq edilməyib.
- Əvvəlki inventory/chrome/core diff-i commit edilməyib. Profil commit-i main-ə merge edilməyib; merge istifadəçi təsdiqinə bağlıdır.

## Relevant files

- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{PublicAccountScreen,OnlineGame,NativeProfileOverview,NativeProfileEditor}.kt
- docs/reviews/2026-11-18-profile-open-latency.md; profile-open-latency/{render,evidence,rendered}/
- docs/reviews/mobile-sizing-visual/render/NativeProfileEditorRenderTest.kt — async art wait düzəlişi.
- Əvvəlki docs/reviews/2026-11-18-inventory-{composition-profile,first-open}.md. Profil QA fixture-i əvvəlki uncommitted testdən asılı olmamaq üçün eyni snapshot/catalog ilə müstəqilləşdirilib.

## Checks performed

- Son normal core154/app37unit+assembleDebug+lintDebugPASS51s; CI deyil. DiffcheckPASS.
- Native12/12PASS20s:2actual profile flow +5overview +5editor/avatar matrix. Ayrıca isolated profile ölçmə1/1PASS10s. Error/fresh gate, retry/save və eyni UID/new epoch/başqa hesab yoxlanıb. Aralıq FAIL-lər düzəldilib, review-da qeyd olunub.
-9native PNG və03-account.png açılıb müqayisə edilib; fresh/cached-error,320/390font1/2,360font2syntheticinset/uzunad/balans, editor və picker. Kontrast Text13.00/Muted8.22/Accent8.99/Danger8.37PASS. PNG-lər repo qaydasına görə ignored-dur, lokal saxlanır.
- Backend profile API/domain/schema uyğunluğu yoxlanıb; dəyişiklik tələb olunmur. Backend test/E2E işlədilməyib. Emulator açılmayıb.

## Exact next step

Bu handoff və18noyabr profile review-nu oxu. İstifadəçinin konkret növbəti menyu/call path göstərişini icra et; bütün app-i yenidən scan etmə və host rəqəmlərini device kimi təqdim etmə. Diff review/commit istənərsə əvvəlki owner işini ayıraraq saxla. CI/merge/push/deploy avtomatik yoxdur.
