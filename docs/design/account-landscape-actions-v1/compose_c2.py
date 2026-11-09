"""Option C revision (c2): no wrapped labels. Kolleksiya and Nailiyyətlər are stacked full
width in the action pane instead of side by side; their icon+label pixels come from the real
live portrait render at the same 420dpi / font scale 1.0 (labels on one line there), so text
size is unchanged. Composed ONLY from real AccountPreviewActivity renders (emulator-5586).
Run: uv run --with pillow --with numpy python compose_c2.py"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

here = Path(__file__).resolve().parent
src = here / 'source'
def load(n): return np.asarray(Image.open(src / n).convert('RGB')).astype(np.int16)
L, LB, LP = load('live-phone-landscape-top.png'), load('live-phone-landscape-scrolled.png'), load('live-phone-portrait.png')
P, N = load('phone-landscape.png'), load('phone-landscape-nocache.png')
H, W = L.shape[:2]
bg = L[:, 1570, :].copy()
bgP = LP[:, 20, :].copy()  # portrait left margin strip

def blank(): return np.repeat(bg[:, None, :], W, axis=1).copy()
def crop(s, box):
    x0, y0, x1, y1 = box
    return s[y0:y1, x0:x1].copy(), y0
def paste(d, piece, sy, x, y, sbg=None):
    sbg = bg if sbg is None else sbg
    h = piece.shape[0]
    d[y:y + h, x:x + piece.shape[1]] = np.clip(piece + (bg[y:y + h] - sbg[sy:sy + h])[:, None, :], 0, 255)
def content_bbox(body, cap):
    mid = body[:, cap:cap + 1]
    diff = np.abs(body - mid).sum(axis=2) > 40
    diff[:12] = False; diff[-12:] = False; diff[:, :cap] = False; diff[:, -cap:] = False
    ys, xs = np.nonzero(diff)
    return xs.min(), ys.min(), xs.max() + 1, ys.max() + 1
def button(s, box, width, label=None, cap=60):
    """Rebuild a horizontally uniform button at `width`, re-centring its content."""
    x0, y0, x1, y1 = box
    body = s[y0:y1, x0:x1]
    b = np.concatenate([body[:, :cap], np.repeat(body[:, cap:cap + 1], width - 2 * cap, axis=1), body[:, -cap:]], axis=1).copy()
    if label is None:
        cx0, cy0, cx1, cy1 = content_bbox(body, cap)
    else:
        cx0, cy0, cx1, cy1 = label[0] - x0 - 6, label[1] - y0 - 4, label[2] - x0 + 6, label[3] - y0 + 4
    c = body[cy0:cy1, cx0:cx1]
    ox = (width - c.shape[1]) // 2
    b[cy0:cy1, ox:ox + c.shape[1]] = c
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
def scaled(piece, w, h):
    im = Image.fromarray(piece.astype(np.uint8))
    k = min(w / im.width, h / im.height)
    return np.asarray(im.resize((int(im.width * k), int(im.height * k)), Image.LANCZOS)).astype(np.int16)
def save(d, name, caption):
    sheet = Image.new('RGB', (W, H + 150), (6, 24, 49))
    sheet.paste(Image.fromarray(d.astype(np.uint8)), (0, 0))
    ImageDraw.Draw(sheet).text((63, H + 50), caption, fill=(244, 247, 255),
        font=ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial Unicode.ttf', 44))
    sheet.save(here / name)

TOP, BOTTOM = 294, 954; GAP, SMALL, BTN = 42, 21, 126
LEFT, MID, RIGHT = (63, 800), (842, 1558), (1600, 2337)
RW = RIGHT[1] - RIGHT[0]

def frame(s):
    d = blank(); paste(d, *crop(s, (0, 0, W, 285)), 0, 0); paste(d, *crop(s, (0, 990, W, H)), 0, 990); return d
def left_pane(d, s):
    b = bbox(s, (63, 294, 1537, 954)); hp = scaled(crop(s, b)[0], LEFT[1] - LEFT[0], BOTTOM - TOP)
    y = TOP + (BOTTOM - TOP - hp.shape[0]) // 2
    paste(d, hp, y, LEFT[0] + (LEFT[1] - LEFT[0] - hp.shape[1]) // 2, y)
def mid_pane(d, s, title_box):
    ct, cb = rows(s, 1960, 375, 840); card = s[ct:cb, 1600:2337]
    mw = MID[1] - MID[0]; card = np.concatenate([card[:, :mw - 60], card[:, -60:]], axis=1)
    t = text(s, title_box); y = TOP + (BOTTOM - TOP - (73 + GAP + card.shape[0])) // 2
    centered(d, *t, *MID, y); paste(d, card, ct, MID[0], y + 73 + GAP)
def stack(d, items):
    total = sum(p.shape[0] for p, _, _, _ in items) + sum(g for _, _, g, _ in items[:-1])
    y = TOP + (BOTTOM - TOP - total) // 2
    for piece, sy, gap, sbg in items:
        paste(d, piece, sy, RIGHT[0], y, sbg); y += piece.shape[0] + gap

# Live: Oyna / Kolleksiya / Nailiyyətlər / Ana ekran, each full pane width, labels on one line.
d = frame(L); left_pane(d, L); mid_pane(d, L, (1747, 294, 2190, 367))
oyna = button(LB, (1600, 470, 2337, 596), RW, (1919, 507, 2018, 560))
koll = button(LP, (63, 1980, 529, 2106), RW)
nail = button(LP, (550, 1980, 1017, 2106), RW)
ana = button(LB, (1600, 828, 2337, 954), RW, (1872, 865, 2065, 918))
stack(d, [(*oyna, GAP, None), (*koll, SMALL, bgP), (*nail, GAP, bgP), (*ana, 0, None)])
save(d, 'c2-live-phone-landscape.png', 'Variant C (düzəliş) — canlı: Kolleksiya və Nailiyyətlər tam endə alt-alta, yazılar bir sətirdə')

# Offline (cached): same pane geometry; status + Yenilə + Ana ekran.
d = frame(P); left_pane(d, P); mid_pane(d, P, (1747, 294, 2190, 367))
status = text(P, (1738, 850, 2200, 923))
pad = np.repeat(bg[:1, None, :], RW, axis=1)  # status is centred text: widen to pane on background
s_piece = np.repeat(bg[status[1]:status[1] + status[0].shape[0], None, :], RW, axis=1).copy()
ox = (RW - status[0].shape[1]) // 2; s_piece[:, ox:ox + status[0].shape[1]] = status[0]
yen = button(N, (1600, 592, 2337, 718), RW, (1909, 629, 2028, 682))
ana2 = button(N, (1600, 760, 2337, 886), RW, (1872, 797, 2065, 850))
stack(d, [(s_piece, status[1], GAP, None), (*yen, SMALL * 2, None), (*ana2, 0, None)])
save(d, 'c2-offline-phone-landscape.png', 'Variant C (düzəliş) — offline: eyni üç panel; status, Yenilə, Ana ekran bir sətirdə')
