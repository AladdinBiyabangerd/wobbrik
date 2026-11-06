"""Asset-only QA, not a Compose screenshot. Run with uv run --with pillow."""
from pathlib import Path
import re
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[3]
tokens = (root / 'docs/design/mobile-v1/tokens.css').read_text()
def token(name):
    return re.search(r'--' + name + r':\s*(#[0-9a-f]+)', tokens)[1]

resources = root / 'android-billing/app/src/main/res/drawable-nodpi'
names = ['home', 'city', 'inventory', 'market']
for width, height, label in [(390, 844, 'phone'), (1280, 800, 'tablet')]:
    canvas = Image.new('RGBA', (width, height), token('background'))
    draw = ImageDraw.Draw(canvas)
    draw.text((24, 24), 'Asset QA / ' + label + ' / not an app screenshot', fill=token('text'))
    for row, name in enumerate(names):
        image = Image.open(resources / ('ui_' + name + '.png')).convert('RGBA')
        assert image.getextrema()[3] == (0, 255), name
        y = 100 + row * 160
        draw.text((24, y - 24), name + ' / 24, 32, 48, 96 px', fill=token('muted'))
        for x, size in zip([24, 80, 144, 224], [24, 32, 48, 96]):
            canvas.alpha_composite(image.resize((size, size), Image.Resampling.LANCZOS), (x, y))
    canvas.convert('RGB').save(Path(__file__).with_name('preview-' + label + '.png'))
