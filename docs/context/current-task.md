# Geometry prototipi üçün mənbə yoxlaması — 4 noyabr 2026

## Completed
- Wobbrik handoff-u, AppBackend AGENTS/README/handoff-u, `docs/wobbrik-game-api.md` geometry müqaviləsi, Wobbrik `docs/kotlin-compose-plan.md` §5 oxundu. AppBackend-in öz handoff-u geometry üzrə köhnədir; son API müqaviləsi və aşağıdakı əvvəlki Wobbrik qeydləri əsas götürüldü.
- Wobbrik mənbə faylları və output model/struktur faylları, AppBackend geometry/model/fixture fayl adları və mövcud checkpoint test nümunələri yoxlandı. Yoxlanan fayllarda təsdiqli eni/dərinliyi, mərtəbə vertex-ləri, lokal koordinat vahidi və ya ölçülü bina modeli tapılmadı. Output JSON-ları UI konsept prompt-larıdır; ölçü authority-si deyil. Canlı DB, şəxsi istifadəçi məlumatı və xarici model mənbələri yoxlanmayıb.
- AppBackend `internal/wobbrik/attempts.go`: snapshot `json.RawMessage`-dir; JSON etibarlılığı və 16 KiB həddi yoxlanır, typed mərtəbə geometry-si yoxlanmır. `finish.go` həmin snapshot-u binaya köçürür. Testlərdə `{}`, `{"state":"stable"}`, `{"last_successful_floor":10}`, `{"floor":10}` var; bunlar real ölçü məlumatı deyil.

## Current state
- Bu vahid mənbə yoxlaması və sxem/precision tövsiyəsidir; işlək geometry prototipi, union alqoritmi və ya təsdiqlənmiş yeni wire müqaviləsi deyil. Real kontur olmadan ölçülü prototip üçün əsas yoxdur.
- Backend kodu/DTO/schema/migration dəyişməsinə bu yoxlama mərhələsində ehtiyac yoxdur. Mobil axın dəyişməyib; placement bağlıdır. Yeni engine/geometry versiyası adı təyin edilməyib.

## Decisions — prototip üçün tövsiyə, təsdiqlənmiş API deyil
- Typed giriş: `FloorGeometryDraft` daxilində ardıcıl, təkrarsız `floor_index` və `projection`; `projection` hər mərtəbənin bina lokal x/y sistemində tam Polygon/MultiPolygon proyeksiyasıdır. Konturu width/depth-dən və ya mərtəbə sayından bərpa etmə. Bu, düzbucaqlı və düz bina fərziyyəsini aradan qaldırır; ayrı floor pose/rotation tətbiq edib konturu iki dəfə transform etmək riskini azaldır. Hündürlük/fizika bərpası bu minimal 2D sxemə daxil deyil.
- Struktur: `PointTicks{x,y}` — canonical işarəli onluq integer string-ləri; `Ring` — nöqtə ardıcıllığı; `Polygon{outer,holes}`; tagged `Projection` — yalnız Polygon və ya MultiPolygon. Ring-lər unikal vertex-lərlə saxlanıb son kənarla başlanğıca bağlanır; wire-da təkrar son vertex yoxdur. Bu daxili draft GeoJSON deyil. Winding və komponent sırası gələcək canonicalization qaydasında sabitlənməlidir.
- `BuildingGeometryDraft` geometry schema/precision policy identifikatoruna və floor siyahısına bağlıdır. Server metadata-sı mənbə attempt, qəbul edilmiş sequence, engine/rules versiyası və dəyişməz shape identifikatorunu bağlamalıdır; provenance client-in özünün yazdığı etibar bayrağı deyil. Server bütün floor proyeksiyalarının union-unu hesablayır; client-in verdiyi yekun footprint authority deyil. Union disjoint hissə və dəlikləri saxlamalıdır.
- Sadəcə schema/topology yoxlaması fiziki oyunun həqiqiliyini sübut etmir. Hər floor konturunun etibarlı model/simulyasiya mənbəyindən necə təsdiqlənməsi ayrıca qurulmalıdır. Opaque structural-v1 snapshot bu sxemə avtomatik çevrilmir.
- Sabit dəqiqlikli integer tick + API decimal-string istiqaməti saxlanır. Konkret tick addımı, unit scale, koordinat/vertex/floor hədləri və bucaq dəqiqliyi seçilmir: ölçülmüş mənbə yoxdur. Pixel, konsept şəkli, sakin sayı və torpaq tutum ekvivalentlərindən rəqəm çıxarmaq olmaz. Arbitrary precision addımı seçmək etibarlı prototip ölçüsü yaratmır.
- Precision seçim metodu: real kontur korpusu, ən dar saxlanmalı xüsusiyyət, mənbə xətası və maksimum radius/koordinat diapazonu əldə ediləndən sonra namizəd tick addımlarını müqayisə et. Ən yaxın tick-ə ties-to-even ilkin sınaq namizədidir, müqavilə qərarı deyil. Tək koordinat yuvarlaqlaşdırma xətası ən çox q/2, nöqtə yerdəyişməsi q/√2-dir; transform/union xətası ayrıca ölçülməlidir. Bucağın Δθ xətası radius R-də 2R·sin(|Δθ|/2) yerdəyişməsi yaradır (Δθ radianla); angle addımı da radius və ümumi xəta büdcəsinə görə seçilir. Bunlar ölçü deyil, seçim meyarlarıdır.
- Lokal normallaşdırma → bütün floor-ların union-u → ayrıca placement transformu → şəhər tick şəbəkəsində yekun normallaşdırma prototipdə müqayisə ediləcək ilkin ardıcıllıqdır. Union kəsişmələri integer vertex-lərdən belə kəsr koordinat yarada bilər; dəqiq aralıq hesab və vahid snap siyasəti yoxlanmalıdır. Giriş və nəticə topology-si yoxlanır; collapse/invalid kontur rədd edilir, gizli təmir yoxdur. Yekun normalized forma üzrə sahə kəsişməsinə epsilon güzəşti yoxdur.

