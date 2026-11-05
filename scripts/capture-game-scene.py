#!/usr/bin/env python3
"""Capture installed DEBUG fixture at representative window sizes; restore device settings."""
import os
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
ADB = str(Path(os.environ.get("ANDROID_HOME", str(Path.home() / "Library/Android/sdk"))) / "platform-tools/adb")
OUT = ROOT / "docs/reviews/game-scene-assets"

def adb(*args):
    return subprocess.check_output([ADB, *args])

def setting(key):
    return adb("shell", "settings", "get", "system", key).decode().strip()

def launch(name, paused=False, floors=4, crane=0, finished=False, drift=0):
    adb("shell", "am", "force-stop", "com.aladdin.wobbrik")
    adb("shell", "am", "start", "-W", "-n", "com.aladdin.wobbrik/.ScenePreviewActivity",
        "--ez", "paused", str(paused).lower(), "--ei", "floors", str(floors),
        "--ei", "crane", str(crane), "--ei", "drift", str(drift),
        "--ez", "finished", str(finished).lower())
    # Android may show a large stale task snapshot while a new fixture starts.
    # Match the debug-only semantic marker, not just screenshot size or a timer.
    marker = (f"Wobbrik fixture floors={floors} crane={crane} paused={str(paused).lower()} "
              f"drift={drift} finished={str(finished).lower()}")
    for _ in range(5):
        adb("shell", "uiautomator", "dump", "/sdcard/wobbrik-capture.xml")
        hierarchy = adb("shell", "cat", "/sdcard/wobbrik-capture.xml").decode()
        if marker in hierarchy:
            time.sleep(1)
            (OUT / f"{name}.png").write_bytes(adb("exec-out", "screencap", "-p"))
            print(name, "captured", flush=True)
            break
        time.sleep(1)
    else:
        raise RuntimeError(f"Expected fixture did not render: {marker}")

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    originals = {key: setting(key) for key in ("font_scale", "accelerometer_rotation", "user_rotation")}
    # Preserve any pre-existing overrides as well as the physical configuration.
    size = adb("shell", "wm", "size").decode()
    density = adb("shell", "wm", "density").decode()
    def override(value):
        return value.split("Override ", 1)[1].split(": ", 1)[1].strip() if "Override " in value else "reset"
    try:
        adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
        adb("shell", "settings", "put", "system", "user_rotation", "0")
        for name, pixels, dpi in (("phone", "1080x2400", "420"),
            ("tablet-portrait", "1200x1920", "240"), ("tablet-landscape", "1920x1200", "240"),
            ("phone-landscape", "1600x720", "280")):
            adb("shell", "wm", "size", pixels)
            adb("shell", "wm", "density", dpi)
            adb("shell", "settings", "put", "system", "font_scale", "1.0")
            launch(name + "-active")
            launch(name + "-pause", paused=True)
        adb("shell", "wm", "size", "1080x2400")
        adb("shell", "wm", "density", "420")
        launch("phone-long-tower", floors=40, crane=120)
        launch("phone-finished", finished=True)
        adb("shell", "wm", "size", "1600x800")
        adb("shell", "wm", "density", "320")
        adb("shell", "settings", "put", "system", "font_scale", "1.5")
        launch("short-window-large-font", paused=True)
    finally:
        adb("shell", "wm", "size", override(size))
        adb("shell", "wm", "density", override(density))
        for key, value in originals.items():
            if value == "null":
                adb("shell", "settings", "delete", "system", key)
            else:
                adb("shell", "settings", "put", "system", key, value)

if __name__ == "__main__":
    main()
