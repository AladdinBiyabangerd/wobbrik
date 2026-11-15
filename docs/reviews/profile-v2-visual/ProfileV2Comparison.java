import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Review artifact assembly only; never alters the underlying native screenshots. */
class ProfileV2Comparison {
    public static void main(String[] args) throws Exception {
        String root = args[0];
        String dir = root + "/docs/reviews/profile-v2-visual/";
        BufferedImage pair = new BufferedImage(800, 805, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphics(pair);
        g.drawString("Approved reference", 10, 24);
        g.drawString("Native: same 390 x 761 viewport", 410, 24);
        draw(g, root + "/docs/design/profile-v2/profile-approved.png", 0, 40, 390, 761);
        draw(g, dir + "reference-profile.png", 400, 40, 390, 761);
        g.dispose();
        ImageIO.write(pair, "png", new File(dir + "p3-profile-comparison.png"));

        BufferedImage contact = new BufferedImage(900, 1850, BufferedImage.TYPE_INT_RGB);
        g = graphics(contact);
        String[] sizes = {"regular", "small", "font"};
        String[] states = {"profile", "edit", "picker"};
        for (int col = 0; col < 3; col++) for (int row = 0; row < 3; row++) {
            String name = sizes[col] + "-" + states[row];
            g.drawString(name, col * 300 + 4, row * 615 + 16);
            draw(g, dir + name + ".png", col * 300, row * 615 + 24, 292, 580);
        }
        g.dispose();
        ImageIO.write(contact, "png", new File(dir + "p3-contact.png"));
    }
    static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(6, 24, 49));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g;
    }
    static void draw(Graphics2D g, String path, int x, int y, int width, int height) throws Exception {
        BufferedImage image = ImageIO.read(new File(path));
        double scale = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
        g.drawImage(image, x, y, (int) Math.round(image.getWidth() * scale), (int) Math.round(image.getHeight() * scale), null);
    }
}
