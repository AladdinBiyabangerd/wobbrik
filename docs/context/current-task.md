# Yekun Android oyunu — icra başlanğıcı, 4 noyabr 2026

## Completed
- AGENTS.md, əvvəlki handoff, kotlin-compose-plan və game-plan oxundu; billing strukturu və AppBackend attempts/city/billing müqaviləsi yoxlandı.
- Wobbrik-də Git repository və mövcud sənəd/billing baseline commit-i yaradıldı: b61a6ef. Əvvəl repository yox idi. Output konseptləri yerindədir, Git-ə daxil deyil.
- chore/local-ci-workflow branch-ində always-apply rule və AGENTS qaydası: uyğun branch/commit, lokal review, lokal CI, failure → fix → rerun, uğurdan sonra merge. GitHub CI yoxdur.
- scripts/local-ci.sh və billing Gradle ci task: whitespace, core test, Android AAR build/lint. Arrowz root ci task yanaşması nümunə götürüldü.

## Current state
- Yekun Android hostu, engine və oynanan tikinti dövrü HƏLƏ YOXDUR. Bu chat yalnız workflow vahidini tamamlayır; əsas oyun vahidi tamamlanmış deyil.
- Billing Kotlin 2.1.10 / AGP 8.13.0 / Gradle 8.13 / JDK21 / SDK36-dır. Host auth, lifecycle və real alış inteqrasiyası qalıb.
- AppBackend qovluğu da Git repository deyil. Bu vahiddə backend faylı/domain/API/DTO/schema dəyişməyib; workflow üçün buna ehtiyac yoxdur. Gələcək domain işi onun branch/CI hazırlığını da tələb edir.
- Backend wobbrik-structural-v1 checkpoint snapshot-u opaque JSON-dur, typed geometry/fizika yoxlamır. city.go placement_enabled:false saxlayır. Mövcud snapshot placement üçün sübut deyil.
- UI/token faylı tapılmadı. Vizual dəyişiklik və render yoxdur; ui-craft oxunub.

## Decisions
- Son göstəriş: ayrıca atılacaq prototip deyil, yekun Kotlin + Compose Android oyunu. İlk oyun vahidi yekun struktur + əsas oynanan tikinti dövrüdür.
- Ölçüləri versiyalı oyun dizaynı parametrləri kimi seçmək artıq İCAZƏLİDİR; əvvəlki ölçü mənbəyi gözləmə qərarı əvəz olunub. Geometry həmin modeldən çıxarılır; rəqəmlər real bina ölçməsi deyil. Konkret rəqəmlər hələ seçilməyib.
- Pure Kotlin engine; Compose Canvas/HUD; fizika draw callback-də deyil. Yeni mərkəzi token faylı yaratmaq bu işin hissəsidir. Billing modulu qorunur, ikinci wallet yoxdur.
- Offline məşq iqtisadi mükafat/inventar yaratmır. Onlayn progression üçün AppBackend authority, revision/dedup və hesab izolyasiyası qorunmalıdır.
- Placement yalnız typed geometry/provenance, union, transform, containment və overlap server yoxlamalarından sonra açılır. Legacy opaque binalar inventarda qalır.
- Real SKU, Console, credential və deploy yoxdur.

## Remaining work
- Yekun Gradle app/engine strukturunu mövcud billing modulları ilə qur; boş gələcək feature modulları yaratma.
- Ölçü/fizika modelini sənədləşdir və geometry-ni ondan çıxar. Kran/drop/eniş, tarazlıq/xilasetmə, artan məhdud çətinlik, ilk 5 bağışlayıcı mərtəbə, failure/restart, kamera və lifecycle fasiləsini implementasiya et.
- Engine invariant/replay testləri və Android build/lint; ui-craft token/kontrast və faktiki render/baxış. UI görünmədən tamam iddiası yoxdur.
- Domain müqaviləsi dəyişirsə AppBackend AGENTS/README/handoff/API-ni oxu, uyğun branch və backend testləri ilə yenilə. Placement hazır sayılmır.

## Relevant files
- AGENTS.md; .cursor/rules/local-delivery.mdc; scripts/local-ci.sh; docs/reviews/local-ci-workflow.md
- docs/kotlin-compose-plan.md (yeni cari göstəriş və §3–5), docs/game-plan.md (§2–5, §8, §23)
- android-billing/{settings.gradle.kts,build.gradle.kts,README.md,core,android}
- AppBackend: AGENTS.md; docs/wobbrik-{billing,game-api}.md; internal/wobbrik/{attempts,city,finish}.go
- Arrowz: build.gradle.kts ci task; gradle/libs.versions.toml. Arrowz dəyişdirilməyib.

## Checks performed
- sh -n scripts/local-ci.sh və git diff --check keçdi.
- İlk lokal CI BUILD SUCCESSFUL: AAR/lint icra edildi; core:test əvvəlki nəticədən up-to-date idi. Təzə yoxlama üçün --rerun-tasks ayrıca işə salındı; yekun nəticə docs/reviews/local-ci-workflow.md-dədir.
- Backend runtime testi yoxdur: backend dəyişməyib. Cihaz/render/real alış yoxlanmayıb.

## Exact next step
- Bu handoff və docs/reviews/local-ci-workflow.md-ni oxu, git status/log ilə workflow merge vəziyyətini təsdiqlə; feat/android-construction-loop branch-i aç. Yalnız istinad edilən faylləri oxuyaraq yekun app + engine strukturunu və əsas oyun dövrünü implementasiya et. Ölçüləri dizayn modeli kimi seç və sənədləşdir; backend iqtisadi müqaviləsini pozma, placement bağlı qalsın. Lokal CI-ni yeni modullara genişləndir, review/fix/rerun uğurundan sonra merge et.
