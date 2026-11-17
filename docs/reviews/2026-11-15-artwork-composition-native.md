# PLAN §1.1 — account artwork / commerce kompozisiyası

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Əvvəlki owner working diff üzərində dörd Kotlin faylına dar əlavə və yeni sibling artwork. Stage/commit/merge/push/deploy/CI yoxdur; index boşdur. Yeganə plan `docs/PLAN.md` §1.1-dir.

## Dəyişiklik və lokal review

- `NativeProfileEditor.kt`: `NativeAccountPortrait` overview/editor/picker üçün eyni üç hüceyrəli şəffaf account sheet-ini çəkir. Native radial mavi fon, cyan/rim frame və bütün selection/input semantikası qalır. BitmapPainter source region istifadə edir; crop üçün əlavə bitmap allocation yoxdur. Hüceyrə sheet hündürlüyündən alınır (2172×724, üç 724×724 hüceyrə). Raster-də UI/çərçivə/yazı yoxdur.
- `NativeProfileOverview.kt`: Rekordlar sheet-inin ayrıca sıx crop-u çıxarıldı, ortaq account portrait istifadə olunur. Daha yaxın reference pozası və ardıcıl baş/çiyin kadrı var. Rekordların artwork-ü dəyişmir. Overview layout/ribbon/shield/stat/save/conflict/XP dəyişmir.
- `MobileCommerceUi.kt`: İnventar Binalar tabının briefcase ikonu təsdiqli bina silueti ilə eyniləşdirildi; dock-un İnventar çantası saxlanır.
- `OwnedCityItemsScreen.kt`: saxlanılan dekor kartı da mövcud Commerce gradientini alır; şəhərdəki dekor Selected qalır. Native ikon, status, disabled placement və real repository yolları saxlanır.

Ərazi tabı/girişi qaytarılmadı, alternativ yer yaradılmadı. Real bina/dekor item ID və asset mapping-i dəyişmir; PNG-dəki nümunə binanı real kataloq əvəzinə qoymaq olmaz. Yeni rəng/ölçü tokeni, dependency, domain/API/DTO/schema/wallet qaydası yoxdur. AppBackend dəyişməsi tələb olunmur; backend testləri bu vahiddə işə salınmadı. Press animasiyası ayrıca qalır.

## Artwork provenance

Built-in `image_gen` istifadə olundu, CLI/API fallback yoxdur. Mənbə `docs/design/unified-mobile-v3/03-account.png`; nəticə `android-billing/app/src/main/res/drawable-nodpi/account_portraits_v3.png` (2172×724, alpha var, təxminən1.8MiB). Köhnə `records_portraits_v3.png` əvəz olunmadı. Alət istənən1536×512 yerinə2172×724 qaytardı; eyni3:1 hüceyrə müqaviləsi saxlandığından native source rect real ölçüdən hesablanır. Bu generativ rekonstruksiyadır, reference-dən lossless pixel extraction deyil; poza yaxınlaşır, üz/işıq detalında piksel fərqi qalır.

İcra edilmiş final prompt:

> Use case: background-extraction. Asset type: production Android avatar artwork sprite sheet. Input image: approved account UI reference, edit target. Extract ONLY the three head-and-shoulders character portraits from the top-right Avatar seçimi panel: builder in yellow helmet, pilot in brown flying helmet/goggles, woman architect with glasses and white hardhat. Preserve precisely their approved faces, expressions, pose, costume colors and proportions. Do not redesign. Produce one transparent PNG 1536x512 containing exactly three equal 512x512 cells in this order left to right builder, pilot, architect. Each character fills its cell, consistent close head-and-shoulders framing as in the approved avatar selector. Entire hat fully visible, shoulders reach cell bottom, centered horizontally. Actual alpha background, no blue background, no frames, no cyan border, no checkmark, no text, no UI, no podium, no ribbons. Characters only. Clean high-quality edges and detailed stylized 3D game rendering faithful to the input.

## Yoxlamalar

- Commerce native5/5 PASS1m22s, failure/error/skipped0. Normal/small/font1/font2, simulated inset+uzun mətn; real production repository/Ready, input/cancel, Ərazi absence və iqtisadi submission0 assertion-ları. Bu tur commerce mənbəyi finaldır; Profil asset əlavəsindən əvvəl compile olunub.
- Son account native overview5+editor5+real shell4 =14/14 PASS1m19s, failure/error/skipped0. Avatar pilot selection/apply və architect cancel, editable=false,48dp action və real shell profile cancel əvvəlki hostlarla yoxlanıb.
- `GameTokens` kontrast7/7 PASSmin6.48:1 (Muted/Raised), qalan6 cüt8.59–16.16:1. Yeni portrait mavi fonu dekorativdir, üzərinə mətn qoyulmur.
- Sandbox daxilində ilk Gradle wrapper lock-a çıxış olmadığına görə test başlamadı; lokal keş çıxışı ilə rerun edildi. Bu failure test PASS kimi sayılmır. Bir erkən picker view cəhdi fayl yaranmamışdan əvvəl tapılmadı; son real PNG-lər açıldı.
- Native PNG-lər `artwork-composition-native/` altında; bütün account renderlər `/private/tmp/wobbrik-account-art-final`, commerce `/private/tmp/wobbrik-art-composition-commerce`. Açılıb baxılanlar: overview bütün5 ölçü; editor390/font1,320/font1/font2,360/font2 inset; picker390/font1/font2,320/font2,360/font2 inset; commerce normal/small/font2/inset dekor və320/font2 bina kartı. `03-account.png` və `05-owned-and-listings.png` ilə müqayisədə personaj poza/kadrı və Binalar ikonu yaxınlaşdı. Font2-də aşağı content/grid scroll-dadır, əsas action pinned qalır; capture-da görünməyən ad sahəsi scroll-dadır.

Son normal invocation (render init olmadan): `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS49s. `git diff --check` PASS; index boşdur. CI işə salınmadı. Son production source-dan sonra dəyişiklik yoxdur; yalnız review/handoff yazıldı.

## Açıq sərhədlər

Bu bütün PLAN§1.1-in bağlanması deyil. Real kataloq bina/dekor artwork-ü PNG-dəki nümunələrlə eyni deyil və bu işdə dəyişdirilmədi. Profil lent/shield relyefi, panel/action ölçü ritmi və ümumi ekranların bütün material/piksel fərqləri qalır. Generativ account portretləri yaxınlaşdırmadır, exact pixel parity deyil. Ölçülmüş axıcılıq da açıqdır. Runtime/account/device/TalkBack/real IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmadı. Sonrakı dar vahid üçün Profil native lent/shield/action ritmi `03-account.png` ilə yoxlanmalıdır; hazır native port və avatar picker yenidən implementasiya edilməməlidir.
