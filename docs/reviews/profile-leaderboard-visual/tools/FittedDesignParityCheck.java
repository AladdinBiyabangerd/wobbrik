import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Approved specimen versus production host with identical public fixture values. */
class FittedDesignParityCheck {
 public static void main(String[] args)throws Exception {
  var root=new File(args[0]);StringBuilder report=new StringBuilder();
  String[] names={"390-font1","360-font1","320-font2"};int[] widths={390,360,320};
  int total=16;for(int w:widths) total+=w*2+32;
  var board=new BufferedImage(total,892,BufferedImage.TYPE_INT_RGB);var g=board.createGraphics();
  g.setColor(new Color(0x061831));g.fillRect(0,0,total,892);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  int x=16;
  for(int i=0;i<names.length;i++) {
   var approved=ImageIO.read(new File(root,"fitted-proposal/"+names[i]+".png"));
   var actual=ImageIO.read(new File(root,"fitted-production/"+names[i]+".png"));
   if(approved.getWidth()!=actual.getWidth()||approved.getHeight()!=actual.getHeight()) throw new AssertionError("dimensions");
   int changed=0,left=actual.getWidth(),top=actual.getHeight(),right=0,bottom=0;
   for(int py=0;py<actual.getHeight();py++) for(int px=0;px<actual.getWidth();px++) if(approved.getRGB(px,py)!=actual.getRGB(px,py)) {
    changed++;left=Math.min(left,px);top=Math.min(top,py);right=Math.max(right,px);bottom=Math.max(bottom,py);
   }
   report.append(names[i]+": changed="+changed+(changed==0?" (identical)":" bbox="+left+","+top+".."+right+","+bottom)+"\n");
   if(changed!=0) throw new AssertionError("Approved design drift: "+names[i]+" pixels="+changed);
   for(int side=0;side<2;side++) {
    var image=side==0?approved:actual;int h=(int)Math.round((double)image.getHeight()*widths[i]/image.getWidth());
    g.setColor(new Color(0xF4F7FF));g.drawString(names[i]+(side==0?" · Təsdiqli":" · Production"),x,22);
    g.drawImage(image,x,32,widths[i],h,null);x+=widths[i]+16;
   }
  }
  int checked=0;
  for(var file:new File(root,"fitted-production").listFiles()) {
   if(!file.getName().endsWith(".png")||file.getName().equals("approved-comparison.png")||file.getName().contains("board")) continue;
   var image=ImageIO.read(file);String base=image.getWidth()==1170?"390-font1":image.getWidth()==1080?"360-font1":"320-font2";
   var reference=ImageIO.read(new File(root,"fitted-production/"+base+".png"));
   for(int py=48;py<192;py++) for(int px=48;px<192;px++)
    if(reference.getRGB(px,py)!=image.getRGB(px,py)) throw new AssertionError("Back/header capture drift: "+file.getName());
   checked++;
  }
  report.append("Post-PNG back-region integrity: "+checked+" captures PASS\n");
  g.dispose();ImageIO.write(board,"png",new File(root,"fitted-production/approved-comparison.png"));
  java.nio.file.Files.writeString(new File(root,"fitted-production/parity.txt").toPath(),report.toString());System.out.print(report);
 }
}
