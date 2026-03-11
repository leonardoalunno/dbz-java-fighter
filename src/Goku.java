import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.AlphaComposite;

public class Goku extends Fighter {

    private int beamEndX = -1; // Variabile specifica per il raggio della Kamehameha

    public Goku(int x, int y, int playerID) {
        super(x, y, playerID, ResourceManager.getInstance().gokuSpriteSheet);
        this.groundY = y;
        this.facingRight = (playerID == 1);
        this.kiBlastImage = ResourceManager.getInstance().kiblastBlue;
        this.hudSrcY = 0; // Coordinata della faccia nell'HUD!

        this.scale = 1.3;
        this.baseWidth = (int)(48 * scale);
        this.baseHeight = (int)(86 * scale);
        this.speed = (int)(4 * scale);
        this.jumpStrength = -12 * scale;
        this.gravity = 0.5 * scale;

        this.MAX_SPECIAL_ENERGY = 2400;
        this.specialDrainRate = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;
        this.MAX_KI_SHOTS = 3;
        this.kiShotsAvailable = 3;
        this.kiBlastDamage = 10;
        this.specialDamage = 35;
    }

    @Override
    public Rectangle getAttackHitbox() {
        if (!isAttacking) return null;

        int reach = 0, boxHeight = (int)(20 * scale), offsetY = (int)(20 * scale);

        if (attackType == 1 || attackType == 4) {
            reach = (int)(36 * scale); offsetY = (int)(15 * scale);
        } else if (attackType == 2 || attackType == 3) {
            reach = (int)(30 * scale); offsetY = (int)(35 * scale);
        } else if (attackType == 6) {
            if (attackTimer <= SPECIAL_CHARGE) return null;
            reach = (int)(GamePanel.SCREEN_WIDTH * scale); // Kamehameha a tutto schermo!
            boxHeight = (int)(40 * scale);
            offsetY = (int)(20 * scale);
        } else return null;

        int hX = facingRight ? x + baseWidth : x - reach;
        int hY = y + offsetY;
        return new Rectangle(hX, hY, reach, boxHeight);
    }

