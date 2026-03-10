import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Goku extends Fighter {

    private int spriteCounter = 0;
    private int spriteNum = 1;
    private int crouchTimer = 0;
    private final int CROUCH_DURATION = 3;
    private int flyCooldown = 0;
    private int flyNum = 1;

    private int attackTimer = 0;
    private int attackType = 0;
    private final int ATTACK_DURATION = 15;
    private final int PUNCH_STARTUP = 5;
    private final int KICK_STARTUP = 7;

    private final int SPECIAL_CHARGE = 40;
    private final int SPECIAL_DURATION = 90;
    private int beamEndX = -1;

    private boolean isWinner = false;
    private int endFrame = 1;
    private int endTimer = 0;

    public Goku(int x, int y, int playerID) {
        super(x, y, playerID, ResourceManager.getInstance().gokuSpriteSheet);
        this.groundY = y;
        this.facingRight = (playerID == 1);
        this.kiBlastImage = ResourceManager.getInstance().kiblastBlue;

        // --- APPLICHIAMO LA SCALA 1.3 ---
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

        int reach = 0;
        int boxHeight = (int)(20 * scale);
        int offsetY = (int)(20 * scale);

        if (attackType == 1 || attackType == 4) {
            reach = (int)(36 * scale); offsetY = (int)(15 * scale);
        } else if (attackType == 2 || attackType == 3) {
            reach = (int)(30 * scale); offsetY = (int)(35 * scale);
        } else if (attackType == 6) {
            if (attackTimer <= SPECIAL_CHARGE) return null;
            reach = (int)(800 * scale);
            boxHeight = (int)(40 * scale);
            offsetY = (int)(20 * scale);
        } else {
            return null;
        }

        int hX = facingRight ? x + baseWidth : x - reach;
        int hY = y + offsetY;
        return new Rectangle(hX, hY, reach, boxHeight);
    }

    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        for (int i = 0; i < activeEffects.size(); i++) {
            VisualEffect eff = activeEffects.get(i);
            eff.update();
            if (eff.isDead) { activeEffects.remove(i); i--; }
        }

        if (hp <= 0) {
            isAttacking = false; isChargingAura = false; isAuraActive = false; isBlocking = false;
            if (y < groundY) { velocityY += gravity; y += (int) velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer > 10) { if (endFrame < 6) endFrame++; endTimer = 0; }
            return;
        }

        if (opponent != null && opponent.hp <= 0) {
            isWinner = true;
            isAttacking = false; isChargingAura = false; isAuraActive = false; isBlocking = false;
            if (!isFlying && y < groundY) { velocityY += gravity; y += (int) velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer > 15) { endFrame = (endFrame == 1) ? 2 : 1; endTimer = 0; }
            return;
        } else isWinner = false;

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

        if (!isAttacking && !isTeleporting && !isChargingAura && opponent != null) this.facingRight = (this.x <= opponent.getX());
        if (input_block && !isAttacking && !isTeleporting && !isChargingAura) isBlocking = true; else isBlocking = false;

        if (input_aura && auraEnergy >= MAX_AURA_ENERGY && !isAuraActive && !isChargingAura && !isAttacking && !isTeleporting && !isBlocking) {
            isChargingAura = true; auraChargeTimer = 0; velocityY = 0;
        }

        if (isChargingAura) {
            auraChargeTimer++;
            if (auraChargeTimer >= AURA_CHARGE_DURATION) { isChargingAura = false; isAuraActive = true; }
        }

        if (isAuraActive) {
            speed = (int)(8 * scale); jumpStrength = -15 * scale; auraEnergy -= AURA_DRAIN_RATE;
            if (auraEnergy <= 0) { auraEnergy = 0; isAuraActive = false; }
        } else {
            speed = (int)(4 * scale); jumpStrength = -12 * scale;
            if (auraEnergy < MAX_AURA_ENERGY && !isChargingAura) auraEnergy++;
        }

        if (kiShotsAvailable < MAX_KI_SHOTS) { kiRechargeTimer++; if (kiRechargeTimer >= RECHARGE_TIME) { kiShotsAvailable++; kiRechargeTimer = 0; } }
        if (shotCooldown > 0) shotCooldown--;

        if (isAttacking && attackType == 6) { specialEnergy -= specialDrainRate; if (specialEnergy < 0) specialEnergy = 0; }
        else if (specialEnergy < MAX_SPECIAL_ENERGY) specialEnergy++;

        if (tapTimerW > 0) tapTimerW--; if (tapTimerA > 0) tapTimerA--; if (tapTimerS > 0) tapTimerS--; if (tapTimerD > 0) tapTimerD--;

        boolean newW = input_up && !prevW; boolean newA = input_left && !prevA;
        boolean newS = input_down && !prevS; boolean newD = input_right && !prevD;

        if (!isChargingAura && !isBlocking) {
            if (!isTeleporting && !isAttacking) {
                if (newA) { if (tapTimerA > 0) { startTeleport((int)(-DASH_DISTANCE * scale), 0, facingRight); tapTimerA = 0; } else tapTimerA = DOUBLE_TAP_WINDOW; }
                if (newD) { if (tapTimerD > 0) { startTeleport((int)(DASH_DISTANCE * scale), 0, facingRight); tapTimerD = 0; } else tapTimerD = DOUBLE_TAP_WINDOW; }
                if (newW) { if (tapTimerW > 0) { startTeleport(0, (int)(-DASH_DISTANCE * scale), facingRight); tapTimerW = 0; } else tapTimerW = DOUBLE_TAP_WINDOW; }
                if (newS && isFlying) { if (tapTimerS > 0) { startTeleport(0, (int)(DASH_DISTANCE * scale), facingRight); tapTimerS = 0; } else tapTimerS = DOUBLE_TAP_WINDOW; }

                if (!isFlying && !isJumping && !isCrouching) {
                    if (input_punch) { isAttacking = true; attackTimer = 0; attackType = 1; }
                    else if (input_kick) { isAttacking = true; attackTimer = 0; attackType = 2; }
                } else if (isFlying) {
                    if (input_kick) { isAttacking = true; attackTimer = 0; attackType = 3; }
                    else if (input_punch) { isAttacking = true; attackTimer = 0; attackType = 4; }
                }

                if (input_kiBlast && kiShotsAvailable > 0 && shotCooldown == 0) {
                    isAttacking = true; attackTimer = 0; attackType = 5;
                    kiShotsAvailable--; shotCooldown = 30;

                    int handX = facingRight ? x + (int)(40 * scale) : x - (int)(10 * scale);
                    int handY = y + (int)(25 * scale);
                    activeEffects.add(new VisualEffect(ResourceManager.getInstance().kiblastBlue,
                            handX, handY, new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60}, 7, 0.6 * scale));
                }

                if (input_kamehameha && specialEnergy >= MAX_SPECIAL_ENERGY) { isAttacking = true; attackTimer = 0; attackType = 6; }
            }

            if (isAttacking) {
                if (attackTimer == 0) hasHit = false;
                attackTimer++;

                if (attackType == 5 && attackTimer == 7) {
                    activeBlasts.add(new KiBlastProjectile(facingRight ? x + (int)(50 * scale) : x - (int)(10 * scale), y + (int)(25 * scale), facingRight, ResourceManager.getInstance().kiblastBlue, scale));
                }

                if (attackType == 6 && attackTimer > SPECIAL_CHARGE) {
                    beamEndX = facingRight ? 800 : 0;
                    Rectangle hitbox = getAttackHitbox();
                    if (hitbox != null && opponent != null && hitbox.intersects(opponent.getBounds())) {
                        beamEndX = facingRight ? opponent.getX() : opponent.getX() + opponent.baseWidth;
                    }
                }

                if (!hasHit && opponent != null) {
                    Rectangle hitbox = getAttackHitbox();
                    if (hitbox != null && hitbox.intersects(opponent.getBounds())) {
                        int damage = 0;
                        if (attackType == 1 || attackType == 4) damage = punchDamage;
                        else if (attackType == 2 || attackType == 3) damage = kickDamage;
                        else if (attackType == 6) damage = specialDamage;

                        if (damage > 0) {
                            opponent.takeDamage(damage);
                            hasHit = true;

                            if (attackType == 6) {
                                int expX = opponent.getX() + (opponent.baseWidth / 2);
                                int expY = opponent.y + (opponent.baseHeight / 2);
                                opponent.activeEffects.add(new VisualEffect(spriteSheet, expX, expY, new int[]{458, 658}, new int[]{1034, 1034}, new int[]{142, 142}, new int[]{120, 120}, 6, 1.0 * scale));
                            }
                        }
                    }
                }

                int currentDuration = ATTACK_DURATION;
                if (attackType == 6) currentDuration = SPECIAL_DURATION;
                if (attackTimer >= currentDuration) { isAttacking = false; attackType = 0; }
            }

            for (int i = 0; i < activeBlasts.size(); i++) {
                KiBlastProjectile blast = activeBlasts.get(i);
                blast.update(activeEffects);

                Rectangle blastHitbox = new Rectangle(blast.px, blast.py - (int)(10 * scale), (int)(40 * scale), (int)(20 * scale));

                if (opponent != null && blastHitbox.intersects(opponent.getBounds())) {
                    opponent.takeDamage(kiBlastDamage);

                    int impactX = blast.pFacingRight ? opponent.getX() + 10 : opponent.getX() + opponent.baseWidth - 10;
                    int impactY = opponent.y + (opponent.baseHeight / 2);

                    opponent.activeEffects.add(new VisualEffect(blast.img,
                            impactX, impactY,
                            new int[]{260, 0, 86}, new int[]{135, 448, 448}, new int[]{126, 70, 70}, new int[]{109, 64, 64},
                            new int[]{0, 0, 0}, new int[]{-40, -40, -40}, 6, 0.5 * scale));

                    activeBlasts.remove(i); i--; continue;
                }
                if (blast.isDead) { activeBlasts.remove(i); i--; }
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
                            if (isFlying && y > groundY - (int)(40 * scale)) y = groundY - (int)(40 * scale);
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
                    if (isFlying) { velocityY = 0; isJumping = false; isCrouching = false; if (y >= groundY) y -= (int)(40 * scale); }
                    else isJumping = true;
                }

                if (isFlying) {
                    if (input_up) y -= speed;
                    if (input_down) { y += speed; if (y > groundY - (int)(40 * scale)) y = groundY - (int)(40 * scale); }
                    if (!isAttacking) {
                        if (input_left) { x -= speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; flyNum = facingRight ? 2 : 4; }
                        else if (input_right) { x += speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; flyNum = facingRight ? 4 : 2; }
                        else flyNum = 1;
                    } else {
                        if (input_left) { x -= speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; }
                        if (input_right) { x += speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; }
                    }
                } else {
                    if (!isCrouching && !isAttacking) {
                        if (input_left) { x -= speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; isMoving = true; }
                        if (input_right) { x += speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; isMoving = true; }
                    }
                    if (input_up && !isJumping && !isCrouching && !isAttacking) { isCrouching = true; crouchTimer = 0; }
                    if (isCrouching) { crouchTimer++; if (crouchTimer >= CROUCH_DURATION) { isCrouching = false; isJumping = true; velocityY = jumpStrength; } }
                    if (isJumping) { velocityY += gravity; y += (int) velocityY; }
                }

                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; isFlying = false; }
                if (y < 0) { y = 0; velocityY = 0; }
                if (x < 0) x = 0;
                if (x > 800 - baseWidth) x = 800 - baseWidth;

                if (isMoving && !isJumping && !isCrouching && !isFlying && !isAttacking) {
                    spriteCounter++;
                    if (spriteCounter > (isAuraActive ? 3 : 5)) { spriteNum++; if (spriteNum > 3) spriteNum = 1; spriteCounter = 0; }
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
        // Nuova struttura iper-pulita: Dichiariamo i frame sorgenti, calcoliamo la scala SOLO alla fine!
        int srcX = 33, srcY = 0, srcW = 48, srcH = 86;

        if (isAuraActive && !isChargingAura) {
            int auraSrcX = 200, auraSrcY = 800, auraSrcW = 78, auraSrcH = 111;
            double auraScale = 1.25 * scale;
            int drawAuraW = (int)(auraSrcW * auraScale);
            int drawAuraH = (int)(auraSrcH * auraScale);
            int drawAuraX = x + (baseWidth - drawAuraW) / 2;
            int drawAuraY = y - (drawAuraH - baseHeight);
            g2d.drawImage(spriteSheet, facingRight ? drawAuraX : drawAuraX + drawAuraW, drawAuraY, facingRight ? drawAuraX + drawAuraW : drawAuraX, drawAuraY + drawAuraH, auraSrcX, auraSrcY, auraSrcX + auraSrcW, auraSrcY + auraSrcH, null);
        }

        if (hp <= 0) {
            srcW = 90; srcH = 91; srcY = 1450;
            int[] koX = {187, 300, 400, 505, 600, 710};
            srcX = koX[Math.min(endFrame - 1, 5)];
        }
        else if (isWinner) {
            if (isFlying) { srcW = 40; srcH = 100; srcY = 1642; int[] winFlyX = {0, 46}; srcX = winFlyX[endFrame - 1]; }
            else { srcW = 33; srcH = 90; srcY = 1649; int[] winGndX = {89, 128}; srcX = winGndX[endFrame - 1]; }
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

        // --- CALCOLO FINALE UNIVERSALE DELLE DIMENSIONI (Tutto Scalato x1.3!) ---
        int drawW = (int)(srcW * scale);
        int drawH = (int)(srcH * scale);
        int drawY = y - (drawH - baseHeight);
        int shiftX = (baseWidth - drawW) / 2;
        if (isAttacking && !facingRight) shiftX = -(drawW - baseWidth);

        // --- DISEGNO GOKU ---
        int sX1 = x + shiftX, sX2 = sX1 + drawW;
        if (!facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
        g2d.drawImage(spriteSheet, sX1, drawY, sX2, drawY + drawH, srcX, srcY, srcX + srcW, srcY + srcH, null);

        // --- DISEGNO KAMEHAMEHA BEAM (Sopra Goku) ---
        if (isAttacking && attackType == 6 && attackTimer > SPECIAL_CHARGE) {
            int bodySrcX = 126, bodySrcY = 1069, bodyW = 146, bodyH = 64, headSrcX = 339, headSrcY = 1069, headW = 86, headH = 64;
            int drawBodyH = (int)(bodyH * scale), drawHeadW = (int)(headW * scale), drawHeadH = (int)(headH * scale);
            int beamY = drawY + (int)(6 * scale);
            int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? 800 : 0);

            if (facingRight) {
                int startX = x + shiftX + drawW;
                if (targetX - drawHeadW > startX) {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX - drawHeadW, beamY + drawBodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                    g2d.drawImage(spriteSheet, targetX - drawHeadW, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                } else {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                }
            } else {
                int startX = x + shiftX;
                if (targetX + drawHeadW < startX) {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX + drawHeadW, beamY + drawBodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                    g2d.drawImage(spriteSheet, targetX + drawHeadW, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                } else {
                    g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                }
            }
        }

        // --- DISEGNO EFFETTI E KIBLAST ---
        for (VisualEffect eff : activeEffects) eff.draw(g2d);
        for (KiBlastProjectile blast : activeBlasts) blast.draw(g2d);

        drawPlayerPin(g2d, x + shiftX, drawY, drawW);
        drawUniversalHUD(g2d, "KAMEHAMEHA");
    }
}