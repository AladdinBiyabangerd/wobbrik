"""Read-only source QA; writes reports/composites, never changes source art.

Run with Pillow: python check_rasters.py
Bounds use alpha >= 8 to exclude almost invisible generated matte noise.
These are source-art bounds, NOT collision polygons or calibrated ground anchors.
"""
import hashlib
import json
import re
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
TOKENS = (ROOT.parent / 'mobile-v1/tokens.css').read_text()
COLORS = dict(re.findall(r'--([\w-]+):\s*(#[0-9a-fA-F]{6})', TOKENS))


def inspect(path, regions):
    im = Image.open(path).convert('RGBA')
    a = im.getchannel('A')
    hist = a.histogram()
    result = dict(file=str(path.relative_to(ROOT)), size=list(im.size),
                  sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
                  alpha_zero=hist[0], alpha_1_to_7=sum(hist[1:8]),
                  alpha_8_to_249=sum(hist[8:250]), alpha_250_to_255=sum(hist[250:]),
                  sprites=[])
    for name, rect in regions:
        mask = a.crop(rect).point(lambda x: 255 if x >= 8 else 0)
        b = mask.getbbox()
        if not b:
            raise ValueError(name)
        bounds = [rect[0]+b[0], rect[1]+b[1], rect[0]+b[2], rect[1]+b[3]]
        w, h = b[2]-b[0], b[3]-b[1]
        result['sprites'].append(dict(id=name, inspection_region=list(rect),
            visible_bounds_alpha8=bounds, native_size=[w, h],
            max_dp_at_density={str(d): [round(w/d, 1), round(h/d, 1)] for d in [1, 2, 3, 4]},
            ground_anchor=None, runtime_ready=False))
    # Two identical pixel-scale composites reveal both dark and light matte fringes.
    preview = Image.new('RGB', (im.width*2, im.height), COLORS['background'])
    for n, color in enumerate([COLORS['background'], COLORS['text']]):
        bg = Image.new('RGBA', im.size, color)
        bg.alpha_composite(im)
        preview.paste(bg.convert('RGB'), (n*im.width, 0))
    preview.save(ROOT / 'qa' / (path.stem + '-dark-light.png'))
    return result


styles = ['old_town', 'brick', 'coast', 'modern', 'garden', 'workshop']
building_regions = [(name, ((i%3)*512, (i//3)*512, (i%3+1)*512, (i//3+1)*512))
                    for i, name in enumerate(styles)]
# Atlas is optically laid out, NOT an equal-cell sheet: long road crosses nominal cells.
plan_regions = [
    (0, 0, 300, 380), (300, 0, 660, 380), (660, 0, 1086, 380),
    (0, 380, 300, 760), (300, 380, 650, 760), (650, 380, 1086, 760),
    (0, 760, 300, 1050), (300, 760, 630, 1050), (630, 760, 1086, 1050),
    (0, 1050, 300, 1448), (300, 1050, 580, 1448), (580, 1050, 1086, 1448)]
ids = [x['id'] for x in json.loads((ROOT/'assets/catalog-map.json').read_text())['items']]
iso_regions = plan_regions.copy()
iso_regions[10] = (300, 1050, 610, 1448)
iso_regions[11] = (610, 1050, 1086, 1448)
report = dict(status='candidate QA; alpha8 bounds exclude low-alpha noise; no runtime certification',
              atlases=[inspect(ROOT/'assets/starting-buildings-v2.png', building_regions),
                       inspect(ROOT/'assets/decor-plan-v2.png', list(zip(ids, plan_regions))),
                       inspect(ROOT/'assets/decor-isometric-v2.png', list(zip(ids, iso_regions)))])
# Manually inspected inhabited-course centers, top to bottom; roof is excluded.
floor_centers = [[65,110,155,198,240,282,325,365,408,447],
                 [70,115,160,205,250,290,330,370,410,455],
                 [70,115,160,205,250,290,330,370,410,455],
                 [590,630,672,714,755,797,838,880,920,965],
                 [590,632,674,716,758,800,842,884,925,965],
                 [580,622,664,706,748,790,832,874,916,965]]
im = Image.open(ROOT/'assets/starting-buildings-v2.png').convert('RGBA')
bg = Image.new('RGBA', im.size, COLORS['background']); bg.alpha_composite(im)
draw = ImageDraw.Draw(bg)
for i, ys in enumerate(floor_centers):
    x = (i % 3 + 1)*512-50
    for level, y in zip(range(10, 0, -1), ys):
        draw.line((x-18,y,x-4,y), fill=COLORS['accent'], width=2)
        draw.text((x,y-6), str(level), fill=COLORS['text'])
    report['atlases'][0]['sprites'][i]['manual_floor_centers_y'] = ys
    report['atlases'][0]['sprites'][i]['manual_floor_count'] = 10
bg.save(ROOT/'qa/starting-buildings-v2-floor-count.png')
parts = ROOT/'02-parts-flow-v2.png'
if parts.exists():
    board = Image.open(parts).convert('RGBA')
    draw = ImageDraw.Draw(board)
    ys = [145,177,208,240,272,304,336,368,400,448]
    for level, y in zip(range(10, 0, -1), ys):
        draw.line((1580,y,1594,y), fill=COLORS['accent'], width=2)
        draw.text((1600,y-6), str(level), fill=COLORS['text'])
    board.save(ROOT/'qa/02-parts-flow-v2-floor-count.png')
    report['boards'] = [dict(file=parts.name, size=list(board.size),
        sha256=hashlib.sha256(parts.read_bytes()).hexdigest(),
        right_hero_manual_floor_centers_y=ys, manual_floor_count=10,
        roof_dormer_excluded=True, runtime_ready=False)]
(ROOT/'qa/raster-report.json').write_text(json.dumps(report, indent=2)+'\n')
print('Wrote raster-report.json, three native-size dark/light composites and floor-count overlay.')
