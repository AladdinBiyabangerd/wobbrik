# Architecture

- Məqsəd: yekun real Android oyunu; ayrıca atılacaq prototip deyil.
- Əsas plan: `docs/kotlin-compose-plan.md` §§1,3–6; məhsul: `docs/game-plan.md`.
- Kod kökü: `android-billing/`; `:app` Compose/Canvas host, `:engine` Android-dən asılı olmayan Kotlin engine, `:core` müqavilə/orchestration, `:android` Android billing/data adapterləri. Mövcud paketlərə uyğun əlavə et.
- Paket/applicationId: `com.aladdin.wobbrik`; minSdk 24, target/compileSdk 36, JVM 21.
- Mövcud pinlər: Gradle 8.13, AGP 8.13.0, Kotlin/Compose plugin 2.1.10, Compose UI/foundation 1.7.6, Material3 1.3.1, Firebase BOM 33.7.0. Mənbə: `android-billing/{build.gradle.kts,app/build.gradle.kts,gradle/wrapper/gradle-wrapper.properties}`; bu xülasə re-pin icazəsi deyil.
- Backend: `/Users/mac/My Workspace/AppBackend`; Firebase Auth → app/UID identity, AppBackend/PostgreSQL → qəbul edilmiş oyun vəziyyəti, progression, wallet/ledger, mülkiyyət, inventar/bazar/şəhər.
- Room server snapshot/cache və exact retry outbox üçün planlaşdırılıb; DataStore cihaz seçimləri üçündür. Hazır olub-olmadığını aktiv branch-də yoxla; iki aktiv journal və ya ikinci wallet yaratma.
- UI/naviqasiya state/domain/data sərhədlərini çağırır; debug fixture canlı backend sübutu deyil. Offline yalnız məşq/cache-dir.
- Əsas naviqasiya və bərpa: plan §4; onlayn inteqrasiya: `docs/stage-three-online-plan.md`. Mərhələ ownership-i üçün plan §6-dakı 1–7 bölgüsünü istifadə et.
- Telefon/planşet, portret/landşaft və dəyişən pəncərə ölçüsü dəstəklənir. Locale siyahısını təxmin etmə; tapşırıqda plan/resurs və təsdiqlənmiş tələbləri yoxla.
