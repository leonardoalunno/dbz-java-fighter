import java.awt.*;

public class Vegeta extends Fighter {

    // Variabile per l'attacco speciale (es. Final Flash / Galick Gun)
    private int beamEndX = -1;

    public Vegeta(int x, int y, int playerID) {
        // Carichiamo lo spritesheet di Vegeta!
        super(x, y, playerID, ResourceManager.getInstance().vegetaSpriteSheet);
        this.groundY = y;
        this.facingRight = (playerID == 1);

        // Vegeta usa tipicamente colpi di ki gialli
        this.kiBlastImage = ResourceManager.getInstance().kiblastYellow;

        // ECCO LA COORDINATA CHE MI HAI DATO!
        this.hudSrcY = 533;

        // Statistiche base (le teniamo identiche a Goku per ora, ma potrai bilanciarle dopo)
        this.scale = 1.3;
        this.baseWidth = (int)(48 * scale);
        this.baseHeight = (int)(86 * scale);
        this.speed = (int)(4 * scale);
        this.jumpStrength = -12 * scale;
        this.gravity = 0.5 * scale;


        this.MAX_SPECIAL_ENERGY = 2400;
        this.MAX_KI_SHOTS = 3;
        this.kiShotsAvailable = 3;

        // Magari Vegeta fa un po' più di danno coi KiBlast e meno con i pugni? Scegli tu!
        this.punchDamage = 5;
        this.kickDamage = 8;
        this.kiBlastDamage = 12;

        // --- BILANCIAMENTO FINAL FLASH ---
        this.specialDamage = 50;         // Danno massiccio! (Goku ha 35)
        this.SPECIAL_CHARGE = 65;        // Caricamento più lento (Goku ha 40)
        this.SPECIAL_DURATION = 100;     // L'onda dura un po' di più a schermo (Goku ha 90)

        // Ricalcoliamo il consumo di energia in base alla nuova durata
        this.specialDrainRate = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;
    }

    @Override
    public Rectangle getAttackHitbox() {
        if (!isAttacking) return null;

        int reach = 0, boxHeight = (int)(20 * scale), offsetY = (int)(20 * scale);

        // TODO: Queste hitbox sono temporanee, basate su Goku.
        // Le adatteremo quando avrai le animazioni di Vegeta.
        if (attackType == 1 || attackType == 4) {
            reach = (int)(36 * scale); offsetY = (int)(15 * scale);
        } else if (attackType == 2 || attackType == 3) {
            reach = (int)(30 * scale); offsetY = (int)(35 * scale);
        } else if (attackType == 6) {
            if (attackTimer <= SPECIAL_CHARGE) return null;
            reach = (int)(GamePanel.SCREEN_WIDTH * scale); // Raggio a tutto schermo
            boxHeight = (int)(40 * scale);
            offsetY = (int)(20 * scale);
        } else return null;

        int hX = facingRight ? x + baseWidth : x - reach;
        int hY = y + offsetY;
        return new Rectangle(hX, hY, reach, boxHeight);
    }

