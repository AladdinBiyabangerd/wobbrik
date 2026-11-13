"""Explicit multi-floor debug fixtures: covers all 68 IDs, not touch-built towers."""
from pathlib import Path
import subprocess,time,json
ROOT=Path(__file__).resolve().parents[4]
ADB='/Users/mac/Library/Android/sdk/platform-tools/adb'
OUT=ROOT/'docs/reviews/game-joins-visual/live/catalog';OUT.mkdir(parents=True,exist_ok=True)
def adb(*a):return subprocess.check_output([ADB,'-s','emulator-5592',*a],timeout=25)
items=json.loads((ROOT/'android-billing/app/src/main/assets/finish/catalog-map.json').read_text())['items']
categories={c:[i['id'] for i in items if i['category']==c] for c in ['wall','window','entrance','lighting','roof']}
manifest=[]
for i in range(14):
 parts={c:ids[i%len(ids)] for c,ids in categories.items()}
 for terminal in [False,True]:
  args=['shell','am','start','-S','-W','-n','com.aladdin.wobbrik/.ScenePreviewActivity',
    '--ei','floors','5','--ei','drift','2','--ez','finished',str(terminal).lower(),
    '--ez','scene_only',str(terminal).lower(),'--ez','animate',str(not terminal).lower()]
  for k,v in parts.items():args+=['--es',k,v]
  adb(*args);time.sleep(.7)
  name=f'{i+1:02d}-'+('roof-fixture' if terminal else 'active-fixture')+'.png'
  (OUT/name).write_bytes(adb('exec-out','screencap','-p'))
  print(name,flush=True)
 manifest.append({'index':i+1,'initial_floors':5,'drift':2,'parts':parts})
covered={v for case in manifest for v in case['parts'].values()}
assert covered=={i['id'] for i in items} and len(covered)==68
(OUT/'manifest.json').write_text(json.dumps({'fixture':True,'covered_ids':sorted(covered),'scenarios':manifest},indent=2))
print('68/68 IDs covered by 14 five-floor Android emulator fixtures',flush=True)
