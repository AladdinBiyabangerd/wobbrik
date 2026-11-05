# Account bootstrap və ekran asset hazırlığı — 5 noyabr 2026

- Branch: `feat/stage-three-account-screens` (yalnız hazırlıq hissəsi).
- Yoxlanmış diff: WobbrikApplication account bootstrap/link sərhədi və
  `docs/design/account-assets-v1/`. Ekran implementasiyası daxil deyil.
- Firebase anonymous login yalnız currentUser olmadıqda çağırılır. Mövcud
  sessiya saxlanır; təkrar bootstrap işi coalesce edilir. Auth Error ayrıca
  StateFlow-dadır, backend Ready ilə eyniləşdirilmir.
- Guest link `linkWithCredential` istifadə edir, UID dəyişməsi rədd edilir;
  credential collision başqa hesaba sign-in və wallet birləşməsi yaratmır.
- Review düzəlişi: auth listener signed-out vəziyyətində köhnə Session
  göstəricisini saxlamır. Link yalnız configured bootstrap-da açıqdır.
- AppBackend gameplay API anonymous UID qəbul edir. Recovery/command API,
  DTO, revision/dedup, qayda versiyası, ledger/schema dəyişmir; backend
  dəyişikliyi lazım deyil. Hesab izolyasiyası mövcud recovery owner-də qalır.
- İstifadəçi çatışmayan qrafikanın dizayna uyğun ayrıca hazırlanmasını seçdi;
  sadələşdirilmiş ekran alternativinə razılıq verilməyib.
- Built-in imagegen ilə şəhər, logo və hesab hero asset-ləri hazırlandı.
  AppKit alpha-preview renderinə GameTokens.Background üzərində baxıldı:
  kənarlar təmizdir, hesab hero önizləməsindəki halo compositing-də görünmür.
  Generativ extraction piksel eyniliyi təmin etmir; layout müqayisəsi hələ yoxdur.
- MainActivity hələ offline PracticeGame göstərir. Anonymous girişin əlavə
  edilməsi oyunu online hazır etmir. Start/resume/progression ekranları qalıb.
- Branch-də CI işlədilməyib. `git diff --check` keçib. Main CI nəticəsi aşağıda.
- Real Firebase/backend/device auth və credential collision sınaqları aparılmayıb.
  Console/SKU/deploy/push yoxdur. Ekran dəyişmədiyindən yeni UI renderi yoxdur.

Auth API istinadı: [Firebase Android anonymous auth](https://firebase.google.com/docs/auth/android/anonymous-auth)
(`signInAnonymously`, guest üçün `linkWithCredential`). Console-da provider
aktivləşdirilməyib; runtime xəta ayrıca AccountState.Error kimi saxlanır.
