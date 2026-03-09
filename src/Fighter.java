import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public abstract class Fighter {
    protected int x, y;
    protected int hp = 100;
    protected int ki = 0;
    protected BufferedImage spriteSheet;

    public Fighter(int x, int y, BufferedImage spriteSheet) {
        this.x = x;
        this.y = y;
        this.spriteSheet = spriteSheet;
    }

    // NUOVO: Permette di sapere dove si trova per girarsi
    public int getX() {
        return x;
    }

    public abstract void draw(Graphics2D g2d);

    // MODIFICA: Aggiunto il parametro "Fighter opponent"
    public abstract void update(KeyHandler keyH, Fighter opponent);
}