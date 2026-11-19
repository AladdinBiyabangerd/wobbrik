from PIL import Image,ImageDraw
from pathlib import Path
root=Path(__file__).resolve().parent
src=Path('/private/tmp/wobbrik-achievements-native')
for p in src.glob('*.png'):
 (root/p.name).write_bytes(p.read_bytes())
ref=Image.open(root.parent.parent/'design/achievements-collection-v1/proposal.png').convert('RGB')
board=Image.new('RGB',(1600,900),'#e8e8e8');d=ImageDraw.Draw(board)
items=[('PNG / Nailiyyətlər',ref.crop((164,9,715,1015))),('Native / accepted fixture',Image.open(src/'regular-achievements.png')),('PNG / Rekordlar',ref.crop((822,9,1376,1015))),('Native / accepted fixture',Image.open(src/'regular-records.png'))]
for i,(label,im) in enumerate(items):
 im=im.convert('RGB');im.thumbnail((390,844));d.text((i*400+5,8),label,fill='black');board.paste(im,(i*400+5,32))
board.save(root/'comparison.jpg',quality=92)
board=Image.new('RGB',(1800,1330),'#e8e8e8');d=ImageDraw.Draw(board)
for j,page in enumerate(['achievements','records']):
 for i,name in enumerate(['small','regular-font2','small-font2','insets-long','regular']):
  im=Image.open(src/f'{name}-{page}.png').convert('RGB');im.thumbnail((350,625));d.text((i*360+5,j*665+5),f'{name}/{page}',fill='black');board.paste(im,(i*360+5,j*665+28))
board.save(root/'responsive.jpg',quality=92)
board=Image.new('RGB',(1800,700),'#e8e8e8');d=ImageDraw.Draw(board)
for i,name in enumerate(['regular-font2','small-font2','insets-long']):
 im=Image.open(src/f'{name}-awards-scrolled.png').convert('RGB');im.thumbnail((350,650));d.text((i*360+5,8),f'{name}/scrolled',fill='black');board.paste(im,(i*360+5,32))
for i,name in enumerate(['regular-offline-retained','regular-empty-error']):
 im=Image.open(src/f'{name}.png').convert('RGB');im.thumbnail((350,650));d.text(((i+3)*360+5,8),name,fill='black');board.paste(im,((i+3)*360+5,32))
board.save(root/'states.jpg',quality=92)
