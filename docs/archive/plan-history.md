# Wobbrik — plan tarixi (arxiv)

Bu bölmələr tarixi qeyd kimi saxlanılır; işlək plan [../PLAN.md](../PLAN.md)-dir. Mətn dəyişdirilməyib.

---

_game-plan.md-dən:_

## 15. Hazırlama mərhələləri və keçid şərtləri

### Mərhələ 0 Konsept qərarları

Açıq qərarları yekunlaşdırmaq, V1 sərhədini təsdiqləmək, sonra texnologiya seçmək. Əvvəl mövcud layihə yoxlanacaq. Hazırda tətbiq texnologiyası seçilməyib; konkret mühərrik bu planla təsdiqlənmir.

### Mərhələ 1 Əsas mexanika prototipi

Sadə fiqurlarla kran, buraxma, mərtəbə oturması, əlaqəli bina yellənməsi, xilasetmə, aşma və restart. Keçid şərti: mərkəz enişi daha sabitdir, əks enişlə xilasetmə mümkündür, gözləmək məğlub etmir və bir toxunuş bir mərtəbədir.

### Mərhələ 2 Oynanılan cəhd

Çətinlik, kamera, fasilə/arxa plan, lokal rekord, nəticə və son uğurlu binanın saxlanması. Mobil toxunma sınağı. Keçid şərti: cəhd başdan sona problemsiz oynanır, məğlubiyyət aydındır və yeni cəhdin vəziyyəti təmizdir.

### Mərhələ 3 Bina kolleksiyası və şəhər

Bina qeydi, hissələr, inventar, yerləşdirmə/dəyişdirmə, əyriliyə görə sahə yoxlaması, sakin tutumu və sadə şəhər animasiyaları. Keçid şərti: binalar itmir, üst-üstə düşmür, şəhər əhalisi düzgün hesablanır.

### Mərhələ 4 İqtisadiyyat və inkişaf

Sikkə, XP, nailiyyət, hissə alışları, levelə bağlı ərazi və qiymət çəkiləri. Keçid şərti: mükafatlar təkrarlanmır, pulsuz irəliləyiş mümkündür, alış müddətləri ölçülür.

### Mərhələ 5 Onlayn funksiyalar

Hesab/bərpa, server saxlanması, nəticə yoxlaması, leaderboard, bazar və sikkə əməliyyatları. Keçid şərti: eyni bina iki dəfə satılmır, bağlantı kəsilməsi bina/sikkə itkisi və ya çoxalması yaratmır. Oflayn sinxronizasiya qaydası burada tətbiq edilir.

### Mərhələ 6 Vizual və monetizasiya

Seçilmiş vizual sistem, tokenlər, eniş/xilas/aşma effektləri, səs/vibrasiya, reklam və platforma alışları. Keçid şərti: mobil render görüntülərinə baxılıb, səssiz hadisələr aydındır, alış/reklam mükafatları düzgün təsdiqlənir, reklam əsas oyunu kəsmir.

### Mərhələ 7 Qapalı sınaq və buraxılış

Real cihazlarda performans, oyun axını və iqtisadiyyat sınağı. Cəhd müddəti, məğlubiyyət səbəbləri, yenidən oynama, xilasetmə, sikkə axını, satılan elan payı, reklamdan sonra çıxış və geri qayıtma ölçülür. Hədəflər ilkin sınaq məlumatından sonra qoyulur; gəlir və bağlılıq zəmanəti verilmir.


---

_game-plan.md-dən:_

## 24. AppBackend yeniliyi və texnologiya seçimi

**Cari son qərar: yalnız Android, Arrowz kimi Kotlin + Jetpack Compose/Canvas və ayrıca Kotlin oyun məntiqi modulu. Unity istifadə olunmayacaq. Aşağıdakı Unity seçimi və lisenziya araşdırması əvvəlki qərarların tarixçəsidir; son qərar bu bölmənin sonunda qeyd olunub.**

3 noyabr 2026. İstifadəçinin yeni qərarı: My Workspace/AppBackend bir neçə tətbiq üçün ortaq servis olacaq, hər tətbiqin öz adapteri olacaq. Bu qərar bölmə 17 və 20–22-dəki «ayrıca backend yoxdur» məhdudiyyətini ortaq servis əhatəsində əvəz edir. Firebase Spark saxlanılır; Blaze/Cloud Functions seçilməyib. Köhnə serversiz alış nəticələri həmin vaxtın araşdırmasıdır, yeni arxitekturanın son vəziyyəti deyil.

