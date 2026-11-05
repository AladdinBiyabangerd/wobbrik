# Mərhələ 3 / vahid 2 — online ekran branch-i, 5 noyabr 2026

## Completed
- MainActivity artıq offline PracticeSession yaratmır; OnlineGame tətbiqə
  məxsus AcceptedEngineHost-un frame/drop/resume və typed start axınına bağlıdır.
- Canonical Ready hər frame referenslə oxunur; bərabər snapshot-lı auth epoch
  UI-də köhnə capability saxlamır. Host stale input-u rədd edir.
- User pause host-a əlavədir: foreground qalır, input/clock bağlanır;
  renderer kamera origin-ini host-dan alır. Restart/abandon yoxdur.
- Hazır şəhər/logo/hesab art Android resurslarına keçib. İstifadəçi əlavə
  asset hazırlığını təsdiqləyib; coin/account/level/medal/trophy ikonları və
  recovery hero hazırlanıb, görüntülərə baxılıb. Crown istifadə olunmur.
- Ana/hesab/recovery və server progression xülasələri yazılıb; build/engine
  testləri keçib. Vahid 2-nin tam hazır olması iddia edilmir.

## Current state
- Branch `feat/stage-three-online-screens`, main-ə merge edilməyib.
- Artwork commit `118e0ee`; ekran/host commit `b8c9d31`.
  Yoxlamalar və açıq tapıntılar üçün review qeydini oxu.
- Google hesabı ilə guest link istifadəçi tərəfindən seçilib, HƏLƏ YAZILMAYIB.
- Son APK real MainActivity-də backend origin çatışmazlığını göstərir;
  canlı server axını yoxlanmayıb. Endpoint uydurulmayıb.
- Təsdiqlənmiş dizaynlarla kompozisiya/tipoqrafiya/dekor fərqləri, tablet rail
  və hesab actions çatışmazlığı var. Kolleksiya/nailiyyət yalnız say xülasəsidir.
- Əlaqəsiz untracked responsive-rule/compose-game-host faylları qorunub.

## Decisions
- Eyni qonaq UID-si Google credential ilə link olunmalıdır; hesabların
  wallet-ləri birləşdirilmir. Google seçiminə yenidən sual vermə.
- Console, real SKU, deploy və push ayrıca göstərişədək daxil deyil.
- Əlavə asset hazırlığına icazə verilib; dizaynı sadələşdirməyə icazə yoxdur.
- Auth uğuru Ready deyil; bütün balans/progression server snapshot-dandır.
- AppBackend müqaviləsi yenidən oxunub, bu UI bağlantısı API/schema dəyişikliyi
  tələb etmir. Room/outbox və fault proof ayrıca plan vahidləridir.

## Remaining work
- Google credential alınmasını hesab ekranında WobbrikApplication.linkGuest-a
  bağla: eyni UID, cancellation, collision/retry və auth epoch revocation.
- UI-ni təsdiqlənmiş mobile-v1/account-states, tablet-v1 və game-tap-v2
  dizaynları ilə tamamla; summary səhifələri tam ekran kimi təqdim etmə.
- Son diff review tapıntılarını bağla, telefon/planşet/landşaft/böyük şrift
  renderlərini yenilə və görüntülərə bax. Real auth/backend yoxlamasını ayır.
- Yalnız branch hazır olanda tarix saxlanılmaqla main-ə merge et və main-də
  ./scripts/local-ci.sh işə sal. Hazırkı branch-də CI işlədilməyib.

## Relevant files
- docs/reviews/2026-11-05-online-screens.md
- docs/stage-three-online-plan.md
- docs/design/account-assets-v1/ui-icons/README.md (promptlar və asset yolları)
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MainActivity,OnlineGame,GameTokens,WobbrikApplication}.kt
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedEngineHost.kt
- android-billing/app/src/debug/kotlin/com/aladdin/wobbrik/AccountPreviewActivity.kt
- scripts/capture-online-screens.py; docs/reviews/online-screens/

## Checks performed
- Son source diff: :app:assembleDebug :engine:test --offline keçib (6s);
  engine 45 test, 0 failure/error. Lokal CI deyil.
- Kontrast: 16.57 / 8.22 / 11.45; diff-check keçib.
- İlk renderlərə baxılıb; 1.5 şriftdə kəsilmə tapılıb və layout düzəldilib.
  Son software renderində 9 fixture və real MainActivity görüntüsünə baxılıb;
  başlıq kəsilməsi düzəlib, tam dizayn uyğunluğu keçmiş sayılmır.
  Ölçülər və məhdudiyyətlər review qeydinin Son render / handoff bölməsindədir.
- Hardware emulator ColorBuffer xətaları/boş starting window uğurlu render
  sayılmır; software renderer ilə təkrar cəhd edilib. Canlı auth/DB sübutu yoxdur.

## Exact next step
- Yalnız bu qeydi və online-screens review-ni oxu; cari branch-i saxla.
  Google credential üçün rəsmi Android/Firebase sənədlərini yoxla və native
  Google seçim axınını mövcud linkGuest-a bağla; Console-u dəyişmə. Sonra eyni
  vahid 2 daxilində açıq dizayn tapıntılarını bağla və review/merge/main CI et.
