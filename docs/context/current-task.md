# Cari tapşırıq — İnventar atlas gecikməsi / 17 noyabr 2026

## Completed

- Əvvəlki City batching Android85307bf/AppBackend23102b4 (backend remote main838c0c7), HTTPS scoped cancellation02fbf5a və inventory detail4fbd615 saxlanır; bu vahiddə təkrarlanmayıb.
- Root İnventar BuildingPreview ölçüldü: hər kartda dörd purple PNG main-thread decode84–90ms wall/84–88msCPU,25 161 744 bayt. Dörd kart+remount32fərqli bitmap/201 293 952bayt müşahidə edildi (peak heap deyil).
- Purple art mövcud ApprovedArt IO hazırlığı/process immutable APK-art keşinə qoşuldu. Kart/remount eyni4bitmap paylaşır; helper<1ms wall/CPU. Eyni host fixture ilk kart905→556ms; hər ikisi3HTTP/2170responseUTF8bayt/1city. Bunlar route-after-preparation tək host nümunələridir, device/production/cold-start deyil.

## Current state

Android `fix/inventory-detail-latency`; atlas vahidinin bazası4fbd615. Production dəyişiklik yalnız ApprovedArt.kt/PurpleBuildingArt.kt. Əvvəlki detail scheduling commit-dədir. CI/main merge/push/deploy yoxdur. AppBackend dəyişməyib; app-wide performans/redesign bitməyib.

## Decisions

- Yeganə plan docs/PLAN.md§1.1/§2–3; PNG/token/layout/server authority/geometry/account/cache/Ready/sole journal dəyişmir. CI yalnız ayrıca tələb ilə main-də lokal; emulator avtomatik açılmır.
- Purple4bitmap~24MiB bir dəfə IO-da hazırlanır və process ömrü boyu saxlanır. Bütün ApprovedArt qrupları~84MiB (əvvəl~60); dörd kartın purple retention-u~96→24MiB. Cold startup-a əlavə decode xərcinin təsiri ayrıca ölçülməyib. Production root art gate hazırlığı gözləyir; preparesiz birbaşa composable sinxron fallback saxlayır.

## Checks performed

- Normal core/appunit+assembleDebug+lintDebugPASS40s; diffcheckPASS. CI deyil.
- Native atlas suite5/5PASS10s; concurrent preparation1/1PASS; səkkiz IO çağırışı eyni map/dörd bitmap qaytarır. Son source ilə əvvəlki InventoryDetailLatencyTest7/7PASS19s.
- Root4nativePNG açıldı:320×568font1/2,360×640font1,390×844font2synthetic inset. 01-main.png ilə müqayisə;360font1əvvəl/sonraPNG SHA256 eynidir. Kontrast Text13.00/Muted8.22/Accent8.99PASS.
- Yalnız bundled art loader dəyişir; API/DTO/domain/schema/backend dəyişikliyi tələb etmir. AppBackend dəyişməyib/test işlədilməyib; server performansı iddiası yoxdur.

## Remaining work

- Bildirilən root İnventar10+s və menyular2+s device/production-da ölçülməyib və həll edilmiş sayılmır. Cold startup/memory büdcəsi, BuildingPreview digər resource işi və Profile foreground/skin CPU açıqdır.
- Font2/Long.MAX header clipping, kiçik portret dock, scroll, illustration miqyası və tam parity borcları qalır. Real OS inset/IME/locale/TalkBack/GPU yoxlanmayıb; fixture padding real inset deyil.
- Main merge/push yoxdur; HTTPS/libcore/DNS/connect/handshake/upload cancellation və authenticated Android→GoE2E ölçülməyib.

## Relevant files

- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{ApprovedArt,PurpleBuildingArt,InventoryScreen,ResourceArtLoading,ProfilePresentation,GameTokens}.kt.
- docs/reviews/2026-11-17-inventory-atlas-latency.md; inventory-atlas-latency/{render,rendered,evidence}/.
- Əvvəlki docs/reviews/2026-11-17-inventory-detail-latency.md və onun fixture/evidence-i.

## Exact next step

Bu handoff və PLAN§1.1-i oxu; ayrıca növbəti ölçmə vahidi kimi qalan route/cold-start gecikməsini araşdır. Cihaz ölçüsü varsa first-content/main-threadCPU/memory-ni ayır; yoxdursa host fixture ilə Profile/resource işi və hazırlıq xərcini ayrıca ölç, device10+s həll edildi demə. Purple duplicate decode/detail scheduling/City batching/HTTPS işini təkrarlama. Yeni dizayn/stack/cache authority uydurma; CI/merge/push/deploy avtomatik yoxdur.