### Mövcud servisin sənədləşdirilmiş vəziyyəti

AppBackend README, docs/context/current-task.md, docs/integration.md və docs/verification.md oxunub. Sənədlərə görə Go/net/http/PostgreSQL alış nüvəsi Firebase ID tokeni və Google Play alış yoxlaması, daimi hüquqlar, bir dəfə sikkə krediti və server ack/consume növbəsi saxlayır. Lokal test nəticələri sənədləşdirilib; bu chatdə testlər təkrar aparılmayıb. Real credential/SKU, mobil inteqrasiya və deploy yoxdur. Hazır servis tam oyun backend-i deyil.

Bölmə 22.5-dəki cihaz yoxlaması gələcək alış inteqrasiyasının əsas modeli kimi götürülməməlidir: yeni istiqamət serverdə Play yoxlaması və server çatdırılmasıdır. Faktiki keçid mobil inteqrasiya və real alış sınağından sonra işlək sayılacaq. PENDING hüquq vermir, paket təkrar kreditləşmir, daimi hüquqlar bərpa olunur. Wobbrik üçün hesab silmə/transfer və xərclənmiş sikkə refund siyasəti ayrıca açıqdır; başqa tətbiqin siyasəti avtomatik köçürülmür.

### Mühərrik müqayisəsi və son seçim

- **Unity / C#: əsas tövsiyə.** Android build vasitələri, 2D/3D fizika, rəsmi Firebase Unity SDK-sında Auth/Firestore/App Check/Crashlytics və rəsmi AdMob Unity plugin-i var. Wobbrik-in geniş V1 inteqrasiyalarında xüsusi mobil körpü işini azaltması gözlənilir; bu layihə üzrə mühəndis qiymətləndirməsidir. Mənfi tərəflər: daha ağır build/SDK asılılıqları və kommersiya lisenziyası şərtləri. Personal maliyyə uyğunluğu ayrıca yoxlanmalıdır; Runtime Fee ləğv edilib.
- **Godot / GDScript: əsas alternativ.** Android export, 2D/3D imkanları və MIT lisenziyası var. AppBackend-ə HTTP çıxışı mümkündür. Firebase-in rəsmi Unity SDK-sına bərabər Godot SDK yolu göstərilmir; native Android plugin və ya community inteqrasiyasının Auth, token refresh, App Check, Billing və reklam uyğunluğu ayrıca yoxlanmalıdır. Backend yaranması Firestore-a birbaşa asılılığı azalda bilər, amma mobil SDK ehtiyaclarını tam götürmür.
- **Defold / Lua: 2D əhatəsi qəti seçilərsə namizəd.** Android export, Box2D əsaslı 2D fizika və AdMob/IAP extension-ları var. Wobbrik mexanikası üçün yetərli namizəddir; tam Firebase Auth/Firestore/App Check inteqrasiyasını yalnız Firebase Analytics extension-ının mövcudluğu ilə hazır saymaq olmaz. Şəhərin gələcək 3D istiqaməti açıqkən Unity/Godot daha çevik seçim görünür.

Son istifadəçi göstərişi ilə mühərrik seçimi: **Unity LTS + C#**. Dəqiq LTS versiyası implementasiya vaxtı SDK uyğunluğu yoxlanaraq seçiləcək. Unity Personal istifadəsi maliyyə uyğunluğu şərtinə tabedir; istifadəçinin faktiki uyğunluğu bu chatdə müəyyən edilməyib. APK/RAM/FPS müqayisə benchmark-ı aparılmayıb; ölçülmüş üstünlük iddiası yoxdur.

### Fizika və servis sərhədi üzrə təklif

