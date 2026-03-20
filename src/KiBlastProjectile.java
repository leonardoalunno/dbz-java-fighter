import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class KiBlastProjectile {
    public int px, py, pSpeed;
    public boolean pFacingRight;
    public int frame = 0, timer = 0;
    public BufferedImage img;
    public boolean isDead = false;
    public double pScaleMultiplier; // Conserva la scala del lanciatore

    public KiBlastProjectile(int x, int y, boolean dir, BufferedImage img, double scaleMultiplier) {
        this.px = x;
        this.py = y;
        this.pFacingRight = dir;
        this.img = img;
        this.pScaleMultiplier = scaleMultiplier;

        // Rapportiamo la velocità alla scala!
        this.pSpeed = (int)(11 * scaleMultiplier);
    }

    public void update(ArrayList<VisualEffect> activeEffects) {
        if (pFacingRight) px += pSpeed;
        else px -= pSpeed;

        timer++;
        if (timer > 3) {
            frame++;
            if (frame > 2) frame = 0;
            timer = 0;
        }

        // Scia: una scintilla ogni 8 tick, posizionata dietro il proiettile
        if (timer == 0) {
            // Calcola la larghezza approssimativa del proiettile per posizionare la scia dietro
            int projW = (int)(250 * 0.5 * pScaleMultiplier);
            int trailX = pFacingRight
                    ? px - (int)(15 * pScaleMultiplier)       // dietro = a sinistra
                    : px + projW + (int)(15 * pScaleMultiplier); // dietro = a destra del proiettile
            activeEffects.add(new VisualEffect(img,
                    trailX, py, new int[]{391}, new int[]{133}, new int[]{61}, new int[]{62},
                    3, 0.4 * pScaleMultiplier));
        }

        if (px > GamePanel.SCREEN_WIDTH + 100 || px < -100) {
            isDead = true;
        }
    }

    public void draw(Graphics2D g2d) {
        if (img != null) {
            int[] bX = {3, 261, 3};
            int[] bY = {3, 3, 130};
            int[] bW = {253, 245, 252};
            int[] bH = {124, 126, 105};

            // Rapportiamo le dimensioni
            double drawScale = 0.5 * pScaleMultiplier;
            int dW = (int)(bW[frame] * drawScale);
            int dH = (int)(bH[frame] * drawScale);

            int dX = px;
            int dY = py - dH / 2;

            if (pFacingRight) g2d.drawImage(img, dX, dY, dX + dW, dY + dH, bX[frame], bY[frame], bX[frame] + bW[frame], bY[frame] + bH[frame], null);
            else g2d.drawImage(img, dX + dW, dY, dX, dY + dH, bX[frame], bY[frame], bX[frame] + bW[frame], bY[frame] + bH[frame], null);
        }
    }
}