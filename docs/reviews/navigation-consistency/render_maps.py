"""Code-derived navigation diagrams, not Android screenshots. Pillow is QA/output only."""
from pathlib import Path
import re, math
from PIL import Image, ImageDraw, ImageFont
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
css=(ROOT/'docs/design/mobile-v1/tokens.css').read_text()
C={k:v for k,v in re.findall(r'--([\w-]+):\s*(#[\da-fA-F]+)',css)}
FONT='/System/Library/Fonts/Supplemental/Arial.ttf'
def f(size): return ImageFont.truetype(FONT,size)
def canvas(title,subtitle):
    im=Image.new('RGB',(2000,1500),C['background']); d=ImageDraw.Draw(im)
    d.text((64,45),title,font=f(46),fill=C['text'])
    d.text((64,112),subtitle,font=f(25),fill=C['muted'])
    d.text((64,1445),'Mənbə: Android mənbə kodu · 13 noyabr 2026 · Runtime/render yoxlanmayıb',font=f(23),fill=C['muted'])
    return im,d
def box(d,rect,title,lines,accent=False):
    x,y,r,b=rect
    d.rounded_rectangle(rect,24,fill=C['surface'],outline=C['accent'] if accent else C['surface-raised'],width=3)
    d.text((x+24,y+20),title,font=f(32),fill=C['accent'] if accent else C['text'])
    for i,line in enumerate(lines):d.text((x+24,y+73+i*34),line,font=f(24),fill=C['muted'])
def arrow(d,points,label,xy,danger=False):
    color=C['danger'] if danger else C['accent']
    d.line(points,fill=color,width=4)
    a,b=points[-2:]; ang=math.atan2(b[1]-a[1],b[0]-a[0])
    d.polygon([b,(b[0]-18*math.cos(ang-.45),b[1]-18*math.sin(ang-.45)),(b[0]-18*math.cos(ang+.45),b[1]-18*math.sin(ang+.45))],fill=color)
    if not label: return
    bounds=d.textbbox(xy,label,font=f(25)); d.rectangle((bounds[0]-8,bounds[1]-5,bounds[2]+8,bounds[3]+5),fill=C['background'])
    d.text(xy,label,font=f(25),fill=color)
