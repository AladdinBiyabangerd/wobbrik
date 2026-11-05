#!/usr/bin/env python3
"""Capture debug presentation fixtures; these are not live Firebase/backend evidence."""
import os
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
ADB = str(Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'Library/Android/sdk'))) / 'platform-tools/adb')
OUT = ROOT / 'docs/reviews/online-screens'

def adb(*args):
    return subprocess.check_output([ADB, *args], timeout=45)

def override(value):
    return value.split('Override ', 1)[1].split(': ', 1)[1].strip() if 'Override ' in value else 'reset'

def main():
    size = adb('shell', 'wm', 'size').decode()
    density = adb('shell', 'wm', 'density').decode()
    font = adb('shell', 'settings', 'get', 'system', 'font_scale').decode().strip()
    OUT.mkdir(parents=True, exist_ok=True)
    try:
        for name, pixels, dpi, scale, page, error in (
            ('phone-home', '1080x2400', '420', '1.0', 'home', False),
            ('phone-account', '1080x2400', '420', '1.0', 'account', False),
            ('phone-error', '1080x2400', '420', '1.0', 'home', True),
            ('tablet-home', '1280x800', '160', '1.0', 'home', False),
            ('tablet-account', '1280x800', '160', '1.0', 'account', False),
            ('phone-game', '1080x2400', '420', '1.0', 'game', False),
            ('tablet-pause', '1280x800', '160', '1.0', 'pause', False),
            ('tablet-portrait', '800x1280', '160', '1.0', 'home', False),
            ('large-font', '900x450', '240', '1.5', 'account', False),
        ):
            adb('shell', 'wm', 'size', pixels)
            adb('shell', 'wm', 'density', dpi)
            adb('shell', 'settings', 'put', 'system', 'font_scale', scale)
            adb('shell', 'am', 'force-stop', 'com.aladdin.wobbrik')
            args = ['shell', 'am', 'start', '-W', '-n', 'com.aladdin.wobbrik/.AccountPreviewActivity', '--es', 'page', page]
            if error:
                args += ['--es', 'status', "'Bağlantı yoxdur'", '--ez', 'unavailable', 'true']
            launch = adb(*args).decode()
            if 'Error' in launch:
                raise RuntimeError(launch)
            time.sleep(3)
            (OUT / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))
            print(name, flush=True)
    finally:
        adb('shell', 'wm', 'size', override(size))
        adb('shell', 'wm', 'density', override(density))
        if font == 'null':
            adb('shell', 'settings', 'delete', 'system', 'font_scale')
        else:
            adb('shell', 'settings', 'put', 'system', 'font_scale', font)

if __name__ == '__main__':
    main()
