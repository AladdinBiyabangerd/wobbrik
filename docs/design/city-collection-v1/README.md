# Şəhər — İnventar üslubunda təsdiqli dizayn

18 noyabr 2026: istifadəçi bu chat-də yaradılmış `proposal.png` nümunəsini “okey bu dizaynı şəhər page üçün implement et” ilə native Kotlin + Compose üçün təsdiqlədi. Yeganə icra planı `../../PLAN.md` §1.1-dir. Bu qovluq ikinci plan deyil.

Mənbə built-in ImageGen, İnventar `../inventory-art-led-v1/reference.png` üslub istinadı ilə generasiya edilib. PNG generativ konseptdir, runtime və real hesab sübutu deyil. Orijinal dəyişdirilmədən workspace-ə köçürülüb.

Təsdiqli kompozisiya: ortaq collection header/balans, Şəhərim + refresh, böyük native səhnə paneli, səhnə daxilində fit/focus idarələri, seçilmiş binanın adı/mərtəbə/sakin və Binaya bax paneli, Şəhər aktiv olan7-slot navbar. Ərazi alışı/alternativ giriş yoxdur. Mətn və idarələr raster skin deyil. Nümunə binaları/rəqəmləri yeni kataloq/ownership deyil; server həndəsəsi və yerləşməsi saxlanır.

İlkin native layout sübutu `../../reviews/2026-11-18-city-collection-native.md`-dədir. Konseptin üzvi daş sahili, sıx ağac/yol kompozisiyası və yeni bina illüstrasiyasının real dinamik renderer-ə uyğunlaşdırılması hələ açıqdır; layout vahidi tam vizual paritet və ya redesign qəbulu deyil. Mövcud şəhər modular artı bütöv maket rasteri ilə əvəz edilməyib.

18 noyabr — scene artwork davamı: ayrıca modular atlas və city-only fasad/daş sahil/landşaft/giriş cığırları inteqrasiya edilib. İlkin layout review-dakı artwork statusu həmin əvvəlki nəticəyə aiddir. Cari artwork, real geometry-yə görə görünən fərqlər və37-test/native render sübutu `../../reviews/2026-11-18-city-scene-art.md`-də, asset provenance `scene-art.md`-dədir. Tam piksel pariteti və bütün app redesign qəbulu iddia edilmir.
