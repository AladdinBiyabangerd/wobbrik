"""Unchanged approved PNG + previous/current native captures; no generated artwork."""
from pathlib import Path
from PIL import Image, ImageDraw
import sys
out=Path(__file__).parent
captures=Path(sys.argv[1])
previous=out.parent/'market-building-materials'
reference=Image.open(out.parents[1]/'design/market-collection-v1/proposal.png')
comparison=Image.new('RGB',(1170,1760),'#eeeeee');draw=ImageDraw.Draw(comparison)
for row,name in enumerate(['market-regular','own-listings-regular']):
    # Original source has three equal-width independent mobile screens.
    ref=reference.crop((0 if row==0 else 1024,0,512 if row==0 else 1536,1024))
    ref.thumbnail((390,844));comparison.paste(ref,(0,row*880+25))
    draw.text((6,row*880+6),'Approved PNG',fill='black')
    for col,folder,label in [(1,previous,'Before - native host'),(2,captures,'After - native host')]:
        im=Image.open(folder/(name+'.png'));im.thumbnail((390,844))
        comparison.paste(im,(col*390,row*880+25));draw.text((col*390+6,row*880+6),label,fill='black')
comparison.save(out/'comparison.jpg',quality=94)
responsive=Image.new('RGB',(1950,1760),'#eeeeee');draw=ImageDraw.Draw(responsive)
for row,prefix in enumerate(['market','own-listings']):
    for col,size in enumerate(['regular','small','regularfont','font','insets']):
        name=f'{prefix}-{size}'
        im=Image.open(captures/(name+'.png'));im.save(out/(name+'.png'));im.thumbnail((390,844))
        responsive.paste(im,(col*390,row*880+25));draw.text((col*390+6,row*880+6),name,fill='black')
responsive.save(out/'responsive.jpg',quality=94)
# Same explicit selections and straight accepted centres, independent of route fixture order.
sheet=Image.open(captures/'reference-materials-before-after.png').convert('RGB')
from PIL import ImageChops
rows=Image.new('RGB',(900,1440),'#eeeeee');draw=ImageDraw.Draw(rows)
rects=[(65,252,229,451),(290,252,470,451),(65,570,235,765),(290,570,470,765)]
for i,(name,rect) in enumerate(zip(['Brick','Plaster','Panel','Limestone'],rects)):
    for col,label in enumerate(['Approved PNG','Legacy native - same IDs','Updated native - same IDs']):
        if col==0:
            im=reference.crop(rect)
        else:
            y0=96+621*i
            im=sheet.crop((0 if col==1 else 585,y0,585 if col==1 else 1170,669+621*i))
            mask=ImageChops.difference(im,Image.new('RGB',im.size,(4,30,56))).convert('L').point(lambda n:255 if n>12 else 0)
            bounds=mask.getbbox()
            if bounds: im=im.crop(bounds)
        scale=min(280/im.width,310/im.height)
        im=im.resize((round(im.width*scale),round(im.height*scale)))
        rows.paste(im,(col*300+(300-im.width)//2,i*360+38))
        draw.text((col*300+6,i*360+5),name+' / '+label,fill='black')
rows.save(out/'reference-art-comparison.jpg',quality=96)
