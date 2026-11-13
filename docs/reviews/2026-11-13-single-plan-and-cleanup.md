# Vahid plan və repository təmizliyi — 13 noyabr 2026

Branch: `docs/single-current-plan`; base `0111ca1`. İstifadəçi hər iki Google auth main merge/push-u, Wobbrik main-dən başqa branch/worktree silinməsini və tək aktual plan saxlanmasını açıq tapşırıb.

## Dəyişiklik və review

- `docs/PLAN.md` tək aktual plan oldu: mərhələlər/cari vəziyyət, authority, lifecycle/retry, məhsul, iqtisadiyyat, bütün 68 hissə/12 dekor kataloqu, dəqiq geometry/provenance/parcel/placement, qalan qərarlar bir sənəddədir. Dörd köhnə plan silindi; git tarixçəsi və verified backup saxlanır.
- Köhnə guest/link, merge-gözləyir, Firestore wallet/client purchase authority, avtomatik resume və ilkin kod-icazəsi statusları cari qaydalarla əvəz edildi. Şəhər API-nin main-də olması mobil stage-four implementasiyası kimi göstərilmir. Google main merge backend deploy demək deyil.
- AGENTS və uyğun Cursor güzgüləri vahid planın bölmələrinə yönləndirildi; handoff qısaldıldı. Tarixi review-dakı yoxlanmış-diff fayl adları tarixçə kimi saxlanır, aktiv plan deyil. Cari mənbə keçidləri yeniləndi.
- Məhsul/qiymət/qrafika/UI/kod/API dəyişməyib. Növbəti stage-four UI təsdiqi hələ açıqdır; canlı yoxlama tələb edilmir. CI yalnız ayrıca istifadəçi tələbi ilə.

## Yoxlamalar

- `git diff --check` PASS. Tək planın mövcudluğu, §1–9 ardıcıllığı, lokal Markdown link hədəfləri, aktiv qaydalarda silinən planlara keçid qalmaması PASS. Köhnə kataloqdakı bütün adlı siyahı sətirləri yeni planda eynilə saxlanıb; əsas iqtisadiyyat/geometry/vaxt invariantları tutuşduruldu. İlk smoke assertion sənəddə olmayan ümumi `max` sözünü axtarırdı; real `ceil(0,05` komissiya ifadəsi ilə düzəldilmiş yoxlama PASS.
- Bu docs-only vahid üçün build/test/CI təkrarlanmadı. Əvvəlki main Android və backend vet/race/disposable DB/build nəticələri auth review-larda saxlanır. Runtime/render yoxlaması aparılmayıb və PASS iddiası yoxdur. Emulator/cihaz istifadə edilməyib.

## Backup və təmizləmə

- Xarici özəl backup: `/Users/mac/My Workspace/Wobbrik-backup-20261113-141911`. `all-refs.bundle` və `before-cleanup.bundle` verify PASS; hər worktree üçün status/patch və dirty/untracked fayl tar-ları yoxlandı. Original untracked fayllar ayrıca original-untracked altında qorunur. Main-ə daxil olmayan commit-lər də bundle-dadır.
- Əlavə Wobbrik worktree-lər və köhnə lokal branch-lər silindi, əsas `/Users/mac/My Workspace/Wobbrik` main-ə qaytarıldı. Yalnız bu sənəd vahidinin müvəqqəti branch-i main merge/push-dan sonra silinir. Üç remote non-main branch də silinir; final ref/worktree siyahısı ayrıca yoxlanır.
- AppBackend Google auth 7d52fe4 merge, yoxlama qeydi ilə final main 6fc1214 remote-a push edilib. İstifadəçi ayrıca istədiyi üçün yalnız bu iş üçün yaradılmış Google backend worktree silindi; digər backend worktree-ləri saxlanır.
