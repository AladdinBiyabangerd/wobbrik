"""Run from repository root after installing the debug APK. Restores device overrides."""
import sys
sys.dont_write_bytecode = True
import importlib.util
from pathlib import Path

root = Path(__file__).resolve().parents[3]
spec = importlib.util.spec_from_file_location('scene_capture', root / 'scripts/capture-game-scene.py')
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)
c.OUT = Path(__file__).resolve().parent
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
        for suffix, drift in [('left', -8), ('center', 0), ('right', 8)]:
            c.launch(name + '-' + suffix, floors=12, drift=drift)
        c.launch(name + '-pause', floors=12, drift=8, paused=True)
        c.launch(name + '-tall', floors=40, drift=-3, crane=120)
finally:
    c.adb('shell', 'wm', 'size', override(size))
    c.adb('shell', 'wm', 'density', override(density))
    for key, value in originals.items():
        if value == 'null':
            c.adb('shell', 'settings', 'delete', 'system', key)
        else:
            c.adb('shell', 'settings', 'put', 'system', key, value)
