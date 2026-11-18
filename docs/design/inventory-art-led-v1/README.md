# İnventar — 18 noyabr dizayn sınağı

Yeganə icra planı: `../../PLAN.md` §1.1. İstifadəçi yalnız bu səhifəni sınamaq, nəticəni bəyəndikdən sonra qalan səhifələrə keçmək istəyib. `reference.png` chat-də bəyənilən built-in ImageGen maketidir, native render deyil. 7-slot naviqasiya, aktiv İnventar vurğusu, yığcam tablar və art-led kartlar istinaddır.

Kartın təsviri mövcud modular bina renderer-i ilə seçilmiş material/forma əsasında çəkilir; əvvəlcədən çəkilmiş sample tower istifadə edilmir. Mövcud dekorativ preview renderer-i 6–8 representative mərtəbə göstərir, mətn həqiqi server mərtəbə sayıdır. Onun dizayn/fizika sərhədi bu vahiddə dəyişmir. Yeni fon yalnız dekorativ səhnədir; idarəetmə və mətn rasterə bişirilmir.

## Asset provenance

`android-billing/app/src/main/res/drawable-nodpi/inventory_collection_backdrop.png`: built-in ImageGen ilə yaradılıb, sonra sips ilə512×512 ölçüyə kiçildilib. Bir RGBA decode təxminən1MiB, fayl təxminən256KiB. `rememberResourceArt` IO-da yalnız görünən İnventar kökü üçün bir dəfə yükləyir, kartlar eyni ImageBitmap-i paylaşır; failure məzmunu bloklamır. Bina qrafikası dəyişməyib.

Final generation prompt (verbatim):

> Use case illustration. Create a quiet environmental background illustration asset for the art area of a dark navy whimsical city-building mobile game inventory card. Square bitmap, no UI, no typography, no cards, no frame, no central building. Entire upper 70 percent mostly empty deep navy night atmosphere matching #102C53 and #193D69 very subtle soft variation. Bottom 30 percent: a small softly lit isometric stone plaza extending across frame with tasteful small rounded dark teal shrubs at back left and back right, a few little rocks, distant tiny dark house silhouettes faint in background edges. Center of plaza empty, reserved for separately rendered tall building. Soft painterly 3D casual mobile game art, fine quality, restrained contrast so a bright purple and warm gold tower composited later will dominate. Near edges fade into uniform deep navy #102C53. Avoid bright sky, stars, glowing portals, moon, decorative borders, text, buildings in foreground. This is backdrop only, not a finished interface.
