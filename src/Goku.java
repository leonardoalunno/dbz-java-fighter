import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;

public class Goku extends Fighter {

    private int beamEndX = -1;

    public Goku(int x, int y, int playerID) {
        super(x, y, playerID, ResourceManager.getInstance().gokuSpriteSheet);

        this.auraImage = ResourceManager.getInstance().auraBlue;

        this.facingRight = (playerID == 1);
        this.kiBlastImage = ResourceManager.getInstance().kiblastBlue;
        this.hudSrcY = 0;

        // 1. Definiamo scala e dimensioni
        this.scale = 1;
        this.baseWidth = (int)(72 * scale);
        this.baseHeight = (int)(163 * scale);

        // ==========================================
        // INSERISCI LE TRE RIGHE ESATTAMENTE QUI:
        int universalFloorY = y + 111;
        this.y = universalFloorY - this.baseHeight;
        this.groundY = this.y;
        // ==========================================


        this.speed = (int)(4 * scale);
        this.jumpStrength = -16 * scale;
        this.gravity = 0.35 * scale;

        this.MAX_SPECIAL_ENERGY = 2400;
        this.specialDrainRate = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;
        this.kiBlastKiCost = 80.0;
        this.kiBlastDamage = 10;
        this.specialDamage = 35;

        this.ki = MAX_KI; // Inizia con Ki pieno

        this.auraColor = new Color(0, 118, 255); // Blu Goku

        this.portraitSrcY = 0;
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
            reach = (int)(GamePanel.SCREEN_WIDTH * scale);
            boxHeight = (int)(40 * scale);
            offsetY = (int)(20 * scale);
        } else return null;

        int hX = facingRight ? x + baseWidth : x - reach;
        int hY = y + offsetY;
        return new Rectangle(hX, hY, reach, boxHeight);
    }

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
        // Posizioniamo l'esplosione al centro dell'avversario
        int expX = opponent.getX() + (opponent.baseWidth / 2);
        int expY = opponent.y + (opponent.baseHeight / 2);

        // Aggiungiamo l'effetto fumo usando il nuovo file universale
        opponent.activeEffects.add(new VisualEffect(
                ResourceManager.getInstance().commonVfx, // Il nuovo file caricato
                expX, expY,
                new int[]{0, 200},   // X1 e X2 che mi hai dato
                new int[]{0, 0},     // Y è sempre 0 per entrambi
                new int[]{142, 142}, // Larghezza (W)
                new int[]{120, 120}, // Altezza (H)
                6,                   // Velocità dell'animazione
                1.2 * scale          // Scala (leggermente più grande per il Final Flash)
        ));
    }

    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        super.update(keyH, opponent);

        if (isAttacking && attackType == 6 && attackTimer > SPECIAL_CHARGE) {
            beamEndX = facingRight ? GamePanel.SCREEN_WIDTH : 0;
            Rectangle hitbox = getAttackHitbox();
            if (hitbox != null && opponent != null && hitbox.intersects(opponent.getBounds())) {
                beamEndX = facingRight ? opponent.getX() : opponent.getX() + opponent.baseWidth;
            }
        }

        if (isMoving && !isJumping && !isCrouching && !isFlying && !isAttacking) {
            spriteCounter++;
            if (spriteCounter > (isAuraActive ? 3 : 5)) {
                spriteNum++;
                if (spriteNum > 6) spriteNum = 1;
                spriteCounter = 0;
            }
        } else if (isJumping) {
            spriteCounter++;
            if (spriteCounter > 8) {
                spriteNum++;
                if (spriteNum > 7) spriteNum = 7; // si ferma all'ultimo frame
                spriteCounter = 0;
            }
        } else {
            spriteNum = 1;
        }
    }


    @Override
    public void draw(Graphics2D g2d) {
        // Valori di default (Stance base) usando le variabili ereditate!
        srcX = 455; srcY = 553; srcW = 72; srcH = 163;



        if (hp <= 0) {
            srcW = 90; srcH = 91; srcY = 1450;
            // Aggiungiamo lo 0 come primo frame
            int[] koX = {0, 187, 300, 400, 505, 600, 710};
            srcX = koX[Math.min(endFrame - 1, 6)]; // Il limite dell'indice ora è 6
        }
        else if (isWinner) {
            if (isFlying) { srcW = 40; srcH = 100; srcY = 1642; int[] winFlyX = {0, 46}; srcX = winFlyX[endFrame - 1]; }
            else { srcW = 33; srcH = 90; srcY = 1649; int[] winGndX = {89, 128}; srcX = winGndX[endFrame - 1]; }
        }
        else if (isHit) {
            // Entrambi i frame sono sulla riga del KO (1450)
            srcY = 1450;
            srcW = 90;
            srcH = 91;

            // hitTimer va da 1 a 20
            if (hitTimer <= 10) {
                srcX = 0;   // Frame 1: Colpo ricevuto (impatto secco)
            } else {
                srcX = 300; // Frame 2: Reazione al colpo (rinculo)
            }
        }
        else if (isChargingAura) {
            // NUOVE COORDINATE PRECISE
            srcW = 42;
            srcH = 90;
            srcX = 0;
            srcY = 821;

            // Manteniamo il bellissimo effetto vibrazione/jitter
            shiftX += (int) (Math.random() * 3) - 1;
        }

        else if (isBlocking) {
            int blockFrame = Math.min(blockActiveTimer / 5, 2);
            if (isFlying || isJumping) {
                srcY = 1268; srcH = 140; srcW = 70;
                int[] blockX = {7, 81, 163};
                srcX = blockX[blockFrame];
            } else {
                srcY = 1015; srcH = 132; srcW = 95;
                int[] blockX = {3, 102, 201};
                srcX = blockX[blockFrame];
            }
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
        else if (isCrouching) {
            srcY = 1411; srcH = 162; srcW = 99;
            srcX = 8; // frame 1 — preparazione salto
        }
        else if (isFlying) {
            srcY = 8691; srcH = 156; srcW = 104;
            if      (flyNum == 1) srcX = 0;
            else if (flyNum == 2) srcX = 150;
            else if (flyNum == 3) srcX = 300;
            else if (flyNum == 4) srcX = 450;
            else if (flyNum == 5) srcX = 600;
        }
        else if (isJumping) {
            srcY = 1411; srcH = 162; srcW = 99;
            int[] jumpX = {122, 224, 328, 435, 550, 670, 780};
            srcX = jumpX[Math.min(spriteNum - 1, 6)];
        }
        else if (isMoving) {
            srcY = 885; srcH = 125; srcW = 106;
            int[] walkX = {3, 112, 221, 329, 437, 546};
            srcX = walkX[spriteNum - 1];
        }

        // --- MAGIA! Chiamiamo il rendering universale della classe padre ---
        drawFighterSprite(g2d);

        // DISEGNO KAMEHAMEHA (Ora riutilizza drawY, drawW, shiftX dal metodo padre)
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