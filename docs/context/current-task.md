# Cari tapşırıq — native mobil UI / commit handoff

## Completed

17 noyabr: mövcud working diff asılılıqları ilə yoxlanıb10 məntiqi code commit-inə ayrıldı. Core server achievement progress və dekor position; native token/surface/icon/shell; Home+Nailiyyətlər; Rekordlar; əvvəlki Profil/editor; Parametrlər; İnventar/Bazar; Şəhər böyük şrift; isolated mock build. Son vizual vahid İnventar bina detalının üst xülasəsi (fine-rim commerce kart, yaşıl read-only Sənindir kapsulu, uzun ad horizontal scroll)391f373 daxilindədir. Authority/rule mirror03ea7ba, tarixi review/QA mənbələri fae26b3. Tam siyahı: docs/reviews/2026-11-17-native-ui-logical-commits.md.

## Current state

Wobbrik branch feat/community-tabs-native-consistency; son production commit79d2a5a, sənədlər fae26b3-dək. Main6c96e9c dəyişmir. İki əvvəlki generated app/release/baselineProfiles/{0,1}/app-release.dm commit edilmədən saxlanır: generasiya provenance-i yoxlanmayıb. Bərpa snapshot-u /private/tmp/wobbrik-pre-logical-commits-20261117.tar.gz. Stage/reset/stash ilə owner işi itirilmədi. Android46 snapshot faylının baytları bölmədən sonra eynidir. Bu tur AppBackend-də dəyişiklik edilmədi; əvvəlki backend achievement progress commit cb3cab9 tarixidir, deploy edilməyib.

## Decisions

Yeganə plan docs/PLAN.md§1.1; vizual authority unified-mobile-v3/README.md (Home frameless, başqa ekranlar01–05). Son istifadəçi kod+asılılıqları yoxlamaq, məntiqi commit-lərə ayırmaq və commit etmək üçün açıq icazə verdi; köhnə no-commit qərarı bununla əvəz olundu. 17 noyabr son göstəriş: bu branch üçün push, remote main merge və lokal main yenilənməsi icazəlidir; deploy/CI tələb edilməyib. Bu tur Profilə yeni düzəliş edilmədi, əvvəlki işi ayrıca commit olundu. Mobil Bazar/İnventarda Ərazi girişi qaytarılmır; saxlanmış server hüquqları/dormant legacy kodu dəyişmir. Press animasiyası ayrıca tapşırıqdır. Yeni alternativ dizayn/səviyyə rəqəmi/placement hüququ uydurulmur.

## Checks performed

Hər code namizədi git write-tree/archive ilə disposable /private/tmp/wobbrik-commit-candidate qovluğuna çıxarılıb, unstaged production source olmadan yoxlandı. İlk iki core candidate :core:test PASS;138/138 failure/error/skipped0. Qalan UI candidate-lər :app:compileDebugKotlin PASS. Son mock candidate: debug24/24 və mock27/27 test, debug/mock APK, compileReleaseKotlin, lintDebug/lintMockDebug PASS2m56s. Mock manifest .mock paketindədir, INTERNET və FirebaseInitProvider yoxdur. Kontrast5/5 PASSmin8.59; sahiblik11.82. Diffcheck PASS. Debug/release compiled class-larında MockGameplayTransport yoxdur; mockDebug-də var. Son native production host52/52 PASS3m12s (28suite, failure/error/skipped0); çıxış /private/tmp/wobbrik-committed-native-renders. Son əlavə tarixi QA Kotlin mənbələri compileDebugUnitTestKotlin PASS3s. Emulator və CI işə salınmadı.

## Remaining work

Tam app/PNG pariteti və mərhələ qəbulu açıqdır. İnventar Bina məlumatı panelinin təsdiqli native sətir kompozisiyası və başqa konkret ekran fərqləri tamamlanmayıb. Small/font2 body scroll qalır; sahiblik capture-ında alt padding viewport kənarında qismən kəsilə bilər, mətn görünür. Real device/account/TalkBack/IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Backend deploy ayrıca göstəriş tələb edir; mock real server authority deyil. Ölçülmüş axıcılıq işi açıqdır. İki .dm artifact ayrıca təkrar generasiya/yoxlama qərarı gözləyir, silinməyib.

## Relevant files

- docs/PLAN.md§1.1; docs/design/unified-mobile-v3/{README.md,02-market.png,05-owned-and-listings.png}
- docs/reviews/2026-11-17-native-ui-logical-commits.md;2026-11-17-inventory-detail-summary.md;2026-11-17-market-own-listings.md
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,InventoryScreen,MarketScreen,GameTokens}.kt
- docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest,MobileShellRouteRenderTest}.kt

## Exact next step

Yeni feature-a keçmədən istifadəçinin növbəti dar vizual tapşırığını götür. Son müəyyən edilmiş namizəd: PLAN§1.1/02-market üst orta əsasında yalnız Bina məlumatı panelinin təsdiqli sətirlərini mövcud real field-lərlə tutuşdur; serverdə olmayan səviyyəni uydurma. Hazır üst xülasə/Yeni elan/Mənim elanlarım kompozisiyasını təkrarlama. Profilə toxunma, Ərazini qaytarma. Merge/push/CI-ni ayrıca tapşırıq olmadan etmə.

## Remote integration — 17 November 2026 (completed)

User authorized reviewed native UI branch push and history-preserving remote main merge, followed by local main update. Review: docs/reviews/2026-11-17-native-ui-remote-merge.md. Production source unchanged since 2f1a623; focused premerge checks PASS. Two dirty generated .dm files remain outside commits. AppBackend Wobbrik branches are already merged; remove only merged obsolete refs/worktrees while preserving unrelated dirty recipe work. Exact next step after integration: await the next scoped user task; outstanding visual parity limitations above remain.

- Completed: native UI branch pushed and merged preserving history as Wobbrik main `0315674`; local main fast-forwarded and matched remote. GitHub connector PR creation returned 403, so normal Git merge/push was used. No force push. Focused premerge checks PASS; no CI/deploy/emulator. Two local generated .dm changes excluded and retained.
- AppBackend: 16 explicitly verified merged obsolete Wobbrik local refs and remote attempt-abandon ref removed; no old worktrees remained. New persistent feat/wobbrik and feat/dailyplate branches exist locally/remotely. User-requested policy committed in AGENTS.md and .cursor/rules/app-branches.mdc; merged/pushed main cb684a7. No backend runtime/schema/API change. Temporary task worktrees removed after use.
- Decisions: AppBackend app work uses only its persistent branch; main integration, push/deploy permissions and safe dirty-work handling remain enforced. Prior visual parity/runtime limitations remain unresolved.
- Exact next step: await a new scoped user task; read this handoff and docs/PLAN.md §1.1. Do not start another feature, deploy or CI automatically.
