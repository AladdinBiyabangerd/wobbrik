# Cari tapşırıq — menyu/İnventar performansı

## Completed

İlk dar vahid: İnventar siyahısı optional şəhər oxusunu gözləmir; şəhər ayrıca effect-dir və inventory cursor onu yenidən başlatmır. `AccountViews.read` opt-in local-first callback təqdim edir; İnventar matching hesab/revision keşi dərhal göstərir, canlı nəticəyədək read-only saxlayır. Review: `docs/reviews/2026-11-17-inventory-latency.md`. Bütün menyu performans işi tamamlanmayıb.

## Current state

Branch `fix/menu-inventory-latency`, baza `de86aa3`; cari performans source/test/review vahidi bu branch-dədir; dəqiq commit üçün `git log -1`-ə bax. Əvvəlki iki baselineProfiles/{0,1}/app-release.dm və untracked `2026-11-17-current-screen-parity-audit.md` owner işidir; toxunma. Merge/push/CI yoxdur. Əvvəlki Home/header işi `de86aa3`-dədir.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1. Menyular lokal məlumatla interneti gözləmədən açılmalı, sync arxa planda qənaətli olmalıdır. Server wallet/mülkiyyət/qəbul authority-si, exact Ready/auth epoch/account/revision izolyasiyası və sole DurableJournalFile qalır. Bu vahiddə revision bracket `/sync` çıxarılmayıb. Stale revision cache göstərilmir; köhnə cache mutation capability deyil.

Bu chatdə əlavə istifadəçi: oyuna girərkən dərhal Home əvəzinə yüngül gözləmə ekranında lazımi hissələr internetlə sync oluna bilər. Giriş loading/sync axını növbəti vahidə daxil edilsin; tətbiq edilməyib. İlk vacib sync ilə optional menu warmup ayrılmalı; uzun/uğursuz şəbəkə tətbiqi sonsuz girişdə saxlamamalıdır. Dizaynın mövcud loading istinadı `docs/design/unified-mobile-v3/04-game-states.png`; yeni alternativ dizayn uydurma. Emulator avtomatik açılmır.

## Remaining work

İstifadəçinin müşahidə etdiyi2+s/10+s real route baseline və tam səbəb hələ ölçülməyib. Digər menyular/detail, art decode/bitmap allocation, frame polling, Ready dəyişməsində redundant capture, request coalescing və real payload/latency ölçüləri açıqdır. İnventar list ilk vahidi hazırdır; bütün app optimizasiyası deyil. Native renderdə əvvəlki uzun kart adı kəsilməsi/compact dock fərqləri qalır, dizayn qəbulu deyil. Mərhələ5 qəbulu ayrıca açıqdır.

## Relevant files

- `docs/PLAN.md` §1.1/§2–3/§7; `.cursor/context/architecture.md`; yuxarıdakı review
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{AccountViews,InventoryScreen,OnlineGame,WobbrikApplication,GameAtlas,FinishCosmeticScreen}.kt`
- `android-billing/app/src/test/kotlin/com/aladdin/wobbrik/AccountViewsTest.kt`
- `android-billing/core/src/main/kotlin/appbackend/wobbrik/game/ReadOnlyGameCache.kt`
- Backend `/Users/mac/My Workspace/AppBackend/{AGENTS.md,README.md,docs/context/current-task.md,docs/wobbrik-game-api.md}` və `internal/wobbrik/{market,city,read}.go`

## Checks performed

Normal app28/28, core138/138unit PASS; debug APK/lintDebug PASS44s; diffcheck PASS. AccountViews5test daxilində controlled250ms transport: local5ms/live261ms,10warm read0extra request/0response-body bytes (bu warm reuse əvvəldən var idi). Production benchmark deyil. Native host5/5:320/360/390portret,font1/font2; city heç cavab verməsə də list görünür.320/390font1/font2PNG açılıb baxıldı. Kontrast13.00/8.22/8.99PASS. Log və helper yolları review-da. Real device/auth/server/CPU decode ölçməsi, inset matris və canlı yoxlama yoxdur; PASS deyil. CI işlədilməyib.

Backend API/source uyğunluğu yoxlandı; bu dar scheduling/cache təqdimatı üçün backend dəyişikliyi lazım deyil. Backend test/load yoxlaması yoxdur. Mövcud AppBackend checkout başqa app dirty işindədir; toxunulmayıb. Wobbrik backend dəyişərsə AGENTS-dəki daimi `feat/wobbrik` qaydasını və dirty işi qoruma tələbini yenidən nəzərə al.

## Exact next step

Bu handoff və review-u oxu. PLAN §1.1 performans işinin növbəti dar vahidi: `OnlineGame`/`WobbrikApplication` giriş recovery-si və mövcud loading PNG əsasında istifadəçinin istədiyi yüngül ilkin sync ekranını qur; yalnız həqiqətən zəruri məlumatı gözlə, hesab/Ready izolyasiyasını saxla. Mövcud cache-dən warm göstərim ilə optional warmup-ı ölç və trafik artımı yaratma. Bundan əvvəl scoped source ölçməsi ilə təkrarlanan art decode-un menyu gecikməsinə təsirini dəqiqləşdir; böyük yeni işləri eyni chatdə yığma. İlk vahidi bütün performans tapşırığının tamamlanması kimi təqdim etmə. Merge/push/deploy/CI ayrıca göstəriş tələb edir.