im,d=canvas('01 / Səhifələr və düymə keçidləri','Düzəlişdən sonrakı marşrut xəritəsi. Ekran dizaynı və ya tətbiq screenshot-u deyil.')
box(d,(64,205,520,385),'Google giriş',['Google ilə davam et','Uğur → hesab sinxronizasiyası'])
box(d,(700,205,1260,410),'Ana ekran  /  home',['Profil · Kolleksiya · Nailiyyətlər','Oyna / Cəhdə qayıt → xəritə 02'],True)
arrow(d,[(520,290),(700,290)],'Giriş uğurlu',(525,245))
box(d,(1440,205,1936,510),'Bağlı keçidlər',['Şəhərim · İnventar · Bazar','Rail-də düymələr deaktivdir.','Şəhərim düyməsi də deaktivdir.','Hazır mobil səhifə sayılmır.'])
box(d,(64,650,620,865),'Hesab  /  account',['Profil düyməsi və ya yan menyu','Oyna · Kolleksiya · Nailiyyətlər','Ana ekran / Android Geri'])
box(d,(720,650,1276,865),'Kolleksiya  /  collection',['Ana ekran, Hesab və yan menyu','Hələ ümumi hesab şablonudur.','Açılmış mükafat sayı göstərilir.'])
box(d,(1380,650,1936,865),'Nailiyyətlər  /  achievements',['Ana ekran, Hesab və yan menyu','Hələ ümumi hesab şablonudur.','Açılmış nailiyyət sayı göstərilir.'])
arrow(d,[(790,410),(790,480),(340,480),(340,650)],'Profil / Hesab',(375,438))
arrow(d,[(1000,410),(1000,650)],'Kolleksiya',(1018,520))
arrow(d,[(1170,410),(1170,545),(1655,545),(1655,650)],'Nailiyyətlər',(1415,575))
arrow(d,[(620,730),(720,730)],'',(625,680))
arrow(d,[(620,825),(655,825),(655,940),(1655,940),(1655,865)],'Hesab və Kolleksiya → Nailiyyətlər',(775,908))
arrow(d,[(1380,790),(1276,790)],'',(1248,832))
arrow(d,[(340,865),(340,1020),(32,1020),(32,175),(980,175),(980,205)],'Ana ekran / Android Geri',(67,1070))
arrow(d,[(1000,865),(1000,940)],'',(1000,900))
# Explicit return note avoids many overlapping edges.
box(d,(850,1010,1936,1175),'Bütün əlavə səhifələrdən geri',['Ana ekran düyməsi və Android Geri → yuxarıdakı eyni home.','Profil → Hesab; əlavə səhifələrdə Kolleksiya / Nailiyyətlər → eyni adlı səhifə.'])
box(d,(64,1215,1936,1395),'Bərpa / xəta keçidləri',['Yenilə → eyni səhifədə hesabı yenidən yoxlayır; yeni səhifə açmır.','Journal xətası → ayrıca bərpa ekranı; “Ana ekrana qayıt” → eyni home (Oyna bağlı).','Telefon portretində düymə, geniş/qısa pəncərədə rail girişləri eyni marşrutları çağırır.'])
im.save(HERE/'01-pages.png')
im,d=canvas('02 / Oyun, pauza və nəticə keçidləri','Düzəlişdən sonrakı axın · 92d640d checkpoint fix-i üzərində ayrıca navigation branch-i')
box(d,(64,205,560,420),'Ana ekran / Hesab',['Oyna → start (şəbəkə)','Köhnə açıq cəhd varsa:','abandon → start'])
box(d,(760,205,1270,420),'Oyun — pauza paneli',['Yeni cəhd açıq pauza ilə gəlir.','Davam et → fizika başlayır.','Avtomatik resume yoxdur.'])
box(d,(1450,205,1936,420),'Aktiv oyun',['Səhnəyə toxun → mərtəbə burax','Uğurlu eniş → eyni oyun','Hər enişdə sorğu yoxdur.'],True)
arrow(d,[(560,300),(760,300)],'Start qəbul',(575,255))
arrow(d,[(1270,285),(1450,285)],'Davam et',(1288,240))
arrow(d,[(1680,420),(1680,475),(1000,475),(1000,420)],'Pauza düyməsi / Android Geri → pauza',(1215,485))
box(d,(64,660,560,880),'Eyni Ana ekran',['Menyuya qayıt → home','Açıq cəhd → abandon','Nəticə/bina yaradılmır.'],True)
arrow(d,[(760,365),(650,365),(650,600),(310,600),(310,660)],'Menyuya qayıt',(70,550))
box(d,(760,660,1270,910),'Kosmetik seçim / nəticə',['Məğlubiyyət → yekun checkpoint','0 mərtəbə: checkpoint yoxdur.','Kateqoriya / hissə → eyni ekran','Geri → home; seçim niyyəti qalır.'])
arrow(d,[(1680,420),(1870,420),(1870,580),(1020,580),(1020,660)],'Məğlubiyyət',(1420,610))
arrow(d,[(760,815),(560,815)],'Geri',(615,770))
box(d,(1450,660,1936,910),'Serverdə tamamlama',['Təsdiqlə → finish','Qəbul → eyni Ana ekran','Mükafat/bina serverdədir.','Səhv → Yenilə / eyni retry'])
arrow(d,[(1270,760),(1450,760)],'Təsdiqlə',(1278,715))
arrow(d,[(1680,910),(1680,1010),(310,1010),(310,880)],'Qəbul edilmiş finish → kanonik home',(735,970))
box(d,(64,1090,1936,1380),'Tapılan problemlər və bu branch-in sərhədi',['DÜZƏLDİLDİ: finish-dən sonra game marşrutu ümumi hesab şablonuna düşürdü — ikinci “ana ekran”.','DÜZƏLDİLDİ: Android Geri → pauza; 13 noyabr son istifadəçi qərarıdır.','DÜZƏLDİLDİ: əlavə səhifələrin lokal scroll/presentation yaddaşı ortaq idi — indi marşruta görə ayrılır.','QALIR: Kolleksiya/Nailiyyətlər ümumi şablondur; ayrıca vizual tamamlama bu bug fix deyil.','QALIR: təsdiqli pauza dizaynındakı “Yenidən başla” online paneldə yoxdur; ayrıca kiçik vahiddir.','Qeyd: bağlantı/bərpa zamanı game üçün köhnə şablon əvəzinə mövcud home statusu göstərilir.'])
im.save(HERE/'02-game.png')
