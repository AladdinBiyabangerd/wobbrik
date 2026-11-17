# PLAN §1.1 — commerce kart materialı və tab kompozisiyası

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`; əvvəlki owner working diff üzərində dar əlavədir. Stage/commit/merge/push/deploy/CI yoxdur.

## Dəyişiklik

`MobileCommerceUi.kt`: normal şriftdə bütün tablar sığanda zolağın eni bərabər bölüşdürülür; böyük şriftdə mövcud selected-tab horizontal scroll qalır. `CommerceAction` etiketi qalan enlə ölçülür və daxilində scroll edir: ilkin renderdə görünən “Mənim elanlarım” səthinin kənara çıxması düzəldildi. Minimum48dp hədəf və selected/click/disabled semantikası saxlanır.

`MenuRelief.kt`: ayrıca Commerce variantı mövcud Raised → PanelTop → PanelBottom native material gradientini istifadə edir. Yalnız `MobileOwnedBuildingCard` və mobil `PurchaseCard` bu variantı alır; əvvəlki digər skin-lər və legacy/tablet şaxələri dəyişmir. Yeni token, dependency, raster və kataloq yoxdur. PLAN§1.1 məsuliyyət sətrində köhnə Ərazi tab siyahısı son qərara uyğunlaşdırıldı. Ərazi mobil Bazar/İnventara qaytarılmadı, yeni yer yoxdur.

Domain/repository/API/DTO/schema/wallet/session dəyişmədiyindən AppBackend işi tələb olunmur. Əvvəlki owner dəyişiklikləri saxlanır. Press animasiyası bu vahidə daxil deyil.

## Yoxlama və sərhəd

İlkin native commerce5+shell4=9/9 PASS1m40s; bundan sonra uzun action etiketi üçün en düzəlişi edildi, bu ilk PASS final source sübutu deyil. İlk relative-path edit mənbəyə yazmadan dayandı; sandbox Gradle lock çıxışı da test icrası deyil. Final nəticə aşağıda qeyd olunur.

Təsdiqli `01-main.png` və commerce followup native renderləri ilə müqayisədə kartların yuxarı işıqlanması və tab enləri yaxınlaşdırıldı. Bu iş bütün PLAN§1.1-i bağlamır: real bina/dekor/Profil artwork-ləri reference-dəki pozalarla eyni deyil; digər ekranların kompozisiya/material fərqləri və ölçülmüş axıcılıq açıqdır. Real inventar artwork-ünü generativ nümunə bina ilə səssiz əvəz etmək olmaz. Runtime/account/device, real IME/inset/TalkBack/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmayıb.

İkinci tur9/9 PASS1m24s olsa da renderdə `Süzgəc/Yenilə` etiketlərinin sıfır enə düşməsi aşkarlandı: sərbəst enli Row uşağında Text weight düzgün deyil. `BoxWithConstraints` ilə weight yalnız bounded width-də tətbiq edildi; sərbəst enli action öz etiketinə görə ölçülür. Bu ikinci PASS da final source sübutu deyil. Əlavə contact-sheet cəhdi lokal Python-da Pillow olmadığı üçün alınmadı; raw PNG-lər birbaşa açılır.

Final source: commerce5+shell4=9/9 PASS1m19s, failure/error/skipped0. Bütün final renderlər `/private/tmp/wobbrik-commerce-material-final2`; seçilmiş10PNG `commerce-material-native/` altındadır. Son inventory normal/small/inset+uzun mətn və market small/font2/regular-font2 raw PNG-lər birbaşa açılıb baxıldı; əvvəlki01-main və followup renderləri ilə müqayisə edildi. Tablar normalda tam enə yayılır; böyük şriftdə horizontal scroll qalır. Süzgəc/Yenilə son small renderdə görünür; Mənim elanlarım kartı ekran daxilində qalır. Kiçik/font2 market capture-ı testin saxlanmış vertical scroll mövqeyindədir. Mövcud qrafika/piksel fərqləri qalır.

Kontrast7/7 PASS, minimum6.48:1 (Muted/Raised); digər fill stop-larında8.59–16.16:1, Background/Accent11.45:1. `git diff --check` PASS; index boşdur. Lokal review: yeni variant yalnız iki commerce card callsite-ında istifadə olunur; əvvəlki digər skin-lər/legacy route-lar və domain çağırışları saxlanır. APK/lint nəticəsi aşağıda tamamlanır.

Son normal build invocation (render init olmadan): `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS40s. Son source-dan sonra əlavə production edit yoxdur. CI və emulator işə salınmadı; backend testləri bu UI vahidində tələb olunmadı.
