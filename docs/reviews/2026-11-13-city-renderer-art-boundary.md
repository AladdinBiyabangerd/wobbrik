# Şəhər renderer-i — asset sərhədinin yoxlanması

- Branch: feat/phase-4. Cari owner diff qorunub; implementasiya, commit/merge/push və backend dəyişikliyi edilməyib.
- Oxundu: current-task handoff, PLAN (xüsusən §§1/7/8/9), CitySceneReads.kt, BuildingPreview, CosmeticSprite atlas loader və sprite/roof anchor-ları, GameTokens və mövcud host render resepti.
- Vizual baxıldı: təsdiqli docs/design/city-view.png, production finish/wall-atlas.webp və city/land-thumbnail-v2.png. Bunlar source asset inspeksiyasıdır, Android renderi deyil.

## Faktlar

CitySceneRepository real accepted geometry/parts, server position/quarter-turn və parcel-ləri verir. Legacy null position origin kimi çəkilə bilməz. Membership inventory deyil: yalnız şəhər üzvləri çəkilməlidir. Position və mərtəbə offset-ləri təməl origin-i ətrafında çevrilməlidir; sprite-i ekran müstəvisində döndərmək quarter-turn dünya çevrilməsi deyil.

Mövcud cosmetic loader hər hissəyə bir sprite verir; müxtəlif baxış istiqamətləri və ayrıca səth teksturaları yoxdur. BuildingPreview eyni ön görünüş atlaslarını server x offset-ləri ilə qatlayır; bu, sərbəst şəhər kamera dönüşünün realizasiyası deyil. Təsdiqli city-view səhnəsi daha çox yan səth və həcm göstərir. Mövcud atlaslarla uyğunluğun avtomatik təmin olunduğu iddia edilmir.

Land-thumbnail-v2 bütöv, əvvəlcədən ağac/çiçək çəkilmiş alış illüstrasiyasıdır. Onu server parcel union-u kimi böyütmək real konturu və mülkiyyətə uyğun dekorları verməz. city-view rasterindəki hazır binalar da istifadəçinin real binası kimi göstərilə bilməz.

## Nəticə və açıq qərar

Mövcud atlaslarla ilkin sabit bucaq təqdimatı və ya təsdiqli görünüş üçün əlavə modul səth/baxış asset-ləri tələb edən realizasiya arasında texniki sərhəd dəqiqləşdirilməlidir. Sadələşdirilmiş alternativ tətbiq edilmədi. Placement bağlıdır. UI dəyişmədiyi üçün build/kontrast/render işə salınmadı; vizual PASS yoxdur. Yeni stack/3D mühərrik seçilmədi.

## Son qərar
İstifadəçi mövcud atlaslarla ilkin sabit kamera bucağını “beli” ilə təsdiqlədi. Bu açıq sual bağlandı; implementasiya və məhdudiyyətlər `2026-11-13-phase4-city-buildings.md`-dədir. Torpaq və tam şəhər görünüşünün hazır olduğu nəticəsi çıxarılmır.
