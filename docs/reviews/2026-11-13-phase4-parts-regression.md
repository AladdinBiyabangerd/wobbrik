# Hissə alış → unlock → tətbiq → detal regression review

- Tarix: 13 noyabr 2026. Branch `fix/parts-flow-regression`, baza `c40475b`.
- Scope: PartsMutations / HTTP wire / receipt / application recovery owner / durable journal / inventory detail read və mobil preview. İstifadəçi sonradan inventar fasadı, görünüş seçimi və şəhər oxu xətasının şəkillərini təqdim etdi; həmin steering bu vahidə daxil edildi.
- Mövcud owner handoff əlavələri, release, live-review/XML/QA patch qorunur. Commit/merge/push/CI yoxdur.

## Tapıntılar və düzəlişlər

1. **Bina redaktəsindən geri dönüş detalı itirirdi.** PartsScreen-dən inventory-yə qayıdış bina ID-sini daşımırdı. Seçilmiş inventory ID indi hesab destination sahibi tərəfindən saxlanır; Parts-dan geri dönüş həmin ID ilə real server detail/city oxusunu açır. Back və delete onu təmizləyir; Home/City-dən inventara yeni giriş siyahıdır. Köhnə OwnedBuilding obyekti və ya lokal dəyər ötürülmür. Exact Ready read key qalır.
2. **İnventar/detail və görünüş seçimi köhnə baked-panel renderer-ində qalmışdı.** Oyunun `drawModularFloor`, game atlas/window override, seçilmiş hissələr, ölçülmüş atlas pitch/depth və game base roof cap-ları ortaq BuildingArt preview-da istifadə edilir. Köhnə wall sprite-ın hazır pəncərələri artıq tam bina kimi çəkilmir. Hissə grid/detail müqayisələri də eyni modular layer-ləri çəkir. Təsdiqli oyun qrafikası təkrar istifadə olunur; yeni asset/dizayn/rəng/pin yoxdur. Server accepted center-ləri/floors/ownership dəyişmir, böyük lateral offset-lər səssiz düzləndirilmir. İstifadəçinin konkret 16-floor binasının server payload-u yoxdur; həmin hesabın eynilə təkrarlanması iddia edilmir.
3. **Standart görünüş preview-su “0 mərtəbə” yazırdı.** Aktiv scene olmadıqda bu nümunə “Standart görünüş” kimi işarələnir. Mövcud scene üçün real floor count göstərilir.
4. **320dp/font2-də Geri sözü parçalanırdı.** Dar mobil + böyüdülmüş şriftdə footer düymələri tam enlə şaqulidir, normal font1 kompozisiyası saxlanır. Sözü bir sətirdə göstərmə regression-u əlavə edildi.
5. **Şəhər screenshot-undakı xəta səbəbi bilinmir.** CityScreen bütün istisnaları tək `failed` halına çevirirdi. İndi `WobbrikCity` tag ilə cause/stack loglanır; token/body loglaması əlavə edilmir. Lokal City/Decorations/Building DTO müqaviləsi read-only backend source ilə tutuşduruldu. Boş şəhər+dekor düzgün işləyir; çatışmayan geometry müqaviləsi fail-closed qalır. Konkret screenshot üçün HTTP cavabı/log yoxdur: backend deployment/şəbəkə/DTO/racing write səbəblərindən biri fakt kimi seçilmir. Bu xəta **həll edilmiş sayılmır**.

## Regression əhatəsi

