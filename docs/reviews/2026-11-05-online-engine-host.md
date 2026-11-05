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

## Callback identity follow-up
- İlk implementation commit: 7cacd71; main CI keçdi (1m54s, 106 tasks).
- Branch: fix/online-host-callback-identity.
- Əlavə review tapıntısı: frame/drop da çağıranın exact Ready-sini almalıdır;
  yeni hesab resume edildikdən sonra köhnə UI callback-i təsir etməməlidir.
- Düzəliş: resume/frame/drop null/equal-copy/stale Ready-ni rədd edir.
  Regression test stale frame-in clock/scene-i dəyişmədiyini yoxlayır.
- git diff --check keçdi; branch-də CI işə salınmayıb.

## Final runtime validation
- Reviewed fix commit: 74ef9f6; main merge: a27dac7.
- Main üzərində ./scripts/local-ci.sh: BUILD SUCCESSFUL, 25s,
  106 actionable tasks (16 executed, 90 up-to-date).
- Build/assemble, Android lint, core və engine testləri keçdi.
- Əvvəldən mövcud LocalLifecycleOwner deprecation warning qalır.
- Əlaqəsiz untracked fayllar dəyişdirilməyib. Push/deploy edilməyib.
- Bu əlavə yalnız nəticə/handoff sənədləşməsidir; diff review və
  git diff --check keçdi. Merge-dən sonra main CI yenidən yoxlanılır.
