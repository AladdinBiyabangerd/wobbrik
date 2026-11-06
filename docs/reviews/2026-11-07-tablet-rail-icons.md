# Lokal review — feat/tablet-rail-icons

- Baseline: `0c54b79` (main); scope: dörd yeni PNG, asset QA script/render və prompt sənədi. Lokal self-review; subagent yoxdur.
- Diff: yeni `ui_home`, `ui_city`, `ui_inventory`, `ui_market`; mövcud asset və runtime kodu dəyişmir. Adlar Android drawable qaydalarına uyğundur.
- Tapıntı/düzəliş: ilkin home fon dumanı transparency edit ilə əvəz edildi; seçilmiş PNG-lərin RGBA alpha həddi 0–255 təsdiqləndi.
- İstinad: tablet-v1/primary-landscape.png; ev, skyline, çanta və çəhrayı/ağ tentli mağaza motivləri qorunub. Generativ kontur/bevel fərqləri var, piksel eyniliyi deyil. Yeni alternativ dizayn əlavə edilməyib.
- `render-preview.py`: 390×844 və 1280×800, 24/32/48/96px; iki render açılıb baxıldı, dörd ikon seçilir. Bu asset yoxlamasıdır; Compose ekranı və real rail uyğunluğu deyil. Token palitrası dəyişmədiyinə görə kontrast yenidən hesablanmadı.
- Backend: API/DTO/schema, progression/wallet dəyişmədiyinə görə AppBackend dəyişikliyi tələb olunmur. Backend yoxlaması iddia edilmir.
- Branch-də CI işə salınmayıb. Main merge-dən sonra lokal CI nəticəsi aşağıda qeyd ediləcək.
- Qalan iş: rail runtime inteqrasiyası və telefon/planşet ekran müqayisəsi; əlavə istifadəçi tələbləri (rekord, növbəti XP həddi, inventory sayı, Parametrlər, Şəhərim) bu asset diff-inə daxil deyil.
