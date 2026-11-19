import struct,zlib
p='android-billing/app/src/main/assets/city/collection-illustration-v1.png'
b=open(p,'rb').read();at=8;data=b''
w,h,depth,typ,*_=struct.unpack('>IIBBBBB',b[16:29]);assert (depth,typ)==(8,6)
while at<len(b):
 n=struct.unpack('>I',b[at:at+4])[0]
 if b[at+4:at+8]==b'IDAT':data+=b[at+8:at+8+n]
 at+=n+12
raw=zlib.decompress(data);rows=[];prev=bytearray(w*4);at=0
for y in range(h):
 f=raw[at];at+=1;r=bytearray(raw[at:at+w*4]);at+=w*4
 for x in range(w*4):
  a=r[x-4] if x>=4 else 0;c=prev[x-4] if x>=4 else 0;v=prev[x]
  if f==1:q=a
  elif f==2:q=v
  elif f==3:q=(a+v)//2
  elif f==4:
   t=a+v-c;pa=abs(t-a);pb=abs(t-v);pc=abs(t-c);q=a if pa<=pb and pa<=pc else v if pb<=pc else c
  else:q=0
  r[x]=(r[x]+q)&255
 rows.append(r);prev=r
for cy in range(2):
 for cx in range(3):
  pts=[(x,y) for y in range(cy*512,(cy+1)*512) for x in range(cx*512,(cx+1)*512) if rows[y][x*4+3]>=8]
  print(cx,cy,'bounds',min(x for x,y in pts),min(y for x,y in pts),max(x for x,y in pts),max(y for x,y in pts))
print('corner RGBA',list(rows[0][:4]),'bytes',len(b),'RGBA',w*h*4)
