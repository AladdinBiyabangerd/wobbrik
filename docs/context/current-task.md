# Yekun Android oyunu — icra başlanğıcı, 4 noyabr 2026

## Son tamamlanan vahid — qalan mobil PNG-lər
- Completed: 12 görünüş üç PNG-də: docs/design/mobile-v1/{inventory-market-v2,play-progress-v2,account-states}.png. Built-in imagegen; hamısına baxılıb. Mərtəbə həddi və nəticə mükafatı düzəldilib, icazəsiz enerji pill-i silinib.
- Current state: yalnız mobil portret konseptləri; tətbiq/backend dəyişməyib. Branch feat/mobile-remaining-png. Əvvəlki Android işi aşağıda tarixi kontekstdir.
- Decisions: v2 primary və tokens.css mənbədir; rəqəmlər/kataloq illüstrativdir, game-plan qaydalarını dəyişmir. Server placement bağlı qalır.
- Remaining work: alış/elan əməliyyatları, tam əldəetmə şərtləri və yüklənmə; ana ekranda şəhər girişi; ayrıca planşet/landşaft və responsiv runtime yoxlaması.
- Relevant files: docs/design/mobile-v1/remaining-screens.md və üç cari PNG; docs/reviews/2026-11-04-mobile-remaining.md.
- Checks performed: üç son renderə vizual baxış; altı token kontrastı keçdi; ilkin lokal CI uğurlu. Son CI/merge nəticəsi review qeydindədir.
- Exact next step: remaining-screens.md və üç cari PNG-ni oxu; istifadəçinin seçdiyi növbəti kiçik dizayn vahidini hazırla. Android implementasiyasına avtomatik keçmə.

## Completed
- AGENTS.md, əvvəlki handoff, kotlin-compose-plan və game-plan oxundu; billing strukturu və AppBackend attempts/city/billing müqaviləsi yoxlandı.
- Wobbrik-də Git repository və mövcud sənəd/billing baseline commit-i yaradıldı: b61a6ef. Əvvəl repository yox idi. Köhnə output konseptləri silinib; qüvvədə olan iki referans docs/design/ altında Git-də saxlanılır.
- chore/local-ci-workflow branch-ində always-apply rule və AGENTS qaydası: uyğun branch/commit, lokal review, lokal CI, failure → fix → rerun, uğurdan sonra merge. GitHub CI yoxdur.
- scripts/local-ci.sh və billing Gradle ci task: whitespace, core test, Android AAR build/lint. Arrowz root ci task yanaşması nümunə götürüldü.

## Current state
- Yekun Android hostu, engine və oynanan tikinti dövrü HƏLƏ YOXDUR. Bu chat yalnız workflow vahidini tamamlayır; əsas oyun vahidi tamamlanmış deyil.
- Billing Kotlin 2.1.10 / AGP 8.13.0 / Gradle 8.13 / JDK21 / SDK36-dır. Host auth, lifecycle və real alış inteqrasiyası qalıb.
- AppBackend qovluğu da Git repository deyil. Bu vahiddə backend faylı/domain/API/DTO/schema dəyişməyib; workflow üçün buna ehtiyac yoxdur. Gələcək domain işi onun branch/CI hazırlığını da tələb edir.
- Backend wobbrik-structural-v1 checkpoint snapshot-u opaque JSON-dur, typed geometry/fizika yoxlamır. city.go placement_enabled:false saxlayır. Mövcud snapshot placement üçün sübut deyil.
- UI/token faylı tapılmadı. Vizual dəyişiklik və render yoxdur; ui-craft oxunub.

## Decisions
- Şəhərin yeganə vizual mənbəyi docs/design/README.md və city-plan.png / city-view.png-dir: yuxarıdan yerləşdirmə + eyni şəhərin həcmli görünüşü. Köhnə output/ui-concepts dəsti, prompt və tokenləri silinib; onlardan kod yazılmır.
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

# Aktiv vizual vahid — mobil PNG, 4 noyabr 2026

## Completed
- İstifadəçi şəhər referanslarından token dəsti yaratmağı seçdi. mobile-v1/tokens.css yaradıldı; 6 kontrast cütü keçdi.
- Built-in imagegen ilə ana ekran, oyun, şəhər görünüşü və plan üçün dörd panelli PNG yaradıldı; Burax yazısı düzəldildi, hər iki renderə baxıldı.
## Current state
- İlk mobil konsept dəstidir, bütün ekranlar və responsiv implementasiya tamam deyil. Branch feat/mobile-design-png; əvvəlki untracked responsive rule fayllarına toxunulmur.
## Decisions
- Təsdiqlənmiş city-plan/view referansları saxlanır. Tokenlər yeni konsept mənbəyidir, Android theme deyil. Domain/API dəyişmədiyi üçün backend dəyişikliyi lazım deyil.
## Remaining work
- Ana ekranda birbaşa şəhər girişi; qalan mobil ekranlar və vəziyyətlər; kiçik/geniş telefon və landşaft renderləri. Sonra ayrıca planşet kompozisiyaları, iki panel və ölçü yoxlaması.
## Relevant files
- docs/design/mobile-v1/{README.md,tokens.css,prompt.txt,mobile-primary.png}; docs/reviews/2026-11-04-mobile-design.md; docs/design/README.md.
## Checks performed
- Generativ PNG vizual baxışı; altı token kontrast cütü; lokal CI BUILD SUCCESSFUL. Runtime/device/responsivlik yoxlanmayıb. Son CI və merge nəticəsi review qeydinə yazılır.
## Exact next step
- mobile-v1/README.md-ni və PNG-ni oxu; yeni mobil konsept vahidində ana ekran şəhər girişini və inventar/bina detalı/bazar ekranlarını eyni tokenlərlə hazırla. Android implementasiyasına avtomatik keçmə. Planşetlərdə telefonu sadəcə böyütmə.

## Son düzəliş — dam/mərtəbə
- Cari mobil PNG: docs/design/mobile-v1/mobile-primary-v2.png. Aktiv oyun damsız və eyni ölçülü mərtəbələrlə yenidən render edilib, görüntüyə baxılıb. Əvvəlki PNG arxivdir.
- docs/game-plan.md §3: dam yalnız uduzandan sonra avtomatik əlavə olunur; kran dam daşımır. Mərtəbə eni/dərinliyi/hündürlüyü eynidir. Engine və backend kodu dəyişməyib.
- Review: docs/reviews/2026-11-04-floor-roof.md. Növbəti işdə v2 PNG-ni mənbə götür; əvvəlki qalan mobil/planşet işləri qüvvədədir.
