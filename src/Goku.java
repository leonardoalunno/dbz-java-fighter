import java.awt.*;
import java.util.ArrayList;

public class Goku extends Fighter {

    // --- IDENTITÀ DEL GIOCATORE ---
    private int playerID;

    // --- STATISTICHE BASE ---
    private int maxHP = 100;

    private int speed = 4;
    private int baseWidth = 48;
    private int baseHeight = 86;

    private int spriteCounter = 0;
    private int spriteNum = 1;
    private boolean isMoving = false;
    private boolean facingRight;

    // --- FISICA ---
    private double velocityY = 0;
    private double gravity = 0.5;
    private double jumpStrength = -12;
    private boolean isJumping = false;
    private int groundY;

    // --- STATI MOVIMENTO ---
    private boolean isCrouching = false;
    private int crouchTimer = 0;
    private final int CROUCH_DURATION = 3;

    private boolean isFlying = false;
    private int flyCooldown = 0;
    private int flyNum = 1;

    // --- PARATA ---
    private boolean isBlocking = false;

    private final int DASH_DISTANCE = 150;
    private final int DOUBLE_TAP_WINDOW = 15;
    private int tapTimerW = 0, tapTimerA = 0, tapTimerS = 0, tapTimerD = 0;
    private boolean prevW = false, prevA = false, prevS = false, prevD = false;

    private boolean isTeleporting = false;
    private int teleportPhase = 1;
    private int teleportFrame = 6;
    private int teleportCounter = 0;
    private int targetOffsetX = 0;
    private int targetOffsetY = 0;

    // --- ATTACCHI ---
    private boolean isAttacking = false;
    private int attackTimer = 0;
    private int attackType = 0;
    private final int ATTACK_DURATION = 15;
    private final int PUNCH_STARTUP = 5;
    private final int KICK_STARTUP = 7;
    private final int AERIAL_KICK_DURATION = 42;

    // --- KAMEHAMEHA ---
    private final int KAMEHAMEHA_CHARGE = 40;
    private final int KAMEHAMEHA_DURATION = 90;
    private double kamehamehaEnergy = 0;
    private final double MAX_KAMEHAMEHA_ENERGY = 2400;
    private final double KAMEHAMEHA_DRAIN_RATE = MAX_KAMEHAMEHA_ENERGY / KAMEHAMEHA_DURATION;

    // --- AURA SUPER SAIYAN ---
    private boolean isChargingAura = false;
    private int auraChargeTimer = 0;
    private final int AURA_CHARGE_DURATION = 30;

    private boolean isAuraActive = false;
    private final int AURA_DURATION = 600;
    private double auraEnergy = 0;
    private final double MAX_AURA_ENERGY = 1200;
    private final double AURA_DRAIN_RATE = MAX_AURA_ENERGY / AURA_DURATION;

    // --- KI BLAST ---
    private int kiShotsAvailable = 3;
    private final int MAX_KI_SHOTS = 3;
    private int kiRechargeTimer = 0;
    private final int RECHARGE_TIME = 300;
    private int shotCooldown = 0;
    private ArrayList<KiBlastProjectile> activeBlasts = new ArrayList<>();

    public Goku(int x, int y, int playerID) {
        super(x, y, ResourceManager.getInstance().gokuSpriteSheet);
        this.groundY = y;
        this.playerID = playerID;
        // Il Player 1 parte guardando a destra, il Player 2 parte guardando a sinistra!
        this.facingRight = (playerID == 1);
    }

    private class KiBlastProjectile {
        int px, py, pSpeed = 12;
        boolean pFacingRight;
        public KiBlastProjectile(int x, int y, boolean dir) {
            this.px = x; this.py = y; this.pFacingRight = dir;
        }
        public void update() {
            if (pFacingRight) px += pSpeed; else px -= pSpeed;
        }
    }

    private void startTeleport(int offX, int offY, boolean faceRight) {
        isTeleporting = true;
        teleportPhase = 1; teleportFrame = 6; teleportCounter = 0;
        targetOffsetX = offX; targetOffsetY = offY;
        facingRight = faceRight;
    }

    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        isMoving = false;

        // ==========================================
        // LETTURA COMANDI IN BASE AL PLAYER ID
        // ==========================================
        boolean input_up = (playerID == 1) ? keyH.p1_up : keyH.p2_up;
        boolean input_down = (playerID == 1) ? keyH.p1_down : keyH.p2_down;
        boolean input_left = (playerID == 1) ? keyH.p1_left : keyH.p2_left;
        boolean input_right = (playerID == 1) ? keyH.p1_right : keyH.p2_right;

