"""Compose native captures alongside the unchanged approved/previous review images."""
from PIL import Image, ImageDraw
from pathlib import Path
import sys
p=Path(sys.argv[1]);out=Path(__file__).parent
names=['market-regular','own-listings-regular','market-small','own-listings-small','market-regularfont','own-listings-regularfont','market-font','own-listings-font','market-insets','own-listings-insets']
o=Image.new('RGB',(390*5,880*2),'#eeeeee');d=ImageDraw.Draw(o)
for i,n in enumerate(names):
 im=Image.open(p/(n+'.png'));im.save(out/(n+'.png'));im.thumbnail((390,844));x=i%5*390;y=i//5*880
 o.paste(im,(x,y+25));d.text((x+6,y+6),n,fill='black')
o.save(out/'responsive.jpg')
ref=Image.open(out.parent/'market-collection-followup/comparison.jpg')
o=Image.new('RGB',(390*3,880*2),'#eeeeee');d=ImageDraw.Draw(o)
for row,n in enumerate(['market-regular','own-listings-regular']):
 xref=0 if row==0 else 780
 o.paste(ref.crop((xref,30,xref+390,809)),(0,row*880+25));d.text((6,row*880+6),'Approved PNG',fill='black')
 for col,folder,label in [(1,out.parent/'market-collection-followup','Before'),(2,p,'After - native host')]:
  im=Image.open(folder/(n+'.png'));im.thumbnail((390,844));o.paste(im,(col*390,row*880+25));d.text((col*390+6,row*880+6),label,fill='black')
o.save(out/'comparison.jpg')