## Remaining work
- Etibarlı export: vahid/axis/origin təsviri, hər uğurlu floor-un tam proyeksiyası, dəqiq kontur mənbəyi və maksimum diapazon. Düz, əyri/çıxıntılı və incə xüsusiyyətli real nümunələr lazımdır; bunların ölçüləri burada uydurulmayıb.
- Mənbə əldə ediləndən sonra yalnız izolyasiya edilmiş validator/union/precision prototipi: sərhəd təması, kiçik müsbət sahə kəsişməsi, containment, hole/disjoint hissələr, mənfi koordinatlar, rounding tie, collapse, overflow və təkrar normallaşdırma yoxlamaları. Synthetic testlər istifadə olunsa real ölçü kimi təqdim edilməməlidir.

## Relevant files
- AppBackend: `docs/wobbrik-game-api.md` geometry bölmələri; `internal/wobbrik/attempts.go`, `finish.go`, `city.go`; `internal/platform/wobbrik_{attempts,finish,market,rewards,http}_test.go`.
- Wobbrik: `docs/kotlin-compose-plan.md` §5, `docs/game-plan.md`, bu handoff-un əvvəlki qərarları.

## Checks performed
- Yuxarıdakı kod/müqavilə/fixture oxuları və model fayl axtarışları. Yeni runtime kodu olmadığına görə test suite icra edilmədi. `city.go`-da PlacementEnabled bool zero-value false olaraq qalır; placement aktivləşdirilmədi.
- Wobbrik qovluğu Git repository deyil; `git status` işləmədi. Vizual dəyişiklik və render yoxdur.

## Exact next step
- Etibarlı ölçülü floor export-u/model mənbəyini əldə et; mənbə yoxdursa precision rəqəmi və işlək geometry vədi vermə. Mənbədən sonra bu draft sxemi ilə kiçik backend-dən ayrılmış prototip qur və precision namizədlərini ölç. Yeni API/migration/engine uyğunluğu yalnız həmin nəticələrdən sonra ayrıca vahiddir; legacy inventory-only və placement disabled qalır.

---

# Tamamlanmış vahid — ölçülərdən asılı olmayan şəhər serveri, 4 noyabr 2026

