# Cari tapşırıq — İnventar local-first / 17 noyabr 2026

## Completed

- Əvvəlki City batching85307bf/backend23102b4(remote838c0c7), HTTPS cancellation02fbf5a, detail4fbd615, paylaşılan purple atlas253f569 saxlanır; təkrarlanmayıb.
- İnventar root art gate-dən çıxarıldı. Yalnız illustration Loading/Failed göstərir; qəbul edilmiş cached metadata/network oxuları və naviqasiya gözləmir. Root eyni art status/retry-ni inventory scope-a ötürür.
- Qəbul edilmiş read-only hesab varsa InitialAccountSync avtomatik menyunu açır; yeni login epoch köhnə latch-i daşımır.
- Garden sinxron decode67ms/6MiB ölçüldü: purple-də istifadə olunmayan çağırış çıxarıldı, digər template mövcud resource helper ilə IO-da yüklənir. Eyni asset/crop/renderer saxlanır.

## Current state

Android fix/inventory-detail-latency; bu vahidin bazası253f569. Production4fayl: ApprovedArtLoading, InitialAccountSync, InventoryScreen, OnlineGame.kt. Lokal review/evidence hazırdır. CI/main merge/push/deploy yoxdur. AppBackend dəyişməyib. App-wide performans/redesign bitməyib.

## Decisions

- Son istifadəçi: lokal İnventar şəbəkə/qrafika gözləməsin; lokal/server fərqli oyun görünüşü yaranmasın. Eyni accepted DTO/geometry/render yolu saxlanır, fake lokal bina/wallet/capability yoxdur. Keş UID/login/revision üzrədir; stale revision instant görünüş üçün göstərilmir. Fresh cavabadək mutasiya disabled-dir.
- Yeganə plan docs/PLAN.md§1.1/§2–3. CI yalnız ayrıca tələb ilə main-də lokal. Emulator açılmır; runtime/render yoxlanmayıbsa PASS deyil.
- ApprovedArt~84MiB process atlas büdcəsi saxlanır; cold startup yaddaş/decode xərci ayrıca açıqdır. Digər route-ların art gate-i bu vahiddə dəyişməyib.

## Checks performed

- Native local-first7/7PASS13s: blocked art/network cached list, art failure/retry state, fresh nəticə,0HTTP remount, cached detail mutasiya gate, yeni login epoch;320/360/390portretfont1/2/syntheticpadding və teal template.
- Injected1200ms art gate ilə baza first-card1384ms; son source artLoading/networkclosed ikən ilk host375ms, sonrakı matrix36–57ms. Warm remount13–70ms/0HTTP. Bunlar host nümunələridir; device/realdecode/percentile deyil. İlk host375ms “sürət həll edildi” kimi qəbul edilmir.
- core152/app37unit+assembleDebug+lintDebugPASS40s; əvvəlki detail native7/7PASS16s; diffcheckPASS. CI deyil.
-7saxlanmış PNG açıldı, loading320font2kiçildilmiş surəti də yoxlandı;01-main.png ilə müqayisə. Art/metadata ready vəziyyətində eyni yol; city seal yalnız membership gələndə açılır. Kontrast13.00/8.22/8.99PASS.
- Backend API docs inventory owner/revision/epoch sərhədi yoxlandı; API/domain/schema dəyişmir, backend dəyişikliyi tələb olunmur. AppBackend testləri bu vahiddə işlədilməyib.

## Remaining work

- Device/production İnventar10+s və digər menyu2+s ölçülməyib. Yeni diff-in cihazda fiziki frame latency-si, cold startup/HTTP/payload/memory ayrıca açıqdır. Host ilk375ms daxilində class/JIT/grid işi ayrılmayıb.
- Profile foreground/skin CPU və digər route art gate-ləri növbəti ayrıca vahidlərdir; bu iş onları həll etmir.
- Kiçik portret dock/font2tabs/illustration miqyası, Long.MAX header, tam PNG parity və real inset/IME/locale/TalkBack/GPU borcları qalır. Synthetic padding real OS inset deyil.
- Main merge/push yoxdur; authenticated Android→GoE2E və əvvəlki HTTPS platform cancellation ölçülməyib.

## Relevant files

- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{OnlineGame,ApprovedArtLoading,InitialAccountSync,InventoryScreen,AccountViews,ResourceArtLoading,GameTokens}.kt.
- docs/reviews/2026-11-17-inventory-local-first.md və inventory-local-first/{render,rendered,evidence}/.
- Əvvəlki docs/reviews/2026-11-17-inventory-atlas-latency.md;2026-11-17-inventory-detail-latency.md.

## Exact next step

Bu handoff və PLAN§1.1-i oxu. İnventarın qalan real cihaz/production first-content/frame gecikməsini ayrıca ölç; yoxdursa host ilk375ms class/JIT/grid işini real device kimi təqdim etmədən ayır. Və ya istifadəçinin növbəti konkret menyu tapşırığını götür. Cached-local və art/root gate düzəlişini təkrarlama. Eyni accepted server DTO/renderer/authority sərhədlərini saxla; CI/merge/push/deploy avtomatik yoxdur.
