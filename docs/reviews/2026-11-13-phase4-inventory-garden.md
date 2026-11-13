# Mərhələ 4 — inventar/detail altlıq və atlas anchor-ları

- Branch: `feat/phase-4`; yoxlanmış baza `09185dc`, bu qeydlə birlikdə lokal working diff. Merge/push/CI yoxdur. İstifadəçinin untracked `android-billing/app/release/` faylları qorunub.
- Mənbə: təsdiqli `mobile-v1/inventory-market-v2.png`, `city-completion-v1/04-building-detail.png`, `GameTokens.kt`, mövcud HomeButton və GameScene atlas anchor-ları. Yeni rəng/spacing/radius/type token-i yaradılmadı.
- Binasız şəffaf `inventory_garden.png` əlavə edildi. Qəbul edilmiş center koordinatları saxlanır; altlıq binanı əvəz etmir. Preview fit-i altlıq, dam və əyilən mərtəbələrin ümumi bounding box-unu nəzərə alır. Fasad, pəncərə, giriş, işıq və dam anchor-ları GameScene ilə uyğunlaşdırıldı. Atlasın öz alpha/şəkli dəyişdirilmədi.
- Hissələr tabında atlas və ad üfüqi kartdadır; geri düyməsi dairəvi, inventar başlığı mövcud ikonla; əsas düymədə mövcud HomeButton-un gradient/outline təqdimatı təkrar istifadə edilir. Yeni işləməyən edit/bazar/placement əmri əlavə edilmədi.

## Asset provenance

Built-in `image_gen` (imagegen skill), reference: `04-building-detail.png`; nəticə baxılaraq yoxlanıldı. Layihə faylı: `android-billing/app/src/main/res/drawable-nodpi/inventory_garden.png` (1536×1024 RGBA, alpha var). Alpha yoxlaması: künc və yuxarı mərkəz 0; alpha intervalı 0…254. Original generated image saxlanılıb.

Final prompt: “Create a production game sprite matching the ground dressing under the towers in the reference image. Reference is style/composition source only. One isolated small oval grassy garden island, front three-quarter view, lush rounded green trees at rear left and rear right, low pink flower beds at front corners, tiny warm stepping stones at front center, beveled earth/grass rim. Keep a wide empty flat central building footprint with NO BUILDING, NO WALL, NO DOOR, NO ROOF, NO LANTERN: the real tower is composited over the center by the game. Trees only around the edges. Match saturated polished hand-painted 3D mobile game art precisely. Sprite fills canvas, landscape 3:2 composition, genuine transparent background, no text, no UI, no frame, no shadow outside island.”

## Review və yoxlamalar

- Lokal diff review: server koordinatları, ownership, qiymət və wallet dəyişmir; atlas draw sırası aşağıdan yuxarı qalır; missing/legacy geometry statusu qorunur; hissə adları scroll panelindədir. Dekorativ hissə kartlarına saxta click affordance verilmədi.
- Java 21: `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS. Gradle cache lock sandbox-da bloklandı, icazəli eskalasiya ilə yoxlamalar tamamlandı. CI deyil.
- `git diff --check` PASS. Kontrast checker: Text/Surface 13.00, Muted/Surface 8.22, Background/Accent 11.45 — PASS. Palitra dəyişməyib.
- Android screenshot quruluşu layihədə yoxdur; emulator açılmadı. 360×640/normal portret, font 1.0/2.0, inset/a11y və tam ekran kompozisiyası render edilmədi. Asset görüntüsünə baxmaq Android renderini əvəz etmir. Atlas səthlərinin faktiki birləşməsi və parlaq çərçivə/ikonların dizayna tam eyniliyi qəbul edilməyib; vizual PASS iddiası yoxdur. Canlı yoxlama gate deyil.
- AppBackend dəyişdirilmədi. Mövcud API sənədi yalnız oxundu: `shop/buy` və `buildings/edit` var. Yeni server/schema/DTO müqaviləsi bu təqdimat diff-i üçün tələb olunmur. Backend testləri işlədilməyib.

## Növbəti vahid

İstifadəçinin təsdiqli `02-parts-flow-v2.png` hissələr/mağaza/binaya tətbiq axınına keçid tapşırığı qüvvədədir; implementasiya bu diff-də başlamayıb. Android `AttemptCommand.Route` hazırda yalnız START/CHECKPOINT/FINISH/ABANDON tanıyır; alış/edit üçün mövcud yeganə durable journal və recovery sərhədini genişləndirmək lazımdır. İkinci journal, client wallet/unlock və naməlum nəticədə yeni command yaratmaq olmaz. AppBackend-ə toxunmamaq qadağası qalır. Reklam/real pul inteqrasiyası hazır kimi göstərilmir.
