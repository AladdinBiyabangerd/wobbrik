# İlkin hesab sync ekranı — dar vahid

Branch `fix/menu-inventory-latency`, baza `083f870`; review edilən working diff: `OnlineGame.kt`, yeni `InitialAccountSync.kt` və `initial-sync/render/` host harness. Əvvəlki owner dəyişiklikləri (`docs/PLAN.md`, iki baseline profile və untracked parity audit) saxlanıb, bu vahiddə dəyişdirilməyib. Merge/push/deploy/CI yoxdur.

## Davranış və lokal review

Google sessiyasından sonra Home əvəzinə mövcud application-owned recovery gözlənilir. Ready artıq snapshot/journal reconcile və kataloqu gözləyir; bu ekran əlavə sync, inventory/city/profile warmup və ya ikinci authority yaratmır. Hazır Ready-də əlavə gözləmə yoxdur. Giriş latch-i BillingSession/epoch üzrədir; eyni UID re-login onu sıfırlayır. Latch gameplay return-lərindən əvvəl çağırılır: oyundan çıxanda və sonrakı mutation/recovery-də ekran yenidən açılmır. Exact Ready capability, readOnly, journal və mutation sərhədləri dəyişmir.

Uyğun qəbul edilmiş hesab keşi varsa dərhal, error-da dərhal, şəbəkə uzananda 8000ms-dən sonra “Menyuya keç” və Android Geri çıxışı var. 8s şəbəkə timeout-u deyil; recovery ləğv edilmir və cache mutation hüququ almır. İstifadəçi çıxana/Ready gələnə qədər ekran qalır; sonsuz məcburi loading yoxdur. Error-da təkrar cəhd mövcud owner.onForeground-u çağırır; davam edən işi owner özü dedup edir. Menyuya keçəndən sonra mövcud Home error/journal izahı qalır. İlkin signed-out Google axını dəyişmir.

Native `04-game-states.png` loading üslubu: mövcud GameTokens, Nunito, ortaq header, cyan indikator və MenuRelief. Yeni art və raster UI yoxdur. Girişdə hazır menu capability olmadığından dock yoxdur. Tam yeni startup board/pixel parity iddiası yoxdur; istinad inventory loading ekranıdır. Uzun balansda shared header-in ikinci sətri saxlanır. Dəstək mətni scroll edir, çıxışlar scroll xaricindədir.

## Yoxlamalar

Son source üzərində normal `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (28 app unit, yekun29s); log `/private/tmp/wobbrik-initial-sync-final-checks.log`. İlk run da PASS idi. `git diff --check` PASS. CI işlədilməyib.

Native host: `InitialSyncRenderTest` — 5 portret render (320/360/390, font1/font2, uzun 987654321 balans; 320/font2 error), 2 axın testi (8000ms çıxışı, manual exit latch, eyni UID/new epoch, error retry, Ready-dən sonra Recovering-də təkrar girişin olmaması, başqa hesab/cache çıxışı), ayrıca art baseline. Harness review altında saxlanıb; emulator açılmır. Axın testləri real InitialAccountSync composable-ını, renderlər real InitialSyncScreen-i çağırır. Firebase/live transport/E2E AuthenticatedGame testi deyil.

PNG-lər `/private/tmp/wobbrik-initial-sync-renders/`; 320/360/390 font1 və 320/390 font2 açılıb istinadla müqayisə edildi. Böyük şriftdə uzun izah scroll tələb edir; çıxışlar görünür. İndikator animasiyasının başlanğıc frame-i nöqtə ola bilər. Real device/GPU, tam inset/klaviatura/TalkBack və locale matrisi yoxlanmayıb. Token kontrastı Text/Surface13.00, Muted/Surface8.22, Accent/Surface8.99 PASS. Native səthlər mövcud palitradır; bütün gradient piksellərinin ayrıca kontrast matrisi yoxdur.

## Backend və qalan scope

Mövcud recovery source və AppBackend `docs/wobbrik-game-api.md` `/sync` + `/catalog`, account/epoch/revision müqaviləsi yoxlandı. Bu presentation-only vahiddə API/DTO/schema/domain dəyişikliyi lazım deyil. Backend faylı/testi dəyişdirilməyib. Əlavə sorğu/body-byte mənbəyi əlavə edilməyib; production traffic benchmark-ı deyil.

Menyu/art optimizasiyası tamamlanmayıb. Atlas helper-lərinin təkrarlanan mount ölçməsi host üzrədir, production route-to-frame sübutu deyil. Qalan iş: ölçülmüş decode/allocation-u aradan qaldırmaq, digər menyu/detail cache-first təqdimatı, redundant capture/coalescing, frame polling və request/byte baseline. İlkin sync ekranı bunları həll edilmiş saymır.

Yekun host run: 8/8 PASS,15s; log `/private/tmp/wobbrik-initial-sync-render.log`. 500ms animasiya irəlilətmə ilə host PixelCopy bir error çəkilişini boş verdi; həmin QA variantı çıxarıldı və son renderlər adi frame capture ilə yenidən alındı.

Art baseline: eyni kompozisiyada `rememberCosmeticArt + rememberGameWindowArt + rememberGameAtlases`, ilkin mount +4remount: **254/234/233/272/228ms**,5mount üzrə **50ayrı backing Bitmap**, **314531040allocation byte** (təxminən60MiB/mount). ImageBitmap wrapper-ləri backing `asAndroidBitmap()` identity-si üzrə dedup edilib. Test bitmap-ləri bilərəkdən identity set-də saxlayır; bu rəqəm production retained heap deyil, təkrar allocation sayıdır. Ağır decode Compose thread-də təkrarlanır. Bu ölçü optimizasiya üçün konkret başlanğıcdır, istifadəçinin2+s/10+s route gecikməsinin tam səbəbi deyil. Hələ düzəldilməyib; before/after cütü yoxdur.

Son lokal review düzəlişi: gate girişində `sessions.current()` ilə yanaşı Ready/cache/readOnly owner-in cari canonical value-larından oxunur; əvvəlki frame-də qalmış Ready yeni auth epoch-un latch-ini aça bilməz. Son host suite painted-pixel assertion ilə8/8PASS14s; allocation nəticəsi dəyişmədi. Son ayrı run ms475/515/261/267/246 oldu: host/JIT/CPU variasiyası var, əvvəlki254/234/233/272/228 ölçüsü tək sabit cihaz medianı sayılmır. Son source üçün normal yoxlamalar yenilənir.

Yoxlama düzəlişi: canonical value-ların composition-dan birbaşa oxunduğu aralıq variant lint-də3`StateFlowValueCalledInComposition` xətası ilə FAIL oldu (unit28PASS, APKPASS). Birbaşa oxular mövcud frame müşahidəsi + epoch/state/cache/readOnly-keyed `remember` snapshot-ına keçirildi; suppression/baseline əlavə edilmədi. Bu aralıq FAIL uğurlu sayılmır; düzəlişdən sonra normal suite yenilənir.

Yekun düzəlişdən sonra normal app28unit + assembleDebug + lintDebug PASS29s; log `/private/tmp/wobbrik-initial-sync-final-checks.log`. Son diffcheck PASS.
