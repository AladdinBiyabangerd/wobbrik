# UI assets — 5 noyabr 2026

İstifadəçi çatışmayan asset-lərin hazırlanmasını bu chatdə təsdiqlədi.
Built-in image_gen; CLI/API fallback yoxdur. Generativ rekonstruksiya piksel eyniliyi deyil.

Mənbə: `docs/design/mobile-v1/mobile-primary-v2.png` (ikonlar),
`docs/design/mobile-v1/account-states.png` (recovery hero).
PNG-lər alpha ilə saxlanılıb. Bütün seçilmiş çıxışlara baxılıb.

Android çıxışları: `android-billing/app/src/main/res/drawable-nodpi/` altındakı
`ui_{coin,account,trophy,medal,level}.png`, `recovery_hero.png`.
İstifadə olunmayan crown `docs/design/account-assets-v1/ui-icons/crown.png`-dədir;
rekord məlumatı server snapshot-da yoxdur, lokal rekord uydurulmur.

## Icon prompts

### coin

Use case: stylized-concept. Asset type: single production UI icon for Wobbrik Android game. The provided reference is the approved Wobbrik UI; reproduce its corresponding icon faithfully. Primary request: one gold coin with a five-point star embossed, warm yellow orange glossy rim, front view, same coin shown next to 1250. Preserve the approved polished cartoon 3D game icon shape, icy blue/cyan or gold material as specified, frontal view and subtle bevel and highlight. Single isolated icon centered, large in square canvas, tiny transparent margins. Actual transparent background, no button background, no letters, no text, no other icons, no watermark. This is an icon extraction/reconstruction, not redesign.

### account

Use case: stylized-concept. Asset type: single production UI icon for Wobbrik Android game. The provided reference is the approved Wobbrik UI; reproduce its corresponding icon faithfully. Primary request: one pale blue head and shoulders profile/person bust, same account icon at top right. Preserve the approved polished cartoon 3D game icon shape, icy blue/cyan or gold material as specified, frontal view and subtle bevel and highlight. Single isolated icon centered, large in square canvas, tiny transparent margins. Actual transparent background, no button background, no letters, no text, no other icons, no watermark. This is an icon extraction/reconstruction, not redesign.

### trophy

Use case: stylized-concept. Asset type: single production UI icon for Wobbrik Android game. The provided reference is the approved Wobbrik UI; reproduce its corresponding icon faithfully. Primary request: one pale icy blue silver trophy cup on a short plinth, same trophy next to Nailiyyətlər. Preserve the approved polished cartoon 3D game icon shape, icy blue/cyan or gold material as specified, frontal view and subtle bevel and highlight. Single isolated icon centered, large in square canvas, tiny transparent margins. Actual transparent background, no button background, no letters, no text, no other icons, no watermark. This is an icon extraction/reconstruction, not redesign.

### medal

Use case: stylized-concept. Asset type: single production UI icon for Wobbrik Android game. The provided reference is the approved Wobbrik UI; reproduce its corresponding icon faithfully. Primary request: one pale icy blue silver star award medal with two short blue ribbons, same medal next to Kolleksiya. Preserve the approved polished cartoon 3D game icon shape, icy blue/cyan or gold material as specified, frontal view and subtle bevel and highlight. Single isolated icon centered, large in square canvas, tiny transparent margins. Actual transparent background, no button background, no letters, no text, no other icons, no watermark. This is an icon extraction/reconstruction, not redesign.

### level

Use case: stylized-concept. Asset type: single production UI icon for Wobbrik Android game. The provided reference is the approved Wobbrik UI; reproduce its corresponding icon faithfully. Primary request: one turquoise miniature three-building skyline with windows, same Səviyyə icon. Preserve the approved polished cartoon 3D game icon shape, icy blue/cyan or gold material as specified, frontal view and subtle bevel and highlight. Single isolated icon centered, large in square canvas, tiny transparent margins. Actual transparent background, no button background, no letters, no text, no other icons, no watermark. This is an icon extraction/reconstruction, not redesign.

### crown

Use case: stylized-concept. Asset type: single production UI icon for Wobbrik Android game. The provided reference is the approved Wobbrik UI; reproduce its corresponding icon faithfully. Primary request: one small golden yellow royal crown, same Rekord icon. Preserve the approved polished cartoon 3D game icon shape, icy blue/cyan or gold material as specified, frontal view and subtle bevel and highlight. Single isolated icon centered, large in square canvas, tiny transparent margins. Actual transparent background, no button background, no letters, no text, no other icons, no watermark. This is an icon extraction/reconstruction, not redesign.

## Recovery hero prompt

Use case: background-extraction. Asset type: Wobbrik recovery screen hero PNG. Reference image is the approved account-states.png. Reconstruct ONLY the rightmost panel's isolated blue-roof small shop on a green island with large white cloud above it containing a pink crossed-out wifi symbol. Preserve the original polished cartoon 3D game style, materials, perspective, exact shape and colors of the building and cloud. No UI, no buttons, no text, no other panels or objects. Single centered illustration occupying the canvas with small transparent margins. Actual transparent background.

## Recovery transparency edit

Edit only transparency around the supplied Wobbrik recovery hero. Keep the cloud, crossed wifi symbol, house, island, lamp, all solid geometry colors shading perspective unchanged. Remove ALL colored pink/blue/white fringes and residual halo outside the solid silhouette, especially above/around/under cloud and in the gap between cloud and roof. Those areas must have alpha exactly zero, clean transparent edges, no glow backdrop or diffuse colored pixels. Actual transparent background. No other changes.
