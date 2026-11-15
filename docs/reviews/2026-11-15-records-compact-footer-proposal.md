# Rekordlar — yığcam alt panel təklifi, 15 noyabr 2026

İstifadəçi Daha çox/Yenilə/Hesaba qayıt düymələrinin alt-alta böyük düzülüşünü rədd edib, əvvəl konkret dizayn nümunəsi və yalnız təsdiqdən sonra implementasiya istəyib. **Bu təklif hələ təsdiqlənməyib və production-a tətbiq edilməyib.**

Təklif: üç əməliyyat tək 64dp alt paneldədir. Solda icon+Hesab, ortada vurğulanan Daha çox, sağda icon+Yenilə. Daha çox siyahıdakı ayrıca tam-enli sətirdən bu panelə keçir; hesab və refresh ikinci dərəcəli əməliyyatlardır. Font2-də yan əməliyyatlar icon olaraq qalır, accessibility adları saxlanır. Bütün toxunma sahələri ən az 48×48dp-dir. Siyahı body-si scroll edir, panel aşağıda sabitdir.

Mövcud GameTokens rəng/ölçü/radiusları, təsdiqli podium/avatar art-ı və mövcud başlıq/scope chrome-u saxlanır. Əsas düymə mövcud Accent/Text gradient üslubundadır. Yeni palette/asset/domain adapter/backend dəyişikliyi yoxdur. Preview-nun ad/rank/xalları açıq sintetik fixture-dir; Daha çox klikinin loading və cavab halları host tərəfindən idarə olunur, digər kliklər inert-dir; real oyun axınının hazır implementasiyası deyil.

Fayllar:
- `profile-leaderboard-visual/render/CompactRecordsProposalTest.kt`: yalnız host sourceSet-də preview; app sourceSet-i dəyişməyib.
- `profile-leaderboard-visual/compact-proposal/{390-font1,360-font1,320-font2,preview-board}.png`.
- `profile-leaderboard-visual/tools/DockProposalSheet.java`: tam viewport-ları kəsmədən contact sheet yaradır.

Son Java21/offline native host **3/3 PASS20s**, 390×844font1/360×640font1/320×568font2; üç əməliyyatın ekranda olması və minimum target ölçüləri assert edilib. Hər test sinfi ayrıca JVM-dədir. Son üç raw PNG-yə və yekun board-a baxıldı. Kontrast3/3PASSmin6.48 (Muted:Raised, Text:Raised, Background:Accent). `git diff --check`PASS. Log `/private/tmp/wobbrik-records-dock-proposal-final.log`.

Bu tur production source/UI/data/API dəyişdirilməyib; real callback-lərə qoşulma yalnız konkret təklifin istifadəçi təsdiqindən sonra olacaq. Növbəti implementasiya loading/disabled/retry/end-of-pagination hallarını və mövcud server cursor/scope/session fencing-i saxlamalıdır. Emulator/CI/commit/merge/push/deploy yoxdur. Yeganə plan `docs/PLAN.md`-dir; bu sənəd dizayn review-udur.


## Daha çox klikinin nümunəsi

İstifadəçinin əlavə istəyi ilə `compact-proposal/more-flow-board.png` hazırlandı:

1. Daha çox klikindən sonra mərkəzdə indikator + Yüklənir görünür, təkrar klik deaktivdir; mövcud nəticələr qalır.
2. Nümunə cavabında 4–8-ci yerlər əvvəlki 1–3-ün davamına əlavə olunur. Eyni siyahı davamı göstərmək üçün sürüşdürülür; başqa ekran açılmır, köhnə səhifə silinmir.
3. Növbəti cursor olmadıqda mərkəz Hamısı yükləndi kimi deaktiv göstərilir (böyük şrift üçün qısa Bitdi label-i preview kodundadır).

Son ayrıca native **3/3 PASS21s**: loading disabled/indicator, əvvəlki Aydan sətrinin saxlanması, yeni Nigar və is_self/Sən sətrinin görünməsi, davamda yenidən aktiv düymə və sonda deaktiv düymə assert edilib. İlk selector eyni Əli adına görə qeyri-dəqiq idi; Aydan ilə düzəldilib, ilk uğursuz test PASS sayılmır. Üç son raw state və yekun board render edilib baxıldı. Log `/private/tmp/wobbrik-records-more-proposal-final.log`.

Bu davranış da yalnız təklifdir: serverə sorğu göndərilmir, fixture cavabı host test verir. Production source/data/API dəyişməyib; footer və davranış təsdiqi hələ gözlənir. Bütün mobillərdə bu üç yeni state matrisi ayrıca yoxlanmayıb; bu davam 390×844/font1 nümunəsidir.