## Completed
- Son texniki istiqamətlər də AppBackend/docs/wobbrik-game-api.md müqaviləsinə köçürüldü: lokal oyun vahidi, integer tick/decimal-string istiqaməti, ayrıca immutable shape və placement transformu, server-authoritative normalized footprint, invalid/collapsed konturun rəddi, server-validated typed floor provenance tələbi. Ölçü/scale/precision addımı və konkret sxem/alqoritm açıqdır. Kod, DTO və migration dəyişməyib; placement bağlıdır.
- Davam chatində razılaşdırılmış footprint, dönmə/təmas/kəsişmə, torpaq birləşməsi və genişlənmə, sabit dəqiqlik, normalizasiya, Polygon/MultiPolygon və legacy siyasəti AppBackend/docs/wobbrik-game-api.md sənədinə köçürüldü. Konkret ölçülər/dəqiqlik addımı açıqdır; placement bağlıdır. Bu yalnız sənəd dəyişikliyidir.
- Əvvəlki ümumi/həftəlik stable leaderboard vahidi saxlanıb; API/migration 009 və testlər keçib.
- AppBackend city.go: GET /game/city — öz şəhər üzvlüyü, revision/server_time, səhifədən asılı olmayan sakin cəmi, ID cursor/limit. Population PostgreSQL numeric cəminin dəqiq onluq string-idir; limitsiz inventarda bigint overflow yaratmır.
- POST /game/city/buildings/return — standard envelope və BuildingRequest; own/non-deleted/revision yoxlaması, üzvlüyün silinməsi + account revision + receipt bir commit. Bina məzmunu/revision/mülkiyyət və aktiv elan qalır; wallet/XP/refund yoxdur.
- Migration 010: üzvlük real building owner-a composite FK ilə bağlıdır; ownership transfer əvvəl şəhərdən çıxarmalıdır. Building DELETE üzvlüyü cascade təmizləyir; account purge-in hansı binaları seçməsi dəyişmir. Owner-scoped page indeksi var.
- Əsas API/domain və Wobbrik game-plan/kotlin-compose-plan sənədləri yeniləndi.

## Current state
- Bu məhdud server vahidi tamamdır. Placement/move/replace və dekorasiya yerləşdirməsi yoxdur; GET /city açıq şəkildə placement_enabled:false verir. Tam adapter/Android host hazır deyil.
- City səhifələri live-dır: hər səhifə eyni account share lock altında population/revision/membership oxuyur. Səhifələr arasında revision dəyişərsə client yenidən başlamalıdır; bu materialized leaderboard cursor-u deyil.
- Return aktiv elanı etibarsız etmir; exact receipt retry sonrakı satışdan sonra yeni sahibin yerləşdirməsini silmir. Yeni command ilə artıq inventarda olan binaya return uğurlu desired-state əməliyyatıdır və revision artırır.
- Opaque structural-v1 snapshot-dan forma çıxarılmır; fizika/terminal failure sübutu yoxdur. Reward callback əvvəlki kimi client assertion-dır.
- Oyun/APK/SKU/Console/credential/deploy aparılmayıb. Vizual dəyişiklik yoxdur.

