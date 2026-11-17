# Profil/Parametrlər yığcam nümunəsi — 15 noyabr 2026

Branch `docs/compact-account-preview`, baza `a192f4d`. Production source dəyişmir; bu yalnız Compose review fixture-dir. İstifadəçi son Arrowz əsaslı settings-390 nümunəsini “yes” ilə açıq təsdiqlədi (15 noyabr 2026).

İstifadəçi iri profil düymələrini və Parametrlər sətirlərini yığcamlaşdırmağı istədi, sonra Parametrlər sətirləri üçün Arrowz-a baxıb yenidən çəkməyi tapşırdı. Əvvəlki grouped settings nümunəsi cari deyil.

Arrowz read-only istinadları: feature/settings/SettingsKit.kt SettingsRow/RowIcon, SettingsScreen.kt PhoneBody, design/v9-firstrun/screens/S1-settings-row.png, core/designsystem ArrowToggle və NeonIcons. Telefon prinsipi: hər sətir ayrıca kart, dairəvi ikon, mətn, kiçik toggle/chevron və section labels. Wobbrik palitrası/fontu qorunur; Arrowz oyun qaydaları və əlavə settings funksiyaları köçürülmür. Xətti ikon yolları həmin NeonIcons-dan review fixture-ə götürülüb.

Ölçülər mövcud GameTokens-dandır: sətir min64dp (Target+Gap), dairə40dp (Target−Small), ikon24dp, mətn16sp, gap8dp, radius16dp, toggle48×24dp / 48dp slot. Font2-də mətn wrap edir və body scroll qalır. Parametrlər üçün ayrıca iri aşağı Geri düyməsi yoxdur; başlıqda48dp geri saxlanır. Profil preview-nin ölçüləri ayrıca pending-dir.

Yoxlama: ilkin altı native preview final6/6 PASS21s; son Arrowz settings3/3 PASS; 390×844/360×640 font1 və320×568 font2. Son üç settings PNG-yə baxıldı; font2-də dəstək bölməsi scroll altındadır. Back target ölçü/görünürlük assert var. Kontrast3/3 PASSmin8.22. İlk uğursuz run ortaq board provider çatışmazlığı idi; sonra avatar source-u düzəldildi. Əvvəlki settings390 capture-da başlıq/footer natamam idi; son Arrowz settings şəkillərində başlıq tamdır, footer yoxdur.

Məhdudiyyət: sintetik fixture, real settings persistence/navigation/server yoxdur. Production-da ayrıca SettingsScreen hazır deyil. Dil dəyəri yalnız əvvəlki dizaynın nümunəsidir, dəstəklənən locale siyahısı qərarı deyil. Device/emulator/IME/TalkBack/API24 və planşet yoxlanmayıb. CI/commit/merge/push yoxdur.

Növbəti addım: təsdiqli settings-390 görünüşünü real UI/domain axınına tətbiq et; dizayn təkrar təsdiqə çıxarılmır. Parametrlər funksiyalarının açıq texniki qərarları ayrıca dəqiqləşdirilir. Profil preview təsdiqi bu cavabdan çıxarılmır.
