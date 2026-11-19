"""Unchanged approved PNG + previous/current native captures; no generated artwork."""
from pathlib import Path
from PIL import Image, ImageDraw
import sys
out=Path(__file__).parent
captures=Path(sys.argv[1])
previous=out.parent/'market-building-card-scale'
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
# Keep the actual pixels of the normal first card; enlarged comparison is an art QA crop.
art=Image.new('RGB',(900,650),'#eeeeee');draw=ImageDraw.Draw(art)
for col,label in enumerate(['Approved PNG','Before - native','After - native']):
    if col==0:
        im=reference.crop((65,252,229,451))
    else:
        folder=previous if col==1 else captures
        im=Image.open(folder/'market-regular.png').crop((175,690,440,1140))
    im.thumbnail((280,600));art.paste(im,(col*300+(300-im.width)//2,35))
    draw.text((col*300+6,8),label,fill='black')
art.save(out/'art-detail.jpg',quality=96)
