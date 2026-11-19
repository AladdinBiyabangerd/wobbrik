# Profil kolleksiya PNG — lokal dizayn review

Son düzəliş: `proposal-v2.png` üçün Ad və avatar sətri/thumbnail yığcamlaşdırılıb, shortcut-lar yuxarı çəkilib. Generated PNG açılıb baxılıb; mətn və Dəyiş əməliyyatı görünür. Digər iki görünüş kompozisiyası saxlanıb. Yeni native/adaptiv/kontrast yoxlaması yoxdur; palitra dəyişdirilməyib. Kod implementasiyası yoxdur.

- Scope: yalnız yeni PNG təklifi və sənədlər; mobil/backend kodu dəyişdirilməyib. Mövcud owner diff-i saxlanıb.
- Mənbələr: PLAN §1.1, current-task, GameTokens/InventoryCollectionChrome, ProfilePresentation, NativeProfileOverview/Editor və PublicAccountScreen; market-collection və profile-v2 istinadları.
- `docs/design/profile-collection-v1/proposal.png` built-in ImageGen ilə yaradılıb və açılıb baxılıb. Əsas profil, redaktə, üç mövcud avatar seçimi göstərilir. Kolleksiya header/navbar/panel/düymə üslubu təkrar istifadə edilir. Tac/şəxsiyyət motivi saxlanır.
- Funksiya yoxlaması: ad/avatar, 1–15 simvol, save/cancel, server səviyyə/XP intervalı, rekord və ümumi XP; parametrlər/nailiyyətlər/rekordlar girişləri görünür. Nümunə rəqəmlər server qaydası deyil.
- Kontrast: skill checker bu Kotlin token faylından rəng çıxarmadı (PASS deyil). Ayrı sRGB WCAG hesabı ilə Text/InventoryCollection.PanelTop 12.51:1, Muted/PanelTop 7.91:1, Accent/PanelTop 8.65:1. PNG gradientləri üzrə tam kontrast yoxlanmayıb.
- Tapıntı: yeni domain/əlavə avatar yoxdur; PNG native render və dəqiq piksel pariteti sübutu deyil. FontScale2/kiçik mobil/inset/error/offline halları bu dizayn təklifində render edilməyib.
- Build/test/CI işlədilməyib: kod implementasiyası yoxdur. Merge/push/deploy yoxdur. Yeni dizayn istifadəçi təsdiqini gözləyir.
