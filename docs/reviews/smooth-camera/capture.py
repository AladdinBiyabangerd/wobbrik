import importlib.util
from pathlib import Path
import sys
sys.dont_write_bytecode = True
root = Path(__file__).resolve().parents[3]
spec = importlib.util.spec_from_file_location('capture', root / 'scripts/capture-game-scene.py')
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)
c.OUT = Path(__file__).resolve().parent
original = c.adb
ticks = 0
def adb(*args):
    if args[:3] == ('shell', 'am', 'start'):
        args += ('--ei', 'camera_from', '0', '--ei', 'ticks', str(ticks))
    return original(*args)
c.adb = adb
size = c.adb('shell','wm','size').decode()
density = c.adb('shell','wm','density').decode()
def override(s):
    return s.split('Override ',1)[1].split(': ',1)[1].strip() if 'Override ' in s else 'reset'
try:
    for name, pixels, dpi in [('phone','1080x2400','420'),('tablet','1920x1200','240')]:
        c.adb('shell','wm','size',pixels)
        c.adb('shell','wm','density',dpi)
        for ticks in (0, 6, 30):
            c.launch(f'{name}-{ticks}',floors=12,drift=8)
finally:
    c.adb('shell','wm','size',override(size))
    c.adb('shell','wm','density',override(density))
