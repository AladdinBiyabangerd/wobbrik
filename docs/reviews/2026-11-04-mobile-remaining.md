# Qalan mobil PNG-lərin lokal review-u

- Branch: feat/mobile-remaining-png. Scope: üç yeni dörd-ekranlı konsept, iki düzəliş, promptlar və əlaqəli sənədlər. Əvvəldən olan responsive rule/review faylları scope-a daxil deyil.
- Mənbə: mobile-primary-v2.png və mövcud tokens.css; ui-craft və built-in imagegen. Tokenlər dəyişməyib.
- Tapıntılar: inventar/bazarda minimum 10-dan aşağı saylar; nəticədə yanlış adi mükafat və icazəsiz enerji göstəricisi. Hədəfli redaktələr istənib; yekun vizual yoxlama aşağıda qeydə alınır.
- Kontrast: altı cüt keçdi, nisbətlər 8.22–16.57. PNG-nin bütün piksellərinin avtomatik kontrast auditi deyil.
- Backend: domain/API/DTO/schema və ledger dəyişmir; backend dəyişikliyi lazım deyil. Göstərilən qiymətlər/kataloq/nailiyyət bonusları illüstrativdir, qayda yeniliyi deyil.
- Məhdudiyyətlər remaining-screens.md-dədir: geometry/modul sayı illüstrativ; responsivlik və runtime təsdiqi yoxdur, planşet/landşaft render edilməyib, bütün əməliyyat axınları əhatə olunmur.
- İlkin lokal CI: sandbox Gradle lock girişinə görə dayandı; icazəli təkrar BUILD SUCCESSFUL, 43 task (42 up-to-date). Son fayl dəsti və merge üçün təkrar tələb olunur.
- Yekun vizual baxış: inventory-market-v2 və account-states 1706×922; play-progress-v2 1774×887. Hər lövhədə dörd telefon portret konsepti; minimum mərtəbə mətnləri, enerji göstəricisinin silinməsi və +12/+12 nəticə mükafatı təsdiqləndi. Kəsilmiş əsas düymə görünmədi. İlkin variantlar arxivdir.
- Son fayl dəsti üçün lokal CI: BUILD SUCCESSFUL (43 task, 42 up-to-date). Lokal diff review tamamlandı; açıq məhdudiyyətlər sənədləşdirildi.
