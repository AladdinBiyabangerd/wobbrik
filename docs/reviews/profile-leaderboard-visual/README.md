# Cari rekord renderləri — 15 noyabr 2026

Cari review: `../2026-11-15-records-fitted-implementation.md`. Yeni təsdiqli panelsiz dizayn production ekranına bağlıdır. `fitted-production/` raw PNG-lər, request metadata, native nəticələr, paritet və üç telefon board-unu saxlayır. Sintetik server cavabı ilə real Compose/repository host-dur; device/real HTTP deyil.

```sh
cd android-billing
JAVA_HOME=/Users/mac/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home ./gradlew --offline -I ../docs/reviews/profile-leaderboard-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*FittedLeaderboard*RenderTest'
```

Repo kökündən `tools/FittedDesignParityCheck.java` alətini Java21 ilə və `docs/reviews/profile-leaderboard-visual` arqumenti ilə işlət: üç əsas raw render təsdiqli fitted-proposal ilə tam piksel eyniliyi tələb edir; bütün raw çəkilişlərin geri düyməsi bölgəsini yoxlayır. `tools/DockProposalSheet.java <fitted-production-dir> production` bütöv viewport contact sheet yaradır. Hər snapshot sinfi təzə JVM; header/footer stitching yoxdur. Normal build/CI-yə qoşulmur; emulator açılmır.

Köhnə pagination host-u `render/ProfileLeaderboardRenderTest.kt.txt` kimi arxivlənib: artıq olmayan Daha çox/footer axınına aiddir, cari render komandası deyil. Aşağıdakı 28 nəticə yalnız əvvəlki dizaynın tarixi sübutudur.

---

# Profil/rekord renderləri

Cari nəticə: `2026-11-15-phase5-profile-leaderboard-parity.md` review-u.

- `final/`: 28 müstəqil production Compose host PNG. Server fixture sintetikdir; device/real HTTP deyil.
- `final-preview.png`: profil, həftəlik və bütün vaxtlar, 390×844/font1.
- `final-font2.png`: profil/editor/rekordlar, 320×568/font2; şəkillər tam viewport ilə göstərilir.
- `approved-comparison.png`: təsdiqli PNG və production host yanaşıdır; tam ekran pixel parity PASS deyil.
- `native-results.txt`: son host nəticəsi. `checks.txt`: son adi unit/build/lint/kontrast nəticəsi.
- Köhnə root PNG-ləri və `parity/` ara çəkilişləri tarixi sübutdur; natamam header/footer ola bilər, final kimi istifadə etmə.

Repo kökündən host:

```sh
cd android-billing
JAVA_HOME=/Users/mac/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home ./gradlew --offline -I ../docs/reviews/profile-leaderboard-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*ProfileLeaderboard*RenderTest'
```

Hər snapshot sinfi təzə JVM-dədir; header/footer postprocess/stitching yoxdur. Yalnız lazım olan state-in ilk tam çəkilişi saxlanır; eyni state-in ikinci çəkilişinin eyniliyi də yoxlanır. Sonra repo kökündən:

```sh
/Users/mac/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home/bin/java -Djava.awt.headless=true docs/reviews/profile-leaderboard-visual/tools/CaptureIntegrityCheck.java docs/reviews/profile-leaderboard-visual/final
```

Alət bütün geri düyməsi bölgələrini, rekordlar header/footer sahələrini piksel üzrə müqayisə edir və contact sheet-ləri yaradır. Source render PNG-ləri dəyişdirilmir. Bu init/alət CI-yə və normal build-ə qoşulmur; emulator açmır.
