# Dam və mərtəbə ölçüsü düzəlişi

- Branch: fix/mobile-floor-roof; yoxlanan diff: game-plan.md, mobile-v1 README/prompt/v2 PNG, current-task və bu qeyd.
- Tapıntı: əvvəlki aktiv oyun təsvirində texniki dam elementi və fərqli mərtəbə ölçüləri vardı. Yeni renderdə krandakı hissə və bina vahid düzbucaqlı modul kimi göstərilib, dam avadanlığı silinib. Görüntüyə baxılıb; şəhər panellərində bitmiş damlar saxlanıb.
- Məhsul qaydası: aktiv oyunda dam verilmir; uduzanda avtomatik tamamlanır. Modul eni/dərinliyi/hündürlüyü və çəkisi eynidir. Dam ayrıca mərtəbə və mərtəbə mükafatı deyil.
- Tokenlər dəyişməyib. Bu yalnız PNG/sənəd düzəlişidir; generativ perspektiv dəqiq geometry ölçmə sübutu deyil. Telefon portret konseptinə baxılıb; planşet/landşaft/runtime yoxlanmayıb.
- Backend: işlək domain, API, DTO, schema, wallet və reward müqaviləsi dəyişmir; bu vizual düzəliş backend dəyişməsi tələb etmir. Gələcək engine/finish implementasiyası serverlə ayrıca yoxlanmalıdır.
- Lokal diff review: səhv dam elementi aradan qaldırılıb; köhnə PNG tarixçə üçün saxlanıb, README cari v2-yə yönəldilib. Lokal CI nəticəsi commit-dən əvvəl yoxlanır; merge nəticəsi də ayrıca işlədilir.
- İlkin lokal CI: BUILD SUCCESSFUL (43 task, 42 up-to-date). Son fayl dəsti üçün təkrar və merge CI aşağıdakı çat icra nəticələri ilə təsdiqlənir; uğursuzluq olarsa merge dayandırılır.
