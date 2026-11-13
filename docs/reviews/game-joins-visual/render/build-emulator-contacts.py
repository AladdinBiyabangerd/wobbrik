"""Compose untouched emulator crops/screens, preserving raw PNGs and ID manifests."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,shutil
LIVE=Path(__file__).resolve().parents[1]/'live'
p=LIVE/'variants'
for names,dest,rows in [(['purple','teal','plaster','panel','wood','concrete'],'all-six-emulator.png',2),
                        (['mixed-base','mixed-teal','mixed-earned'],'mixed-emulator.png',1)]:
 sheet=Image.new('RGB',(1620,1235*rows),'#061831');d=ImageDraw.Draw(sheet)
 for i,name in enumerate(names):
  record=json.loads((p/f'{name}-touches.json').read_text())
  assert record['three_floors'] and record['initial_floors']==0 and record['final'][2]==3
  im=Image.open(p/f'{name}-multi-live.png').convert('RGB');assert im.size==(540,1200)
  sheet.paste(im,((i%3)*540,(i//3)*1235+35))
  d.text(((i%3)*540+12,(i//3)*1235+10),name+' / 3 floors / real touches from zero',fill='white')
 sheet.save(p/dest)
p=LIVE/'catalog';manifest=json.loads((p/'manifest.json').read_text());assert len(manifest['covered_ids'])==68
for mode in ['active','roof']:
 sheet=Image.new('RGB',(2030,1040),'#061831');d=ImageDraw.Draw(sheet)
 for i in range(14):
  im=Image.open(p/f'{i+1:02d}-{mode}-fixture.png').convert('RGB');assert im.size==(540,1200)
  box=(125,690,415,1180) if mode=='roof' else (125,650,415,1140)
  sheet.paste(im.crop(box),((i%7)*290,(i//7)*520+25))
  d.text(((i%7)*290+8,(i//7)*520+6),f'{i+1:02d} / 5-floor {mode} fixture',fill='white')
 sheet.save(p/f'all-68-{mode}-emulator.png')
