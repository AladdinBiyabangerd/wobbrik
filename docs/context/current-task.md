# Cari tapşırıq — Profil dizaynı — 19 noyabr 2026

- Completed: Yeni elan native axını və kart polish nəticəsi istifadəçi tərəfindən qəbul edildi; ayrıca scoped commit hazırlanır. Son renderlər cards-polish/ daxilindədir.
- Current state: feat/market-new-listing-native; əvvəlki Uğurlar/Şəhər owner diff-i saxlanır. Yeni elan commit-i üçün git log-u yoxla. Merge/CI/push/deploy yoxdur.
- Decisions: istifadəçi qalan yeni dizayn işlərini Profil, sonra ana səhifə kimi müəyyən etdi. Əvvəl Profil dizayn nümunəsi hazırlanır; native implementasiya üçün yeni dizaynın təsdiqi lazımdır. Yeganə plan docs/PLAN.md §1.1. Mövcud profil ad/avatar/server axını saxlanır; yeni domain uydurulmur.
- Remaining work: Profil üçün mövcud native axını və ortaq kolleksiya üslubunu oxu, yeni PNG nümunəsi hazırla; sonra istifadəçinin dizayn review-u. Ana səhifə bundan sonradır. Tam mərhələ qəbulu deyil.
- Relevant files: docs/design/profile-v2/README.md; android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{ProfilePresentation.kt,NativeProfilePresentation.kt,GameTokens.kt}; docs/design/market-collection-v1/proposal.png; docs/reviews/2026-11-19-market-new-listing-native.md; docs/reviews/market-new-listing-native/cards-polish/.
- Checks performed: Yeni elan son source native host5/5, assembleDebug/lintDebugPASS50s; kontrast token4/4min7.41, radial kompozit min6.07; diff-checkPASS. Normal/kiçik/font2/inset renderlər açılıb. Real cihaz/API24/IME/TalkBack/GPU yoxlanmayıb. Profil üçün yeni yoxlama və ya dizayn yoxdur.
- Exact next step: Profilə aid source fayllarının mövcud adlarını rg --files ilə dəqiqləşdir, yalnız həmin axını və GameTokens/kolleksiya PNG istinadını oxu. ui-craft + imagegen ilə mövcud üslubda Profil dizayn PNG-si hazırla; implementasiya etmə.
