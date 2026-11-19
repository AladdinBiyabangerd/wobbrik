# Bazar bina art-ı — istifadəçi rəyi və rədd edilmiş fon sınağı

19 noyabr 2026. `fix/market-approved-native`, baza `a98272c`, mövcud owner working diff. İstifadəçi ölçü düzəlişinin kifayət etmədiyini, təsdiqli PNG-də binaların daha canlı göründüyünü bildirdi. Bu rəy qəbul edilməmiş vizual nəticədir; əvvəlki build/test PASS vizual qəbul sayılmır.

## Müşahidə və sınaq

Təsdiqli `docs/design/market-collection-v1/proposal.png` ilə son native müqayisədə fərq yalnız ölçü deyil. PNG daha zəngin fasad rəngi və işıq-kölgə, aydın dam/balkon çıxıntıları, isti pəncərələr və arxa yaşıllıqla vahid illüstrasiya təsiri verir. Native preview hazır modul atlasları ilə daha nazik/təkrarlanan və kəsilmiş fiqur kimi görünür. Bütün üslubları eyni bənövşəyi bina ilə əvəz etmək real seçilmiş hissələri itirər; qəbul edilmiş geometry, hissə ID-ləri və domain dəyişdirilməməlidir.

Yalnız Bazar kartları üçün hazırlanmış sınaq mövcud prepared atlasdan yumşaq arxa yaşıllıq və mövcud GoldLight tokenindən zəmin işığı əlavə etdi. İlk crop variantında düz aşağı kənar göründü; son variant tam şəffaf `front_left` sprite-ından istifadə etdi. Son `market-building-atmosphere/comparison.jpg` və `responsive.jpg` açılıb baxıldı: fon dolğunlaşsa da binanın özünün material/siluet keyfiyyəti PNG-yə çatmadı. Buna görə sınaq rədd edildi və yalnız bu sınağın iki production faylındakı dəyişiklikləri byte-identical əvvəlki vəziyyətə qaytarıldı. Əvvəlki ölçü düzəlişi və bütün owner işi saxlanır. `rejected-trial.diff` yalnız təkrar istehsal edilən QA sübutudur, aktiv implementasiya deyil. Bu qovluqdakı PNG-lər rədd edilmiş sınağın renderləridir, cari app-in screenshot-u kimi təqdim edilməməlidir.

## Yoxlama

Rədd edilmiş son-source sınaq üçün5/5host route/native render PASS,0failure/error/skipped; contrast15/15 PASS. Bu vizual uyğunluq PASS deyil. Build/lint həmin sınaqda işlədilməyib. Production faylları əvvəlki eyni-byte vəziyyətə qaytarılıb; əvvəlki build/lint nəticəsi yeni run deyil. `git diff --check` PASS. Domain/API/backend/cache/request yolu dəyişməyib, əlavə bitmap decode/prewarm və frame-state yoxdur. Emulator/real device/ART/GPU/IME/TalkBack yoxlanmayıb; font2/lazy capture məhdudiyyətləri saxlanır. Commit/merge/CI/push/deploy yoxdur.

## Dəqiq davam

Yeni geniş UI redesign-ı yox: Bazar üçün istifadə edilən modular bina art-ının fasad, dam/balkon, pəncərə işığı və yaşıllıq hissələrini təsdiqli PNG-nin vizual dilinə uyğunlaşdır. Əvvəlcə mövcud `PurpleBuildingArt.kt`, `BuildingArt.kt`, `InventoryScreen.kt::BuildingPreview`, `assets/building-purple/`, `assets/game-v3/` və `assets/finish/` əlaqələrini xəritələ. Real həndəsə/selected part ID və bütün üslublar qorunsun; bütöv fake bina şəkli ilə əvəz etmə. Mürəkkəb raster hissələri yeniləmək lazım gələrsə imagegen skill-i oxu; təsdiqli dizayn üçün yeni alternativ və təkrar təsdiq dövrəsi açma. Native normal/compact/font2 render və yanaşı müqayisə ilə yoxla. Yeganə plan `docs/PLAN.md` §1.1-dir.