## Decisions
- İstifadəçi qalan texniki seçimləri agentin tövsiyəsinə həvalə etdi. Əvvəlki konkret ölçü və dəqiqlik addımını açıq saxlamaq məhdudiyyəti ləğv edilməyib; placement aktivləşdirmə səlahiyyəti verilməyib.
- Tövsiyə olunan texniki istiqamət: ekran pikselindən asılı olmayan lokal oyun koordinatları; serverdə sabit dəqiqlikli tam ədəd tick-ləri, API-də dəqiq ötürmə üçün decimal-string təqdimatı; scale/diapazon real modellər üzrə sonradan seçilir. Sərbəst dönmə məhdud istiqamətlərə bağlanmır; bucaq kodlaşdırması/dəqiqliyi sonradan yoxlanır.
- Geometry sxemi üçün istiqamət: ayrıca dəyişməz lokal Polygon/MultiPolygon konturu və ayrıca placement mövqeyi/bucağı; sxem geometry versiyasına və precision siyasətinə bağlıdır. Server normallaşdırılmış yekun formanı yoxlayır və client həmin formanı göstərir. Normallaşdırma etibarsız və ya çökmüş kontur yaradırsa gizli təmir əvəzinə rədd edilir. Konkret alqoritm/kitabxana və rəqəmlər implementasiya yoxlamasından sonra seçilir.
- Etibarlı geometry mənbəyi üçün istiqamət: serverin doğruladığı typed mərtəbə məlumatlarından proyeksiya birləşməsi; kosmetika footprint-i dəyişmir. Köhnə opaque snapshot-dan təxmin edilmir; etibarlı bərpa mümkünlüyü ayrıca yoxlanana qədər köhnə bina inventarda qalır. Bu istiqamətlər hələ backend sxemi və ya işlək kod deyil.
- İstifadəçi şəhər görünüşü konseptini bəyəndi və iki UI qərarını təsdiqlədi: **Plan / Şəhər görünüşü** keçidi və binaya ayrıca baxışda kameranı onun ətrafında döndərmək. Plan yuxarıdan yerləşdirmə/footprint baxışıdır; Şəhər görünüşü maili kameradan fasad, hündürlük və əyriliyi göstərir. Ayrı bina baxışındakı kamera döndürməsi şəhərdə binanın mövqeyini/dönməsini dəyişmir. Bunlar gələcək UI tələbləridir, tətbiq olunmayıb; konseptdəki ölçü, mərtəbə sayı və vizual detallar ölçü müqaviləsi deyil.
- Bəyənilən şəhər görünüşü PNG referansı: `/Users/mac/My Workspace/Wobbrik/output/ui-concepts/city-views/city-view-concept.png`. Plan PNG referansı: `/Users/mac/My Workspace/Wobbrik/output/ui-concepts/city-views/plan-view-concept.png`.
- İstifadəçi tövsiyələrdən yalnız bu ikisini təsdiqlədi: (a) vahid yuvarlaqlaşdırma/normalizasiya qaydasından sonra yekun geometry üzrə sərhəd təmasına icazə, sahə kəsişməsinə ayrıca güzəşt yoxdur; (b) lokal x/y üçün versiyalı typed Polygon/MultiPolygon formatı. Konkret yuvarlaqlaşdırma addımı, alqoritm, format sxemi və versiya adı seçilməyib.
- Sonrakı cavabda istifadəçi koordinatların sabit dəqiqliklə saxlanmasını da təsdiqlədi. Konkret ölçülər və dəqiqlik addımı açıq qalır; kodlaşdırma təqdimatı, hədlər və bucaq dəqiqliyi seçilməyib.
- Bina və dekorasiya footprint-i alınmış torpaq sahələrinin daxili sərhədlərini keçə bilər; bütün footprint şəhərin ümumi torpaq birləşməsinin daxilində qalmalıdır. Şəhərin xarici sərhədinə toxunmaq olar, kənara çıxmaq olmaz. Konkret ölçülər açıqdır.
- Dekorasiyalara da sərbəst dönmə və sərhədlərin toxunmasına icazə tətbiq edilir. Dekorasiya footprint-i bina və digər dekorasiya footprint-ləri ilə sahə üzrə üst-üstə düşə bilməz. Dekorasiyaların konkret ölçüləri açıqdır.
- Yeni genişlənmə mövcud torpaqla ortaq sərhəd hissəsi paylaşmalıdır; yalnız bir küncdən/nöqtədən toxunmaq kifayət etmir. Ortaq sərhədin konkret minimum uzunluğu seçilməyib; ölçülər açıq saxlanır.
- Genişlənmənin şəhərin hansı tərəfinə əlavə ediləcəyini oyunçu seçir; yeni sahə mövcud torpağa bitişik olmalıdır. Yerlər əvvəlcədən sabit deyil. Konkret ölçülər yenə açıqdır; yalnız küncdən toxunmanın bitişmə sayılıb-sayılmaması hələ dəqiqləşdirilməyib.
- Genişlənmələr ayrıca bitişik torpaq sahələri əlavə edir; onların birləşməsi qeyri-düzbucaqlı şəhər yarada bilər. Konkret ölçülər, konturlar, mövqelər və bitişmənin dəqiq tərifi hələ açıqdır.
- Binalar yerləşdirilərkən sərbəst döndərilə bilər; əvvəlcədən müəyyən edilmiş istiqamətlərlə məhdudlaşmır.
- Footprint sərhədləri bir-birinə toxuna bilər, lakin footprint-lər sahə üzrə üst-üstə düşməməlidir; məcburi ara məsafəsi tələb edilmir. Hesablama dəqiqliyi/tolerans hələ seçilməyib.
- Davam chatində istifadəçi təsdiqlədi: footprint binanın **bütün mərtəbələrinin yuxarıdan x/y proyeksiyalarının birləşməsidir**; əhatə edən düzbucaqlı ilə əvəz edilmir.
- Təsdiqlənmiş ölçü mənbəyi yoxdur; standart bina eni/dərinliyi və digər konkret ölçülər açıq saxlanır, rəqəm seçilmir.
- Geometry-si olmayan köhnə binalar geometry əldə edilənədək inventarda qalır və yerləşdirilmir; avtomatik düz bina footprint-i verilmir.
- Bu chatdə istifadəçi şəhər modelini seçdi: **ikiölçülü yuxarıdan x/y plan; bina və dekorasiyalar sahə tutur**.
- İkinci cavab: **ölçüləri açıq saxla; yalnız onlardan asılı olmayan server hissəsini tamamla**. İlkin rəqəmlər seçmək və placement aktivləşdirmək səlahiyyəti verilməyib.
- Base 8, land əlavələri +4/+5/+6/+7/+8 standart düz bina ekvivalenti, mövcud XP/qiymətlər və land hüququ alış qaydaları qalır. Sərt bina sayı limiti deyil.
- Billing JSON, vahid wallet, wobbrik-v1 və bina content revision semantikası dəyişməyib. City API additive-dir.

