import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class Fighter {

    // --- VARIABILI COMUNI A TUTTI I PERSONAGGI ---
    protected int x, y;
    protected int playerID;
    protected BufferedImage spriteSheet;

    public int hp = 100;
    protected int maxHP = 100;
    protected int speed = 4;
    protected int baseWidth = 48;
    protected int baseHeight = 86;

    protected boolean facingRight;
    protected int groundY;

    // --- STATI UNIVERSALI ---
    protected boolean isMoving = false;
    protected boolean isJumping = false;
    protected boolean isCrouching = false;
    protected boolean isFlying = false;
    protected boolean isBlocking = false;
    protected boolean isAttacking = false;
    protected boolean isTeleporting = false;
    protected boolean isChargingAura = false;
    protected boolean isAuraActive = false;

    // --- FISICA ---
    protected double velocityY = 0;
    protected double gravity = 0.5;
    protected double jumpStrength = -12;

    // --- TELETRASPORTO (Logica Universale) ---
    protected final int DASH_DISTANCE = 150;
    protected final int DOUBLE_TAP_WINDOW = 15;
    protected int tapTimerW = 0, tapTimerA = 0, tapTimerS = 0, tapTimerD = 0;
    protected boolean prevW = false, prevA = false, prevS = false, prevD = false;
    protected int teleportPhase = 1;
    protected int teleportFrame = 6;
    protected int teleportCounter = 0;
    protected int targetOffsetX = 0;
    protected int targetOffsetY = 0;

    // --- AURA E RISORSE UNIVERSALI ---
    protected int auraChargeTimer = 0;
    protected final int AURA_CHARGE_DURATION = 30;
    protected final int AURA_DURATION = 600;
    protected double auraEnergy = 0;
    protected double MAX_AURA_ENERGY = 1200;
    protected double AURA_DRAIN_RATE = MAX_AURA_ENERGY / AURA_DURATION;

    // --- ENERGIA MOSSA SPECIALE E KI BLAST ---
    protected double specialEnergy = 0;
    protected double MAX_SPECIAL_ENERGY = 2400; // Sovrascrivibile per ogni personaggio
    protected double specialDrainRate;

    protected int kiShotsAvailable = 3;
    protected int MAX_KI_SHOTS = 3; // Sovrascrivibile (es. Vegeta ne avrà 5)
    protected int kiRechargeTimer = 0;
    protected int RECHARGE_TIME = 300;
    protected int shotCooldown = 0;

    // --- SISTEMA DANNI E HITBOX ---
    protected int punchDamage = 5;
    protected int kickDamage = 8;
    protected int kiBlastDamage; // Definito dal personaggio
    protected int specialDamage; // Definito dal personaggio
    protected boolean hasHit = false; // Evita di fare danno 60 volte al secondo!

    public Fighter(int x, int y, int playerID, BufferedImage spriteSheet) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        this.spriteSheet = spriteSheet;
    }

    public int getX() { return x; }

    // Hitbox del "Corpo" per impedire che si attraversino
    public Rectangle getBounds() {
        return new Rectangle(x, y, baseWidth, baseHeight);
    }

    // Hitbox per gli attacchi (che ogni personaggio calcolerà a modo suo)
    public abstract Rectangle getAttackHitbox();

    // Ricezione del danno con logica di Parata
    public void takeDamage(int amount) {
        if (isBlocking) {
            // Se in parata, subisce solo un quarto dei danni
            hp -= Math.max(1, amount / 4);
        } else {
            hp -= amount;
        }
        if (hp < 0) hp = 0; // Impedisce alla barra della vita di andare in negativo
    }

    protected void startTeleport(int offX, int offY, boolean faceRight) {
        isTeleporting = true;
        teleportPhase = 1;
        teleportFrame = 6;
        teleportCounter = 0;
        targetOffsetX = offX;
        targetOffsetY = offY;
        facingRight = faceRight;
    }

    public abstract void draw(Graphics2D g2d);
    public abstract void update(KeyHandler keyH, Fighter opponent);

    // ==========================================
    // --- HUD UNIVERSALE SIMMETRICO ---
    // ==========================================
    protected void drawUniversalHUD(Graphics2D g2d, String specialName) {
        ResourceManager resM = ResourceManager.getInstance();

        if (resM.hudFull != null) {
            double uiScale = 0.25;
            int hSrcX = 0, hSrcY = 0, hSrcW = 1142, hSrcH = 410;
            int hDrawW = (int)(hSrcW * uiScale);
            int hDrawH = (int)(hSrcH * uiScale);

            int hX = (playerID == 1) ? 20 : 800 - hDrawW - 20;
            int hY = 20;

            // 1. BARRE DINAMICHE
            double hpPercent = (double) hp / maxHP;
            int hBarW = (int)(860 * uiScale);
            int hBarH = (int)(92 * uiScale);
            int currentHpW = (int) (hBarW * hpPercent);

            int hBarX = (playerID == 1) ? hX + (int)(272 * uiScale) : hX + hDrawW - (int)(272 * uiScale) - currentHpW;
            int hBarY = hY + (int)(104 * uiScale);

            if (hpPercent > 0.50) g2d.setColor(Color.GREEN);
            else if (hpPercent >= 0.21) g2d.setColor(Color.ORANGE);
            else g2d.setColor(Color.RED);
            g2d.fillRect(hBarX, hBarY, currentHpW, hBarH);

            double sPercent = specialEnergy / MAX_SPECIAL_ENERGY;
            int sBarW = (int)(570 * uiScale);
            int sBarH = (int)(68 * uiScale);
            int currentSW = (int) (sBarW * sPercent);

            int sBarX = (playerID == 1) ? hX + (int)(292 * uiScale) : hX + hDrawW - (int)(292 * uiScale) - currentSW;
            int sBarY = hY + (int)(216 * uiScale);

            g2d.setColor(Color.CYAN);
            g2d.fillRect(sBarX, sBarY, currentSW, sBarH);

            double aPercent = auraEnergy / MAX_AURA_ENERGY;
            int aBarW = (int)(325 * uiScale);
            int aBarH = (int)(68 * uiScale);
            int currentAW = (int) (aBarW * aPercent);

            int aBarX = (playerID == 1) ? hX + (int)(292 * uiScale) : hX + hDrawW - (int)(292 * uiScale) - currentAW;
            int aBarY = hY + (int)(306 * uiScale);

            g2d.setColor(new Color(220, 20, 60));
            g2d.fillRect(aBarX, aBarY, currentAW, aBarH);

            // 1.5 ETICHETTE (Nome speciale dinamico)
            if (resM.saiyanFont != null) {
                int labelMargin = 10;

                // HP
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 20f));
                String hpLabel = "HP";
                int hpLabelX = (playerID == 1) ? hX + (int)(272 * uiScale) + hBarW + labelMargin
                        : (hX + hDrawW - (int)(272 * uiScale) - hBarW) - g2d.getFontMetrics().stringWidth(hpLabel) - labelMargin;

                if (hpPercent > 0.50) g2d.setColor(Color.GREEN);
                else if (hpPercent >= 0.21) g2d.setColor(Color.ORANGE);
                else g2d.setColor(Color.RED);
                g2d.drawString(hpLabel, hpLabelX, hY + (int)(175 * uiScale));

                // SPECIAL NAME
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 16f));
                int specLabelX = (playerID == 1) ? hX + (int)(292 * uiScale) + sBarW + labelMargin
                        : (hX + hDrawW - (int)(292 * uiScale) - sBarW) - g2d.getFontMetrics().stringWidth(specialName) - labelMargin;
                g2d.setColor(Color.CYAN);
                g2d.drawString(specialName, specLabelX, hY + (int)(268 * uiScale));

                // AURA
                String auraLabel = "AURA";
                int auraLabelX = (playerID == 1) ? hX + (int)(292 * uiScale) + aBarW + labelMargin
                        : (hX + hDrawW - (int)(292 * uiScale) - aBarW) - g2d.getFontMetrics().stringWidth(auraLabel) - labelMargin;
                g2d.setColor(new Color(220, 20, 60));
                g2d.drawString(auraLabel, auraLabelX, hY + (int)(358 * uiScale));
            }

            // 2. CORNICE HUD
            if (playerID == 1) {
                g2d.drawImage(resM.hudFull, hX, hY, hX + hDrawW, hY + hDrawH, hSrcX, hSrcY, hSrcX + hSrcW, hSrcY + hSrcH, null);
            } else {
                g2d.drawImage(resM.hudFull, hX + hDrawW, hY, hX, hY + hDrawH, hSrcX, hSrcY, hSrcX + hSrcW, hSrcY + hSrcH, null);
            }

            // 3. ICONE KI BLAST
            int iW = 20, iH = 14;
            int iSpacing = iW + 5;
            int totalIconsWidth = (MAX_KI_SHOTS * iW) + ((MAX_KI_SHOTS - 1) * 5);

            int iStartX = (playerID == 1) ? hX + (int)(272 * uiScale)
                    : (hX + hDrawW) - (int)(272 * uiScale) - totalIconsWidth;
            int iStartY = hY + hDrawH + 5;

            for(int i = 0; i < MAX_KI_SHOTS; i++) {
                int renderIndex = (playerID == 1) ? i : (MAX_KI_SHOTS - 1 - i);
                int dX = iStartX + (renderIndex * iSpacing);
                int sIX = (i < kiShotsAvailable) ? 367 : 415;

                int dX1 = dX;
                int dX2 = dX + iW;
                if (playerID == 2) { int temp = dX1; dX1 = dX2; dX2 = temp; }

                g2d.drawImage(spriteSheet, dX1, iStartY, dX2, iStartY + iH, sIX, 989, sIX + 24, 989 + 16, null);
            }
        }
    }
}