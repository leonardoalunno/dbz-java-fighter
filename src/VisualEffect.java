import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class VisualEffect {
    public BufferedImage image;
    public int ex, ey;
    private int[] sX, sY, sW, sH, offX, offY; // Aggiunti gli array di Offset!
    private int frame = 0, timer = 0, speed;
    public boolean isDead = false;
    private double scale;

    // Costruttore "Base" (Retrocompatibile: mette gli offset a zero in automatico)
    public VisualEffect(BufferedImage image, int x, int y, int[] sx, int[] sy, int[] sw, int[] sh, int speed, double scale) {
        this(image, x, y, sx, sy, sw, sh, new int[sx.length], new int[sx.length], speed, scale);
    }

    // Costruttore "Avanzato" (Accetta gli array offX e offY per l'allineamento millimetrico)
    public VisualEffect(BufferedImage image, int x, int y, int[] sx, int[] sy, int[] sw, int[] sh, int[] offX, int[] offY, int speed, double scale) {
        this.image = image;
        this.ex = x; this.ey = y;
        this.sX = sx; this.sY = sy; this.sW = sw; this.sH = sh;
        this.offX = offX; this.offY = offY;
        this.speed = speed;
        this.scale = scale;
    }

    public void update() {
        timer++;
        if (timer > speed) {
            frame++;
            timer = 0;
            if (frame >= sX.length) isDead = true;
        }
    }

    public void draw(Graphics2D g2d) {
        if (!isDead && frame < sX.length && image != null) {
            int dW = (int)(sW[frame] * scale);
            int dH = (int)(sH[frame] * scale);

            // Calcoliamo la X e la Y includendo l'offset (moltiplicato per la scala)
            int dX = ex - dW / 2 + (int)(offX[frame] * scale);
            int dY = ey - dH / 2 + (int)(offY[frame] * scale);

            g2d.drawImage(image, dX, dY, dX + dW, dY + dH, sX[frame], sY[frame], sX[frame] + sW[frame], sY[frame] + sH[frame], null);
        }
    }
}