    // --- HOOKS: I superpoteri univoci di Goku! ---
    @Override
    protected void spawnKiBlastVFX() {
        int handX = facingRight ? x + (int)(40 * scale) : x - (int)(10 * scale);
        int handY = y + (int)(25 * scale);
        activeEffects.add(new VisualEffect(kiBlastImage, handX, handY, new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60}, 7, 0.6 * scale));
    }

    @Override
    protected void fireKiBlastProjectile() {
        activeBlasts.add(new KiBlastProjectile(facingRight ? x + (int)(50 * scale) : x - (int)(10 * scale), y + (int)(25 * scale), facingRight, kiBlastImage, scale));
    }

    @Override
    protected void onSpecialAttackHit(Fighter opponent) {
        int expX = opponent.getX() + (opponent.baseWidth / 2);
        int expY = opponent.y + (opponent.baseHeight / 2);
        opponent.activeEffects.add(new VisualEffect(spriteSheet, expX, expY, new int[]{458, 658}, new int[]{1034, 1034}, new int[]{142, 142}, new int[]{120, 120}, 6, 1.0 * scale));
    }

    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        // 1. CHIAMIAMO LA CLASSE PADRE! Fa tutto lei: salti, aura, movimento, danni base.
        super.update(keyH, opponent);

        // 2. Logica Specifica: Calcolo dell'estensione del raggio della Kamehameha
        if (isAttacking && attackType == 6 && attackTimer > SPECIAL_CHARGE) {
            beamEndX = facingRight ? GamePanel.SCREEN_WIDTH : 0;
            Rectangle hitbox = getAttackHitbox();
            if (hitbox != null && opponent != null && hitbox.intersects(opponent.getBounds())) {
                beamEndX = facingRight ? opponent.getX() : opponent.getX() + opponent.baseWidth;
            }
        }

        // 3. Logica Specifica: Avanzamento dei frame dell'animazione (solo quando cammina)
        if (isMoving && !isJumping && !isCrouching && !isFlying && !isAttacking) {
            spriteCounter++;
            if (spriteCounter > (isAuraActive ? 3 : 5)) { spriteNum++; if (spriteNum > 3) spriteNum = 1; spriteCounter = 0; }
        } else spriteNum = 1;
    }

    @Override
    public void draw(Graphics2D g2d) {
        int srcX = 33, srcY = 0, srcW = 48, srcH = 86;

        if (isAuraActive && !isChargingAura) {
            int auraSrcX = 200, auraSrcY = 800, auraSrcW = 78, auraSrcH = 111;
            double auraScale = 1.25 * scale;
            int drawAuraW = (int)(auraSrcW * auraScale), drawAuraH = (int)(auraSrcH * auraScale);
            int drawAuraX = x + (baseWidth - drawAuraW) / 2, drawAuraY = y - (drawAuraH - baseHeight);
            g2d.drawImage(spriteSheet, facingRight ? drawAuraX : drawAuraX + drawAuraW, drawAuraY, facingRight ? drawAuraX + drawAuraW : drawAuraX, drawAuraY + drawAuraH, auraSrcX, auraSrcY, auraSrcX + auraSrcW, auraSrcY + auraSrcH, null);
        }

        if (hp <= 0) {
            srcW = 90; srcH = 91; srcY = 1450; int[] koX = {187, 300, 400, 505, 600, 710}; srcX = koX[Math.min(endFrame - 1, 5)];
        }
        else if (isWinner) {
            if (isFlying) { srcW = 40; srcH = 100; srcY = 1642; int[] winFlyX = {0, 46}; srcX = winFlyX[endFrame - 1]; }
            else { srcW = 33; srcH = 90; srcY = 1649; int[] winGndX = {89, 128}; srcX = winGndX[endFrame - 1]; }
        }
        else if (isHit) {
            // Primo frame dell'animazione di KO
            srcW = 90;
            srcH = 91;
            srcY = 1450;
            srcX = 187;
        }
        else if (isChargingAura) { srcW = 78; srcH = 111; srcX = 0; srcY = 800; }
        else if (isBlocking) {
            if (isFlying || isJumping) { srcW = 37; srcH = 87; srcX = 160; srcY = 2; }
            else { srcW = 41; srcH = 78; srcX = 0; srcY = 972; }
        }
        else if (isTeleporting) { srcW = 30; srcH = 91; srcY = 1748; int[] tX = {3, 38, 72, 111, 147, 185}; srcX = tX[Math.min(teleportFrame - 1, 5)]; }
        else if (isAttacking) {
            if (attackType == 1) { srcY = 442; srcW = 87; srcH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 0 : 130; }
            else if (attackType == 2) { srcY = 439; srcW = 80; srcH = 89; srcX = (attackTimer <= KICK_STARTUP) ? 242 : 325; }
            else if (attackType == 3) { srcY = 536; srcW = 65; srcH = 88; srcX = (attackTimer <= KICK_STARTUP) ? 0 : 100; }
            else if (attackType == 4) { srcY = 535; srcW = 64; srcH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 204 : 300; }
            else if (attackType == 5) { srcY = 972; srcW = 65; srcH = 84; srcX = (attackTimer <= 7) ? 202 : 300; }
            else if (attackType == 6) {
                if (attackTimer <= SPECIAL_CHARGE) { srcY = 1065; srcW = 54; srcH = 77; srcX = 0; }
                else { srcY = 1065; srcW = 54; srcH = 77; srcX = 59; }
            }
        }
        else if (isCrouching) { srcW = 48; srcH = 91; srcX = 0; srcY = 180; }
        else if (isFlying) {
            srcY = 273; srcH = 91;
            if (flyNum == 1) { srcX = 0; srcW = 48; } else if (flyNum == 2) { srcX = 101; srcW = 57; } else if (flyNum == 4) { srcX = 352; srcW = 76; }
        }
        else if (isJumping) { srcW = 48; srcH = 91; srcX = 72; srcY = 180; }
        else if (isMoving) {
            srcW = 48; srcH = 91;
            if (spriteNum == 1) { srcX = 0; srcY = 87; } else if (spriteNum == 2) { srcX = 55; srcY = 85; } else if (spriteNum == 3) { srcX = 103; srcY = 87; }
        }

        int drawW = (int)(srcW * scale), drawH = (int)(srcH * scale), drawY = y - (drawH - baseHeight);
        int shiftX = (baseWidth - drawW) / 2;
        if (isAttacking && !facingRight) shiftX = -(drawW - baseWidth);

        // --- EFFETTO LAMPEGGIO INVINCIBILITÀ ---
        if (isInvincible && invincibleTimer % 10 < 5) {
            // Rende il personaggio semi-trasparente
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        }

        // DISEGNO GOKU
        int sX1 = x + shiftX, sX2 = sX1 + drawW;
        if (!facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
        g2d.drawImage(spriteSheet, sX1, drawY, sX2, drawY + drawH, srcX, srcY, srcX + srcW, srcY + srcH, null);

        // RIPRISTINA L'OPACITÀ NORMALE SUBITO DOPO
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // DISEGNO KAMEHAMEHA
        if (isAttacking && attackType == 6 && attackTimer > SPECIAL_CHARGE) {
            int bodySrcX = 126, bodySrcY = 1069, bodyW = 146, bodyH = 64, headSrcX = 339, headSrcY = 1069, headW = 86, headH = 64;
            int drawBodyH = (int)(bodyH * scale), drawHeadW = (int)(headW * scale), drawHeadH = (int)(headH * scale);
            int beamY = drawY + (int)(6 * scale);
            int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? GamePanel.SCREEN_WIDTH : 0);

            if (facingRight) {
                int startX = x + shiftX + drawW;
                if (targetX - drawHeadW > startX) {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX - drawHeadW, beamY + drawBodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                    g2d.drawImage(spriteSheet, targetX - drawHeadW, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                } else g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                int startX = x + shiftX;
                if (targetX + drawHeadW < startX) {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX + drawHeadW, beamY + drawBodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                    g2d.drawImage(spriteSheet, targetX + drawHeadW, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                } else g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        }

        for (VisualEffect eff : activeEffects) eff.draw(g2d);
        for (KiBlastProjectile blast : activeBlasts) blast.draw(g2d);

        drawPlayerPin(g2d, x + shiftX, drawY, drawW);
        drawUniversalHUD(g2d, "KAMEHAMEHA");
    }
}