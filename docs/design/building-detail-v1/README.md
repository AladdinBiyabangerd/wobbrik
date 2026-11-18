# Bina detalı — dizayn təklifi

18 noyabr 2026. Yeganə plan `../../PLAN.md` §1.1. İstifadəçi emulatorun yeni navbarını app üzrə təsdiqlədi, köhnə navbar üslubunu ləğv etdi və bundan sonra bina detalı üçün yeni dizayn nümunəsi istədi.

`proposal.png`: built-in ImageGen ilə `../../reviews/inventory-redesign/evidence/refinement/inventory-regular.png` native renderinə əsaslanan təklifdir. Dizayn baxışı üçün saxlanır; implementasiya, real account screenshot-u, native render, mərhələ qəbulu və ya detal dizaynının istifadəçi təsdiqi deyil. İstifadəçi navbarı artıq təsdiqləyib; yeni generativ şəkilin xırda navbar fərqləri həmin qərarı əvəz etmir. Native navbar authority-si `InventoryCollectionChrome.kt` / `CollectionNavbar`, commit `c2600f1` görünüşüdür.

Kompozisiya: mövcud gecə header-i və wallet; contextual geri/Bina detalı; çərçivəsiz böyük bina/plaza hero; ad və read-only sahiblik nişanı; mərtəbə/sakin məlumatları; maksimum satış və şəhər statusu; Bina görünüşü/Satışa çıxar əməliyyatları; ayrıca, zəif vurğulu Binanı sil; təsdiqli navbar. Nümunə15mərtəbə/60sakin/30sikkə məlumatları domain qaydası yaratmır. Native implementasiya real DTO dəyərlərini, ownership/revision/fresh mutation və satış/deletion icazələrini saxlamalıdır. Yeni level/rarity/upgrade/placement girişi yoxdur.

Tam generasiya prompt-u `prompt.txt`-dədir. Bu mərhələdə Kotlin, asset istehlakı, backend/API və runtime davranışı dəyişməyib. Native layout/font2/inset/kontrast/performance yoxlamaları implementasiya edildikdə aparılacaq; generativ PNG belə yoxlamaların sübutu deyil.

## Native implementasiya — 18 noyabr

İstifadəçi “okey implement” ilə bu dizaynı təsdiqlədi. `OwnedBuildingDetailPresentation.kt` real DTO və mövcud əməliyyat icazələri ilə native Compose implementasiyasıdır. Vahid `CollectionNavbar` bütün mobil menyu çağırışlarını əvəz edir. Əvvəlki təklif statusu tarixi generasiya mərhələsinə aiddir; nəticə və sübut `../../reviews/2026-11-18-building-detail-native.md`-dədir.