## Remaining work
- Koordinat vahidi/precision və hesablama toleransı; standart binanın eni/dərinliyi; bütün mərtəbələrin proyeksiya birləşməsini verən typed geometry; başlanğıc və 5 bitişik genişlənmənin konkret konturu/ölçüsü/mövqeyi və bitişmə qaydası; 12 dekorasiyanın footprint-i açıqdır. Torpaq birləşməsi qeyri-düzbucaqlı ola bilər. Sərbəst dönmə və sərhəd təmasına icazə təsdiqlənib, sahə üzrə üst-üstə düşmə qadağandır.
- Geometry format/version və köhnə binaların etibarlı geometry əldə etmə mexanizmi açıqdır. Legacy yerləşdirmə siyasəti təsdiqlənib: geometry əldə edilənədək inventarda qalır, yerləşdirilmir. Hazır olmayan geometry_version adı və ya default düz bina footprint-i seçilməyib.
- Bu qərarlardan sonra typed geometry, server collision/bounds, atomic place/move/replace, dekorasiya nüsxəsinin yerləşdirmə/return-u və inteqrasiya testləri. Android typed adapter/host və ad active-time/SSV də qalıb.

## Relevant files
- AppBackend/docs/wobbrik-game-api.md — əsas müqavilə; son City membership/read/return və Pending versioned 2D geometry bölmələri.
- AppBackend/internal/wobbrik/{city,market,transaction,attempts,finish}.go
- AppBackend/internal/platform/{wobbrik_http,wobbrik_city_test,deletion}.go
- AppBackend/migrations/010_wobbrik_city_membership.sql; 005_wobbrik_market.sql
- Wobbrik/docs/game-plan.md §7 və son qərar; docs/kotlin-compose-plan.md §5 şəhər vahidi.