Wobbrik hər mərtəbənin ayrıca dağılmasını tələb etmir. Buna görə çoxlu rigidbody-dən qüllə yığmaq əvəzinə sabit zaman addımı ilə idarə olunan tarazlıq/yellənmə modeli, düşən mərtəbənin eniş hesabı və ayrıca görünüş qatı təklif olunur. Xilasetmə, zəifləmə və «gözləmək təkbaşına uduzdurmur» qaydasını bununla tənzimləmək daha rahat ola bilər. 2D və ya 2.5D təqdimat ayrıca seçilməlidir. Unity fizikasını işlətmək cihazlararası deterministik replay təminatı deyil; Go servisdə Unity fizikasının eyni nəticəni verməsi fərz edilmir.

Təklif olunan bölgü: mühərrik oyun/görünüş və mobil SDK-lar; Firebase Spark hesab və seçilmiş save/sync məlumatları; AppBackend alış və Wobbrik domain adapteri. Oyun sikkəsi, pullu sikkə və bazar üçün bir vahid etibarlı pul mənbəyi layihələndirilməlidir. PostgreSQL və Firestore-u birlikdə bir DB tranzaksiyası kimi təqdim etmək olmaz. Pul və bazarın eyni server tranzaksiyasına daşınması namizəd həllidir, təsdiqlənmiş implementasiya deyil. Oyun nəticələrinin və reward reklamın server yoxlaması bu alış nüvəsində yoxdur; əvvəl qəbul edilmiş bu risklər avtomatik bağlanmır.

### Mənbələr və yoxlama sərhədi

3 noyabr 2026-da rəsmi mənbələr oxunub:
- Unity Android: https://docs.unity3d.com/6000.1/Documentation/Manual/android-sdksetup.html
- Firebase Unity SDK: https://firebase.google.com/docs/unity/setup
- AdMob Unity: https://developers.google.com/admob/unity/quick-start
- Unity lisenziya həddi: https://unity.com/pages/license-compliance
- Runtime Fee: https://unity.com/products/pricing-updates
- Godot Android: https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html
- Godot Android plugin-ləri: https://docs.godotengine.org/en/stable/tutorials/platform/android/android_plugin.html
- Godot MIT: https://docs.godotengine.org/en/stable/about/complying_with_licenses.html
- Defold fizika və Android: https://defold.com/manuals/physics/ və https://defold.com/manuals/android/
- Defold AdMob: https://defold.com/extension-admob/

Kod, prototip, SDK quraşdırılması, canlı xidmət çağırışı və cihaz sınağı aparılmayıb. Mühərrik seçimi Unity LTS + C# kimi qeyd olunub. Növbəti ayrıca tapşırıqda Wobbrik adapterinin vahid wallet/bazar sərhədini layihələndirmək olar; hələ kod yazılmır.


### Son dəqiqləşdirmə — Unity Personal, Arrowz və Wobbrik adapteri

3 noyabr 2026: istifadəçinin qərarları yeniləmə göstərişi əsasında Unity LTS + C# seçimi və **AppBackend-də ayrıca Wobbrik adapterinin olması** qeydə alınıb. Bu adapterin tam bazar/wallet implementasiyası hazır sayılmır; pul üçün vahid əsas mənbənin seçimi ayrıca layihələndiriləcək. Ortaq servisin digər tətbiqlərinin qaydaları Wobbrik-ə avtomatik tətbiq edilmir. Firebase Spark qalır.

Unity Personal pulsuzdur və maliyyə uyğunluğu olduqda kommersiya Android oyunu hazırlayıb yayımlamaq üçün istifadə edilə bilər. 3 noyabr 2026-da rəsmi səhifələrdə son 12 ay üçün 200.000 ABŞ dolları həddi göstərilir. Fərdi, üçüncü tərəfə xidmət göstərməyən istifadəçi üçün Unity istifadəsi ilə bağlı gəlir; hüquqi şəxs üçün ümumi gəlir və maliyyələşmə nəzərə alınır. Bu, yalnız Wobbrik-in gəlirinə qoyulan limit deyil. Hədd aşıldıqda uyğun pullu plana keçmək lazımdır. İstifadəçinin maliyyə məlumatı yoxlanmayıb; pulsuz uyğunluq şərtsiz təsdiqlənmir. C# üçün ayrıca Unity dil haqqı yoxdur. Google Play, backend hosting və seçilən pullu resurs/xidmət xərcləri Unity Personal lisenziyasından ayrıdır.

Mənbələr: https://unity.com/products/unity-personal və https://unity.com/pages/license-compliance .