        boolean input_punch = (playerID == 1) ? keyH.p1_punch : keyH.p2_punch;
        boolean input_kick = (playerID == 1) ? keyH.p1_kick : keyH.p2_kick;
        boolean input_kiBlast = (playerID == 1) ? keyH.p1_kiBlast : keyH.p2_kiBlast;
        boolean input_kamehameha = (playerID == 1) ? keyH.p1_kamehameha : keyH.p2_kamehameha;
        boolean input_fly = (playerID == 1) ? keyH.p1_fly : keyH.p2_fly;
        boolean input_aura = (playerID == 1) ? keyH.p1_aura : keyH.p2_aura;
        boolean input_block = (playerID == 1) ? keyH.p1_block : keyH.p2_block;

        // === DYNAMIC FACING ===
        // Se c'è un avversario e non stiamo facendo un'animazione che ci blocca, guardiamolo in faccia!
        if (!isAttacking && !isTeleporting && !isChargingAura && opponent != null) {
            this.facingRight = (this.x <= opponent.getX());
        }

        // --- GESTIONE PARATA ---
        if (input_block && !isAttacking && !isTeleporting && !isChargingAura) {
            isBlocking = true;
        } else {
            isBlocking = false;
        }

        // --- GESTIONE CARICAMENTO E BUFF AURA ---
        if (input_aura && auraEnergy >= MAX_AURA_ENERGY && !isAuraActive && !isChargingAura && !isAttacking && !isTeleporting && !isBlocking) {
            isChargingAura = true;
            auraChargeTimer = 0;
            velocityY = 0;
        }

        if (isChargingAura) {
            auraChargeTimer++;
            if (auraChargeTimer >= AURA_CHARGE_DURATION) {
                isChargingAura = false;
                isAuraActive = true;
            }
        }

        if (isAuraActive) {
            speed = 8;
            jumpStrength = -15;
            auraEnergy -= AURA_DRAIN_RATE;
            if (auraEnergy <= 0) {
                auraEnergy = 0;
                isAuraActive = false;
            }
        } else {
            speed = 4;
            jumpStrength = -12;
            if (auraEnergy < MAX_AURA_ENERGY && !isChargingAura) {
                auraEnergy++;
            }
        }

        // --- RICARICA KI BLAST E KAMEHAMEHA ---
        if (kiShotsAvailable < MAX_KI_SHOTS) {
            kiRechargeTimer++;
            if (kiRechargeTimer >= RECHARGE_TIME) { kiShotsAvailable++; kiRechargeTimer = 0; }
        }
        if (shotCooldown > 0) shotCooldown--;

        if (isAttacking && attackType == 6) {
            kamehamehaEnergy -= KAMEHAMEHA_DRAIN_RATE;
            if (kamehamehaEnergy < 0) kamehamehaEnergy = 0;
        } else if (kamehamehaEnergy < MAX_KAMEHAMEHA_ENERGY) {
            kamehamehaEnergy++;
        }

        if (tapTimerW > 0) tapTimerW--;
        if (tapTimerA > 0) tapTimerA--;
        if (tapTimerS > 0) tapTimerS--;
        if (tapTimerD > 0) tapTimerD--;

        boolean newW = input_up && !prevW;
        boolean newA = input_left && !prevA;
        boolean newS = input_down && !prevS;
        boolean newD = input_right && !prevD;

