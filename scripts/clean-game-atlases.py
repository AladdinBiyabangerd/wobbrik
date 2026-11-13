#!/usr/bin/env python3
"""Approved exterior-only contour masks; never repaint sprite interiors.
Run with Pillow. Sources remain intact. Coordinates are measured source pixels,
not UI dimensions or authoritative geometry. All six sprites keep shared cells.
"""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageChops
ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'docs/design/game-tap-v2/atlas-v3-proposal'
OUT = ROOT / 'android-billing/app/src/main/assets/game-v3'
# Physical contours, clockwise, on original 1536 x 1024 sheets.
FLOOR = [(47,219),(169,148),(186,148),(494,163),(499,167),(499,187),
 (492,195),(492,323),(498,325),(501,330),(500,347),(385,425),
 (378,427),(47,408),(44,404),(44,384),(54,375),(54,249),(48,246)]
ENTRY = [(x+494,y) for x,y in FLOOR]
ROOF = [(1037,352),(1176,268),(1185,267),(1495,283),(1500,287),
 (1499,307),(1380,405),(1372,408),(1038,389),(1034,385),(1034,358)]
LAMP = [(1224,645),(1226,630),(1236,615),(1251,607),(1268,607),
 (1285,614),(1298,629),(1307,651),(1321,652),(1336,663),(1349,680),
 (1357,703),(1358,725),(1350,743),(1335,752),(1310,754),(1304,827),
 (1293,841),(1296,854),(1280,861),(1273,875),(1263,881),(1253,881),
 (1243,872),(1241,864),(1186,849),(1184,840),(1193,831),(1186,747),
 (1167,738),(1165,727),(1202,696),(1201,687),(1229,675),(1232,663)]
CELLS = {'floor':[40,144,464,288], 'ground':[534,144,464,288],
 'roof':[1030,264,474,148], 'window':[132,615,280,282],
 'entrance':[596,560,340,365], 'lighting':[1160,602,204,284]}
OUT.mkdir(parents=True, exist_ok=True)
qa=[]
for style in ('purple','teal'):
    source=Image.open(SOURCE/f'{style}.png').convert('RGBA')
    assert source.size==(1536,1024)
    mask=Image.new('L',(1536*4,1024*4))
    draw=ImageDraw.Draw(mask)
    for poly in (FLOOR,ENTRY,ROOF,LAMP): draw.polygon([(x*4,y*4) for x,y in poly],fill=255)
    draw.rounded_rectangle((135*4,619*4,407*4,893*4),radius=23*4,fill=255)
    # Arch and straight jambs of isolated doorway, same contour for both palettes.
    draw.ellipse((600*4,563*4,930*4,893*4),fill=255)
    draw.rectangle((600*4,730*4,930*4,919*4),fill=255)
    mask=mask.resize(source.size,Image.Resampling.LANCZOS)
    # Eliminate resampling specks outside the antialiased contour.
    mask=mask.point(lambda a: 0 if a<8 else a)
    result=source.copy(); result.putalpha(ImageChops.multiply(source.getchannel('A'),mask))
    result.save(OUT/f'{style}.png',optimize=True)
    assert result.getchannel('A').getpixel((0,0))==0
    # RGB is byte-for-byte source, even outside the mask; only alpha changes.
    assert result.convert('RGB').tobytes()==source.convert('RGB').tobytes()
    qa.append({'style':style,'bounds':result.getbbox(),'rgb_preserved':True})
    # QA backdrop only, not a production asset.
    previews=[]
    for bg in ('#121a33','#edf0f5'):
        canvas=Image.new('RGBA',source.size,bg);canvas.alpha_composite(result)
        previews.append(canvas.convert('RGB').resize((768,512)))
    sheet=Image.new('RGB',(768,1024));sheet.paste(previews[0]);sheet.paste(previews[1],(0,512))
    dest=ROOT/'docs/reviews/game-joins-visual';dest.mkdir(exist_ok=True)
    sheet.save(dest/f'{style}-alpha-qa.png')
metadata={'version':1,'source_size':[1536,1024],'cells':CELLS,
          'floor_pitch_px':176,'floor_depth_px':112,
          'top_anchors_px':[[8,90],[139,16],[450,30],[342,105]],
          'notes':'Complete composites: never add duplicate window/entrance/light overlays. Old catalog remains for mixed selections.'}
(OUT/'atlas.json').write_text(json.dumps(metadata,indent=2)+'\n')
print(json.dumps(qa,indent=2))