    @Override
    protected void spawnKiBlastVFX() {
        // CARICAMENTO: Vegeta alza il braccio, la sfera appare sopra la testa!
        // La Y è negativa o quasi nulla per farla apparire in alto.
        int handX = facingRight ? x + (int)(22 * scale) : x + baseWidth - (int)(40 * scale);
        int handY = y - (int)(10 * scale);

        activeEffects.add(new VisualEffect(kiBlastImage, handX, handY, new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60}, 7, 0.6 * scale));
    }

    @Override
    protected void fireKiBlastProjectile() {
        // LANCIO: Vegeta abbassa il braccio in avanti, il colpo parte ad altezza petto/spalla
        int startX = facingRight ? x + (int)(55 * scale) : x - (int)(15 * scale);
        int startY = y + (int)(20 * scale);

        activeBlasts.add(new KiBlastProjectile(startX, startY, facingRight, kiBlastImage, scale));
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

        // Logica Raggio Speciale (Final Flash / Galick Gun)
        if (isAttacking && attackType == 6 && attackTimer > SPECIAL_CHARGE) {
            beamEndX = facingRight ? GamePanel.SCREEN_WIDTH : 0;
            Rectangle hitbox = getAttackHitbox();
            if (hitbox != null && opponent != null && hitbox.intersects(opponent.getBounds())) {
                beamEndX = facingRight ? opponent.getX() : opponent.getX() + opponent.baseWidth;
            }
        }

        if (isMoving && !isJumping && !isCrouching && !isFlying && !isAttacking) {
            spriteCounter++;
            if (spriteCounter > (isAuraActive ? 3 : 5)) { spriteNum++; if (spriteNum > 3) spriteNum = 1; spriteCounter = 0; }
        } else spriteNum = 1;
    }

    @Override
    public void draw(Graphics2D g2d) {
        // =========================================================
        // TODO: TUTTE QUESTE COORDINATE (srcX, srcY, srcW, srcH)
        // SONO QUELLE DI GOKU. DOVREMO SOSTITUIRLE CON QUELLE DI VEGETA!
        // =========================================================

        // --- STANCE BASE DI VEGETA ---
        srcX = 9;
        srcY = 6;
        srcW = 43;
        srcH = 75;


        if (isAuraActive && !isChargingAura) {
            int auraSrcX = 200, auraSrcY = 800, auraSrcW = 78, auraSrcH = 111;
            double auraScale = 1.25 * scale;
            int drawAuraW = (int)(auraSrcW * auraScale), drawAuraH = (int)(auraSrcH * auraScale);
            int drawAuraX = x + (baseWidth - drawAuraW) / 2, drawAuraY = y - (drawAuraH - baseHeight);
            g2d.drawImage(ResourceManager.getInstance().gokuSpriteSheet, facingRight ? drawAuraX : drawAuraX + drawAuraW, drawAuraY, facingRight ? drawAuraX + drawAuraW : drawAuraX, drawAuraY + drawAuraH, auraSrcX, auraSrcY, auraSrcX + auraSrcW, auraSrcY + auraSrcH, null);
        }

        // --- ANIMAZIONE KO VEGETA ---
        if (hp <= 0) {
            // Usiamo la riga dell'impatto (784) e le dimensioni base (75x79)
            srcW = 75;
            srcH = 79;
            srcY = 784;

            // La tua nuova sequenza di 7 frame
            int[] koX = {0, 200, 300, 400, 500, 600, 725};

            // endFrame viene gestito in Fighter.java e va da 1 a 7
            srcX = koX[Math.min(endFrame - 1, 6)];
        }
        else if (isWinner) {
            if (isFlying) {
                // --- VITTORIA IN VOLO ---
                // Se non hai frame extra, usiamo quelli a terra ma manteniamo isFlying
                srcW = 32; srcH = 80; srcY = 885;
                int[] winFlyX = {12, 53};
                srcX = winFlyX[Math.min(endFrame - 1, 1)];
            }
            else {
                // --- VITTORIA A TERRA ---
                srcW = 32; srcH = 80; srcY = 885;
                int[] winGndX = {12, 53};
                srcX = winGndX[Math.min(endFrame - 1, 1)];
            }
        }
        else if (isHit) {
            // Entrambi i frame condividono queste dimensioni
            srcY = 784;
            srcW = 75;
            srcH = 79;

            // Il timer 'hitTimer' va da 1 a 20.
            // Mostriamo il Frame 1 (X=0) per la prima metà e il Frame 2 (X=200) per la seconda.
            if (hitTimer <= 10) {
                srcX = 0;
            } else {
                srcX = 200;
            }
        }
        else if (isChargingAura) {
            // --- CARICAMENTO AURA ---
            srcW = 39;
            srcH = 77;
            srcX = 444;
            srcY = 627;

            // Effetto vibrazione: sposta lo sprite casualmente di 1 o 2 pixel
            shiftX += (int)(Math.random() * 3) - 1;
        }
        else if (isBlocking) {
            if (isFlying || isJumping) { srcW = 37; srcH = 87; srcX = 160; srcY = 2; }
            else { srcW = 41; srcH = 78; srcX = 0; srcY = 972; }
        }
        else if (isTeleporting) {
            // Scegliamo il frame base a seconda di dove si trova
            if (isFlying || isJumping) {
                // Frame base VOLO
                srcW = 32; srcH = 80; srcX = 12; srcY = 885;
            } else {
                // Frame base STANCE (A terra)
                srcW = 43; srcH = 73; srcX = 11; srcY = 10;
            }

            // --- EFFETTO DISSOLVENZA ---
            // teleportFrame in Fighter.java va da 6 (visibile) a 1 (invisibile)
            // Calcoliamo l'alpha: 6/6 = 1.0 (opaco), 1/6 = 0.16 (quasi trasparente)
            float alpha = (float) teleportFrame / 6.0f;

            // Applichiamo la trasparenza al Graphics2D
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, Math.min(1.0f, alpha))));
        }
        else if (isAttacking) {
            if (attackType == 1) {
                // --- PUGNO A TERRA ---
                srcY = 274;
                srcW = 70;
                srcH = 74;
                // Se è nei primi frame mostra il caricamento, poi l'impatto!
                srcX = (attackTimer <= PUNCH_STARTUP) ? 4 : 81;
            }
            else if (attackType == 2) {
                // --- CALCIO A TERRA ---
                srcY = 451;
                srcW = 41;
                srcH = 75;
                // Se è nei primi frame mostra il caricamento, poi l'impatto!
                srcX = (attackTimer <= KICK_STARTUP) ? 8 : 70;
            }
            else if (attackType == 3) {
                // --- CALCIO IN VOLO ---
                srcY = 282;
                srcW = 66;
                srcH = 72;
                // Se è nei primi frame mostra il caricamento, poi l'impatto!
                srcX = (attackTimer <= KICK_STARTUP) ? 208 : 290;
            }
            else if (attackType == 4) {
                // --- PUGNO IN VOLO ---
                srcY = 451;
                srcW = 58;
                srcH = 71;
                // Se è nei primi frame mostra il caricamento, poi l'impatto!
                srcX = (attackTimer <= PUNCH_STARTUP) ? 206 : 280;
            }
            else if (attackType == 5) {
                // --- LANCIO KI BLAST ---
                srcY = 615;
                srcW = 67;
                srcH = 80;
                // Il timer del Ki Blast usa un valore fisso di 7 frame per il caricamento
                srcX = (attackTimer <= 7) ? 124 : 220;
            }
            else if (attackType == 6) {
                // --- FINAL FLASH ---
                srcY = 1067;
                srcW = 53;
                srcH = 76;

                // Usa il frame X=10 per tutto il caricamento (SPECIAL_CHARGE = 65)
                // e passa al frame X=80 per il lancio dell'onda
                if (attackTimer <= SPECIAL_CHARGE) {
                    srcX = 10;
                } else {
                    srcX = 80;
                }
            }
        }
        else if (isCrouching) {
            srcW = 39;
            srcH = 83;
            srcX = 5;
            srcY = 176;
        }
        else if (isFlying) {
            // L'altezza e la larghezza sono uguali per tutti e tre i frame
            srcW = 41;
            srcH = 79;

            if (flyNum == 1) {
                // Volo Stazionario / Su e Giù
                srcX = 210;
                srcY = 6;
            } else if (flyNum == 2) {
                // Volo in Avanti
                srcX = 231;
                srcY = 190;
            } else if (flyNum == 4) {
                // Volo all'Indietro
                srcX = 186;
                srcY = 190;
            }
        }
        else if (isJumping) {
            srcW = 39;
            srcH = 83;
            srcX = 50;
            srcY = 176;
        }
        else if (isMoving) {
            if (spriteNum == 1 || spriteNum == 3) {
                // Frame 1
                srcW = 39;
                srcH = 82;
                srcX = 7;
                srcY = 89;
            } else if (spriteNum == 2) {
                // Frame 2
                srcW = 39;
                srcH = 82;
                srcX = 55;
                srcY = 89;
            }
        }

        // Il metodo magico della classe padre disegna il personaggio base
        drawFighterSprite(g2d);

        // --- DISEGNO FINAL FLASH (GIALLO) ---
        if (isAttacking && attackType == 6 && attackTimer > SPECIAL_CHARGE) {
            // Coordinate che mi hai passato
            int bodySrcX = 323, bodySrcY = 1278, bodyW = 36, bodyH = 22;
            int headSrcX = 362, headSrcY = 1249, headW = 86, headH = 80;

            int drawBodyH = (int)(bodyH * scale);
            int drawHeadW = (int)(headW * scale);
            int drawHeadH = (int)(headH * scale);

            // Calcolo per centrare il raggio rispetto all'altezza della testa
            int beamY = drawY + (int)(0 * scale); // Punto di origine della testa
            int bodyOffsetY = (drawHeadH - drawBodyH) / 2; // Allineamento centrale per il corpo

            int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? GamePanel.SCREEN_WIDTH : 0);

            if (facingRight) {
                int startX = x + shiftX + drawW;
                if (targetX - drawHeadW > startX) {
                    // Disegna il corpo centrato
                    g2d.drawImage(spriteSheet, startX, beamY + bodyOffsetY, targetX - drawHeadW, beamY + bodyOffsetY + drawBodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                    // Disegna la testa
                    g2d.drawImage(spriteSheet, targetX - drawHeadW, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                } else {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                }
            } else {
                int startX = x + shiftX;
                if (targetX + drawHeadW < startX) {
                    // Disegna il corpo centrato
                    g2d.drawImage(spriteSheet, startX, beamY + bodyOffsetY, targetX + drawHeadW, beamY + bodyOffsetY + drawBodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                    // Disegna la testa
                    g2d.drawImage(spriteSheet, targetX + drawHeadW, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                } else {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                }
            }
        }

        // Effetti e HUD
        for (VisualEffect eff : activeEffects) eff.draw(g2d);
        for (KiBlastProjectile blast : activeBlasts) blast.draw(g2d);

        drawPlayerPin(g2d, x + shiftX, drawY, drawW);
        drawUniversalHUD(g2d, "FINAL FLASH");

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}