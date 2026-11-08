#!/usr/bin/env python3
"""Reapply the reviewed alpha mask without changing the approved board's RGB pixels.

Run from any directory: uv run --with pillow python <this-file>
The saved mask was produced locally with GrabCut and visually reviewed on dark/light
backgrounds, after explicit user authorization for pixel-preserving Python masking.
No generative output is used. OpenCV is not needed to reproduce the final asset.
"""
from hashlib import sha256
from pathlib import Path
import shutil
from PIL import Image, ImageChops

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
SOURCE = HERE.parent / 'mobile-v1/mobile-primary-v2.png'
SOURCE_SHA256 = 'a5ec0d5e34b17119c00726f406aeb241ae2b5630d79b5be61e31c80d66675553'
CROP = (20, 154, 435, 605)  # Leftmost home illustration, exclusive right/bottom.


def main():
    if sha256(SOURCE.read_bytes()).hexdigest() != SOURCE_SHA256:
        raise SystemExit('Approved source changed; review the crop and alpha mask first.')
    rgb = Image.open(SOURCE).convert('RGB').crop(CROP)
    mask = Image.open(HERE / 'home-city-cutout-v2-mask.png').convert('L')
    if mask.size != rgb.size:
        raise SystemExit('Mask dimensions do not match the approved crop.')
    result = rgb.convert('RGBA')
    result.putalpha(mask)
    if ImageChops.difference(rgb, result.convert('RGB')).getbbox() is not None:
        raise SystemExit('RGB pixels changed.')
    output = HERE / 'home-city-cutout-v2.png'
    result.save(output)
    runtime = ROOT / 'android-billing/app/src/main/res/drawable-nodpi/home_city_cutout_v2.png'
    shutil.copyfile(output, runtime)
    print(f'{result.width}×{result.height}; RGB difference zero; alpha {mask.getextrema()}')


if __name__ == '__main__':
    main()
