# Rekordlar — Arrowz say qaydası və Daha çox olmayan təklif, 15 noyabr 2026

İstifadəçi Arrowz layihəsində **yalnız göstərilən oyunçu sayının necə seçildiyini** öyrənib eyni prinsipi Wobbrik-də tətbiq etməyi, Daha çox olmayacağına görə yeni dizayn nümunəsi göstərməyi istəyib. Say qərarı təsdiqlidir; yeni görünüşün production tətbiqi hələ təsdiq gözləyir.

## Arrowz-da yoxlanmış cari qayda

Read-only repository: `/Users/mac/My Workspace/arrowz`, `main`, HEAD `8ca9c86a`. Arrowz faylları dəyişdirilməyib; oradakı inherited dirty sənədlərə toxunulmayıb.

- `feature/social/.../LeaderboardScreen.kt:120,381,445,470`: göstərilən say ekranın real board sahəsinə sığan podium + tam sətirlərdir. Board scroll etmir, Show more yoxdur. Podiumdakı 3 nəfər cəmə daxildir; aşağıdakı siyahı onları təkrarlamır. Yarım sətir yerləşdirilmir.
- `app/.../social/QuotaCaches.kt:99–105`: serverdən soruşulan say MinRows4, MaxRows20, DefaultRows8-dir. Bu 4 rəqəmi görünüşə zorla ən az 4 nəfər yerləşdirmək demək deyil; UI yalnız tam sığanı yerləşdirir.
- `app/.../ui/SocialRoutes.kt:145`: fit callback sorğu sayını 4–20-yə clamp edir. Ölçü ilk 500ms-də gəlməsə 8 fallback-dır.
- `app/src/test/kotlin/com/aladdin/arrowz/social/LeaderboardFitTest.kt`: podiumun saya daxil olması, bütöv sətir sərhədi və sığan say qədər oxu üçün testlər mövcuddur. Arrowz testləri bu tur işə salınmayıb; source/test read-only yoxlanıb.

Köhnə country planındakı `.limit(50)` tarixi qeyddir; cari kod və fit testləri əsas götürülüb. Firestore, quota TTL/cache siyasəti, country, həftə UTC sərhədi, ulduz/reward/tie-break, sıfır ulduzun podium qaydası və ayrıca pinned me-rank Wobbrik-ə köçürülmür. Wobbrik AppBackend authority-si, qəbul edilmiş mərtəbə hesabı, Bakı həftəsi və real `is_self` müqaviləsi qalır.

## Yeni dizayn nümunəsi

`profile-leaderboard-visual/fitted-proposal/fit-preview-board.png`:

- Alt panel və Daha çox yoxdur. Sol üst ox Hesaba qayıdır; sağ üst 48dp dairəvi düymə Yenilə üçündür. Mövcud Wobbrik source art/tokens/vizual dili saxlanır.
- İlk üçlük podiumda adları ilədir; aşağıda 4-cü yerdən davam edir. Real tutum hər sətirin faktiki ölçüsünü ölçüb seçir, 20 nəfəri keçmir. Göstərilən şəxs podiumda və adi siyahıda iki dəfə sayılmır.
- Qalan oyunçular üçün aşağıda yalnız dekorativ `•••` var; düymə və ya gizli pagination deyil. Scroll yoxdur. Boş yer saxta oyunçularla doldurulmur.
- Font2-də əvvəlki Wobbrik adaptasiyası kimi dekorativ podium göstərilmir; sığan tam normal sətirlər seçilir. Minimum request4/font fallback8 bu halda kəsilən və ya saxta əlavə oyunçu göstərmək üçün istifadə olunmur.
- `is_self` rəngi yalnız fixture-də görünən real sıra nümunəsinə tətbiq edilir; ayrıca own-rank endpoint/pinned row uydurulmur.

Son measured nümunələr: 390×844/font1 **7** unikal nəfər (podium3+4sətir), 360×640/font1 **5** (podium3+2sətir), 320×568/font2 **2** tam sətir. Bu saylar per-device sabit rəqəm deyil; viewport/font/məzmun ölçülərinə görə hesablanır. Fixture 20 sintetik oyunçudur; serverə sorğu yoxdur.

## Fayllar və yoxlamalar

- `render/FittedRecordsProposalTest.kt`: yalnız host dizayn nümunəsi. `CompactRecordsProposalTest.kt`-də yalnız iki preview helper-i internal edilib ki, eyni row/icon üslubu təkrar istifadə olunsun. App sourceSet dəyişməyib.
- `tools/DockProposalSheet.java <fitted-proposal-dir> fit`: kəsilməyən tam viewport board-u və ölçülmüş sayları göstərir. Raw PNG/count.txt də saxlanır.
- Son Java21/offline native **3/3 PASS21s**. Hər yerləşdirilən sətirin board sərhədi daxilində bütöv olması, unikal oyuncu sayı, Daha çox-un olmaması və iki header target-in minimum48×48dp/visibility yoxlamaları assert edilib.
- Son 3rawPNG və son board-a baxıldı. Böyük şriftdə dekorativ dots üçün sabit token ölçülü Canvas seçilib; mətn şriftinin böyüməsi əlavə boşluq tutub sığan oyunçunu gizlətmir. Mətn fontScale-i saxlanır.
- Kontrast4/4PASSmin8.22; `git diff --check`PASS. Log `/private/tmp/wobbrik-records-fit-proposal-final.log`. Bir əvvəlki font2 geometry diaqnostikası `/private/tmp/wobbrik-records-fit-font.log` tarixi testdir, son count deyil.

Production UI/core/data/API və AppBackend bu tur dəyişməyib. Say qərarı `docs/PLAN.md`-də qeyd edilib; dizayn təsdiqindən sonra həmin real axına tətbiq ediləcək. Emulator, CI, commit, merge, push, deploy yoxdur. Bu review empty/error/few-player/rank-gap/uzun ad/full locales və real HTTP/device qəbulu deyil. Köhnə Daha çox dock/flow təklifləri yeni bu görünüşün əvəzi deyil.
