# Şəhər — real bina qatı, sabit kamera

- Branch `feat/phase-4`, baza `bb02b50`; review edilən working diff: əvvəlki scene reader/bootstrap və bu bina renderer-i. İstifadəçi cari vahid bitəndə commit etməyi tapşırıb. Merge/push yoxdur; owner `app/release/` faylları xaricdədir.
- Dizayn `docs/design/city-view.png` təsdiqlidir. Asset sərhədi göstərildikdən sonra istifadəçi mövcud atlaslarla ilkin sabit kamera bucağını “beli” ilə ayrıca təsdiqlədi. Əvvəlki art-boundary review-un açıq kamera sualı bununla bağlandı. Bu, tam şəhər ekranının vizual qəbulu deyil.

## Dəyişiklik və lokal review

`CityScreen` exact Ready identity ilə mount edilir. Eyni auth session ilə `CitySceneRepository` oxunur; nəticəni qəbul etməzdən əvvəl session və canonical Ready yenidən yoxlanır. Yeni login/recovery zamanı köhnə səhnə/selection saxlanmır. İkinci wallet/cache/journal və placement əmri yoxdur. Backend dəyişmədi: mövcud read müqaviləsi kifayətdir.

`CityBuildingProjection` accepted floor centers-i təməl origin-i ətrafında quarter-turn ilə dünya koordinatlarına keçirir, sonra sabit kameraya proyeksiya edir. Mərtəbə sayı/ofsetləri recenter və clamp edilmir. Double aralıq hesabı böyük tick koordinatlarında kiçik ofseti saxlayır. Binalar təməl dərinliyi, x və ID üzrə sabit sıralanır; bütün siluet və dam viewport-a sığdırılır. Bu, bina səviyyəsində painter order-dir; kəsişən çox hündür siluetlər üçün per-pixel occlusion realizasiyası deyil.

Fasad/window/entrance/lighting/roof yalnız server parts ID-lərindən mövcud atlas crop-ları ilə çəkilir. Native crop-dan böyük piksel böyütmə dayandırılır. Inventory ilə eyni art anchor helper-i (`BuildingArt.kt`) istifadə edilir; inventar anchor dəyərləri dəyişməyib. Legacy mövqe, geometry və ya art çatışmazlığında saxta origin/düz bina çəkilmir, sayla xəbər verilir. Bina toxunuşu və accessibility custom action seçir; “Binaya bax” fokuslayır, şəhər görünüşünə və Android Geri ilə overview-a qayıtmaq olur. Home → Şəhərim və şəhər → İnventar/Ana ekran qoşulub. Plan/placement, market və orbit əməliyyatı əlavə edilməyib.

Review-da tapılıb düzəldilənlər: telefon HomeControls-da şəhər girişi yalnız tablet şərtində idi, mobil giriş də açıldı; ilkin Bax düyməsi seçimdən əvvəl disabled idi, indi ilk real binanı fokuslaya bilir; fokusdan Geri overview-a qayıdır; native-pixel cap əlavə edildi. Son diff check PASS.

## Vizual sübut və məhdudiyyətlər

`city-scene-visual/contact.png` və `design-comparison.png` açılıb baxıldı. Production `CityContent` host native Compose/Robolectric API34, density3 ilə render edildi: 360×640dp font1/2, 390×844dp font1; hər birində overview/focus. Empty/error və mobil Home şəhər giriş renderlərinə də baxıldı. Test səhnəsindəki üç bina açıq synthetic fixture-dir; production-a demo adapter qoşulmur. Bu, real hesab/Firebase/device axınının yoxlanması deyil.

Bina qatı görünür, fərqli parts və dönmüş əyrilik qorunur, yoxlanmış ölçülərdə əsas düymələr görünür. Tam `city-view.png` ilə uyğunluq PASS deyil: torpaq parcel union səthi, dekor, fountain/paths, logo/tab/footer/camera-panel kompozisiyasının tam eyniliyi, orbit və V1 canlılıq yoxdur. Binaların altında hazır alış adası və dekorativ şəhər rasteri saxta real şəhər kimi istifadə edilmədi. Mövcud atlasların fasad birləşmələri keyfiyyət məhdudiyyəti olaraq qalır. Tablet/landscape bu vahidin işi deyil. Real status/navigation insets və TalkBack cihaz yoxlaması edilməyib. Emulator açılmadı.

## Yoxlamalar

- Host render harness: 6/6 test PASS (üç ölçü/font ssenarisi, empty, error, Home giriş/click); 9 screenshot. Proyeksiya unit testləri 5/5 PASS: dörd quarter-turn/origin, böyük koordinatda signed subunit offset, legacy/missing geometry, sabit depth order və roof-inclusive fit.
- Son production kodunda normal Java21 `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (24s). Host init script normal build/CI-yə qoşulmur və pinləri dəyişmir.
- Token kontrastı Text/Muted/Accent/Danger on Surface 4/4 PASS, minimum 8.22; Background cütləri də PASS. Palette dəyişməyib.
- `git diff --check` PASS. CI/backend test/remote push/deploy yoxdur.

## Son istifadəçi screenshot-u — növbəti ayrıca vahid

İstifadəçi əvvəl bu işi commit, sonra aktiv oyun screenshot-undakı ciddi UI problemini düzəltməyi istəyir. Screenshot-da: mərtəbələr ayrı panel kimi görünür, tünd aralıq/birləşmə uyğunsuzluğu var; game island rasteri hazır ev daşıdığı üçün ikinci baza/giriş yaranır; aktiv oyunda dam çəkilir; kəndir ucları daşınan mərtəbə artwork anchor-ları ilə uyğun deyil. `GameScene.kt` statik baxışında aktiv phase üçün roof çağırışı təsdiqləndi. Həmin fayl bu vahiddə dəyişdirilmədi. Növbəti iş fizika/accepted geometry-ni dəyişmədən art crop/anchors/laying order və roof phase düzəlişidir; əvvəl mövcud təsdiqli game-tap-v2 istinadı təqdim edilməlidir. Yeni alternativ dizayn uydurulmur.
