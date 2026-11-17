# Bina detalı — native artwork şüaları

Branch `feat/native-ui-parity`, baza `141863d`. Authority: `docs/PLAN.md` §1.1 və `docs/design/unified-mobile-v3/{README.md,02-market.png}`. Dar fərq: əvvəlki radial işığa çatışmayan şüalı fon əlavə edildi. Feature-private `OwnedBuildingDetailArtwork` daxilində 12 dar sektor native Path və radial Brush ilə çəkilir; Path/Brush ölçü üzrə drawWithCache-də saxlanır. Artwork sərhədi clipRect ilə qorunur. Həndəsi nisbətlər illüstrasiya koordinatlarıdır; yeni spacing/font/rəng tokeni yoxdur.

Mövcud Raised tokeni və şəffaf fade istifadə edilir. İki qat da eyni rəng olduğundan kompozit Raised-dən açıq olmur: fallback Muted/Raised 6.48:1, Text/Raised 10.24:1, contrast.py --fail-under4.5 PASS. Real BuildingPreview geometry/selected parts/fallback, ölçülər, mətn, semantika, input, action və tablet renderer-i saxlanır. API/DTO/domain/schema/wallet/ownership/şəhər müqaviləsinə toxunulmur; backend dəyişməsi lazım deyil. Yeni placement hüququ yoxdur.

İlk sandbox Gradle cəhdi cache lock icazəsinə görə başlamadı. İlk compiler run drawWithCache-də center-in hazır property olmadığını göstərdi; ölçüdən explicit Offset hesablanaraq düzəldildi. Kontakt vərəqi üçün PIL mövcud deyil; renderlər sips ilə yalnız QA baxışı üçün kiçildildi. Bu cəhdlər PASS deyil.

Tam PNG eyniliyi iddia edilmir: şüalar istinaddan zəifdir, bina real geometry/parts ilə çəkilir və nümunə silueti ilə əvəz olunmur. Mövcud əlavə məlumat/əməliyyat kompozisiyası saxlanır. Böyük şriftdə outer body scroll və mətnin viewport kənarında kəsilməsi əvvəlki davranışdır; əsas əməliyyatlar sabitdir. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Emulator/CI/merge/push/deploy yoxdur. İki əvvəlki baselineProfiles .dm bu commit-ə daxil deyil.

Son source: commerce 5/5 PASS, failures/errors/skipped0; lintDebug PASS, BUILD SUCCESSFUL. Log `/private/tmp/wobbrik-detail-rays.log`, renderlər `/private/tmp/wobbrik-detail-rays/`. 390×844 və320×568 font1/font2,360×640 font2 simulated inset+uzun ad/balans üzrə beş detail renderi açıldı və02-market.png ilə müqayisə edildi. Şüalar görünür, label sahəsinə çıxmır; kiçik/font2 body scroll saxlanır, əsas əməliyyatlar görünür. Canlı və bütün fallback halları ayrıca render edilməyib.

Lokal diff review: dəyişiklik yalnız artwork fonundadır; Path/Brush hər frame yenidən yaradılmır, ölçü ilə cache edilir. Semantika/input/layout modifier-i əlavə olunmur. Import/yerləşmə yoxlandı; əlavə layer/domain dublikatı yoxdur. `git diff --check` PASS. Yalnız source, bu review və handoff commit edilir.
