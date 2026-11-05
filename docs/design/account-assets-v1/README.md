# Hesab və ana ekran asset hazırlığı — 5 noyabr 2026

İstifadəçi çatışmayan asset-lərin təsdiqlənmiş dizayna uyğun ayrıca hazırlanmasını
seçdi. Built-in `image_gen` istifadə edilib; CLI/API fallback yoxdur.

- `home-city.png`: mobile-primary-v2.png-in soldakı ana ekran şəhəri.
- `wobbrik-logo.png`: həmin ana ekranın logo referansı.
- `account-hero.png`: account-states.png-in soldakı hesab illüstrasiyası.

Hər üç PNG alpha kanallıdır. Hesab hero-sunun ilkin önizləməsində rəngli halo
görüldüyü üçün yalnız fon təmizləmə redaktəsi istəndi. Sonra AppKit ilə hər üçü
GameTokens.Background (#061831) üzərində render edildi və görüntüyə baxıldı:
fon şəffafdır, halo görünmür. `alpha-preview.png` bu QA renderidir, ekran deyil.
Təkrarlama: `swift docs/design/account-assets-v1/render-preview.swift
docs/design/account-assets-v1/home-city.png docs/design/account-assets-v1/wobbrik-logo.png
docs/design/account-assets-v1/account-hero.png` — nəticə `/tmp/wobbrik-asset-qa.png`.
Promptlar `prompts.md`-dədir. Orijinal konsept PNG-ləri dəyişdirilmir.

Bu asset-lər hələ Android resurslarına və ekranlarına bağlanmayıb. Generativ
extraction orijinal piksellərin kəsilməsi deyil: şəhərin detallarında və logo
hərflərində fərqlər ola bilər. Təsdiqlənmiş mobile-v1/tablet-v1 dizaynları əsas
qalır; birəbir uyğun ekran tamamlandığı iddia edilmir. UI ikonları və dekorativ
elementlər növbəti ekran vahidində həmin dizaynlarla yoxlanmalıdır.
