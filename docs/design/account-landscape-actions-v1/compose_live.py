"""Live Ready account page (phone landscape) under options A/B/C, composed ONLY from real
AccountPreviewActivity renders (emulator-5586, source/live-*.png). Elements are cropped and
moved; button widths shortened by removing empty middle slices; C scales the hero block.
Not a Compose screenshot. Run: uv run --with pillow --with numpy python compose_live.py"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

here = Path(__file__).resolve().parent
src = here / 'source'
L = np.asarray(Image.open(src / 'live-phone-landscape-top.png').convert('RGB')).astype(np.int16)
LB = np.asarray(Image.open(src / 'live-phone-landscape-scrolled.png').convert('RGB')).astype(np.int16)
H, W = L.shape[:2]
bg = L[:, 1570, :].copy()  # empty strip between hero and controls

def blank(): return np.repeat(bg[:, None, :], W, axis=1).copy()
def crop(s, box):
    x0, y0, x1, y1 = box
    return s[y0:y1, x0:x1].copy(), y0
def paste(d, piece, sy, x, y):
    h = piece.shape[0]
    d[y:y + h, x:x + piece.shape[1]] = np.clip(piece + (bg[y:y + h] - bg[sy:sy + h])[:, None, :], 0, 255)
def clip_paste(d, piece, sy, x, y, ymax, fade=48):
    h = max(0, min(piece.shape[0], ymax - y))
    if not h: return
    paste(d, piece[:h], sy, x, y)
    if h < piece.shape[0]:
        f = min(fade, h)
        for i in range(f):
            a = (i + 1) / f; r = y + h - f + i
            d[r, x:x + piece.shape[1]] = (d[r, x:x + piece.shape[1]] * (1 - a) + bg[r] * a).astype(np.int16)
def button(s, box, label, width):
    x0, y0, x1, y1 = box
    body = s[y0:y1, x0:x1]; cap = 60
    b = np.concatenate([body[:, :cap], np.repeat(body[:, cap:cap + 1], width - 2 * cap, axis=1), body[:, -cap:]], axis=1).copy()
    lx0, ly0, lx1, ly1 = label
    lab = s[ly0 - 4:ly1 + 4, lx0 - 6:lx1 + 6]
    ox = (width - lab.shape[1]) // 2; oy = ly0 - 4 - y0
    b[oy:oy + lab.shape[0], ox:ox + lab.shape[1]] = lab
    return b, y0
def text(s, box):
    x0, y0, x1, y1 = box
    return s[y0 - 4:y1 + 4, x0 - 8:x1 + 8].copy(), y0 - 4
def centered(d, piece, sy, cx0, cx1, y): paste(d, piece, sy, cx0 + (cx1 - cx0 - piece.shape[1]) // 2, y)
def bbox(s, box):
    x0, y0, x1, y1 = box
    diff = np.abs(s[y0:y1, x0:x1] - bg[y0:y1][:, None, :]).sum(axis=2) > 24
    ys, xs = np.nonzero(diff)
    return (x0 + xs.min(), y0 + ys.min(), x0 + xs.max() + 1, y0 + ys.max() + 1)
def rows(s, x, y0, y1):
    ys = np.nonzero(np.abs(s[y0:y1, x] - bg[y0:y1]).sum(axis=1) > 6)[0]
    return y0 + ys.min(), y0 + ys.max() + 1

COL = (1600, 2337); TOP, BOTTOM = 294, 954; GAP, SMALL, BTN = 42, 21, 126
head = crop(L, (0, 0, W, 285)); foot = crop(L, (0, 990, W, H))
lb = bbox(L, (63, 294, 1537, 954)); left_block = crop(L, lb)  # "Hesab" + account hero
ct, cb = rows(L, 1960, 375, 840); card = crop(L, (COL[0], ct, COL[1], cb))
title = text(L, (1747, 294, 2190, 367))
OYNA = ((1600, 470, 2337, 596), (1919, 507, 2018, 560))
ANA = ((1600, 828, 2337, 954), (1872, 865, 2065, 918))
row = crop(LB, (1600, 638, 2337, 786))  # Kolleksiya | Nailiyyətlər exactly as rendered

def base():
    d = blank(); paste(d, *head, 0, 0); paste(d, *foot, 0, 990); return d
def save(d, name, caption):
    sheet = Image.new('RGB', (W, H + 150), (6, 24, 49))
    sheet.paste(Image.fromarray(d.astype(np.uint8)), (0, 0))
    ImageDraw.Draw(sheet).text((63, H + 50), caption, fill=(244, 247, 255),
        font=ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial Unicode.ttf', 44))
    sheet.save(here / name)

save(L.copy(), 'live-current-phone-landscape.png', 'Cari canlı hesab səhifəsi — Oyna yarımçıq, Kolleksiya/Nailiyyətlər və Ana ekran qatın altında')

# A: Oyna | Ana ekran pinned at the bottom; title, card, Kolleksiya/Nailiyyətlər scroll above.
d = base(); paste(d, *left_block, lb[0], lb[1])
half = (COL[1] - COL[0] - SMALL) // 2; ry = BOTTOM - BTN
centered(d, *title, *COL, TOP)
clip_paste(d, *card, COL[0], TOP + 73 + GAP, ry - GAP)
paste(d, *button(LB, *OYNA, half), COL[0], ry)
paste(d, *button(LB, *ANA, half), COL[0] + half + SMALL, ry)
save(d, 'live-option-a-phone-landscape.png', 'Canlı, Variant A — Oyna | Ana ekran altda sabit; kart və Kolleksiya/Nailiyyətlər yuxarıda scroll olur')

# B: actions first (Oyna, Ana ekran), then title + card + Kolleksiya/Nailiyyətlər (scroll).
d = base(); paste(d, *left_block, lb[0], lb[1])
y = TOP
paste(d, *button(LB, *OYNA, COL[1] - COL[0]), COL[0], y); y += BTN + SMALL * 2
paste(d, *button(LB, *ANA, COL[1] - COL[0]), COL[0], y); y += BTN + GAP
centered(d, *title, *COL, y); y += 73 + GAP
clip_paste(d, *card, COL[0], y, BOTTOM)
save(d, 'live-option-b-phone-landscape.png', 'Canlı, Variant B — Oyna və Ana ekran sütunun başında; kart və Kolleksiya/Nailiyyətlər altda (scroll)')

# C: three panes: scaled "Hesab" + hero | title + card | Oyna, Kolleksiya/Nailiyyətlər, Ana ekran.
d = base(); left = (63, 800); mid = (842, 1558)
hp = Image.fromarray(left_block[0].astype(np.uint8))
sc = min((left[1] - left[0]) / hp.width, (BOTTOM - TOP) / hp.height)
hp = np.asarray(hp.resize((int(hp.width * sc), int(hp.height * sc)), Image.LANCZOS)).astype(np.int16)
hy = TOP + (BOTTOM - TOP - hp.shape[0]) // 2
paste(d, hp, hy, left[0] + (left[1] - left[0] - hp.shape[1]) // 2, hy)
mw = mid[1] - mid[0]
narrow_card = np.concatenate([card[0][:, :mw - 60], card[0][:, -60:]], axis=1)
y = TOP + (BOTTOM - TOP - (73 + GAP + narrow_card.shape[0])) // 2
centered(d, *title, *mid, y); paste(d, narrow_card, card[1], mid[0], y + 73 + GAP)
act = BTN + GAP + row[0].shape[0] + GAP + BTN
y = TOP + (BOTTOM - TOP - act) // 2
paste(d, *button(LB, *OYNA, COL[1] - COL[0]), COL[0], y); y += BTN + GAP
paste(d, *row, COL[0], y); y += row[0].shape[0] + GAP
paste(d, *button(LB, *ANA, COL[1] - COL[0]), COL[0], y)
save(d, 'live-option-c-phone-landscape.png', 'Canlı, Variant C — üç panel: kiçik Hesab illüstrasiyası | kart | Oyna, Kolleksiya/Nailiyyətlər, Ana ekran')
print('left', lb, 'card', ct, cb)
