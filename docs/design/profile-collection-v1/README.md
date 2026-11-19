# Profil — kolleksiya üslubunda dizayn təklifi

Son nümunə: `proposal-v2.png`. İstifadəçinin göstərişi ilə yalnız “Ad və avatar” sətri və thumbnail yığcamlaşdırılıb, alt shortcut-lar yuxarı çəkilib. Built-in ImageGen edit; vizual baxış aparılıb. Qalan kompozisiya saxlanır; generativ edit piksel eyniliyi iddiası deyil. İstifadəçi proposal-v2-ni təsdiqləyib native implementasiyanı tapşırdı. Prompt: `compact-edit-prompt.txt`.

19 noyabr 2026. İstifadəçi tapşırığı: current-task və PLAN §1.1 əsasında mövcud funksiyaları saxlayan yeni PNG; implementasiya yoxdur. İlkin `proposal.png` əvəzinə son təsdiqli vizual authority `proposal-v2.png`-dir.

Üç görünüş: Profil, Profili dəyiş, Avatar seç. Ad/avatar redaktəsi, 1–15 simvol, saxla/ləğv, builder/pilot/architect seçimi, səviyyə/server XP intervalı, rekord və ümumi XP saxlanır. Parametrlər və mövcud Nailiyyətlər/Rekordlar girişləri, kolleksiya header-i və Profil aktiv 7-slot navbar göstərilir. Rəqəmlər nümunədir; yeni iqtisadiyyat/domain qaydası deyil.

Üslub mənbəyi `../market-collection-v1/proposal.png`; personaj/funksiya mənbələri `../profile-v2/{profile-approved,edit-avatar-approved}.png`. GameTokens.InventoryCollection palitrası və mövcud ölçü pillələri əsas götürülüb. Native implementasiya, backend/API, runtime və adaptiv render yoxlaması aparılmayıb. PNG sadə UI üçün production skin deyil.

Built-in ImageGen istifadə edilib, CLI/API fallback yoxdur. Tam prompt `generation-prompt.txt`-dədir. Generativ PNG açılıb vizual yoxlanıb; mətn/idarəetmə və yeddi navbar girişi görünür. Token kontrastı: Text/PanelTop 12.51, Muted/PanelTop 7.91, Accent/PanelTop 8.65 (dəqiq nəticə review-dadır); bu PNG-nin hər gradient pikselinin və native əlçatanlığın ölçülməsi deyil.

Native implementasiya və aktual yoxlama sərhədləri: `../../reviews/2026-11-19-profile-collection-native.md`. Yuxarıdakı implementasiya edilməyib qeydləri ilkin dizayn mərhələsinə aiddir.
