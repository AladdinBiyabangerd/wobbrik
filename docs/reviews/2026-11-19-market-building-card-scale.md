# Bazar — bina kartlarının art miqyası və yerləşməsi

19 noyabr 2026. `fix/market-approved-native`, baza `a98272c`, owner-in mövcud working diff-i üzərində yalnız bu vahidin düzəlişi. İstifadəçi “Mənim elanlarım” və satışdakı bina kartlarını təsdiqli `docs/design/market-collection-v1/proposal.png` ilə uyğunlaşdırmağı tapşırdı. Scope: `MarketScreen.kt` daxilində preview konteynerləri; əvvəlki Şəhər/İnventar və digər owner dəyişiklikləri saxlanır. Commit/merge/CI/push/deploy yoxdur.

## Dəyişiklik və lokal review

- Normal satış grid art sahəsi144→160dp; kiçik portretdə72→96dp. Böyük şriftdə96dp illustration frame saxlanır. Mövcud GameTokens ölçüləridir, yeni rəng/ölçü tokeni yoxdur.
- Mənim elanlarım normal art konteyneri120×120→144×160dp; kiçik portretdə96×120→120×144dp. Bina sol sütunda, mətn sağda qalır; divider art sahəsinə uyğun böyüyür. Font2 vertical layout-da96→120dp. Bina qeyri-bərabər scale/crop ilə enləndirilmir.
- İlk kiçik-portret sınağında satış art-ı120dp idi: ilk Bax düyməsinin altı dock arxasında qalırdı. Son variant96dp-dir; bu aralıq variant final sübut kimi istifadə edilmir.
- Accepted geometry/centers, mərtəbə və hissələr, mövcud renderer və onun representative preview qaydası dəyişməyib. Domain/DTO/repository/cache/fresh detail/journal yolu toxunulmayıb. Yeni decode/cache/HTTP/prewarm yoxdur; performans optimallaşdırması iddiası yoxdur. API/schema/domain müqaviləsi dəyişmədiyindən AppBackend dəyişməsi tələb edilmir; backend testləri yenidən işlədilməyib.

## Native müqayisə və sərhədlər

Sübutlar `market-building-card-scale/` altındadır. `comparison.jpg`: təsdiqli PNG / əvvəlki native / son native; `responsive.jpg`: normal390×844, kiçik320×568 font1/2 və360×640 font2 simulated24/48dp inset+uzun ad/Long.MAX_VALUE. `compare.py` mövcud reference və native capture-ları montaj edir; renderlər production komponentlərini synthetic HTTP/account fixture-i ilə Robolectric native graphics üzərində çağıran əvvəlki `market-collection-native/render/` harness-indən gəlir.

Tam piksel pariteti iddia edilmir: real bina hissələri və mövcud representative preview, fixture-də4elan, fərqli mətn/qiymətlər,48dp input, font2 scroll və ortaq navbar adaptasiyası saxlanır. Böyük şriftdə əsas əməliyyat scroll ilə açılır. Sonrakı lazy art capture anında loading-də ola bilər; bu kartlar tam art parity sübutu deyil. Emulator/device/ART/GPU/frame/IME/TalkBack və bütün locale matrisi yoxlanmayıb. Core/cache suite bu presentation vahidində təkrar işlədilməyib.

Son `comparison.jpg` və `responsive.jpg` açılıb baxıldı. Normal ölçüdə bina sol art sahəsində daha dolğundur; satış gridində art böyüyüb.320×568 font1-də ilk Bax düyməsi tam dock üstündədir. Font2/uzun mətn hallarında zəruri horizontal/vertical scroll saxlanır; yeni üst-üstə düşmə müşahidə edilmədi. Bu yalnız art konteynerlərinin düzəlişidir, qalan ikon/filter/header fərqlərinin həlli deyil.

Son-source host5/5 PASS,0failure/error/skipped; alış/cancel, öz elan/new/withdraw, fresh confirmation və dekor regression yoxlamaları mövcud route harness-indədir. Palitra dəyişmir; scope palette üçün contrast15/15 PASS, minimum7.41. CI və emulator işlədilməyib.

Təkrar render əmri (`android-billing/` daxilində):

```sh
./gradlew --offline -I ../docs/reviews/market-collection-native/render/render.init.gradle :app:testDebugUnitTest --tests 'com.aladdin.wobbrik.MarketApproved*Test' -Dwobbrik.render.output=/private/tmp/wobbrik-market-card-scale-final
```

Yalnız bu chatin kod dəyişikliyi `market-building-card-scale/scoped.diff`-dədir; bütün branch diff-i ilə qarışdırılmır.

Son-source `:app:assembleDebug :app:lintDebug` PASS (44s); `git diff --check` PASS. Log: `market-building-card-scale/build-lint.log`. Lokal scope review-da bu vahidin geometry/read/mutation koduna toxunmadığı, mövcud tokenlərdən istifadə etdiyi və son native kiçik-portret CTA düzəlişinin saxlandığı yoxlandı.
