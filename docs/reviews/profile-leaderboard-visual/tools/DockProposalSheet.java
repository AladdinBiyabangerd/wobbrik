import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Contact sheet of complete viewports; no modification of the rendered UI. */
class DockProposalSheet {
 public static void main(String[] args) throws Exception {
  var dir=new File(args[0]);
  String[] names={"390-font1","360-font1","320-font2"};
  String[] labels={"Normal telefon · 390×844", "Kiçik telefon · 360×640", "Böyük şrift · 320×568"};
  int[] widths={390,360,320};
  boolean flow=args.length>1&&args[1].equals("flow");
  boolean fit=args.length>1&&args[1].equals("fit");
  boolean production=args.length>1&&args[1].equals("production");
  if(fit) for(int i=0;i<names.length;i++) {
   String first=java.nio.file.Files.readAllLines(new File(dir,names[i]+"-count.txt").toPath()).get(0);
   String count=first.split("=")[1].split(";")[0];
   labels[i]+=" · "+count+" nəfər";
  }
  if(flow) {
   names=new String[]{"more-loading","more-appended","more-end"};
   labels=new String[]{"1. Yüklənir", "2. Siyahının davamı — 4–8-ci yerlər", "3. Bütün nəticələr yükləndi"};
   widths=new int[]{390,390,390};
  }
  int total=16;for(int width:widths) total+=width+16;
  var board=new BufferedImage(total,892,BufferedImage.TYPE_INT_RGB);
  var g=board.createGraphics();g.setColor(new Color(0x061831));g.fillRect(0,0,total,892);
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));int left=16;
  for(int i=0;i<names.length;i++) {
   var image=ImageIO.read(new File(dir,names[i]+".png"));
   int height=(int)Math.round((double)image.getHeight()*widths[i]/image.getWidth());
   g.setColor(new Color(0xF4F7FF));g.drawString(labels[i],left,22);
   g.drawImage(image,left,32,widths[i],height,null);left+=widths[i]+16;
  }
  g.dispose();ImageIO.write(board,"png",new File(dir,flow?"more-flow-board.png":fit?"fit-preview-board.png":production?"production-board.png":"preview-board.png"));
 }
}
