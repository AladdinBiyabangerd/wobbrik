# Bina detalı — yuxarı kəsilmə düzəlişi

Branch: feat/inventory-art-led-redesign; baza c5d20af. Scoped diff: OwnedBuildingDetailPresentation.kt və GameTokens.kt.

Səbəb: düzgün fit hesablayan BuildingPreview üzərinə scale1.2 və -16dp translation tətbiqi hero-nun scroll viewport sərhədindən çıxmasına səbəb olurdu. Transform və artıq istifadə edilməyən token çıxarıldı. Mövcud aspect-fit renderer bütün bina/garden bounds-unu sahəyə sığdırır. Layout ölçüləri, navbar, rənglər və real metadata/action müqaviləsi dəyişmir; yeni read/decode/backend dəyişikliyi yoxdur.

Fokuslu native Compose/Robolectric 5/5 PASS (1m1s): regular/small, font1/2 və inset/uzun mətn. Regular render açılıb baxıldı: dam və baca tam görünür, alt hissə və əməliyyatlar yerindədir. Evidence: building-detail-native/evidence/roof-fix/. Palet dəyişməyib; əvvəlki contrast sübutu qüvvədədir. Production account/performance iddiası yoxdur. Lokal diff review: blocking tapıntı yoxdur. CI/main merge/push yoxdur; owner .dm və əvvəlki cost review toxunulmayıb.

Android Studio mockDebug rebuild/install uğurludur, debugger qoşulub. Pixel6 API37.1-də inventar → detail açıldı; dam/baca və garden tam göründü. Faktiki cihaz screenshot-u roof-fix/device-mock-detail.png-dədir.