        if (!isChargingAura && !isBlocking) {
            if (!isTeleporting && !isAttacking) {
                if (newA) {
                    if (tapTimerA > 0) { startTeleport(-DASH_DISTANCE, 0, facingRight); tapTimerA = 0; }
                    else tapTimerA = DOUBLE_TAP_WINDOW;
                }
                if (newD) {
                    if (tapTimerD > 0) { startTeleport(DASH_DISTANCE, 0, facingRight); tapTimerD = 0; }
                    else tapTimerD = DOUBLE_TAP_WINDOW;
                }
                if (newW) {
                    if (tapTimerW > 0) { startTeleport(0, -DASH_DISTANCE, facingRight); tapTimerW = 0; }
                    else tapTimerW = DOUBLE_TAP_WINDOW;
                }
                if (newS && isFlying) {
                    if (tapTimerS > 0) { startTeleport(0, DASH_DISTANCE, facingRight); tapTimerS = 0; }
                    else tapTimerS = DOUBLE_TAP_WINDOW;
                }

                if (!isFlying && !isJumping && !isCrouching) {
                    if (input_punch) { isAttacking = true; attackTimer = 0; attackType = 1; }
                    else if (input_kick) { isAttacking = true; attackTimer = 0; attackType = 2; }
                } else if (isFlying) {
                    if (input_kick) { isAttacking = true; attackTimer = 0; attackType = 3; }
                    else if (input_punch) { isAttacking = true; attackTimer = 0; attackType = 4; }
                }

                if (input_kiBlast && kiShotsAvailable > 0 && shotCooldown == 0) {
                    isAttacking = true; attackTimer = 0; attackType = 5;
                    activeBlasts.add(new KiBlastProjectile(facingRight ? x + 50 : x - 10, y + 25, facingRight));
                    kiShotsAvailable--; shotCooldown = 30;
                }

                if (input_kamehameha && kamehamehaEnergy >= MAX_KAMEHAMEHA_ENERGY) {
                    isAttacking = true; attackTimer = 0; attackType = 6;
                }
            }

            if (isAttacking) {
                attackTimer++;
                int currentDuration = ATTACK_DURATION;
                if (attackType == 3) currentDuration = AERIAL_KICK_DURATION;
                else if (attackType == 6) currentDuration = KAMEHAMEHA_DURATION;
                if (attackTimer >= currentDuration) { isAttacking = false; attackType = 0; }
            }

            for (int i = 0; i < activeBlasts.size(); i++) {
                activeBlasts.get(i).update();
                if (activeBlasts.get(i).px > 1000 || activeBlasts.get(i).px < -100) { activeBlasts.remove(i); i--; }
            }

            if (isTeleporting) {
                teleportCounter++;
                if (teleportCounter > 2) {
                    teleportCounter = 0;
                    if (teleportPhase == 1) {
                        teleportFrame--;
                        if (teleportFrame < 1) {
                            x += targetOffsetX; y += targetOffsetY;
                            if (y > groundY) y = groundY;
                            if (isFlying && y > groundY - 40) y = groundY - 40;
                            teleportPhase = 2; teleportFrame = 1;
                        }
                    } else if (teleportPhase == 2) {
                        teleportFrame++;
                        if (teleportFrame > 6) isTeleporting = false;
                    }
                }
            } else {
                if (flyCooldown > 0) flyCooldown--;
                if (input_fly && flyCooldown == 0 && !isAttacking) {
                    isFlying = !isFlying; flyCooldown = 20;
                    if (isFlying) { velocityY = 0; isJumping = false; isCrouching = false; if (y >= groundY) y -= 40; }
                    else isJumping = true;
                }

                if (isFlying) {
                    if (input_up) y -= speed;
                    if (input_down) { y += speed; if (y > groundY - 40) y = groundY - 40; }
                    if (!isAttacking) {
                        // VOLO RELATIVO: calcola l'animazione corretta in base a dove sta guardando
                        if (input_left) {
                            x -= speed;
                            flyNum = facingRight ? 2 : 4;
                        }
                        else if (input_right) {
                            x += speed;
                            flyNum = facingRight ? 4 : 2;
                        }
                        else flyNum = 1;
                    } else {
                        if (input_left) x -= speed; if (input_right) x += speed;
                    }
                } else {
                    if (!isCrouching && !isAttacking) {
                        // CAMMINATA: Muoviti senza forzare il cambio di facing (ci pensa il Dynamic Facing su in alto)
                        if (input_left) { x -= speed; isMoving = true; }
                        if (input_right) { x += speed; isMoving = true; }
                    }
                    if (input_up && !isJumping && !isCrouching && !isAttacking) { isCrouching = true; crouchTimer = 0; }
                    if (isCrouching) {
                        crouchTimer++;
                        if (crouchTimer >= CROUCH_DURATION) { isCrouching = false; isJumping = true; velocityY = jumpStrength; }
                    }
                    if (isJumping) { velocityY += gravity; y += (int) velocityY; }
                }

                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; isFlying = false; }
                if (y < 0) { y = 0; velocityY = 0; }
                if (x < 0) x = 0;
                if (x > 800 - baseWidth) x = 800 - baseWidth;

                if (isMoving && !isJumping && !isCrouching && !isFlying && !isAttacking) {
                    spriteCounter++;
                    if (spriteCounter > (isAuraActive ? 3 : 5)) {
                        spriteNum++; if (spriteNum > 3) spriteNum = 1; spriteCounter = 0;
                    }
                } else spriteNum = 1;
            }
        } else {
            if (!isFlying && y < groundY) {
                isJumping = true;
                velocityY += gravity;
                y += (int) velocityY;
                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; }
            }
        }

        prevW = input_up; prevA = input_left; prevS = input_down; prevD = input_right;
    }

    @Override
    public void draw(Graphics2D g2d) {
        int srcX = 33, srcY = 0, drawW = baseWidth, drawH = baseHeight, drawY = y, shiftX = 0;

        for (KiBlastProjectile blast : activeBlasts) {
            int sW = 24, sH = 16, sX = 367, sY = 989;
            int bX1 = blast.px, bX2 = bX1 + sW;
            if (!blast.pFacingRight) { bX1 = bX2; bX2 = blast.px; }
            g2d.drawImage(spriteSheet, bX1, blast.py, bX2, blast.py + sH, sX, sY, sX + sW, sY + sH, null);
        }

        // --- DISEGNO DELL'AURA FIAMMEGGIANTE ---
        if (isAuraActive && !isChargingAura) {
            int auraW = 62;
            int auraH = 96;
            int auraSrcX = 70;
            int auraSrcY = 868;

            int drawAuraX = x + (baseWidth - auraW) / 2;
            int drawAuraY = y - (auraH - baseHeight);

            g2d.drawImage(spriteSheet,
                    facingRight ? drawAuraX : drawAuraX + auraW, drawAuraY,
                    facingRight ? drawAuraX + auraW : drawAuraX, drawAuraY + auraH,
                    auraSrcX, auraSrcY, auraSrcX + auraW, auraSrcY + auraH, null);
        }

        // --- DISEGNO GOKU ---
        if (isChargingAura) {
            drawW = 62; drawH = 96; srcX = 0; srcY = 868;
            drawY = y - (drawH - baseHeight);
            shiftX = (baseWidth - drawW) / 2;
        }
        else if (isBlocking) {
            drawW = 41; drawH = 78; srcX = 0; srcY = 972;
            drawY = y - (drawH - baseHeight);
            shiftX = (baseWidth - drawW) / 2;
        }
        else if (isTeleporting) {
            drawW = 30; drawH = 91; srcY = 1748;
            int[] tX = {3, 38, 72, 111, 147, 185};
            srcX = tX[Math.min(teleportFrame - 1, 5)];
            drawY = y - (drawH - baseHeight);
            shiftX = (baseWidth - drawW) / 2;
        }
        else if (isAttacking) {
            if (attackType == 1) { srcY = 442; drawW = 87; drawH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 0 : 130; }
            else if (attackType == 2) { srcY = 439; drawW = 80; drawH = 89; srcX = (attackTimer <= KICK_STARTUP) ? 242 : 325; }
            else if (attackType == 3) { srcY = 1346; drawW = 70; drawH = 94; int f = Math.min((attackTimer - 1) / 6, 6); int[] cX = {0, 109, 202, 302, 401, 500, 600}; srcX = cX[f]; }
            else if (attackType == 4) { srcY = 535; drawW = 64; drawH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 204 : 300; }
            else if (attackType == 5) { srcY = 972; drawW = 65; drawH = 84; srcX = (attackTimer <= 7) ? 202 : 300; }
            else if (attackType == 6) {
                if (attackTimer <= KAMEHAMEHA_CHARGE) {
                    srcY = 1065; drawW = 54; drawH = 77; srcX = 0;
                } else {
                    srcY = 1065; drawW = 54; drawH = 77; srcX = 59;
                    int bodySrcX = 126, bodySrcY = 1069, bodyW = 146, bodyH = 64;
                    int headSrcX = 339, headSrcY = 1069, headW = 86, headH = 64;
                    int beamY = drawY + 6;
                    if (facingRight) {
                        g2d.drawImage(spriteSheet, x + shiftX + drawW, beamY, 800 - headW, beamY + bodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                        g2d.drawImage(spriteSheet, 800 - headW, beamY, 800, beamY + headH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                    } else {
                        g2d.drawImage(spriteSheet, x + shiftX, beamY, headW, beamY + bodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                        g2d.drawImage(spriteSheet, headW, beamY, 0, beamY + headH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                    }
                }
            }
            drawY = y - (drawH - baseHeight);
            if (!facingRight) shiftX = -(drawW - baseWidth);
        }
        else if (isCrouching) { drawW = 48; drawH = 91; srcX = 0; srcY = 180; drawY = y - (drawH - baseHeight); }
        else if (isFlying) {
            srcY = 273; drawH = 91;
            if (flyNum == 1) { srcX = 0; drawW = 48; } else if (flyNum == 2) { srcX = 101; drawW = 57; } else if (flyNum == 4) { srcX = 352; drawW = 76; }
            drawY = y - (drawH - baseHeight);
        }
        else if (isJumping) { drawW = 48; drawH = 91; srcX = 72; srcY = 180; drawY = y - (drawH - baseHeight); }
        else if (isMoving) {
            drawW = 48; drawH = 91; drawY = y - (drawH - baseHeight);
            if (spriteNum == 1) { srcX = 0; srcY = 87; } else if (spriteNum == 2) { srcX = 55; srcY = 85; } else if (spriteNum == 3) { srcX = 103; srcY = 87; }
        }

        int sX1 = x + shiftX, sX2 = sX1 + drawW;
        if (!facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
        g2d.drawImage(spriteSheet, sX1, drawY, sX2, drawY + drawH, srcX, srcY, srcX + drawW, srcY + drawH, null);


        // ==========================================
        // --- DISEGNO NUOVO HUD SIMMETRICO (HD) ---
        // ==========================================
        ResourceManager resM = ResourceManager.getInstance();

        if (resM.hudFull != null) {
            double uiScale = 0.25;
            int hSrcX = 0, hSrcY = 0, hSrcW = 1142, hSrcH = 410;
            int hDrawW = (int)(hSrcW * uiScale);
            int hDrawH = (int)(hSrcH * uiScale);

            int hX = (playerID == 1) ? 20 : 800 - hDrawW - 20;
            int hY = 20;

            // 1. DISEGNO DELLE BARRE (Ricalibrate per riempire i vuoti)

            // --- BARRA HP ---
            double hpPercent = (double) hp / maxHP;
            int hBarW = (int)(860 * uiScale);
            int hBarH = (int)(92 * uiScale); // Aumentata da 80 a 92
            int currentHpW = (int) (hBarW * hpPercent);

            // Y spostata a 104 (da 110) per coprire il gap superiore
            int hBarX = (playerID == 1) ? hX + (int)(272 * uiScale) : hX + hDrawW - (int)(272 * uiScale) - currentHpW;
            int hBarY = hY + (int)(104 * uiScale);

            if (hpPercent > 0.50) g2d.setColor(java.awt.Color.GREEN);
            else if (hpPercent >= 0.21) g2d.setColor(java.awt.Color.ORANGE);
            else g2d.setColor(java.awt.Color.RED);
            g2d.fillRect(hBarX, hBarY, currentHpW, hBarH);

            // --- BARRA SPECIALE (Kamehameha) ---
            double sPercent = kamehamehaEnergy / MAX_KAMEHAMEHA_ENERGY;
            int sBarW = (int)(570 * uiScale);
            int sBarH = (int)(68 * uiScale); // Aumentata da 60 a 68
            int currentSW = (int) (sBarW * sPercent);

            // Y spostata a 216 (da 220)
            int sBarX = (playerID == 1) ? hX + (int)(292 * uiScale) : hX + hDrawW - (int)(292 * uiScale) - currentSW;
            int sBarY = hY + (int)(216 * uiScale);

            g2d.setColor(java.awt.Color.CYAN);
            g2d.fillRect(sBarX, sBarY, currentSW, sBarH);

            // --- BARRA AURA ---
            double aPercent = auraEnergy / MAX_AURA_ENERGY;
            int aBarW = (int)(325 * uiScale);
            int aBarH = (int)(68 * uiScale); // Aumentata da 60 a 68
            int currentAW = (int) (aBarW * aPercent);

            // Y spostata a 306 (da 310)
            int aBarX = (playerID == 1) ? hX + (int)(292 * uiScale) : hX + hDrawW - (int)(292 * uiScale) - currentAW;
            int aBarY = hY + (int)(306 * uiScale);

            g2d.setColor(new java.awt.Color(220, 20, 60));
            g2d.fillRect(aBarX, aBarY, currentAW, aBarH);

            // 1.5 DISEGNO ETICHETTE TESTUALI RELATIVE (SaiyanFont)
            if (resM.saiyanFont != null) {
                int labelMargin = 10; // Distanza fissa dal bordo di ogni barra

                // --- ETICHETTA HP ---
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 20f));
                String hpLabel = "HP";
                int hpLabelX;
                if (playerID == 1) {
                    // Fine della barra HP + margine
                    hpLabelX = hX + (int)(272 * uiScale) + hBarW + labelMargin;
                } else {
                    // Inizio della barra HP (specchiata) - larghezza testo - margine
                    hpLabelX = (hX + hDrawW - (int)(272 * uiScale) - hBarW) - g2d.getFontMetrics().stringWidth(hpLabel) - labelMargin;
                }

                if (hpPercent > 0.50) g2d.setColor(java.awt.Color.GREEN);
                else if (hpPercent >= 0.21) g2d.setColor(java.awt.Color.ORANGE);
                else g2d.setColor(java.awt.Color.RED);
                g2d.drawString(hpLabel, hpLabelX, hY + (int)(175 * uiScale));

                // --- ETICHETTA KAMEHAMEHA ---
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 16f));
                String specLabel = "KAMEHAMEHA";
                int specLabelX;
                if (playerID == 1) {
                    specLabelX = hX + (int)(292 * uiScale) + sBarW + labelMargin;
                } else {
                    specLabelX = (hX + hDrawW - (int)(292 * uiScale) - sBarW) - g2d.getFontMetrics().stringWidth(specLabel) - labelMargin;
                }
                g2d.setColor(java.awt.Color.CYAN);
                g2d.drawString(specLabel, specLabelX, hY + (int)(268 * uiScale));

                // --- ETICHETTA AURA ---
                String auraLabel = "AURA";
                int auraLabelX;
                if (playerID == 1) {
                    auraLabelX = hX + (int)(292 * uiScale) + aBarW + labelMargin;
                } else {
                    auraLabelX = (hX + hDrawW - (int)(292 * uiScale) - aBarW) - g2d.getFontMetrics().stringWidth(auraLabel) - labelMargin;
                }
                g2d.setColor(new java.awt.Color(220, 20, 60));
                g2d.drawString(auraLabel, auraLabelX, hY + (int)(358 * uiScale));
            }

            // 2. DISEGNO CORNICE HUD (Ribaltata per P2)
            if (playerID == 1) {
                // Disegno normale per Player 1
                g2d.drawImage(resM.hudFull, hX, hY, hX + hDrawW, hY + hDrawH, hSrcX, hSrcY, hSrcX + hSrcW, hSrcY + hSrcH, null);
            } else {
                // Disegno specchiato per Player 2 (invertendo hX1 e hX2)
                g2d.drawImage(resM.hudFull, hX + hDrawW, hY, hX, hY + hDrawH, hSrcX, hSrcY, hSrcX + hSrcW, hSrcY + hSrcH, null);
            }

            // 3. ICONE KI BLAST (Simmetria Totale, Inversione Ordine e Mirroring Immagine)
            int iW = 20, iH = 14;
            int iSpacing = iW + 5;
            int totalIconsWidth = (MAX_KI_SHOTS * iW) + ((MAX_KI_SHOTS - 1) * 5);

            int iStartX;
            if (playerID == 1) {
                iStartX = hX + (int)(272 * uiScale);
            } else {
                iStartX = (hX + hDrawW) - (int)(272 * uiScale) - totalIconsWidth;
            }

            int iStartY = hY + hDrawH + 5;

            for(int i = 0; i < MAX_KI_SHOTS; i++) {
                int renderIndex = (playerID == 1) ? i : (MAX_KI_SHOTS - 1 - i);
                int dX = iStartX + (renderIndex * iSpacing);

                int sIX = (i < kiShotsAvailable) ? 367 : 415;

                // --- IL TOCCO FINALE: MIRRORING ---
                int dX1 = dX;
                int dX2 = dX + iW;

                // Se è il Player 2, invertiamo le coordinate X di destinazione per specchiare l'icona
                if (playerID == 2) {
                    int temp = dX1;
                    dX1 = dX2;
                    dX2 = temp;
                }

                g2d.drawImage(spriteSheet, dX1, iStartY, dX2, iStartY + iH,
                        sIX, 989, sIX + 24, 989 + 16, null);
            }
        }
    }
}