import java.awt.*;import java.awt.image.*;import javax.imageio.ImageIO;import java.io.*;import java.util.*;
class CityIslandPolishSheets {
 static BufferedImage read(String p)throws Exception{return ImageIO.read(new File(p));}
 static void put(Graphics2D g,BufferedImage im,int x,int y,int w,int h){double s=Math.min((double)w/im.getWidth(),(double)h/im.getHeight());int a=(int)(im.getWidth()*s),b=(int)(im.getHeight()*s);g.drawImage(im,x+(w-a)/2,y,a,b,null);}
 public static void main(String[]a)throws Exception{
 String root="/Users/mac/My Workspace/Wobbrik/",out=root+"docs/reviews/city-island-polish/";
 File[] files=new File("/Users/mac/My Workspace/Wobbrik/docs/reviews/city-island-polish/screens").listFiles((d,n)->n.endsWith(".png"));Arrays.sort(files);
 BufferedImage matrix=new BufferedImage(1200,560*((files.length+4)/5),BufferedImage.TYPE_INT_RGB);Graphics2D g=matrix.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,matrix.getWidth(),matrix.getHeight());g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 for(int i=0;i<files.length;i++){int x=i%5*240,y=i/5*560;g.setColor(Color.BLACK);g.drawString(files[i].getName().replace("city-",""),x+3,y+18);put(g,read(files[i].toString()),x,y+26,240,530);}g.dispose();ImageIO.write(matrix,"jpg",new File(out+"matrix.jpg"));
 String[] paths={root+"docs/design/city-collection-v1/proposal.png",root+"docs/reviews/city-island-ground/screens/city-390-font1.png","/Users/mac/My Workspace/Wobbrik/docs/reviews/city-island-polish/screens/city-390-font1.png","/Users/mac/My Workspace/Wobbrik/docs/reviews/city-island-polish/screens/city-390-expanded-six-parcels.png"};String[] titles={"Approved proposal","Before (same fixture)","Island polish (native host)","Six parcels (same buildings)"};
 BufferedImage comparison=new BufferedImage(1560,876,BufferedImage.TYPE_INT_RGB);g=comparison.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,1560,876);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 for(int i=0;i<4;i++){g.setColor(Color.BLACK);g.drawString(titles[i],390*i+10,20);put(g,read(paths[i]),390*i,32,390,844);}g.dispose();ImageIO.write(comparison,"jpg",new File(out+"comparison.jpg"));
 BufferedImage preview=new BufferedImage(390,844,BufferedImage.TYPE_INT_RGB);g=preview.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);put(g,read(paths[2]),0,0,390,844);g.dispose();ImageIO.write(preview,"png",new File(out+"preview.png"));System.out.println("screens="+files.length);
 }
}
