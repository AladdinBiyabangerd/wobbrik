"""Summarize test-only Compose/JFR spans; durations overlap, never add them as phases."""
import csv, json, sys, collections, datetime
from pathlib import Path
root=Path(sys.argv[1]); events=json.load(open(sys.argv[2]))['recording']['events']
def union(intervals):
    end=total=0
    for a,b in sorted(intervals):
        total+=max(0,b-max(end,a));end=max(end,b)
    return total
spans=list(csv.DictReader(open(root/'spans.tsv'),delimiter='\t'))
measures=list(csv.DictReader(open(root/'measure.tsv'),delimiter='\t')) if (root/'measure.tsv').exists() else []
result=[]
for r in csv.DictReader(open(root/'rounds.csv')):
    a,b=int(r['start']),int(r['end']);lo,hi=int(r['wallStartMs'])/1000,int(r['wallEndMs'])/1000
    inclusive=collections.Counter();composition=[];layout=[]
    for s in spans:
        x,y=max(a,int(s['start'])),min(b,int(s['end']))
        if y>x:
            inclusive[s['info']]+=(y-x)/1e6
            if s['thread'].startswith('SDK 34 Main Thread'):composition.append((x,y))
    for s in measures:
        x,y=max(a,int(s['start'])),min(b,int(s['end']))
        if y>x:layout.append((x,y));inclusive[s['info']]+=(y-x)/1e6
    classes=[];class_count=0;compiler=collections.Counter();compile_count=0;samples=collections.Counter()
    for e in events:
        v=e['values'];start=datetime.datetime.fromisoformat(v['startTime']).timestamp()
        if not lo<=start<=hi:continue
        duration=float(v.get('duration','PT0S')[2:-1])
        thread=v.get('eventThread',v.get('sampledThread',{})).get('javaName','')
        if e['type']=='jdk.ClassLoad' and thread.startswith('SDK 34 Main Thread'):
            classes.append((max(lo,start),min(hi,start+duration)));class_count+=1
        if e['type']=='jdk.Compilation':compiler[v['compiler']]+=duration*1000;compile_count+=1
        if e['type']=='jdk.ExecutionSample' and thread.startswith('SDK 34 Main Thread'):
            for f in (v.get('stackTrace') or {}).get('frames',[]):samples[f['type']]+=1
    result.append(dict(round=int(r['round']),elapsedMs=float(r['elapsedMs']),
        mainCompositionUnionMs=union(composition)/1e6,inventoryMeasurePlaceUnionMs=union(layout)/1e6,
        mainClassLoadUnionMs=union(classes)*1000,mainClassLoads=class_count,
        concurrentCompilerMs=dict(compiler),compilations=compile_count,sampledMainFrameTypes=dict(samples),
        inclusiveSpansMs=dict(inclusive.most_common(30))))
print(json.dumps(result,indent=2))
