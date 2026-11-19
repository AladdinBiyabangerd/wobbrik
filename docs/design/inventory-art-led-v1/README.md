# İnventar — 18 noyabr dizayn sınağı

Yeganə icra planı: `../../PLAN.md` §1.1. İstifadəçi yalnız bu səhifəni sınamaq, nəticəni bəyəndikdən sonra qalan səhifələrə keçmək istəyib. `reference.png` chat-də bəyənilən built-in ImageGen maketidir, native render deyil. 7-slot naviqasiya, aktiv İnventar vurğusu, yığcam tablar və art-led kartlar istinaddır.

Kartın təsviri mövcud modular bina renderer-i ilə seçilmiş material/forma əsasında çəkilir; əvvəlcədən çəkilmiş sample tower istifadə edilmir. Mövcud dekorativ preview renderer-i 6–8 representative mərtəbə göstərir, mətn həqiqi server mərtəbə sayıdır. Onun dizayn/fizika sərhədi bu vahiddə dəyişmir. Yeni fon yalnız dekorativ səhnədir; idarəetmə və mətn rasterə bişirilmir.

## Asset provenance

`android-billing/app/src/main/res/drawable-nodpi/inventory_collection_backdrop.png`: built-in ImageGen ilə yaradılıb, sonra sips ilə512×512 ölçüyə kiçildilib. Bir RGBA decode təxminən1MiB, fayl təxminən256KiB. `rememberResourceArt` IO-da yalnız görünən İnventar kökü üçün bir dəfə yükləyir, kartlar eyni ImageBitmap-i paylaşır; failure məzmunu bloklamır. Bina qrafikası dəyişməyib.

Final generation prompt (verbatim):

> Use case illustration. Create a quiet environmental background illustration asset for the art area of a dark navy whimsical city-building mobile game inventory card. Square bitmap, no UI, no typography, no cards, no frame, no central building. Entire upper 70 percent mostly empty deep navy night atmosphere matching #102C53 and #193D69 very subtle soft variation. Bottom 30 percent: a small softly lit isometric stone plaza extending across frame with tasteful small rounded dark teal shrubs at back left and back right, a few little rocks, distant tiny dark house silhouettes faint in background edges. Center of plaza empty, reserved for separately rendered tall building. Soft painterly 3D casual mobile game art, fine quality, restrained contrast so a bright purple and warm gold tower composited later will dominate. Near edges fade into uniform deep navy #102C53. Avoid bright sky, stars, glowing portals, moon, decorative borders, text, buildings in foreground. This is backdrop only, not a finished interface.


## Header/card/navbar refinement — 18 noyabr

İstifadəçi ilk native nəticəni nümunəyə eyni saymayıb, yalnız header, kartlar və navbarın uyğunlaşdırılmasını istəyib. Yeni header şəhər/yarpaq scenery-si built-in ImageGen edit ilə `reference.png`-dən ayrıca mətnsiz illüstrasiya kimi yaradılıb; `res/drawable-nodpi/inventory_collection_header.png` (1024×341) altında saxlanır. Dəqiq prompt `header-prompt.txt`-dədir. Heç bir idarə/mətn/logo/coin bitmap scenery-yə bişirilməyib.

Native header logo üçün mövcud1774×887 PNG-nin alpha bounds-u68,102–1681,797 ölçüldü; BitmapPainter həmin mənbə sahəsini yeni bitmap yaratmadan çəkir. Yeni rənglər reference piksel nümunələrindən GameTokens.InventoryCollection-a keçirildi. Kartlarda top160dp art sahəsi, native gradient/kontur,16sp başlıq/12sp metadata, fon və bina ayrı çəkilir; narrow/large-font halları adaptivdir. Navbar64dp səth,40dp görünən Play (48dp input target),20dp native ikon və aktiv tab çərçivəsidir. Header scenery-si aşağı kənardakı dekor üçün də eyni bitmap-dən istifadə edir.

Root/detail eyni shell content slotunu saxlayır; iki scenery bitmap-i görünən Inventory route lifetime-da bir IO decode ilə paylaşılır və detail/back zamanı saxlanır. Digər route-lar yükləmir. İki scenery-nin birlikdə RGBA ölçüsü təxminən2.33MiB, kart başına deyil. Mətn/şəbəkə oxusu optional scenery-ni gözləmir. Detal və digər səhifələrin mövcud chrome-u saxlanır.
