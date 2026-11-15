# Rekordlar — təsdiqli panelsiz dizaynın tətbiqi

Branch `feat/market-leaderboard`, baza `9653b98`, uncommitted working diff. Əvvəlki market/data/owner/release işi qorunur. İstifadəçi fitted-proposal nümunəsini açıq təsdiqləyib tətbiq istədi; sonra rank >9999 üçün `99999+`, ad üçün maksimum15 simvol qərarı verdi. Köhnə More/footer nümunələri cari tətbiq əsası deyil.

## Nəticə və sərhədlər

- `LeaderboardScreen`/yeni `LeaderboardBoard`: solda Hesaba qayıt, sağda Yenilə; alt panel, Daha çox və list scroll yoxdur. Podium3 cəmə daxildir, təkrar sətir deyil. Yalnız tam sığan real sətirlər yerləşir; qalan nəticələr üçün passiv dots. Font2 podiumsuzdur. Böyük rəqəmli xal və uzun sətir üçün mövcud kompakt token pilləsi istifadə edilir; kiçik/normal ekran görünüşü qorunur.
- `LeaderboardReads`/`LeaderboardWindowPolicy`: ilkin/cəmi hədəf4–20, ölçü alınmazsa fallback8. Pəncərə kiçiləndə mövcud snapshot reuse, böyüyəndə yalnız çatışmayan1–N nəticə dəqiq opaque cursor ilə oxunur. HTTP oxuları serialdır; ölçü dəyişikliyi in-flight cavabı ləğv etmir. Refresh yeni snapshot, network growth retry eyni cursor, expiry400/invalid snapshot fresh retry. Uğursuzluq özbaşına request dövrü yaratmır.
- Session/Ready/epoch/cancellation fencing, scope/week/snapshot/expiry, rank+score monotonluğu və server is_self qorunur. Boş progress cursor, oversize response, qarışıq snapshot qəbul edilmir. UI üçün primitiv sahələrdən ölçü probe-u yerləşdirilmir, saxta domain nəticəsi yaratmır. Privacy rank gaps doldurulmur.
- `CommunityFrame` refresh parametrləri optional-dır; hesabın mövcud kompozisiyası qorunur. Source PNG-lər dəyişməyib. `GameTokens.Label` mövcud canonical CSS `--type-label`14sp qarşılığıdır; yeni palitra/dependency/pin yoxdur.
- Rank >9999 yalnız təqdimatda `99999+`; real rank/order dəyişmir. `ProfileReplacement` və editor yeni/dəyişdirilən adları maksimum15 Unicode code point ilə məhdudlaşdırır, 16-simvolluq input qəbul edilmir. Əvvəlki24 ad oxunur və mərhələli qısaldıla bilir, 15-ə düşənədək save disabled-dır. Parserin24 uyğunluğu saxlanır.

## AppBackend uyğunluğu

Ekran tutumu üçün server dəyişikliyi tələb olunmur: `docs/wobbrik-game-api.md` limit1–100; cursor səhifə ölçüsündən asılı deyil. `leaderboard.go` SQL limit+1, son rank cursor; mövcud platform testində limit1→10 var. Arrowz-dan yalnız say/fit prinsipi götürülüb; ranking, UTC, Firestore, reward və pinned-row köçürülməyib.

Adın15 həddi ayrıca AppBackend `feat/wobbrik-public-profiles` dirty branch-ində `ValidProfileName`, sərhəd testi və müqavilədə dəyişdirildi. Schema/read/DTO/wallet/ledger dəyişmir, köhnə client >15 PUT üçün mövcud invalid-request alır. Mövcud uzun adlar silinmir. Backend fokuslu normal/race `TestValidProfileName` və `go vet ./internal/wobbrik` PASS; bu input validation dəyişikliyi üçün PostgreSQL inteqrasiya suite-i təkrarlanmayıb. Deploy yoxdur.

## Yoxlamalar

Son nəticələr: native16/16 PASS55s; adi core136/engine95/app24 (cəmi255) test, assembleDebug və lintDebug PASS15s; kontrast4/4min8.22, diffcheck və source/copy SHA256 PASS. Əsas3raw changed0,15PNG back-region PASS. `profile-leaderboard-visual/fitted-production/native-results.txt`, `checks.txt` və `parity.txt`-də saxlanır. Native host real screen/repository/wire işlədərək yalnız transport/recovery fixture-ni əvəz edir; production demo adapteri əlavə edilmir.

Ssenarilər: üç portret ölçü/font, all scope, loading, error+retry/no-loop, empty, iki oyunçu, rank gaps,15-simvolluq ad/Long.MAX rank+xal, refresh, shrink/grow no-extra-read, session leave/relogin, expired cursor fresh retry,503 same-cursor retry, profil legacy24→23→15/input16 rəddi/PUT15. Whole-row bounds,48dp header controls, real back callback, no More/footer/scroll assertions var.

İlkin native long testində görünən node ilə yerləşdirilməyən ölçü node-u eyni mətnə malik olduğundan assertion çoxlu node tapdı; görünən node yoxlaması ilə düzəldildi. Əvvəlki oversized row kənar halı kompakt mövcud spacing ilə düzəldilib. Adi lint ilk run-da ölçü nümunəsində API26 Instant.EPOCH istifadəsini tapdı; görünməyən ölçü ortaq primitive-row renderer-ə keçirildi, suppression/baseline əlavə edilmədi. Uğursuz ara run-lar PASS sayılmır.

## Lokal diff review

Bu vahidin yeni/əvvəlki dirty source-u scope üzrə oxundu: request limit/path/parse, cursor və session fencing, error retry, podium təkrarı/whole-row layout, name PUT/read uyğunluğu. Boş cursor progress, səhifələrarası score, cancel cavabı, scope ölçü reset-i, error auto-loop və API26 ölçü asılılığı tapılıb düzəldildi. Qalan bloklayıcı tapıntı yoxdur; unrelated inherited diff ayrıca dəyişiklik kimi qəbul edilməyib.

## Vizual sübut və məhdudiyyətlər

`fitted-production/production-board.png`: son üç telefon renderi. `approved-comparison.png`: hər ölçü üçün təsdiqli nümunə və production eyni fixture ilə yanaşı; əsas üç raw renderdə piksel fərqi0 tələb edilir. Back-region integrity PNG encode-dan sonra yoxlanır; natamam header/footer patch/stitch edilmir. Error/empty/long/profile raw-lar ayrıca saxlanır.

Bu vahid təsdiqli rekord dizaynının tətbiqidir; profilin əvvəlki bütöv ekran paritet fərqlərini və mərhələ5 qəbulunu bağlamır. Native SDK34 host/sintetik serverdir; device/real HTTP/TalkBack/IME/GPU/API24/bütün locale yoxlanmayıb. Mobil portret; planşet/landşaft son qərarla bu vahiddən kənardır. Emulator açılmadı. CI/commit/merge/push/deploy aparılmadı.
