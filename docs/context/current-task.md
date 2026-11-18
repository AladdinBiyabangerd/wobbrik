# Cari tapşırıq — bina detalı native / 18 noyabr 2026

- Completed: istifadəçinin “okey implement” təsdiqi ilə yeni native bina detalı və app üzrə vahid CollectionNavbar tətbiq edildi. Köhnə mobil detail/navbar renderer çıxarıldı; real DTO və satış/silmə icazələri saxlandı. Android Studio mockDebug emailsiz yeniləndi, debugger qoşuldu, inventardan detail açılıb görüldü.
- Current state: feat/inventory-art-led-redesign, baza c2600f1; bu review ilə saxlanan feature dəyişiklikləri. Owner iki release .dm və əvvəlki city-detail review toxunulmadan qalır. Main/CI/push yoxdur.
- Decisions: plan docs/PLAN.md§1.1; detail authority docs/design/building-detail-v1/proposal.png + təsdiqli CollectionNavbar. Digər səhifələrin məzmun redesign-ı bu vahidə daxil deyil.
- Remaining work: istifadəçi native nəticəni review etsin. Mock şəhər statusu mövcud deyil; real hesab/backend və production performans yoxlanmayıb. Hero dam/baca kənarı proposal-dan fərqlidir; pixel-perfect iddia edilmir. Sonrakı səhifə ayrıca tapşırıqdır.
- Relevant files: OwnedBuildingDetailPresentation.kt, InventoryScreen.kt, InventoryCollectionChrome.kt, MobileMenuNavbar.kt, GameTokens.kt; docs/reviews/2026-11-18-building-detail-native.md; docs/reviews/building-detail-native/evidence/device-mock-detail.png.
- Checks performed: final native9/9; debug41/41 və mock44/44 unit; hər iki APK build və lint PASS. Font1/2, kiçik/normal/inset native renderlər açıldı; contrast6 cüt PASS. Backend API/domain dəyişmir. CI işlədilməyib.
- Exact next step: istifadəçi cihaz görüntüsünü qiymətləndirsin. Düzəliş istəsə həmin scoped UI faylları ilə davam et; yeni səhifəyə və main merge-ə ayrıca göstərişsiz keçmə.
