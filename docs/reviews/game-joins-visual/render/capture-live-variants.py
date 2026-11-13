from pathlib import Path
import subprocess,threading,time,re,json,sys
ADB='/Users/mac/Library/Android/sdk/platform-tools/adb'
OUT=Path('/Users/mac/My Workspace/Wobbrik/docs/reviews/game-joins-visual/live/variants')
def adb(*a):return subprocess.check_output([ADB,'-s','emulator-5592',*a])
adb('install','-r','/Users/mac/My Workspace/Wobbrik/android-billing/app/build/outputs/apk/debug/app-debug.apk')
scenarios=[(i,n,{}) for i,n in enumerate(['purple','teal','plaster','panel','wood','concrete'],1)]
scenarios += [(1,'mixed-base',{'window':'base_3_window','entrance':'coins_entrance_2','lighting':'reward_lighting_1','roof':'paid_roof_1'}),
 (2,'mixed-teal',{'window':'reward_window_2','entrance':'balance_entry','lighting':'paid_lighting_1','roof':'summit_crown'}),
 (1,'mixed-earned',{'wall':'coins_wall_3','window':'precision_frame','entrance':'paid_entrance_1','lighting':'coins_lighting_2','roof':'reward_roof_2'})]
for style,name,parts in scenarios:
 if len(sys.argv)>1 and name not in sys.argv[1:]: continue
 adb('logcat','-c')
 args=['shell','am','start','-S','-W','-n','com.aladdin.wobbrik/.ScenePreviewActivity','--ei','floors','0','--ei','style',str(style),'--ez','animate','true','--ez','telemetry','true']
 for key,value in parts.items():args+=['--es',key,value]
 adb(*args)
 pid=adb('shell','pidof','com.aladdin.wobbrik').decode().strip()
 log=subprocess.Popen([ADB,'-s','emulator-5592','logcat','--pid='+pid,'-s','AtlasQA:D','*:S'],stdout=subprocess.PIPE,text=True)
 latest=[None];events=[];stop=[False]
 def read(stream=log.stdout,target=latest):
  for line in stream:
   m=re.search(r'AtlasQA.*?: (SWINGING|FALLING|FINISHED) ([\d.eE+-]+) (\d+)',line)
   if m: target[0]=(m[1],float(m[2]),int(m[3]),time.monotonic())
 thread=threading.Thread(target=read,daemon=True);thread.start()
 started=time.monotonic();last_drop=-1;success=False
 while time.monotonic()-started<180:
  state=latest[0]
  if state:
   phase,x,count,ts=state
   if count>=3 and phase=='SWINGING':
    success=True;break
   if phase=='FINISHED': break
   if phase=='SWINGING' and abs(x)<12 and count!=last_drop and time.monotonic()-ts<.5:
    adb('shell','input','tap','270','375');last_drop=count;events.append({'before_count':count,'crane_x':x})
    print(name,'drop at',round(x,2),'count',count,flush=True)
    if count==0: (OUT/f'{name}-drop-live.png').write_bytes(adb('exec-out','screencap','-p'))
  time.sleep(.015)
 time.sleep(.15)
 (OUT/f'{name}-multi-live.png').write_bytes(adb('exec-out','screencap','-p'))
 print(name,'final',latest[0],flush=True)
 (OUT/f'{name}-touches.json').write_text(json.dumps({'initial_floors':0,'parts':parts,'style':style,'touches':events,'final':latest[0],'three_floors':success},indent=2))
 # Observe real terminal phase via a deliberately off-centre touch, unchanged engine.
 if success:
  adb('shell','input','keyevent','4');time.sleep(.25)
  (OUT/f'{name}-pause-live.png').write_bytes(adb('exec-out','screencap','-p'))
  adb('shell','input','tap','270','560');time.sleep(.2)
  t=time.monotonic()
  while time.monotonic()-t<18:
   state=latest[0]
   if state and state[0]=='SWINGING' and abs(state[1])>112:
    adb('shell','input','tap','270','375');time.sleep(1.2);break
   time.sleep(.02)
  (OUT/f'{name}-terminal.png').write_bytes(adb('exec-out','screencap','-p'))
  (OUT/f'{name}-terminal-state.json').write_text(json.dumps({'state':latest[0],'fixture':False},indent=2))
  print(name,'terminal',latest[0],flush=True)
 log.terminate();log.wait();thread.join(timeout=2)
