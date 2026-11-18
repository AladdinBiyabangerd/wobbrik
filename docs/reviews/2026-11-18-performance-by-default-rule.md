# Yeni səhifələr üçün performans qaydası / 18 noyabr 2026

Branch `docs/performance-by-default`, baza `083eedb`; docs-only diff: AGENTS.md, `.cursor/rules/performance.mdc`, cari handoff və bu review. İstifadəçi yeni səhifələrin əvvəlcədən performanslı hazırlanmasını tapşırıb; qayda bütün yeni/dəyişən ekran/tab/detail və həmin axının backend oxularına şamil olunur.

Local-first display/fresh mutation ayrılığı, bounded page/detail sorğusu, N+1 və full-collection scan-dən yayınma, lifecycle/cancellation, main-thread xərcinin çıxarılması, DTO/cache ölçü büdcəsi və böyük-hesab regression/ölçmə qaydaları əlavə edildi. Mövcud helper-lər və yeganə PLAN saxlanır; yeni framework, stack pin, sübutsuz latency hədəfi və canlı yoxlama/CI gate-i yaradılmır.

Lokal review: AGENTS bölməsi ilə always-on güzgü mətninin tam eyniliyi, frontmatter, mövcud authority/revision/fresh gate/emulator/CI qaydaları ilə uyğunluq və diffcheck PASS. Runtime mənbə dəyişmədiyindən build/test/render işlədilmədi. Bu qayda gələcək implementasiyaların ölçülmüş performans zəmanəti deyil; hər axının öz sübutu tələb edilir. Owner release binariləri və əvvəlki city-detail review əlavəsi scope xaricində qorunur. Main merge/push/deploy/CI yoxdur.
