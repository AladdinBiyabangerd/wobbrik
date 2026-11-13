#!/usr/bin/env python3
"""Read-only purchase crop QA. Needs Pillow; never alters source pixels or metadata."""
import hashlib
import json
import re
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'android-billing/app/src/main/assets/city'
OUT = ROOT / 'docs/reviews/city-purchases-visual'
TOKENS = (ROOT / 'android-billing/app/src/main/kotlin/com/aladdin/wobbrik/GameTokens.kt').read_text()
COLORS = dict(re.findall(r'val (\w+) = Color\(0xFF([0-9A-F]{6})\)', TOKENS))
metadata = json.loads((ART / 'purchase-art.json').read_text())
source = ART / metadata['source']
im = Image.open(source).convert('RGBA')
# Fixed inspection regions keep neighbouring sprites out; these are artwork, not geometry.
regions = [(0,0,300,380),(300,0,645,380),(645,0,1086,390),
           (0,410,300,730),(300,410,645,730),(645,390,1086,740),
           (0,770,300,1050),(300,770,620,1050),(620,770,1086,1050),
           (0,1160,310,1340),(310,1050,615,1420),(615,1160,1086,1340)]
report = {'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
          'threshold': 8, 'note': 'Source/crop QA only; no collision or runtime proof', 'sprites': []}
preview = Image.new('RGB', (960, 4*280))
draw = ImageDraw.Draw(preview)
for i, (entry, region) in enumerate(zip(metadata['sprites'], regions, strict=True)):
    x,y,w,h = (entry[k] for k in ['x','y','width','height'])
    assert region[0] <= x < x+w <= region[2] and region[1] <= y < y+h <= region[3]
    bounds = im.crop(region).getchannel('A').point(lambda a: 255 if a >= 8 else 0).getbbox()
    assert x <= region[0]+bounds[0] and y <= region[1]+bounds[1]
    assert x+w >= region[0]+bounds[2] and y+h >= region[1]+bounds[3]
    crop = im.crop((x,y,x+w,y+h))
    # Production's no-upsample rule, at a card-size preview; bright/dark twins expose matte fringes.
    scale = min(1, 144/w, 230/h)
    thumb = crop.resize((round(w*scale), round(h*scale)), Image.Resampling.LANCZOS)
    for bg_index, token in enumerate(['Surface','Text']):
        left = (i % 3)*320 + bg_index*160; top = (i//3)*280
        tile = Image.new('RGBA', (160,280), '#'+COLORS[token])
        tile.alpha_composite(thumb, ((160-thumb.width)//2, 20+(230-thumb.height)//2))
        preview.paste(tile.convert('RGB'), (left,top))
        draw.text((left+4,top+260),entry['id'].removeprefix('decor_'), fill='#'+COLORS['Text' if bg_index==0 else 'Background'])
    report['sprites'].append(dict(id=entry['id'], crop=[x,y,w,h],
        body_bounds=[region[0]+bounds[0],region[1]+bounds[1],region[0]+bounds[2],region[1]+bounds[3]],
        max_dp_at_density={str(d): [round(w/d,2),round(h/d,2)] for d in [1,2,3,4]},
        crop_contains_alpha8_body=True))
# The separately approved land cutout must retain real transparency and fit its native pixels.
land = metadata['land']
land_source = ART / land['source']
land_image = Image.open(land_source).convert('RGBA')
x,y,w,h = (land[k] for k in ['x','y','width','height'])
assert 0 <= x < x+w <= land_image.width and 0 <= y < y+h <= land_image.height
alpha = land_image.getchannel('A')
assert alpha.histogram()[0] > 0
bounds = alpha.point(lambda a: 255 if a >= 8 else 0).getbbox()
assert x <= bounds[0] and y <= bounds[1] and x+w >= bounds[2] and y+h >= bounds[3]
land_preview = Image.new('RGB', (600,300))
crop = land_image.crop((x,y,x+w,y+h))
crop.thumbnail((288,288), Image.Resampling.LANCZOS)
for i, token in enumerate(['Surface','Text']):
    tile = Image.new('RGBA', (300,300), '#'+COLORS[token])
    tile.alpha_composite(crop, ((300-crop.width)//2,(300-crop.height)//2))
    land_preview.paste(tile.convert('RGB'), (i*300,0))
report['land'] = dict(source_sha256=hashlib.sha256(land_source.read_bytes()).hexdigest(),
    crop=[x,y,w,h], transparent_pixels=alpha.histogram()[0], crop_contains_alpha8_body=True,
    max_dp_at_density={str(d): [round(w/d,2),round(h/d,2)] for d in [1,2,3,4]})
OUT.mkdir(parents=True,exist_ok=True)
preview.save(OUT/'decor-crops-dark-light.png')
land_preview.save(OUT/'land-cutout-dark-light.png')
(OUT/'art-report.json').write_text(json.dumps(report,indent=2)+'\n')
print('PASS: 12 decor crops + transparent land contain alpha>=8 bodies; originals unchanged; native-pixel caps recorded.')
