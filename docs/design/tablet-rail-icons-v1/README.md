# Tablet rail icons v1 — 7 noyabr 2026

Built-in image_gen ilə təsdiqlənmiş `../tablet-v1/primary-landscape.png` rail ikonlarından generativ rekonstruksiya. Piksel eyniliyi iddia edilmir; kontur, bevel və işıq detalları fərqlənə bilər. UI/naviqasiya dəyişməyib.

Android: `android-billing/app/src/main/res/drawable-nodpi/ui_{home,city,inventory,market}.png` — 1254×1254 RGBA, alpha 0–255. Mövcud PNG-lər əvəz edilməyib. Seçim fonu/glow asset-ə daxil deyil. ArtIcon ölçüləri və optik padding rail inteqrasiyasında yoxlanmalıdır.

Palitra mənbəyi: `../mobile-v1/tokens.css`; dəyişdirilməyib. Preview-lərdə həmin fon/mətn tokenləri oxunur. `uv run --with pillow python docs/design/tablet-rail-icons-v1/render-preview.py` — 390×844 və 1280×800 asset QA, 24/32/48/96px. Hər iki renderə baxılıb: dörd simvol oxunur, ilkin evdəki fon dumanı seçilmiş çıxışda yoxdur. Bunlar Compose ekranı deyil; real ekran uyğunluğu, planşet rail və toxunma hədəfləri yoxlanmış sayılmır.

## Prompts

### home

Production Wobbrik Android navigation icon: home. Reference image is approved tablet board; reconstruct ONLY its corresponding rail icon: small pale icy-blue house, simple peaked roof, dark open doorway, frontal view. Match the unselected home icon in the lower-left rail. Preserve approved polished softly beveled 3D cartoon silhouette, proportions, materials and shading. One isolated centered icon, square canvas, large with modest transparent margins. Actual transparent background. No rail, button, tile, selection cyan glow, background, text, letters, watermark, or other objects. This is faithful asset reconstruction, not redesign. Must remain legible at 24 and 32 logical pixels.

### city

Production Wobbrik Android navigation icon: city. Reference image is approved tablet board; reconstruct ONLY its corresponding rail icon: three pale icy-blue buildings of varying heights with dark small windows, frontal skyline. Match the unselected city icon in upper-left rail. Preserve approved polished softly beveled 3D cartoon silhouette, proportions, materials and shading. One isolated centered icon, square canvas, large with modest transparent margins. Actual transparent background. No rail, button, tile, selection cyan glow, background, text, letters, watermark, or other objects. This is faithful asset reconstruction, not redesign. Must remain legible at 24 and 32 logical pixels.

### inventory

Production Wobbrik Android navigation icon: inventory. Reference image is approved tablet board; reconstruct ONLY its corresponding rail icon: small pale icy-blue rounded satchel/bag with top handle and dark seams and clasp. Match the inventory bag third icon in upper-left rail. Preserve approved polished softly beveled 3D cartoon silhouette, proportions, materials and shading. One isolated centered icon, square canvas, large with modest transparent margins. Actual transparent background. No rail, button, tile, selection cyan glow, background, text, letters, watermark, or other objects. This is faithful asset reconstruction, not redesign. Must remain legible at 24 and 32 logical pixels.

### market

Production Wobbrik Android navigation icon: market. Reference image is approved tablet board; reconstruct ONLY its corresponding rail icon: small shop front with pink and white striped awning, pale icy-blue base and dark opening. Match the market fourth icon in upper-left rail. Preserve approved polished softly beveled 3D cartoon silhouette, proportions, materials and shading. One isolated centered icon, square canvas, large with modest transparent margins. Actual transparent background. No rail, button, tile, selection cyan glow, background, text, letters, watermark, or other objects. This is faithful asset reconstruction, not redesign. Must remain legible at 24 and 32 logical pixels.

### Home transparency correction

Remove ALL background and white grey blue haze from around this house icon. Keep only the solid house silhouette exactly unchanged, preserve color geometry and dark doorway. Outside silhouette alpha must be exactly ZERO, no shadows no glow no residual pixels anywhere. Actual clean transparent production UI cutout.

