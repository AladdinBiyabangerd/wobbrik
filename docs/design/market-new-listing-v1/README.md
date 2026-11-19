# Bazar — Yeni elan dizayn nümunəsi

19 noyabr 2026. İstifadəçi Uğurlar podium/chrome nəticəsini “okeydir” ilə qəbul etdi və Bazardakı yeni elan yerləşdirmə yeri üçün dizayn istədi. Bu vahid dizayn nümunəsidir; yeni native implementasiya təsdiqi deyil. Branch `docs/market-new-listing-design`, baza `82e6ca0`; əvvəlki Uğurlar/Şəhər working diff-i saxlanır, commit/merge/push yoxdur.

Vizual istinad `../market-collection-v1/proposal.png`. Mövcud `GameTokens.InventoryCollection` palitrası, `MarketScreen.kt` picker və `MarketSalePage`, PLAN §1.1 oxundu. İki görünüş: satılacaq öz binasını seçmək və seçilmiş bina üçün satış qiymətini daxil etmək. Mənim elanlarım → + Yeni elan girişi saxlanır; Ərazi/Dekor satışı və yeni menyu yeri yoxdur.

Mövcud qaydalar saxlanır: maksimum qiymət binanın `maxPrice` məlumatıdır; komissiya5%, eyni anda5aktiv elan; bina şəhərdə qalır. Nümunə100qiymət/5komissiya/95satıcıya və150maksimum real hesab/kataloq deyil. Yeni iqtisadiyyat və ya API qaydası yaradılmır. Nümunədə iki mərhələ işarəsi yalnız mövcud seçim→qiymət axınının vizual təqdimatıdır.

Built-in ImageGen istifadə olunur; tam prompt `prompt.txt`. Generativ PNG native Compose implementasiya, toxunma sahəsi, kontrast, font2/kiçik ekran/IME/error/loading/retry sübutu deyil. Implementasiya ayrıca tapşırıqla hazırlananda real qəbul edilmiş bina/geometry, fresh mutation, journal, revision/dedup və hesab sərhədləri saxlanmalıdır. UI/mətn/ikon/panel native, mürəkkəb bina illüstrasiyası ayrıca art olmalıdır; bütöv PNG skin deyil. Yeganə plan `../../PLAN.md`-dir.

## Şəkilsiz qiymət mərhələsi — v2

İstifadəçi ilkin implementasiya göstərişindən dərhal sonra işi dayandırdı; app kodu dəyişmədi, yalnız `feat/market-new-listing-native` branch-i açıldı. Sonra qiymət mərhələsində bina şəklini təkrarlamamağı müzakirə etdi və yeni PNG istədi. `proposal-v2.png` built-in ImageGen edit-dir: qiymət ekranında böyük bina illüstrasiyası çıxarılıb, yerinə ad/mərtəbə və “Dəyiş” keçidli yığcam panel qoyulub. Bina seçim ekranı saxlanır. Son PNG açılıb baxıldı; v2 hələ istifadəçi təsdiqi və implementasiya göstərişi gözləyən nümunədir.

## Sadə qiymət mərhələsi — v3

İstifadəçi v2 qiymət mərhələsini çoxmətnli və çaşdırıcı saydı, təklif olunan sadə variantın yeni PNG-sini istədi. `proposal-v3.png`: bina adı/mərtəbə/Dəyiş bir sətir, dominant qiymət xanası, kiçik maksimum helper, komissiya/net bir sətir, tək dərc düyməsi. Ayrı maliyyə paneli, məlumat qeydləri və Ləğv et çıxarılıb; geri oxu saxlanır. Built-in ImageGen edit; nəticə açıq görüntüdə yoxlanıb. Bu yalnız yeni nümunədir, implementasiyaya başlanmır.

## Yığcam mərkəzi qiymət xanası — v4

İstifadəçi v3-də qiymət xanasını mənasız uzun xətt saydı və yeni dizayn istədi. `proposal-v4.png` built-in ImageGen edit-dir: geniş xana çıxarılıb, mərkəzdə təxminən150dp ekvivalent enli yığcam qiymət sahəsi, kiçik başlıq və helper yerləşdirilib. Digər sadə axın saxlanır. Nəticə görüntüdə yoxlanıb; yalnız nümunədir, implementasiya təsdiqi deyil. Tam edit prompt `prompt-v4.txt`.

## Qiymət daxil edilərkən — klaviatura nümunəsi

`price-keyboard-v1.png` istifadəçinin ayrıca istədiyi focus/IME görünüşüdür. Qiymət eyni yığcam xanada redaktə edilir, native Android rəqəm klaviaturası açılır; ayrıca modal və custom keypad nəzərdə tutulmur. Bu təklifdə klaviatura açıqkən navbar gizlənir və dərc düyməsi IME üstündə görünür. Klaviaturanın konkret şəkli sistem/istifadəçi seçimindən asılıdır; PNG-dəki generativ klaviatura real Gboard screenshot-u və ya inset/IME render sübutu deyil. Built-in ImageGen istifadə edilib; nəticədə rəqəmlər/caret/qiymət/action görünür. Son dizayn təsdiqi və implementasiya ayrıca gözlənir.

19 noyabr — son istifadəçi qərarı: v4 və klaviatura nümunəsi “okeydir implement etmeye basla” ilə təsdiqləndi. Native tətbiq `feat/market-new-listing-native` working diff-indədir; sübut və məhdudiyyətlər `../../reviews/2026-11-19-market-new-listing-native.md`-də saxlanır. Generativ nümunələr real klaviatura screenshot-u deyil.
