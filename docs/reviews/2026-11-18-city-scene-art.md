# Şəhər — dinamik scene artwork vahidi

18 noyabr 2026. Branch `feat/city-collection-design`, baza `a98272c`, working diff. Authority `docs/PLAN.md` §1.1 və `docs/design/city-collection-v1/proposal.png`. Əvvəlki native layout vahidi və owner-in dirty release `.dm`/Inventory review dəyişiklikləri saxlanıb. Commit/merge/CI/push/deploy yoxdur.

## Nəticə və vizual sərhəd

Native header, scene frame, fit/focus, summary/CTA və navbar dəyişdirilməyib. Şəhər üçün ayrıca modular illustration atlası, seçilmiş materiallı nazik fasad, tək mərkəzi front pəncərə, sağ üz kölgəsi, daşlı alpha sahil, çiçək yataqları, qısa əyri daş giriş cığırları, su ripple-ları və landşaft ağacları inteqrasiya olunub. Yeni tekstura/bitki artwork provenance və source ölçüləri `../design/city-collection-v1/scene-art.md`-dədir. UI şəkilə bişirilməyib. Game/Inventory renderer-ləri dəyişmir; legacy tablet city illustration seçmir.

Real server floor count/centres, rotations, footprint, parcel union, ownership və parts saxlanır. Base2 kərpic və base3 suvaq ayrıca yeni material alır; digər materiallar seçilmiş kataloq art-ından gəlir. Bütün window/entrance/lighting/roof seçimləri mövcud ID qatlarıdır. Owned tree1 yeni sprite istifadə edir; qalan dekorlar öz seçilmiş art-ını saxlayır. Landşaft ağacları və giriş cığırları presentation-dır: nə yeni mülkiyyət/dekor nüsxəsi, nə placement, nə də domain road. Şəhərin accessibility təsvirində real dekor sayı dəyişmir. Torpağın daxili tikişləri boolean union ilə yox edilir; landşaft və daşlar owned surface/cliff ilə clip olunur. Mobil32-unit cliff presentation depth-dir; server contour dəyişmir.

`city-scene-art/comparison.jpg` təsdiqli proposal / eyni synthetic fixture-in əvvəlki native nəticəsi / son native scene-i yanaşı göstərir və açılıb baxılıb. Son31 screenshot matrisi və normal tamölçülü render ayrıca açılıb baxılıb. Fasad/pəncərə materialları, alpha kənarları, daş sahil, landşaft painter depth və input regression yoxlanıb.

**Tam piksel pariteti iddia edilmir.** Proposal-dakı beş eyni bənövşəyi damlı bina real fixture-in fərqli seçilmiş roof/materiallarını əvəz etmir. Bina sıxlığı və qəbul edilmiş torpaq forması artwork ilə dəyişdirilmir. Nümunədəki geniş birləşən park yolu yerinə real foundation-a bağlı qısa giriş cığırları var; su işıq əksi nümunədəki qədər güclü deyil. Mövcud adaptiv native layout kiçik/font2-də scene-i kiçildir,320dp-də navbar wrap qalır. Bunlar screenshot-larda açıq görünür; bütöv maket rasteri ilə gizlədilməyib. Bütün app redesign/mərhələ qəbulu bu nəticə deyil.

## Xərc və authority

Yeni atlas ilk tələb olunan city art preparation-da IO üzərində decode edilir, process daxilində paylaşılır; account/Activity saxlamır. Təkrar city mount üçün yeni atlas decode/bitmap allocation yoxdur. Shader üçün bir400×400 crop da həmin IO hazırlığındadır. Atlas2,299,240 APK source baytı,6,291,456 RGBA +640,000 paving crop = əlavə6,931,456 saxlanmış RGBA baytı (~6.61MiB). Əvvəlki city atlas təxmini37,630,176 ilə birlikdə44,561,632 bayt (~42.50MiB); decoder/native/GPU overhead daxil deyil. Yeni scenery cığır/beds/bucket hazırlığı qəbul edilmiş scene üçün `remember`-dədir. Occupancy məkan bucket-i bütün şəhəri hər shore nöqtəsi üçün gəzməyi əvəz edir; əlavə ağac cap128-dir. Existing ambient layer sort/draw xərci və GPU/heap/GC ölçülməyib, optimallaşdırma/latency/frame PASS iddiası yoxdur.

Eyni əvvəlki read fixture ilə cold4/760 response UTF8 baytı; warm0, offline accepted replay0, refresh4. Yeni artwork əlavə endpoint/DTO/API sorğusu yaratmır. Bu say əvvəlki layout review-dakı4/760/0/0/4 ilə eynidir; first-content/təkrar giriş cihaz vaxtı ölçülməyib.1000-building/10000-shore host fixture əlavə landşaftın cap-ini, foundation exclusion-u və mənbə geometry-nin dəyişməməsini yoxlayır; scheduler millisecond assertion yoxdur. AppBackend `docs/wobbrik-game-api.md` city bundled geometry/positions müqaviləsi təkrar yoxlandı: mobil render dəyişir, API/schema/domain dəyişməsi və backend işi lazım deyil.

## Yoxlamalar və lokal review

Son komanda `:app:assembleDebug :app:lintDebug :app:testDebugUnitTest` + seçilmiş City testləri, mövcud offline Robolectric native init ilə PASS: `BUILD SUCCESSFUL in 1m 11s`, `/private/tmp/city-art-final.log`.37/37,0failure/error/skipped: CityCollection5, CityIllustration2, projection6, terrain4, life8, account-bound reads12. Test XML nəticələri yoxlanıb. İlk geniş run36/37 idi: materialın alpha-sı251 olduğu halda test255 gözləyirdi. Opaq native underpaint əlavə olundu; texture integrity yoxlaması painterly alpha≥240, kənar alpha0 tələbi ilə düzəldildi, son bütün37 test təkrar keçdi. İlk layout-art assemble/lint və dar projection/terrain/normal render run-u da PASS idi; uğursuz run son PASS kimi gizlədilmir.

Matris320×568 və390×844 fontScale1/2;360×640 font2 + süni inset, uzun ad və Long.MAX_VALUE balans; fit/focus/custom accessibility action/refresh/retry/navigation/cache/loading/error/empty və metadata scroll. Yeni palette/UI ölçü sistemi yoxdur; `GameTokens` təkrar istifadə olunub. UI contrast6/6 PASS, minimum7.41:1 (`city-scene-art/contrast.txt`). Decorative artwork kontrastı mətn/input yoxlamasının əvəzi deyil.

Lokal review: yalnız scene integration, part ID/material mapping, unchanged geometry, memoization/account sərhədi, IO/shared asset load, alpha source samples, cap/occupancy, painter order, imports və legacy rendering fallback yoxlandı. Son diff-check PASS. Testlər synthetic JVM/native host render-dir; real account/device/ART/GPU/latency/TalkBack/API24/runtime inset/IME və bütün locales yoxlanmayıb. Emulator açılmayıb. CI işlədilməyib. Tam vizual qəbulu istifadəçi verir.

## Yol/su davamı

Bu qeyddəki qısa cığır/zəif su əksi fərqi sonrakı eyni tarixli vahiddə işlənib. Cari nəticə və yoxlamalar `2026-11-18-city-road-water.md`-dədir; əvvəlki screenshot/check tarixçəsi burada saxlanır.
