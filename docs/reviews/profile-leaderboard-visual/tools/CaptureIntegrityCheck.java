import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Read-only PNG validation and contact sheets. No screenshot patching or stitching. */
class CaptureIntegrityCheck {
    static final String[] STATES = {"account", "editor", "weekly", "all", "self-page", "error", "empty"};
    static final String[] SIZES = {"360-font1", "390-font1", "320-font2", "320-font2-long"};
    static BufferedImage read(File dir, String state, String size) throws Exception {
        return ImageIO.read(new File(dir, state + "-" + size + ".png"));
    }
    static void sameRegion(BufferedImage a, BufferedImage b, int x, int y, int w, int h, String label) {
        for(int row=y;row<y+h;row++) for(int col=x;col<x+w;col++)
            if(a.getRGB(col,row)!=b.getRGB(col,row)) throw new AssertionError(label+" differs at "+col+","+row);
    }
    static int white(BufferedImage image, int x, int y, int w, int h) {
        int result=0;
        for(int row=y;row<y+h;row++) for(int col=x;col<x+w;col++) {
            var color=new Color(image.getRGB(col,row));
            if(color.getRed()>210&&color.getGreen()>210&&color.getBlue()>210) result++;
        }
        return result;
    }
    static void sheet(File dir, String name, String[] states, String[] sizes, String[] labels) throws Exception {
        int cellWidth=390, header=32;
        var first=read(dir,states[0],sizes[0]);
        int height=(int)Math.round((double)first.getHeight()*cellWidth/first.getWidth());
        var image=new BufferedImage(cellWidth*states.length,height+header,BufferedImage.TYPE_INT_RGB);
        var g=image.createGraphics();
        g.setColor(new Color(0x061831));g.fillRect(0,0,image.getWidth(),image.getHeight());
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        for(int i=0;i<states.length;i++) {
            var source=read(dir,states[i],sizes[i]);
            g.setColor(new Color(0xF4F7FF));g.drawString(labels[i],i*cellWidth+12,22);
            int width=(int)Math.round((double)source.getWidth()*height/source.getHeight());
            g.drawImage(source,i*cellWidth+(cellWidth-width)/2,header,width,height,null);
        }
        g.dispose(); ImageIO.write(image,"png",new File(dir.getParentFile(),name+".png"));
    }
    static void referenceSheet(File dir) throws Exception {
        var root=new File(dir,"../../../..");
        var account=ImageIO.read(new File(root,"docs/design/mobile-v1/account-states.png")).getSubimage(16,16,412,883);
        var records=ImageIO.read(new File(root,"docs/design/mobile-v1/play-progress-v2.png")).getSubimage(1333,14,429,859);
        var panels=new BufferedImage[]{account,read(dir,"account","390-font1"),records,read(dir,"weekly","390-font1")};
        var labels=new String[]{"Təsdiqli PNG — Hesab","Production host — Profil","Təsdiqli PNG — Rekordlar","Production host — Həftəlik"};
        var image=new BufferedImage(1560,876,BufferedImage.TYPE_INT_RGB);
        var g=image.createGraphics();g.setColor(new Color(0x061831));g.fillRect(0,0,1560,876);
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        for(int i=0;i<panels.length;i++) {
            var panel=panels[i];double scale=Math.min(390.0/panel.getWidth(),844.0/panel.getHeight());
            int w=(int)Math.round(panel.getWidth()*scale),h=(int)Math.round(panel.getHeight()*scale);
            g.setColor(new Color(0xF4F7FF));g.drawString(labels[i],i*390+12,22);
            g.drawImage(panel,i*390+(390-w)/2,32,w,h,null);
        }
        g.dispose();ImageIO.write(image,"png",new File(dir.getParentFile(),"approved-comparison.png"));
    }
    public static void main(String[] args) throws Exception {
        var dir=new File(args[0]);int checked=0;
        for(var size:SIZES) {
            var account=read(dir,"account",size);var weekly=read(dir,"weekly",size);
            if(white(account,48,48,144,144)<30) throw new AssertionError("Missing back arrow: account-"+size);
            if(white(weekly,0,weekly.getHeight()-216,weekly.getWidth(),216)<100)
                throw new AssertionError("Missing primary border: weekly-"+size);
            for(var state:STATES) {
                var image=read(dir,state,size);
                if(image.getWidth()!=account.getWidth()||image.getHeight()!=account.getHeight())
                    throw new AssertionError("Wrong dimensions: "+state+"-"+size);
                sameRegion(account,image,48,48,144,144,"Back artwork "+state+"-"+size);
                if(!state.equals("account")&&!state.equals("editor")) {
                    sameRegion(weekly,image,0,0,image.getWidth(),288,"Records header "+state+"-"+size);
                    sameRegion(weekly,image,0,image.getHeight()-216,image.getWidth(),216,"Records footer "+state+"-"+size);
                }
                checked++;
            }
        }
        System.out.println("Capture integrity PASS: "+checked+" PNG; exact back region, records header/footer; visible arrow and primary border.");
        sheet(dir,"final-preview",new String[]{"account","weekly","all"},new String[]{"390-font1","390-font1","390-font1"},new String[]{"Profil — 390×844", "Həftəlik — 390×844", "Bütün vaxtlar — 390×844"});
        referenceSheet(dir);
        sheet(dir,"final-font2",new String[]{"account","editor","weekly"},new String[]{"320-font2","320-font2","320-font2"},new String[]{"Profil — 320×568, font 2", "Editor — 320×568, font 2", "Rekordlar — 320×568, font 2"});
    }
}
