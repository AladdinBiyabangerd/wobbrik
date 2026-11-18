# Cari tapşırıq — İnventar review və commit / 18 noyabr 2026

## Completed

- Profil optimizasiyası ayrıca `db6e70d` commit-indədir: session/epoch-scoped cached identity, fresh edit gate və paralel/paylaşılan mobile portrait IO. Əvvəlki host təkrar profil mətn ölçüsü451–475→24–38ms; cold sürətlənmə sübut olunmayıb.
- Qalan İnventar/cache/chrome diff-i lokal review edildi; əlavə correctness problemi/düzəliş ehtiyacı tapılmadı. Unrelated disk-write kilidindən azad replay,1/2sütun lazy rows və shell-owned async logo birlikdə ayrıca İnventar commit-i ilə saxlanır.
- Əvvəlki ölçmələr ayrıca review-larda qorunur: controlled disk gözləməsi621→1ms; real-Home host ilk inventory191→160ms. Device/percentile deyil, bütün app performansının bitməsi deyil.

## Current state

`fix/inventory-review`, parent `db6e70d`. Profil və İnventar ayrı commit-lərdədir. İki release baseline-profile binarisi ayrıca dirty saxlanır, bu işdə dəyişdirilməyib və commit-ə daxil deyil. CI/main merge/push/deploy yoxdur. AppBackend dəyişməyib.

## Decisions

- Yeganə plan docs/PLAN.md§1.1/§2–3; yeni stack/dizayn/prewarm yoxdur.
- Cache UID/view üzrə file lock saxlayır; eyni fayl read/write serialdır. Cached məlumat Ready/mutation capability-si deyil; fresh/revision/epoch/sole journal sərhədləri dəyişmir.
- Profil memory-si login epoch-a bağlı yalnız server identity display-idir. Hər giriş fon GET edir; load/error halında redaktə fresh cavabadək disabled-dır.
- CI yalnız ayrıca tələb ilə main-də lokal; emulator avtomatik açılmır. Merge ayrıca istifadəçi təsdiqinə bağlıdır.

## Remaining work

- İki release baseline-profile binarisinin ayrıca owner dəyişiklikləri qalır.
- Digər menyuların konkret call path-ləri, production2+s/İnventar10+s ölçüləri, ART/frame/GPU/HTTP/payload/yaddaş/battery və authenticated Android→GoE2E açıqdır.
- Profil ilk GET/~35ms composition və route remount portrait IO xərci qalır. İnventarda host class-load/ilk preparation və warm sürətlənmə məhdudiyyətləri əvvəlki review-da göstərilir.
- Font2kiçik illustration/horizontal tabs, iki sətir dock, tam PNG parity və real inset/IME/locale/TalkBack borcları saxlanır.

## Relevant files

- docs/reviews/2026-11-18-inventory-final-review.md — cari scope, tapıntılar və yenilənmiş checks.
- docs/reviews/2026-11-18-inventory-{first-open,composition-profile}.md; inventory-{first-open,composition-profile}/{render,evidence,rendered}/.
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{InventoryScreen,MobileHomeScreen,MobileMenuShell}.kt; core/src/{main,test}/kotlin/appbackend/wobbrik/game/ReadOnlyGameCache{,Test}.kt.
- docs/reviews/2026-11-18-profile-open-latency.md; Profil production/QA faylları həmin review-dadır.

## Checks performed

- Yenilənmiş cache6/6PASS1s və native inventory7+rows4=11/11PASS13s. Yeni production dəyişməsi yoxdur; eyni source-da bu chatdəki core154/app37unit+assembleDebug+lintDebugPASS51s nəticəsi reuse edilir. CI deyil.
- DiffcheckPASS; native XML və sourceSHA inventory-composition-profile/evidence/review-final/ altındadır.
-4yenilənmiş native PNG açılıb01-main.pngilə müqayisə edilib:320font1loading/320font2ready/360font1ready/390font2ready+syntheticinset. Kontrast13.00/8.22/8.99PASS. Tam PNG/device parity deyil.
- Backend müqaviləsi yoxlanıb; API/domain/schema dəyişikliyi tələb olunmur. Backend test/E2E/emulator yoxdur.

## Exact next step

Cari lokal review və commit tapşırığı tamamlanıb. Sonrakı böyük işi yeni chatdə bu handoff və docs/PLAN.md§1.1 ilə başlat. İstifadəçi konkret növbəti menyu/call path seçəndə yalnız həmin axını ölç; repo-nu yenidən bütöv scan etmə. İki baseline-profile owner binary-sinə toxunma. CI/merge/push/deploy avtomatik yoxdur.
