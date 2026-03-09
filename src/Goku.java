import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Goku extends Fighter {

    // --- ANIMAZIONI SPECIFICHE ---
    private int spriteCounter = 0;
    private int spriteNum = 1;
    private int crouchTimer = 0;
    private final int CROUCH_DURATION = 3;
    private int flyCooldown = 0;
    private int flyNum = 1;

    // --- ATTACCHI SPECIFICI DI GOKU ---
    private int attackTimer = 0;
    private int attackType = 0;
    private final int ATTACK_DURATION = 15;
    private final int PUNCH_STARTUP = 5;
    private final int KICK_STARTUP = 7;
    private final int AERIAL_KICK_DURATION = 42;

    // --- KAMEHAMEHA ---
    private final int SPECIAL_CHARGE = 40;
    private final int SPECIAL_DURATION = 90;

    // --- KI BLAST SPECIFICI ---
    private ArrayList<KiBlastProjectile> activeBlasts = new ArrayList<>();

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

    public Goku(int x, int y, int playerID) {
        // Chiama il motore universale in Fighter.java
        super(x, y, playerID, ResourceManager.getInstance().gokuSpriteSheet);

        // Imposta i valori iniziali per Goku
        this.groundY = y;
        this.facingRight = (playerID == 1);

        // Calcola il consumo della Kamehameha
        this.MAX_SPECIAL_ENERGY = 2400;
        this.specialDrainRate = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;
        this.MAX_KI_SHOTS = 3;
        this.kiShotsAvailable = 3;

        // --- DANNI SPECIFICI DI GOKU ---
        this.kiBlastDamage = 10;
        this.specialDamage = 35;
    }

    @Override
    public Rectangle getAttackHitbox() {
        if (!isAttacking) return null;

        int reach = 0;
        int boxHeight = 20;
        int offsetY = 20;

        if (attackType == 1 || attackType == 4) { // Pugno a terra o in volo
            reach = 30; offsetY = 15;
        } else if (attackType == 2 || attackType == 3) { // Calcio a terra o in volo
            reach = 40; offsetY = 35;
        } else if (attackType == 6) { // KAMEHAMEHA
            if (attackTimer <= SPECIAL_CHARGE) return null; // Nessun danno mentre carica
            reach = 800; // Copre tutto lo schermo
            boxHeight = 40;
            offsetY = 20;
        } else {
            return null; // I Ki Blast sono proiettili indipendenti
        }

        int hX = facingRight ? x + baseWidth : x - reach;
        int hY = y + offsetY;
        return new Rectangle(hX, hY, reach, boxHeight);
    }

    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        isMoving = false;

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

        if (!isAttacking && !isTeleporting && !isChargingAura && opponent != null) {
            this.facingRight = (this.x <= opponent.getX());
        }

        if (input_block && !isAttacking && !isTeleporting && !isChargingAura) isBlocking = true;
        else isBlocking = false;

        if (input_aura && auraEnergy >= MAX_AURA_ENERGY && !isAuraActive && !isChargingAura && !isAttacking && !isTeleporting && !isBlocking) {
            isChargingAura = true; auraChargeTimer = 0; velocityY = 0;
        }

        if (isChargingAura) {
            auraChargeTimer++;
            if (auraChargeTimer >= AURA_CHARGE_DURATION) { isChargingAura = false; isAuraActive = true; }
        }

        if (isAuraActive) {
            speed = 8; jumpStrength = -15; auraEnergy -= AURA_DRAIN_RATE;
            if (auraEnergy <= 0) { auraEnergy = 0; isAuraActive = false; }
        } else {
            speed = 4; jumpStrength = -12;
            if (auraEnergy < MAX_AURA_ENERGY && !isChargingAura) auraEnergy++;
        }

        if (kiShotsAvailable < MAX_KI_SHOTS) {
            kiRechargeTimer++;
            if (kiRechargeTimer >= RECHARGE_TIME) { kiShotsAvailable++; kiRechargeTimer = 0; }
        }
        if (shotCooldown > 0) shotCooldown--;

        if (isAttacking && attackType == 6) {
            specialEnergy -= specialDrainRate;
            if (specialEnergy < 0) specialEnergy = 0;
        } else if (specialEnergy < MAX_SPECIAL_ENERGY) {
            specialEnergy++;
        }

        if (tapTimerW > 0) tapTimerW--;
        if (tapTimerA > 0) tapTimerA--;
        if (tapTimerS > 0) tapTimerS--;
        if (tapTimerD > 0) tapTimerD--;

        boolean newW = input_up && !prevW; boolean newA = input_left && !prevA;
        boolean newS = input_down && !prevS; boolean newD = input_right && !prevD;

        if (!isChargingAura && !isBlocking) {
            if (!isTeleporting && !isAttacking) {
                if (newA) { if (tapTimerA > 0) { startTeleport(-DASH_DISTANCE, 0, facingRight); tapTimerA = 0; } else tapTimerA = DOUBLE_TAP_WINDOW; }
                if (newD) { if (tapTimerD > 0) { startTeleport(DASH_DISTANCE, 0, facingRight); tapTimerD = 0; } else tapTimerD = DOUBLE_TAP_WINDOW; }
                if (newW) { if (tapTimerW > 0) { startTeleport(0, -DASH_DISTANCE, facingRight); tapTimerW = 0; } else tapTimerW = DOUBLE_TAP_WINDOW; }
                if (newS && isFlying) { if (tapTimerS > 0) { startTeleport(0, DASH_DISTANCE, facingRight); tapTimerS = 0; } else tapTimerS = DOUBLE_TAP_WINDOW; }

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

                if (input_kamehameha && specialEnergy >= MAX_SPECIAL_ENERGY) {
                    isAttacking = true; attackTimer = 0; attackType = 6;
                }
            }

            if (isAttacking) {
                if (attackTimer == 0) hasHit = false; // Resetta la flag di colpo a segno
                attackTimer++;

                // Calcolo Danni Corpo a Corpo e Kamehameha
                if (!hasHit && opponent != null) {
                    Rectangle hitbox = getAttackHitbox();
                    if (hitbox != null && hitbox.intersects(opponent.getBounds())) {
                        int damage = 0;
                        if (attackType == 1 || attackType == 4) damage = punchDamage;
                        else if (attackType == 2 || attackType == 3) damage = kickDamage;
                        else if (attackType == 6) damage = specialDamage;

                        if (damage > 0) {
                            opponent.takeDamage(damage);
                            hasHit = true; // Colpito! Evita di infliggere danno al prossimo frame
                        }
                    }
                }

                int currentDuration = ATTACK_DURATION;
                if (attackType == 3) currentDuration = AERIAL_KICK_DURATION;
                else if (attackType == 6) currentDuration = SPECIAL_DURATION;
                if (attackTimer >= currentDuration) { isAttacking = false; attackType = 0; }
            }

            // Calcolo Danni e Movimento Ki Blast
            for (int i = 0; i < activeBlasts.size(); i++) {
                KiBlastProjectile blast = activeBlasts.get(i);
                blast.update();

                Rectangle blastHitbox = new Rectangle(blast.px, blast.py, 24, 16);
                if (opponent != null && blastHitbox.intersects(opponent.getBounds())) {
                    opponent.takeDamage(kiBlastDamage);
                    activeBlasts.remove(i);
                    i--;
                    continue;
                }

                if (blast.px > 1000 || blast.px < -100) { activeBlasts.remove(i); i--; }
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
                        if (input_left) {
                            x -= speed;
                            if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; // RIMBALZA!
                            flyNum = facingRight ? 2 : 4;
                        }
                        else if (input_right) {
                            x += speed;
                            if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; // RIMBALZA!
                            flyNum = facingRight ? 4 : 2;
                        }
                        else flyNum = 1;
                    } else {
                        if (input_left) {
                            x -= speed;
                            if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; // RIMBALZA!
                        }
                        if (input_right) {
                            x += speed;
                            if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; // RIMBALZA!
                        }
                    }
                } else {
                    if (!isCrouching && !isAttacking) {
                        if (input_left) {
                            x -= speed;
                            if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; // RIMBALZA!
                            isMoving = true;
                        }
                        if (input_right) {
                            x += speed;
                            if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; // RIMBALZA!
                            isMoving = true;
                        }
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
                isJumping = true; velocityY += gravity; y += (int) velocityY;
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

        if (isAuraActive && !isChargingAura) {
            int auraW = 62, auraH = 96, auraSrcX = 70, auraSrcY = 868;
            int drawAuraX = x + (baseWidth - auraW) / 2, drawAuraY = y - (auraH - baseHeight);
            g2d.drawImage(spriteSheet, facingRight ? drawAuraX : drawAuraX + auraW, drawAuraY,
                    facingRight ? drawAuraX + auraW : drawAuraX, drawAuraY + auraH,
                    auraSrcX, auraSrcY, auraSrcX + auraW, auraSrcY + auraH, null);
        }

        if (isChargingAura) { drawW = 62; drawH = 96; srcX = 0; srcY = 868; drawY = y - (drawH - baseHeight); shiftX = (baseWidth - drawW) / 2; }
        else if (isBlocking) { drawW = 41; drawH = 78; srcX = 0; srcY = 972; drawY = y - (drawH - baseHeight); shiftX = (baseWidth - drawW) / 2; }
        else if (isTeleporting) { drawW = 30; drawH = 91; srcY = 1748; int[] tX = {3, 38, 72, 111, 147, 185}; srcX = tX[Math.min(teleportFrame - 1, 5)]; drawY = y - (drawH - baseHeight); shiftX = (baseWidth - drawW) / 2; }
        else if (isAttacking) {
            if (attackType == 1) { srcY = 442; drawW = 87; drawH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 0 : 130; }
            else if (attackType == 2) { srcY = 439; drawW = 80; drawH = 89; srcX = (attackTimer <= KICK_STARTUP) ? 242 : 325; }
            else if (attackType == 3) { srcY = 1346; drawW = 70; drawH = 94; int f = Math.min((attackTimer - 1) / 6, 6); int[] cX = {0, 109, 202, 302, 401, 500, 600}; srcX = cX[f]; }
            else if (attackType == 4) { srcY = 535; drawW = 64; drawH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 204 : 300; }
            else if (attackType == 5) { srcY = 972; drawW = 65; drawH = 84; srcX = (attackTimer <= 7) ? 202 : 300; }
            else if (attackType == 6) {
                if (attackTimer <= SPECIAL_CHARGE) { srcY = 1065; drawW = 54; drawH = 77; srcX = 0; }
                else {
                    srcY = 1065; drawW = 54; drawH = 77; srcX = 59;
                    int bodySrcX = 126, bodySrcY = 1069, bodyW = 146, bodyH = 64, headSrcX = 339, headSrcY = 1069, headW = 86, headH = 64, beamY = drawY + 6;
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

        drawUniversalHUD(g2d, "KAMEHAMEHA");
    }
}