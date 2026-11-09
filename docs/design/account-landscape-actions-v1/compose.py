"""Proposal mockups composed ONLY from real AccountPreviewActivity renders (emulator-5586).
Elements are cropped and moved; button/card widths are shortened by removing empty middle
slices. Not a Compose screenshot. Run: uv run --with pillow --with numpy python compose.py"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

here = Path(__file__).resolve().parent
cur = here / 'source'
out = here
P = np.asarray(Image.open(cur / 'phone-landscape.png').convert('RGB')).astype(np.int16)
N = np.asarray(Image.open(cur / 'phone-landscape-nocache.png').convert('RGB')).astype(np.int16)
H, W = P.shape[:2]
BGX = 1570  # empty vertical strip between hero and controls in both renders
bg = P[:, BGX, :].copy()

def blank():
    return np.repeat(bg[:, None, :], W, axis=1).copy()

def crop(src, box):
    x0, y0, x1, y1 = box
    return src[y0:y1, x0:x1].copy(), y0

def paste(dst, piece, sy, x, y):
    h = piece.shape[0]
    delta = (bg[y:y + h] - bg[sy:sy + h])[:, None, :]  # match vertical gradient at new row
    dst[y:y + h, x:x + piece.shape[1]] = np.clip(piece + delta, 0, 255)

def narrow(piece, width, keep_left):
    """Shorten a horizontally uniform control: keep left part and right cap."""
    right = width - keep_left
    return np.concatenate([piece[:, :keep_left], piece[:, -right:]], axis=1)

def button(src, box, label, width):
    x0, y0, x1, y1 = box
    body = src[y0:y1, x0:x1]
    cap = 60
    mid = body[:, cap:cap + 1]
    b = np.concatenate([body[:, :cap], np.repeat(mid, width - 2 * cap, axis=1), body[:, -cap:]], axis=1).copy()
    lx0, ly0, lx1, ly1 = label
    lab = src[ly0 - 4:ly1 + 4, lx0 - 6:lx1 + 6]
    ox = (width - lab.shape[1]) // 2
    oy = ly0 - 4 - y0
    b[oy:oy + lab.shape[0], ox:ox + lab.shape[1]] = lab
    return b, y0

def text(src, box):
    x0, y0, x1, y1 = box
    return src[y0 - 4:y1 + 4, x0 - 8:x1 + 8].copy(), y0 - 4

def centered(dst, piece, sy, cx0, cx1, y):
    paste(dst, piece, sy, cx0 + (cx1 - cx0 - piece.shape[1]) // 2, y)

def hero_bbox(src, box):
    x0, y0, x1, y1 = box
    region = src[y0:y1, x0:x1]
    diff = np.abs(region - bg[y0:y1][:, None, :]).sum(axis=2) > 24
    ys, xs = np.nonzero(diff)
    return (x0 + xs.min(), y0 + ys.min(), x0 + xs.max() + 1, y0 + ys.max() + 1)

def card_rows(src, x, y0, y1):
    col = np.abs(src[y0:y1, x] - bg[y0:y1]).sum(axis=1) > 6
    ys = np.nonzero(col)[0]
    return y0 + ys.min(), y0 + ys.max() + 1

COL = (1600, 2337)               # controls column (px), 2.625 px/dp
TOP, BOTTOM = 294, 954
GAP, SMALL, BTN = 42, 21, 126    # 16dp, 8dp, 48dp
head = crop(P, (0, 0, W, 285))
foot = crop(P, (0, 990, W, H))
hb = hero_bbox(P, (63, 294, 1537, 954))
hero = crop(P, hb)
ct, cb = card_rows(P, 1960, 375, 840)
card = crop(P, (COL[0], ct, COL[1], cb))
title = text(P, (1747, 294, 2190, 367))
status = text(P, (1738, 850, 2200, 923))
YEN_BOX, YEN_LAB = (1600, 592, 2337, 718), (1909, 629, 2028, 682)
ANA_BOX, ANA_LAB = (1600, 760, 2337, 886), (1872, 797, 2065, 850)

def base():
    d = blank()
    paste(d, *head, 0, 0)
    paste(d, *foot, 0, 990)
    return d

def save(d, name, caption):
    img = Image.fromarray(d.astype(np.uint8))
    band = 150
    sheet = Image.new('RGB', (W, H + band), (6, 24, 49))
    sheet.paste(img, (0, 0))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial Unicode.ttf', 44)
    draw.text((63, H + 50), caption, fill=(244, 247, 255), font=font)
    sheet.save(out / name)

def clip_paste(d, piece, sy, x, y, ymax, fade=48):
    h = max(0, min(piece.shape[0], ymax - y))
    if not h: return
    paste(d, piece[:h], sy, x, y)
    if h < piece.shape[0]:  # clipped by the scroll viewport: fade into background like a scroll edge
        f = min(fade, h)
        for i in range(f):
            a = (i + 1) / f
            r = y + h - f + i
            d[r, x:x + piece.shape[1]] = (d[r, x:x + piece.shape[1]] * (1 - a) + bg[r] * a).astype(np.int16)

# A: actions pinned to the bottom of the controls column; info scrolls above.
d = base()
paste(d, *hero, hb[0], hb[1])
half = (COL[1] - COL[0] - SMALL) // 2
row_y = BOTTOM - BTN
status_y = row_y - SMALL - status[0].shape[0]
scroll_end = status_y - SMALL
centered(d, *title, *COL, TOP)
clip_paste(d, *card, COL[0], TOP + 73 + GAP, scroll_end)
paste(d, *button(N, YEN_BOX, YEN_LAB, half), COL[0], row_y)
paste(d, *button(N, ANA_BOX, ANA_LAB, half), COL[0] + half + SMALL, row_y)
centered(d, *status, *COL, status_y)
save(d, 'option-a-sticky-actions-phone-landscape.png',
     'Variant A — əməllər sütunun altında sabit (Yenilə | Ana ekran yan-yana), keş məlumatı yuxarıda scroll olur')

# B: status + actions first, cached info below (scroll).
d = base()
paste(d, *hero, hb[0], hb[1])
y = TOP
centered(d, *status, *COL, y); y += status[0].shape[0] + GAP
paste(d, *button(N, YEN_BOX, YEN_LAB, COL[1] - COL[0]), COL[0], y); y += BTN + SMALL * 2
paste(d, *button(N, ANA_BOX, ANA_LAB, COL[1] - COL[0]), COL[0], y); y += BTN + GAP
clip_paste(d, *title, COL[0] + (COL[1] - COL[0] - title[0].shape[1]) // 2, y, BOTTOM); y += 73 + GAP
clip_paste(d, *card, COL[0], y, BOTTOM)
save(d, 'option-b-status-first-phone-landscape.png',
     'Variant B — status və Yenilə sütunun başında, keş kartı altda (scroll ilə)')

# C: three panes: smaller hero | cached info | status + actions.
d = base()
left = (63, 800); mid = (842, 1570); right = (1612, 2337)
hpiece = Image.fromarray(hero[0].astype(np.uint8))
scale = min((left[1] - left[0]) / hpiece.width, (BOTTOM - TOP) / hpiece.height)
hpiece = np.asarray(hpiece.resize((int(hpiece.width * scale), int(hpiece.height * scale)), Image.LANCZOS)).astype(np.int16)
hy = TOP + (BOTTOM - TOP - hpiece.shape[0]) // 2
paste(d, hpiece, hy, left[0] + (left[1] - left[0] - hpiece.shape[1]) // 2, hy)
mw = mid[1] - mid[0]
info_h = 73 + GAP + card[0].shape[0]
y = TOP + (BOTTOM - TOP - info_h) // 2
centered(d, *title, *mid, y)
paste(d, narrow(card[0], mw, mw - 60), card[1], mid[0], y + 73 + GAP)
rw = right[1] - right[0]
act_h = status[0].shape[0] + GAP + BTN + SMALL * 2 + BTN
y = TOP + (BOTTOM - TOP - act_h) // 2
centered(d, *status, *right, y); y += status[0].shape[0] + GAP
paste(d, *button(N, YEN_BOX, YEN_LAB, rw), right[0], y); y += BTN + SMALL * 2
paste(d, *button(N, ANA_BOX, ANA_LAB, rw), right[0], y)
save(d, 'option-c-three-panes-phone-landscape.png',
     'Variant C — üç panel: kiçik illüstrasiya | keş kartı | status + Yenilə + Ana ekran')
print('hero', hb, 'card', ct, cb)

# Consistency: current renders where everything already fits (unchanged in all options).
names = ['phone-portrait', 'tablet-portrait', 'tablet-landscape']
ims = [Image.open(cur / (n + '.png')).convert('RGB') for n in names]
th = 1200
ims = [im.resize((int(im.width * th / im.height), th), Image.LANCZOS) for im in ims]
gw = 48
sheet = Image.new('RGB', (sum(i.width for i in ims) + gw * (len(ims) + 1), th + 2 * gw + 150), (6, 24, 49))
x = gw
for im in ims:
    sheet.paste(im, (x, gw)); x += im.width + gw
draw = ImageDraw.Draw(sheet)
font = ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial Unicode.ttf', 44)
draw.text((gw, th + gw + 50), 'Cari render: telefon portret, planşet portret, planşet landşaft — Yenilə görünür; bütün variantlarda dəyişmir', fill=(244, 247, 255), font=font)
sheet.save(out / 'consistency-unchanged-portrait-tablet.png')