İstifadəçinin «Arrows» dediyi layihə My Workspace/arrowz kimi yoxlanıb: README.md, engine/build.gradle.kts, feature/game/build.gradle.kts və feature/game/src/main/kotlin/com/aladdin/arrowz/feature/game/BoardCanvas.kt. **Arrowz native Android / Kotlin + Jetpack Compose** üzərindədir. Oyun lövhəsi Compose Canvas ilə çəkilir; engine modulu layihənin öz Kotlin/JVM məntiq moduludur, Unity/Godot mühərriki deyil. Bu texnologiya müşahidəsidir, Wobbrik seçimini dəyişmir. Arrowz və AppBackend faylları dəyişdirilməyib.


### Platforma dəqiqləşdirməsi — yalnız Android

İstifadəçinin son qərarı: **iOS versiyası olmayacaq; Wobbrik yalnız Android üçündür.** Bu qərar əvvəlki «iOS sonrakı buraxılışda» planını əvəz edir.

Android ilə məhdudlaşma və Arrowz-un mövcud Kotlin/Compose təcrübəsi nəzərə alınaraq agentin yenilənmiş tövsiyəsi: 2D/2.5D təqdimat üçün Kotlin + Jetpack Compose Canvas və ayrıca Kotlin oyun/tarazlıq modulu. Bu tövsiyə hələ istifadəçi tərəfindən texnologiya dəyişikliyi kimi təsdiqlənməyib; əvvəl qeyd edilən Unity LTS + C# seçimi avtomatik dəyişdirilmir. Oyun dövrü, kamera, animasiya və cihaz performansı ayrıca prototip yoxlaması tələb edəcək. AppBackend-də ayrıca Wobbrik adapteri və Firebase Spark qərarları saxlanılır. Kod yazılmır.


### Son təsdiq — Unity ilə davam

İstifadəçi «Unity istifadə edək» deyərək **Unity LTS + C#** seçimini açıq təsdiqlədi. Android-only qərarı saxlanılır. Kotlin + Compose-a keçid tövsiyəsi seçilmədi. Dəqiq LTS versiyası və mobil SDK uyğunluğu sonra yoxlanacaq. İstifadəçi Unity hesabı üçün heselo.online@gmail.com Chrome profilindən istifadəni göstərib; bu, yeni hesab yaratmaq, şərtlər qəbul etmək, pullu abunə və ya oyun koduna başlamaq icazəsi deyil. Unity Personal uyğunluğu və aktivləşmə ayrıca yoxlanmalıdır. PNG nümunələri təsdiqlənmiş UI və mühərrik renderi deyil.


### Unity Personal yoxlamasının nəticəsi

3 noyabr 2026: Heselo hesabının Unity Cloud My Products → Subscriptions səhifəsi yükləndi və «No results» göstərdi. Bu cədvəl Unity Personal-ın cihazda aktiv olub-olmadığını müəyyən etmir. /Applications və ~/Applications qovluqlarında Unity Hub/Editor tapılmadı; başqa yerdə quraşdırılma araşdırılmayıb.

Rəsmi https://docs.unity.com/en-us/hub/manage-license qaydasına görə pulsuz davam üçün Unity Hub quraşdırılıb həmin hesabla giriş edilməli, Settings → Licenses bölməsində Personal aktivliyi yoxlanmalıdır. Avtomatik aktivləşməzsə Add license → Get a free personal license → şərtləri oxuma → Agree and get personal edition license axını var. Maliyyə uyğunluğu saxlanmalıdır; şəxsi gəlir/maliyyələşmə yoxlanmayıb. Sonra SDK-larla uyğun LTS və Android Build Support (SDK/NDK/OpenJDK) seçilməlidir. Bunlar növbəti hazırlıq addımlarıdır, bu yoxlamada quraşdırma, aktivləşmə, şərt qəbulu, ödəniş və kod yazılması aparılmayıb.


### Cari son qərar — Unity əvəzinə Kotlin + Compose

İstifadəçi «Unity yox, digərini istifadə edərik. Unity bizi ləngidəcək» deyərək əvvəlki Unity LTS + C# seçimini ləğv etdi. **Wobbrik Arrowz kimi native Android / Kotlin + Jetpack Compose ilə hazırlanacaq; oyun səhnəsi üçün Compose Canvas, məntiq üçün ayrıca Kotlin modulu istiqaməti seçildi.** Yalnız Android qərarı qüvvədədir.

