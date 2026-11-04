# Mobil PNG konseptlərinin lokal review-u

- Branch: feat/mobile-design-png. Diff: docs/design/mobile-v1/* və bu review qeydi (ilk commit-in məzmunu).
- Mənbələr: təsdiqlənmiş şəhər PNG-ləri; ui-craft və imagegen workflow. Yeni tokenlər istifadəçinin açıq seçimi ilə yaradıldı.
- Tapıntı/düzəliş: ilk PNG-də Burax yazısı səhv idi; imagegen redaktəsi ilə düzəldildi, nəticəyə baxıldı.
- Açıq məhdudiyyətlər: ana ekranda şəhərə giriş çatışmır; PNG geometry eyniliyini, responsivliyi və minimum tap ölçüsünü sübut etmir. Planşet, landşaft və işlək Android UI yoxdur. Bunlar mobile-v1/README.md-də açıqdır; konsept istehsal UI kimi təqdim edilmir.
- Backend: yalnız sənəd/konsept; wallet, progression, city domain və API dəyişməyib, backend dəyişikliyi lazım deyil.
- Kontrast: altı semantik cüt 4.5:1 həddini keçdi. Şəkildəki faktiki bütün piksellər üçün iddia yoxdur.
- Lokal CI: ilk sandbox cəhdi Gradle lock çıxışına görə dayanıb; icazəli təkrar BUILD SUCCESSFUL (43 task, 42 up-to-date). Son sənəd dəyişikliklərindən sonra və merge nəticəsində yenidən yoxlanacaq.
- Başlanğıcdakı responsive-ui.mdc və responsive-rule review istifadəçinin əvvəlki untracked fayllarıdır; bu commit-ə daxil edilmir.
- Son konsept və sənəd dəstindən sonra CI təkrarı: BUILD SUCCESSFUL, 43 task (42 up-to-date). Handoff sənədinə bu vahidin nəticəsi əlavə edildi. Diff mətnləri və PNG nəzərdən keçirildi; məhdudiyyətlər yuxarıda saxlanır.
