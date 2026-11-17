# İnventar — bina detalının üst xülasəsi

Branch `feat/community-tabs-native-consistency`, HEAD `6c96e9c` və əvvəlki owner working diff. Bu vahidin əvvəlki mənbəsi `/private/tmp/inventory-summary-before.kt`. Bu qeyd yoxlanmış working diff-in sübutudur; production kodunun ayrıca commit-i deyil. Merge/push/deploy/CI yoxdur.

PLAN §1.1 və `unified-mobile-v3/02-market.png` üst orta kart əsasında yalnız `MobileInventoryPresentation.kt` üst xülasəsi dəyişdi: mövcud ControlRadius/fine-rim commerce relyefi, yaşıl Success fonlu tünd mətnli native sahiblik kapsulu. Nişan kliklənmir. Uzun ad sözlərə bölünmür, üfüqi scroll saxlanır. Normal fontda bina solda, mətn sağda; font2 şaquli variant və body scroll qalır. Yeni rəng/ölçü tokeni, asset və dependency yoxdur.

Bina məlumatı paneli, placement/satış/silmə əməliyyatları və real kataloq artwork-u dəyişmir. Tam bina detalı/PNG pariteti iddia edilmir. Profilə toxunulmadı, Ərazi qaytarılmadı. Bu yalnız mövcud OwnedBuilding məlumatının təqdimatıdır: DTO/API/domain/schema/wallet dəyişmir; AppBackend dəyişikliyi tələb olunmur.

Kontrast ui-craft 3/3 PASS: Background/Success 11.82, Text/PanelTop 13.58, Muted/PanelTop 8.59. İlk native commerce 5/5 PASS (1m17s). Debug/mockDebug APK + lintDebug PASS (1m16s). Son test-only əlavə sahiblik nişanını scroll ilə görünən vəziyyətə gətirir və klik action olmadığını yoxlayır; yekun nəticə aşağıda yazılır.

Native renderlər `/private/tmp/wobbrik-inventory-summary-renders` altında: 390/844 və 320/568 font1/font2, 360/640 font2 simulated inset + uzun ad/balans. İlk normal, small/font2 və inset/font2 PNG-lər açılıb baxıldı; normal kart təsdiqli PNG ilə müqayisə edildi. Kiçik/font2 ilkin capture-da nişan viewport-dan aşağıdır, body scroll tələb edir. Real device/account/TalkBack/IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmadı. Tam app redesign/mərhələ qəbulu deyil.

Son test-only capture/assertion run: commerce5/5 PASS1m18s, failures/errors/skipped0. Beş sahiblik PNG-si `inventory-detail-summary/` altında saxlanıb; hamısı açılıb baxıldı. Small/font2 və inset capture-lar scroll vəziyyətidir; kapsulun alt padding-i viewport kənarında qismən kəsilə bilər, mətn görünür. Scoped source/test diff review və git diff --check PASS. Mövcud nullable String warning qalır.

Commit hazırlığı: istifadəçi commit etməyə icazə verdi. Yalnız bu review qeydi ayrıca commit olunur. Production `MobileInventoryPresentation.kt` və render hostu untracked-dır; MenuRelief/MobileCommerceUi və dəyişmiş shared token/shell asılılıqları da ayrıca commit olunmayıb. Son kart düzəlişi bu bazadan müstəqil build edilən commit kimi ayrılmır; qarışıq owner kodu bu sənəd commit-inə daxil edilmir.
