# İnventar təqdimatı — optional disk gözləməsinin çıxarılması

Branch: `fix/inventory-payload-latency`; review bazası `3c7c19d`, bu vahidin source/test diff-i. Əvvəlki payload düzəlişi və owner baseline-profile binariləri dəyişmir.

`capture` accepted revision bracket-dən keçmiş tam oxunu optional persistence-dən əvvəl bildirir. `AccountViews` mövcud display-only callback-i çağıranın dispatcher-ində işlədir; fresh hüququ və completed-cache reuse yalnız əvvəlki completion nöqtəsində açılır. Disk yazısı əvvəlki kimi gözlənilir və durable qalır, amma məlumatın təqdimatını saxlamır. Fire-and-forget writer və yeni cache layer yoxdur.

Lokal review: callback disk xətalarını udan blokun xaricindədir; cancellation callback-dən sonra disk yazısına keçmir. Session yoxlamaları callback-in əvvəlində/sonunda saxlanır. Final revision uyğunsuzluğu təqdimat və persistence-dən əvvəl rədd edilir. Native layout/art/token dəyişmir; vizual nəticə/render iddia edilmir.

Yoxlamalar: əvvəlki source-da disk-barrier regression gözlənildiyi kimi FAIL, düzəlişdə PASS. Caller dispatcher, diskdən əvvəl accepted təqdimat, final revision mismatch və callback cancellation yoxlanır. Core və bütün app unit suite, assembleDebug/lintDebug PASS. Lokal Kotlin repository/cache işləyən Go HTTP middleware və real disposable PostgreSQL ilə yoxlanıb; synthetic account/token verifier və idarə olunan disk stall istifadə olunur. Android cihazı, Firebase verifier, TLS/production workload və first-frame ölçüsü deyil.

Backend uyğunluğu yoxlanıb: API/DTO/schema/domain dəyişməsinə ehtiyac yoxdur. Müvəqqəti Go test overlay-i backend repository-ni dəyişmir. Main merge, remote push/deploy və CI yoxdur. İstifadəçinin son göstərişinə uyğun ayrıca ölçmə hesabatı yaradılmır.