- Yeni `PartsFlowTest`: real HTTP adapteri və receipt parser-i, recovery owner, journal, inventory reader üzərindən buy→sync unlock→edit→sync→fresh detail. Sync gate receipt-in təkbaşına unlock/Ready vermədiyini göstərir. Təkrar basma, insufficient balance, free/reward/unknown item, locked/invalid/no-op seçimlər; wallet bir dəfə azalır, edit pulsuzdur; bina revision/parts/value yenilənir, floors/residents qorunur. Bu deterministic lokal transport fixture-dir, real backend inteqrasiya sübutu deyil.
- Mövcud lost-response testləri shop/edit üçün exact persisted envelope/operation ID retry-ni yoxlayır. Definitive rejection siyahısı building/shop/not_found/revision conflict ilə genişləndi; ambiguity journal-da qalır. Malformed/uncommitted/fee/price receipt regression-u əlavə edildi. Same-UID relogin isolation testi saxlanır.
- `CitySceneReadsTest`: empty scene/decor və missing geometry müqaviləsi regression-ları; əvvəlki mixed revision/account epoch testləri qalır.
- Native host harness: `parts-flow-visual/render/`. Production preview/picker və inventory detail, altı fasad + mixed windows, bütün68category art, 360×640/font1,320×568/font2; detail serverdən oxunur və Back siyahıya keçir. Sintetik hesabdır. PNG-lər lokal QA artefaktlarıdır.

## Backend və sərhədlər

- AppBackend `shop.go`, `market.go` və city/decor read müqavilələri yalnız oxundu. Qiymət, permanent unlock, owner/building revision, active listing, zero-cost edit və value recalculation müqaviləsi dəyişmir. Schema/API/DTO/backend dəyişikliyi tələb olunmadı; AppBackend dəyişdirilmədi və backend testləri işlədilmədi.
- Alınmış binanın hissələri unlock hüququ vermir; edit request-in bütün beş hissəsi backend-də free/unlocked yoxlanır. Mobil də eyni müqaviləni saxlayır; ayrıca server qayda dəyişikliyi bu tapşırıqda edilmədi.
- Mobil portret işidir; emulator açılmadı. Cihaz/API24runtime/real hesab/inset/TalkBack/GPU/performance/planşet/landşaft yoxlanmayıb. Şəhər screenshot-unun root cause-u açıqdır; canlı yoxlama gate deyil. Tam mərhələ4 qəbulu iddiası yoxdur.

## Yoxlamalar

- Core regression: PartsFlow1/1, PartsMutations5/5, CitySceneReads9/9 PASS.
- İlk normal Java21/offline core/engine/app unit+APK+lint PASS36s. Son adaptive/source sonrası nəticələr aşağıda qeyd edilir.
- Token mənbəyi `GameTokens.kt`; kontrast4/4 PASS, min10.48 (Text/Muted on Background, Background on Accent/Success). Palette dəyişmədi. Diff check PASS.
- İlk host cəhdində bir import xətası düzəldi; növbəti run cached Android jar resolver qovluğu olmadığından başlamadı. Mövcud Maven jar-larına temp symlink yaradıldı, yeni dependency yüklənmədi. Sonrakı4/4və5/5host run-ları PASS; adaptive footer dəyişəndə bir Compose receiver compile xətası düzəldi. Font2 text assertion-u şəkil və real line bounds ilə dəqiqləşdirildi (softWrap=false üçün hasVisualOverflow flag-i əvəzinə lineCount və line edges ölçülür). Bunlar CI deyil.

- Son native host: **5/5 PASS22s**. Final320/font2, normal picker/mixed, altı fasad+mixed və68category PNG-lərə baxıldı. Geri/Təsdiqlə görünür, Geri bir sətirdədir. Normal font1 kompozisiyası qorunur. Detala qaytma testi real InventoryScreen və HTTP reader ilə işləyir.
- Lokal source/diff review: navigation reset/delete, exact Ready read sərhədi, modular seçilmiş ID-lər, game roof/pitch, native pixel cap və locked-art dim saxlanması yoxlandı; yeni əlavə bug tapılmadı.

- **Son source:** Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` **PASS37s**,108task; kontrast4/4 və diff check PASS. Production source bundan sonra dəyişmədi. İstifadəçinin lokal commit göstərişi var; əlaqəsiz owner faylları və əvvəlki handoff hissələri index-dən kənarda saxlanır.
