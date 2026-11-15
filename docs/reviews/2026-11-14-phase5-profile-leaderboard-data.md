# Mərhələ 5 — rekordlar/profil data vahidi, 14 noyabr 2026

Branch: `feat/market-leaderboard`. Review scope: bu davamda əlavə edilmiş `PublicProfiles.kt`, `ProfileEditor.kt`, `LeaderboardReads.kt`, `PublicProfilesLeaderboardTest.kt`; `GameplayHttpBackend.kt` daxilində ProfileAccess, GET/PUT method və rekordlar read allowlist əlavələri. Əvvəlki bazar working diff-i qorunub; commit/merge/push/deploy yoxdur.

## Nəticə və lokal review

- AppBackend `docs/wobbrik-game-api.md` public profile və immutable leaderboard müqaviləsi, `internal/wobbrik/{profile,leaderboard}.go` yoxlandı. Backend dəyişikliyi tələb olunmur; mövcud branch müqaviləsidir, deploy sübutu deyil.
- Profil revision-u iqtisadi revision/journal/receipt-dən ayrıdır. GET və tam PUT replacement, 1–24 Unicode code point, server whitespace/control/format məhdudiyyətləri və üç allowlist avatar ID-si tətbiq edilir. Google ad/photo oxunmur.
- ProfileEditor bir login epoch-a bağlıdır. Qeyri-müəyyən PUT cavabı eyni replacement ilə təkrarlanır; başqa draft üçün əvvəl GET tələb olunur. Konflikt replacement-i atır, cari profili oxuyur və Conflict qaytarır; yeni save ayrıca caller/user əməliyyatıdır. Avtomatik overwrite yoxdur.
- Rekordlar all/weekly scope, opaque 256-character cursor, server snapshot/expiry/week, rank/floors/is_self/ad/avatar oxuyur. Traversal Mutex ilə serialdır, hesab epoch-u əvvəl/sonra yoxlanır, xəta cursor-u istehlak etmir. Səhifələr arasında snapshot/həftə/expiry/rank uyğunluğu yoxlanır. Privacy rank boşluqları qəbul olunur; result_id müxtəlif hesablarda toqquşa bildiyindən qlobal unikal hesab identifikatoru sayılmır. Öz nəticəsi yalnız serverin verdiyi səhifələrdə göstərilə bilər; ayrıca me endpoint-i uydurulmur.
- Lokal review zamanı çatışmayan AccountChanged import-u düzəldildi. Son fokuslu və tam core yoxlamaları bu düzəlişdən və editor əlavəsindən sonradır. Yeni konkret açıq data tapıntısı yoxdur.

## Yoxlamalar

Java21, offline `:core:test :app:compileDebugKotlin`: PASS, 5s. Core 130/130, yeni 7/7; failures/errors/skipped 0. Unicode/surrogate/control/avatar, exact retry, konflikt/refetch/no overwrite, eyni UID yeni epoch, cursor retry, həftə/snapshot qarışması və query injection yoxlanıb. `git diff --check`: PASS.

CI işlədilməyib (branch; istifadəçi ayrıca istəməyib). App assemble/lint, real HTTP PUT cihaz E2E, serverə canlı sorğu və ekran renderi bu vahiddə yoxlanmayıb. Production UI dəyişməyib. Təsdiqli play-progress-v2.png baxıldı; yeni asset/placeholder/redesign hazırlanmayıb. UI, avatar/podium asset-ləri, navigation və visual parity hələ açıqdır. Bu data vahidi mərhələ 5 qəbulu deyil.
