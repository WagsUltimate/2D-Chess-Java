package chess;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;

public class RenderPreview {
    public static void main(String[] args) throws Exception {
        ChessPanel p = new ChessPanel(new ChessGame(), new JLabel());
        p.setSize(790,790);
        BufferedImage img = new BufferedImage(790,790,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        p.paint(graphics);
        graphics.dispose();
        ImageIO.write(img,"png", new File("preview.png"));
    }
}
