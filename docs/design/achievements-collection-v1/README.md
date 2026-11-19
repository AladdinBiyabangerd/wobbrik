# Uğurlar — kolleksiya üslubunda dizayn təklifi

19 noyabr 2026. İstifadəçi Bazar vahidinin commit edilməsini və Uğurlar səhifəsi üçün yeni dizayn hazırlanmasını istədi. Bazar commit-i `82e6ca0`; dizayn branch-i `docs/achievements-design`. Mövcud Şəhər owner working diff-i saxlanır.

`proposal.png` Nailiyyətlər və Rekordlar görünüşlərini göstərən built-in ImageGen dizayn nümunəsidir. Üslub istinadı `../market-collection-v1/proposal.png`; promptdan əvvəl `GameTokens.InventoryCollection`, mövcud `MobileAchievementsScreen.kt` və PLAN §1.1/rekord qaydaları yoxlanıb. Tam prompt `prompt.txt`-dədir.

Ortaq loqo/landşaft header-i, balans, alt xəttli Nailiyyətlər/Rekordlar tabları və Uğurlar aktiv 7-slot navbar. Nailiyyətlərdə trophy, accepted progress kartları və tamamlanma statusu; Rekordlarda Ümumi/Həftəlik, podium, tam sığan sıralama və öz oyunçu sətrinin vurğusu nəzərdə tutulur. Adlar, rəqəmlər, səviyyə və irəliləyiş nümayiş üçündür; yeni domain qaydası, mükafat və ya claim əməliyyatı yaratmır. Completion yalnız server award-dır. Rekordlarda görünən say gələcək native implementasiyada viewport/font ölçüsündən seçilməlidir; board-dakı say sabit məhsul tələbi deyil.

19 noyabrda istifadəçi bu dizaynı təsdiqlədi və native Kotlin + Compose implementasiyasına başlamağı tapşırdı. İlkin generativ addımda app kodu/API/backend dəyişdirilməmişdi; native vahidin sübutu ayrıca `../../reviews/2026-11-19-achievements-approved-native.md`-də saxlanır. Generativ board kontrast, 48dp input, kiçik mobil/font2/inset və runtime sübutu deyil. Native implementasiyada səth, mətn, ikon və progress kodla qurulacaq; illüstrasiyalar ayrıca art ola bilər. Bütöv board raster skin kimi istifadə edilmir. Yeganə plan `../../PLAN.md`-dir.

Son `proposal.png` açılıb baxıldı: hər iki görünüşdə Uğurlar aktivdir, tab selection ayrıdır, podium xalları86/74/68 düzgün sıralanır. İlkin board `proposal-initial.png`, rəqəm düzəlişi `score-edit-prompt.txt`-də saxlanır. Generativ xırda tipoqrafiya/ikon fərqləri native parity iddiası deyil.
