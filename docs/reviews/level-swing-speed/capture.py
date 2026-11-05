"""Run from repository root after installing the debug APK. Restores device overrides."""
import sys
import time
sys.dont_write_bytecode = True
import importlib.util
from pathlib import Path

root = Path(__file__).resolve().parents[3]
spec = importlib.util.spec_from_file_location('scene_capture', root / 'scripts/capture-game-scene.py')
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)
c.OUT = Path(__file__).resolve().parent
original_adb = c.adb
def sampled_adb(*args):
    if args[:3] == ('shell', 'am', 'start'):
        args += ('--ei', 'ticks', '30')
    return original_adb(*args)
c.adb = sampled_adb
originals = {key: c.setting(key) for key in ('font_scale', 'accelerometer_rotation', 'user_rotation')}
size = c.adb('shell', 'wm', 'size').decode()
density = c.adb('shell', 'wm', 'density').decode()
def override(value):
    return value.split('Override ', 1)[1].split(': ', 1)[1].strip() if 'Override ' in value else 'reset'
try:
    c.adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
    c.adb('shell', 'settings', 'put', 'system', 'user_rotation', '0')
    c.adb('shell', 'settings', 'put', 'system', 'font_scale', '1.0')
    for name, pixels, dpi in [('phone', '1080x2400', '420'), ('tablet', '1920x1200', '240')]:
        c.adb('shell', 'wm', 'size', pixels)
        c.adb('shell', 'wm', 'density', dpi)
        # Same 30-tick (0.5s) interval from the center of a fresh swing.
        for level in (1, 11, 26):
            c.launch(f'{name}-level-{level}', floors=level - 1)
            # Some resized emulator frames omitted cable strokes on first capture.
            # Take the settled frame too; final images still require visual inspection.
            time.sleep(3)
            (c.OUT / f'{name}-level-{level}.png').write_bytes(c.adb('exec-out', 'screencap', '-p'))

finally:
    c.adb('shell', 'wm', 'size', override(size))
    c.adb('shell', 'wm', 'density', override(density))
    for key, value in originals.items():
        if value == 'null':
            c.adb('shell', 'settings', 'delete', 'system', key)
        else:
            c.adb('shell', 'settings', 'put', 'system', key, value)