## Checks performed
- Son texniki müqavilə əlavəsi yenidən oxunub yoxlandı; yalnız sənəd dəyişikliyi olduğundan test icra edilmədi.
- Backend müqaviləsinin yenilənmiş hissəsi oxunub yoxlandı; AppBackend `git diff --check` icra olunmadı: qovluq Git repository deyil. Bu sənəd dəyişikliyi üçün testlər yenidən icra edilmədi.
- Davam chatində yalnız qərarlar bu qeydə yazıldı; kod/API davranışı dəyişdirilmədi, yeni test icra edilmədi. Aşağıdakı yoxlamalar əvvəlki server vahidinə aiddir.
- Tam real PostgreSQL go test -race -count=1 -json ./... PASS: 77 əsas test, 59 inteqrasiya testi; subtestlərlə 99 PASS, failure/test skip yoxdur, 4 testsiz paket var.
- Yeni 4 test: own/empty city, səhifə/population, 16 parallel exact return/recovery, foreign/stale rejection, receipt failure rollback, aktiv elan/sale uyğunluğu, sale-return yarışı, köhnə retry/new owner izolyasiyası, bigint-dən böyük sakin cəmi, blocked hesab, strict HTTP/no placement route, membership owner constraint və account deletion cascade.
- İlk test JSONB key order səbəbindən byte/struct müqayisədə uğursuz oldu; test qəbzin məzmununu semantik müqayisə edəcək şəkildə düzəldildi. Yekun suite keçib.
- go vet ./... və native cmd/server build PASS. Build=/private/tmp/wobbrik-city-server; log=/private/tmp/wobbrik-city-tests.jsonl. Device/provider/load/deploy yoxlanmayıb.
- Go=/private/tmp/appbackend-toolchain/go/bin/go; GOCACHE=/private/tmp/appbackend-gocache; GOPROXY/GOSUMDB=off.
- Disposable DB 127.0.0.1:55447/isolation_test; klaster=/private/tmp/appbackend-isolation-check/pg; bu işdə başladılıb, yekunda dayandırılıb. 55439-a toxunulmayıb. Backend yazıları escalation ilə aparılıb.

## Exact next step
- Sabit dəqiqlikli koordinat saxlanması, vahid yuvarlaqlaşdırma qaydası (normallaşdırılmış formada kəsişmə güzəşti yoxdur) və versiyalı Polygon/MultiPolygon istiqaməti təsdiqlənib; yenidən soruşma. Konkret ölçülər, dəqiqlik addımı, bucaq dəqiqliyi, kodlaşdırma təqdimatı/hədləri, sxem/alqoritm/versiya adı seçilməyib. Aşağıdakı əvvəlki açıq məsələləri bu təsdiqlərlə birlikdə oxu.
- Footprint mənbəyi, legacy siyasəti, bina/dekorasiya üçün sərbəst dönmə, sərhəd təması və qarşılıqlı sahə kəsişməsinin qadağası, həmçinin oyunçunun seçdiyi tərəfə ortaq sərhəd hissəsi ilə bitişən genişlənmə qərarlarını yenidən soruşma; tək künc təması kifayət deyil. Footprint daxili torpaq sərhədlərini keçə və xarici sərhədə toxuna bilər, lakin ümumi torpaq birləşməsindən kənara çıxa bilməz. Açıq qalanlar: koordinat vahidi/precision, hesablama toleransı, typed geometry formatı və mənbəyi, torpaq/dekorasiya konturları və konkret ölçülər. Ortaq sərhəd üçün ayrıca minimum uzunluq seçilməyib. Backend müqaviləsinə bu qərarlar köçürülüb; placement bağlıdır. Ölçülər üçün təsdiqlənmiş mənbə əldə edilmədən rəqəm seçmə.
- Bu faylı və AppBackend AGENTS/README/current-task qeydini oxu, əsas API-nin son şəhər bölməsini aç. İstifadəçinin cari scope-u geometry-dən asılı olmayan server işi idi və bu bitib. Növbəti placement üçün əvvəl açıq 2D footprint/ölçü və legacy geometry qərarlarını dəqiqləşdir; rəqəmlər uydurma və hazır olmayan placement-i aktivləşdirmə.
- Oyun/SKU/Console/deploy-a keçmə. Backend workspace xaricindədir; yazılar escalation tələb edir. Yeni böyük vahidə yeni chatdə başlamaq tövsiyə olunur; istifadəçi burada davam seçərsə qərarına əməl et.
