# Şəhər — kafe terrasının vizual davamı

## Scope və nəticə
14 noyabr 2026. Branch `fix/city-remaining-visuals`, baza `ce12acf`. İstifadəçi göstərilən `docs/design/city-view.png` əsasında qalan detalları tətbiq etməyi açıq təsdiqlədi. Yeni alternativ kompozisiya yoxdur. Mövcud sahil/balkon/glow/navbar və sabit kamera saxlanır.

Built-in imagegen ilə referensdəki çəhrayı-ağ parasollu kafe terrası şəffaf modul sprite kimi bərpa edildi; geniş ön balkon ikinci mərtəbədə bu ornamentlə əvəz edilir. Yan balkon qalır; corner-window kitində əvvəlki dar balkon saxlanır. Ground qapı/işıq və selected parts dəyişmir. Crop/affine anchor-lar mövcud shell koordinatlarıdır, UI spacing tokeni deyil. Token mənbəyi `GameTokens.kt`; yeni UI rəng/ölçü yoxdur. Provenance: `docs/design/city-garden-v1/terrace-v1.md`. Asset SHA256: `8663048f6c656c54b58c9416f2a17294cb66bf66aa91bcf2a377d6b4f0392f7b`.

## Checks
- Native API34 Compose/Robolectric parity host: 3/3 PASS, BUILD SUCCESSFUL17s. 390×844/font1,360×640/font1,320×568/font2; hər ssenaridə beş binaya focus/return. PNG-lər `docs/reviews/city-remaining-visual/` altındadır; synthetic hesabdır, production demo adapteri deyil.
- Hər üç overview və390/font1 focus0 şəkilləri açılıb baxıldı. Parasollar/railing görünür; girişlər və Bax/Geri/naviqasiya qalır. Reference də ayrıca açılıb tutuşduruldu. Kafe pəncərə qarşısında balkon ornamentidir; tam pəncərə visibility iddiası yoxdur.
- Java21/offline adi core/engine/app unit + assembleDebug + lintDebug PASS20s. XML: core117,engine95,app24;236test,failure/error/skipped0. Core/engine UP-TO-DATE,app icra edildi. Sonrakı source dəyişməsi yalnız KDoc/comment-dir.
- Kontrast4/4PASS: Text/Surface13.00,Muted/Surface8.22,Background/Accent11.45,Text/Raised10.24. Lokal diff review: ground/corner exceptions, crop bounds, affine projection, floor order və source pixel cap yoxlandı. Domain/API/DTO/schema/wallet/ownership/accepted geometry/revision dəyişmir; AppBackend dəyişikliyi tələb olunmur. Backend testləri işlədilmədi.

## Açıq sərhəd
Tam şəhər/mərhələ4qəbulu deyil. Referensin fontanı təsdiqli V1 kataloqunda yoxdur (PLAN§6: ağac/skamya/işıq/yol). İstifadəçi “Mövcud kataloqla tamamla” seçdi: fontan əlavə edilmir, yeni dekor/domain genişləndirilməsi yoxdur. Bu vizual vahid mövcud kataloq sərhədində tamamlandı; qərar tam mərhələ qəbulunu əvəz etmir. Düz/L parcel union forması və real selected bina siluetləri statik yumru konsept adasına çevrilmir. Bu məlumat fərqləri piksel eyniliyi kimi bağlanmır.

Device/API24runtime/inset/TalkBack/GPU/performance və planşet/landşaft yoxlanmayıb. Emulator/CI/commit/merge/push yoxdur. Əvvəlcədən untracked release artefaktları qorunur.

Son QA: `city-remaining-visual/design-comparison.png` solda təsdiqli referens, ortada390/font1, sağda320/font2 native renderdir. AppKit ilə yalnız QA kompoziti hazırlanıb və açılıb baxılıb; məhsul asseti dəyişdirilməyib. Tam kompozisiya/piksel fərqləri görünür və gizlədilmir. Son `git diff --check` PASS.

## Son lokal review və commit — 14 noyabr
İstifadəçi lokal commit və açıq iş yoxdursa mərhələ4 qəbulunu təsdiqlədi. Source/asset metadata/harness diff yenidən oxundu; yeni actionable tapıntı yoxdur. Production davranışında əvvəlki testlərdən sonra dəyişiklik yoxdur,236unit/APK/lint və3native host nəticəsi reuse edilir. Son diffcheck PASS. Razılaşdırılmış mobil mərhələ4 scope tamamdır; qəbul `phase4/acceptance.md`-də qeyd edildi. Fontan yoxdur; əvvəlki cihaz/piksel məhdudiyyətləri dəyişmir. Review edilmiş source/assets/docs/harness lokal commit-ə daxildir; generated release artefaktları daxil deyil. Merge/push/CI yoxdur.
