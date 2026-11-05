# Online engine host — 5 noyabr 2026

- Branch: feat/stage-three-online-host.
- Review: branch diff (host, recovery owner, application wiring, command tests).
- Host foreground + exact Ready ilə explicit resume edir, ilk settled tick-də
  dayanır və yalnız öz physics proposal-ını mövcud commands-a ötürür.
- Recovery callback yalnız invalidation/restore edir; dispatch host monitorundan
  kənardadır (owner/host lock inversion yoxdur).
- Review düzəlişi: Ready nəşrindən əvvəl bitmiş recovery işi sərbəst buraxılır;
  terminal observer köhnə işi aktiv sayıb finish-i itirməməlidir.
- Terminal FINISHED checkpoint serverdə davamlı finish niyyətidir. Yeni proses
  həmin checkpoint-dən free base_6 parts ilə finish edir; hissə seçimi bu vahiddə
  yoxdur. Zero-floor miss birbaşa davamlı finish command-a keçir.
- Backend API müqaviləsi yenidən yoxlandı: sequence/revision, immutable floors,
  exact retry və sync uyğun qalır. API/schema/backend dəyişikliyi lazım deyil.
- Branch-də CI işlədilməyib. git diff --check keçib.
- Main CI: merge-dən sonra nəticə aşağıda qeyd ediləcək.
- Məhdudiyyətlər: fake contract testləri real PostgreSQL/device sübutu deyil.
  UI/auth/Room və continue qərarı bu vahidə daxil deyil; mövcud continued
  attempt fail-closed qalır. Vizual dəyişiklik yoxdur.