Unity Hub/Editor quraşdırılması, Personal aktivləşməsi və Unity hesabı ilə davam artıq qalan iş deyil. Hazırkı qərar texnologiya seçimidir; fizika modeli, oyun dövrü, kamera və performans hədləri ayrıca layihələndirilib prototipdə yoxlanacaq. Mövcud Arrowz kodunun birbaşa köçürülməsi və PNG görünüşünün təsdiqi fərz edilmir. Firebase Spark, AppBackend-də ayrıca Wobbrik adapteri və təsdiqlənmiş məzmun kataloqu saxlanılır. Pul üçün vahid əsas mənbə və bazar adapterinin sərhədi açıqdır. Kod yazılmasına ayrıca tapşırıq verilməyib.



---

_kotlin-compose-plan.md-dən:_

## 6. Mərhələlər və keçid şərtləri

1. **Müqavilə və skelet.** Təsdiqlənmiş authority/vaxt qərarları əsasında oyun vəziyyətləri və API/DB invariantlarını yazmaq. Sonrakı icazə ilə app + engine skeleti; Arrowz konfiqurasiyasını götürmədən build. Çıxış: həll olunmamış məhsul qərarları ayrılıb, minimum debug/release build yolu məlumdur.
2. **Yekun oyunun əsas tikinti dövrü.** Kran, eniş, tarazlıq, xilasetmə, kamera, fasilə/bərpa yekun tətbiq arxitekturasında və təsdiqlənmiş dizaynla implementasiya edilir; ayrıca atılacaq məşq/prototip tətbiqi qurulmur. Mağaza/reklam sonrakı mərhələdə qoşulur. Müxtəlif frame sürətlərində eyni scripted input və uzun cəhd yoxlanır. Çıxış: oyun hissi qəbul edilir; 60 Hz-də ilkin 60 FPS hədəfi real zəif/orta Android-də release tipli build ilə ölçülür. P95/P99 frame vaxtı, jank, RAM və startup qeyd olunur; hazır performans zəmanəti yoxdur.
3. **Onlayn cəhd və progression.** Firebase kimlik, AppBackend checkpoint/dedup, server XP/sikkə/nailiyyət, Room keş/outbox. Çıxış: cavab itkisi, proses ölümü, şəbəkə fasiləsi və paralel cihazda təkrar mükafat yoxdur; bərpa son server qeydinə uyğundur.
4. **Kolleksiya və şəhər.** 10 mərtəbə bina, 68 hissə kataloqu, 12 dekorasiya, yerləşdirmə və beş ərazi; inventar səhifələnir. Çıxış: fizika ilə kosmetika ayrıdır, alış/dekor nüsxələri serverdə ardıcıldır, aktiv elan redaktə qaydası qorunur.
5. **Bazar və leaderboard.** Atomik mübadilə, 5 elan, həftə vaxtı və sıralama. Çıxış: iki alıcı, çatışmayan balans, təkrar sorğu, redaktə/silmə yarışı və commit sonrası timeout real PostgreSQL-də sınaqdan keçir.
6. **Reklam və real alış.** Consent, test reklamları, davam/hissə progress, identity → Play → verify → server hüquq axını. Çıxış: pending, restore, interruption, ack/consume retry və hesab dəyişməsi sandbox cihaz sınağından keçir. Vahid wallet tamamlanmadan production IAP açılmır.
7. **Vizual tamamlama və buraxılış hazırlığı.** Tokenlər, qrafika, səs, haptic, uzun mətn/ekran ölçüləri; render və baxış; balans ölçməsi. Çıxış: seçilmiş cihaz yoxlamaları, load/quota müşahidəsi, DB backup/restore, failed-job alert və qalan risklərin faktiki siyahısı. Məhdud qoruma ilə buraxılış seçimi sınaqları ləğv etmir.

Mərhələlər scope-u kiçiltmir: bütün V1 funksiyaları hədəfdə qalır. Təqvim və vaxt qənaəti faizi ölçü olmadan vəd edilmir. Hər mərhələnin implementasiyası ayrıca tapşırıqla başlayır.

