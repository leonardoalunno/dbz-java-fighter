import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Fighter {

    protected int x, y;
    protected int playerID;
    protected BufferedImage spriteSheet;
    protected BufferedImage kiBlastImage;

    protected int hudSrcY = 0;
    public double scale = 1.0;

    public int hp = 100;
    protected int maxHP = 100;
    protected int speed = 4;
    protected int baseWidth = 48;
    protected int baseHeight = 86;

    protected boolean facingRight;
    protected int groundY;

    // --- VARIABILI DI STATO ---
    protected boolean isMoving = false, isJumping = false, isCrouching = false;
    protected boolean isFlying = false, isBlocking = false, isAttacking = false;
    protected boolean isTeleporting = false, isChargingAura = false, isAuraActive = false;
    protected boolean isWinner = false;

    protected int spriteCounter = 0, spriteNum = 1;
    protected int crouchTimer = 0, flyCooldown = 0, flyNum = 1;
    protected int attackTimer = 0, attackType = 0;
    protected int endFrame = 1, endTimer = 0;

    protected int ATTACK_DURATION = 15;
    protected int PUNCH_STARTUP = 5;
    protected int KICK_STARTUP = 7;
    protected int SPECIAL_CHARGE = 40;
    protected int SPECIAL_DURATION = 90;
    protected final int CROUCH_DURATION = 3;

    protected double velocityY = 0;
    protected double gravity = 0.5;
    protected double jumpStrength = -12;

    protected final int DASH_DISTANCE = 150;
    protected final int DOUBLE_TAP_WINDOW = 15;
    protected int tapTimerW = 0, tapTimerA = 0, tapTimerS = 0, tapTimerD = 0;
    protected boolean prevW = false, prevA = false, prevS = false, prevD = false;
    protected int teleportPhase = 1, teleportFrame = 6, teleportCounter = 0;
    protected int targetOffsetX = 0, targetOffsetY = 0;

    protected int auraChargeTimer = 0;
    protected final int AURA_CHARGE_DURATION = 30;
    protected final int AURA_DURATION = 600;
    protected double auraEnergy = 0;
    protected double MAX_AURA_ENERGY = 1200;
    protected double AURA_DRAIN_RATE = MAX_AURA_ENERGY / AURA_DURATION;

    protected double specialEnergy = 0;
    protected double MAX_SPECIAL_ENERGY = 2400;
    protected double specialDrainRate;

    protected int kiShotsAvailable = 3;
    protected int MAX_KI_SHOTS = 3;
    protected int kiRechargeTimer = 0;
    protected int RECHARGE_TIME = 300;
    protected int shotCooldown = 0;

    protected int punchDamage = 5, kickDamage = 8;
    protected int kiBlastDamage, specialDamage;
    protected boolean hasHit = false;

    protected boolean isHit = false;
    protected int hitTimer = 0;

    protected boolean isInvincible = false;
    protected int invincibleTimer = 0;

    // --- VARIABILI DI RENDERING UNIVERSALI ---
    protected int srcX, srcY, srcW, srcH;
    protected int drawW, drawH, drawY, shiftX;

    // --- INPUT UNIVERSALI ---
    protected boolean inUp, inDown, inLeft, inRight, inPunch, inKick, inKiBlast, inSpecial, inFly, inAura, inBlock;

    protected ArrayList<VisualEffect> activeEffects = new ArrayList<>();
    protected ArrayList<KiBlastProjectile> activeBlasts = new ArrayList<>();

    public Fighter(int x, int y, int playerID, BufferedImage spriteSheet) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        this.spriteSheet = spriteSheet;
    }

    public int getX() { return x; }

    public Rectangle getBounds() { return new Rectangle(x, y, baseWidth, baseHeight); }

    public abstract Rectangle getAttackHitbox();
    public abstract void draw(Graphics2D g2d);

    // --- METODI HOOK ASTRATTI PER EFFETTI VISIVI SPECIFICI DEL PERSONAGGIO ---
    protected abstract void spawnKiBlastVFX();
    protected abstract void fireKiBlastProjectile();
    protected abstract void onSpecialAttackHit(Fighter opponent);

    public void takeDamage(int amount) {
        if (isInvincible) return;

        if (isBlocking) {
            hp -= Math.max(1, amount / 4);
        } else {
            hp -= amount;
            isHit = true;
            hitTimer = 0;
            isAttacking = false;
            isChargingAura = false;
            isTeleporting = false;
        }
        if (hp < 0) hp = 0;
    }

    protected void startTeleport(int offX, int offY, boolean faceRight) {
        isTeleporting = true; teleportPhase = 1; teleportFrame = 6; teleportCounter = 0;
        targetOffsetX = offX; targetOffsetY = offY; facingRight = faceRight;
    }

    // --- MOTORE FISICO E LOGICO UNIVERSALE ---
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

        if (isHit) {
            hitTimer++;
            if (hitTimer < 5) {
                x += facingRight ? -(int)(4 * scale) : (int)(4 * scale);
                if (x < 0) x = 0;
                if (x > GamePanel.SCREEN_WIDTH - baseWidth) x = GamePanel.SCREEN_WIDTH - baseWidth;
            }
            if (hitTimer > 20) {
                isHit = false;
                isInvincible = true;
                invincibleTimer = 0;
            }
        }

        if (isInvincible) {
            invincibleTimer++;
            if (invincibleTimer > 40) isInvincible = false;
        }

        isMoving = false;

        inUp = (playerID == 1) ? keyH.p1_up : keyH.p2_up;
        inDown = (playerID == 1) ? keyH.p1_down : keyH.p2_down;
        inLeft = (playerID == 1) ? keyH.p1_left : keyH.p2_left;
        inRight = (playerID == 1) ? keyH.p1_right : keyH.p2_right;
        inPunch = (playerID == 1) ? keyH.p1_punch : keyH.p2_punch;
        inKick = (playerID == 1) ? keyH.p1_kick : keyH.p2_kick;
        inKiBlast = (playerID == 1) ? keyH.p1_kiBlast : keyH.p2_kiBlast;
        inSpecial = (playerID == 1) ? keyH.p1_kamehameha : keyH.p2_kamehameha;
        inFly = (playerID == 1) ? keyH.p1_fly : keyH.p2_fly;
        inAura = (playerID == 1) ? keyH.p1_aura : keyH.p2_aura;
        inBlock = (playerID == 1) ? keyH.p1_block : keyH.p2_block;

        if (!isAttacking && !isTeleporting && !isChargingAura && opponent != null) this.facingRight = (this.x <= opponent.getX());
        if (inBlock && !isAttacking && !isTeleporting && !isChargingAura) isBlocking = true; else isBlocking = false;

        if (inAura && auraEnergy >= MAX_AURA_ENERGY && !isAuraActive && !isChargingAura && !isAttacking && !isTeleporting && !isBlocking) {
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

        boolean newW = inUp && !prevW; boolean newA = inLeft && !prevA;
        boolean newS = inDown && !prevS; boolean newD = inRight && !prevD;

        if (!isChargingAura && !isBlocking && !isHit) {
            if (!isTeleporting && !isAttacking) {
                if (newA) { if (tapTimerA > 0) { startTeleport((int)(-DASH_DISTANCE * scale), 0, facingRight); tapTimerA = 0; } else tapTimerA = DOUBLE_TAP_WINDOW; }
                if (newD) { if (tapTimerD > 0) { startTeleport((int)(DASH_DISTANCE * scale), 0, facingRight); tapTimerD = 0; } else tapTimerD = DOUBLE_TAP_WINDOW; }
                if (newW) { if (tapTimerW > 0) { startTeleport(0, (int)(-DASH_DISTANCE * scale), facingRight); tapTimerW = 0; } else tapTimerW = DOUBLE_TAP_WINDOW; }
                if (newS && isFlying) { if (tapTimerS > 0) { startTeleport(0, (int)(DASH_DISTANCE * scale), facingRight); tapTimerS = 0; } else tapTimerS = DOUBLE_TAP_WINDOW; }

                if (!isFlying && !isJumping && !isCrouching) {
                    if (inPunch) { isAttacking = true; attackTimer = 0; attackType = 1; }
                    else if (inKick) { isAttacking = true; attackTimer = 0; attackType = 2; }
                } else if (isFlying) {
                    if (inKick) { isAttacking = true; attackTimer = 0; attackType = 3; }
                    else if (inPunch) { isAttacking = true; attackTimer = 0; attackType = 4; }
                }

                if (inKiBlast && kiShotsAvailable > 0 && shotCooldown == 0) {
                    isAttacking = true; attackTimer = 0; attackType = 5;
                    kiShotsAvailable--; shotCooldown = 30;
                    spawnKiBlastVFX();
                }

                if (inSpecial && specialEnergy >= MAX_SPECIAL_ENERGY) { isAttacking = true; attackTimer = 0; attackType = 6; }
            }

            if (isAttacking) {
                if (attackTimer == 0) hasHit = false;
                attackTimer++;

                if (attackType == 5 && attackTimer == 7) fireKiBlastProjectile();

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
                            if (attackType == 6) onSpecialAttackHit(opponent);
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
                if (inFly && flyCooldown == 0 && !isAttacking) {
                    isFlying = !isFlying; flyCooldown = 20;
                    if (isFlying) { velocityY = 0; isJumping = false; isCrouching = false; if (y >= groundY) y -= (int)(40 * scale); }
                    else isJumping = true;
                }

                if (isFlying) {
                    if (inUp) y -= speed;
                    if (inDown) { y += speed; if (y > groundY - (int)(40 * scale)) y = groundY - (int)(40 * scale); }
                    if (!isAttacking) {
                        if (inLeft) { x -= speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; flyNum = facingRight ? 2 : 4; }
                        else if (inRight) { x += speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; flyNum = facingRight ? 4 : 2; }
                        else flyNum = 1;
                    } else {
                        if (inLeft) { x -= speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; }
                        if (inRight) { x += speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; }
                    }
                } else {
                    if (!isCrouching && !isAttacking) {
                        if (inLeft) { x -= speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x += speed; isMoving = true; }
                        if (inRight) { x += speed; if (opponent != null && this.getBounds().intersects(opponent.getBounds())) x -= speed; isMoving = true; }
                    }
                    if (inUp && !isJumping && !isCrouching && !isAttacking) { isCrouching = true; crouchTimer = 0; }
                    if (isCrouching) { crouchTimer++; if (crouchTimer >= CROUCH_DURATION) { isCrouching = false; isJumping = true; velocityY = jumpStrength; } }
                    if (isJumping) { velocityY += gravity; y += (int) velocityY; }
                }

                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; isFlying = false; }
                if (y < 0) { y = 0; velocityY = 0; }
                if (x < 0) x = 0;

                if (x > GamePanel.SCREEN_WIDTH - baseWidth) x = GamePanel.SCREEN_WIDTH - baseWidth;
            }
        } else {
            if (!isFlying && y < groundY) {
                isJumping = true; velocityY += gravity; y += (int) velocityY;
                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; }
            }
        }
        prevW = inUp; prevA = inLeft; prevS = inDown; prevD = inRight;
    }

    // --- NUOVO METODO DI RENDERING CORPO PRINCIPALE ---
    protected void drawFighterSprite(Graphics2D g2d) {
        // Calcola dimensioni basate sulle variabili protette
        drawW = (int)(srcW * scale);
        drawH = (int)(srcH * scale);
        drawY = y - (drawH - baseHeight);

        shiftX = (baseWidth - drawW) / 2;
        if (isAttacking && !facingRight) shiftX = -(drawW - baseWidth);

        // Effetto Invincibilità
        if (isInvincible && invincibleTimer % 10 < 5) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        }

        // Posizionamento e Specchio
        int sX1 = x + shiftX, sX2 = sX1 + drawW;
        if (!facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }

        // Disegno Finale
        g2d.drawImage(spriteSheet, sX1, drawY, sX2, drawY + drawH, srcX, srcY, srcX + srcW, srcY + srcH, null);

        // Ripristino Opacità
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    // --- DISEGNO HUD UNIVERSALE ---
    protected void drawPlayerPin(Graphics2D g2d, int drawX, int drawY, int drawW) {
        ResourceManager resM = ResourceManager.getInstance();
        BufferedImage pin = (playerID == 1) ? resM.pinP1 : resM.pinP2;

        if (pin != null) {
            int pinW = 16;
            int pinH = (pinW * pin.getHeight()) / pin.getWidth();
            int pinX = drawX + (drawW - pinW) / 2;
            int pinY = drawY - pinH - 12;
            g2d.drawImage(pin, pinX, pinY, pinW, pinH, null);
        }
    }

    protected void drawUniversalHUD(Graphics2D g2d, String specialName) {
        ResourceManager resM = ResourceManager.getInstance();
        if (resM.hudFull != null) {
            double uiScale = 0.35;
            int hSrcX = 0, hSrcW = 1142, hSrcH = 410;
            int hDrawW = (int)(hSrcW * uiScale);
            int hDrawH = (int)(hSrcH * uiScale);
            int hX = (playerID == 1) ? 20 : GamePanel.SCREEN_WIDTH - hDrawW - 20;
            int hY = 20;

            if (resM.saiyanFont != null) {
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 26f));
                String pText = (playerID == 1) ? "PLAYER ONE" : "PLAYER TWO";
                g2d.setColor((playerID == 1) ? Color.RED : new Color(50, 150, 255));
                int pTextX = (playerID == 1) ? hX + (int)(272 * uiScale) : hX + hDrawW - (int)(272 * uiScale) - g2d.getFontMetrics().stringWidth(pText);
                int pTextY = hY + (int)(80 * uiScale);
                g2d.drawString(pText, pTextX, pTextY);
            }

            double hpPercent = (double) hp / maxHP;
            int hBarW = (int)(860 * uiScale), hBarH = (int)(92 * uiScale), currentHpW = (int) (hBarW * hpPercent);
            int hBarX = (playerID == 1) ? hX + (int)(272 * uiScale) : hX + hDrawW - (int)(272 * uiScale) - currentHpW;
            int hBarY = hY + (int)(104 * uiScale);

            if (hpPercent > 0.50) g2d.setColor(Color.GREEN);
            else if (hpPercent >= 0.21) g2d.setColor(Color.ORANGE);
            else g2d.setColor(Color.RED);
            g2d.fillRect(hBarX, hBarY, currentHpW, hBarH);

            double sPercent = specialEnergy / MAX_SPECIAL_ENERGY;
            int sBarW = (int)(570 * uiScale), sBarH = (int)(68 * uiScale), currentSW = (int) (sBarW * sPercent);
            int sBarX = (playerID == 1) ? hX + (int)(292 * uiScale) : hX + hDrawW - (int)(292 * uiScale) - currentSW;
            int sBarY = hY + (int)(216 * uiScale);
            g2d.setColor(Color.CYAN);
            g2d.fillRect(sBarX, sBarY, currentSW, sBarH);

            double aPercent = auraEnergy / MAX_AURA_ENERGY;
            int aBarW = (int)(330 * uiScale), aBarH = (int)(68 * uiScale), currentAW = (int) (aBarW * aPercent);
            int aBarX = (playerID == 1) ? hX + (int)(292 * uiScale) : hX + hDrawW - (int)(292 * uiScale) - currentAW;
            int aBarY = hY + (int)(306 * uiScale);
            g2d.setColor(new Color(220, 20, 60));
            g2d.fillRect(aBarX, aBarY, currentAW, aBarH);

            if (resM.saiyanFont != null) {
                int labelMargin = 10;
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 24f));
                String hpLabel = "HP";
                int hpLabelX = (playerID == 1) ? hX + (int)(272 * uiScale) + hBarW + labelMargin : (hX + hDrawW - (int)(272 * uiScale) - hBarW) - g2d.getFontMetrics().stringWidth(hpLabel) - labelMargin;
                if (hpPercent > 0.50) g2d.setColor(Color.GREEN); else if (hpPercent >= 0.21) g2d.setColor(Color.ORANGE); else g2d.setColor(Color.RED);
                g2d.drawString(hpLabel, hpLabelX, hY + (int)(175 * uiScale));

                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 20f));
                int specLabelX = (playerID == 1) ? hX + (int)(292 * uiScale) + sBarW + labelMargin : (hX + hDrawW - (int)(292 * uiScale) - sBarW) - g2d.getFontMetrics().stringWidth(specialName) - labelMargin;
                g2d.setColor(Color.CYAN); g2d.drawString(specialName, specLabelX, hY + (int)(268 * uiScale));

                String auraLabel = "AURA";
                int auraLabelX = (playerID == 1) ? hX + (int)(292 * uiScale) + aBarW + labelMargin : (hX + hDrawW - (int)(292 * uiScale) - aBarW) - g2d.getFontMetrics().stringWidth(auraLabel) - labelMargin;
                g2d.setColor(new Color(220, 20, 60)); g2d.drawString(auraLabel, auraLabelX, hY + (int)(358 * uiScale));
            }

            if (playerID == 1) g2d.drawImage(resM.hudFull, hX, hY, hX + hDrawW, hY + hDrawH, hSrcX, hudSrcY, hSrcX + hSrcW, hudSrcY + hSrcH, null);
            else g2d.drawImage(resM.hudFull, hX + hDrawW, hY, hX, hY + hDrawH, hSrcX, hudSrcY, hSrcX + hSrcW, hudSrcY + hSrcH, null);

            if (kiBlastImage != null && resM.kiblastGray != null) {
                int iW = 36, iH = 18, iSpacing = iW + 8, totalIconsWidth = (MAX_KI_SHOTS * iW) + ((MAX_KI_SHOTS - 1) * 8);
                int iStartX = (playerID == 1) ? hX + (int)(272 * uiScale) : (hX + hDrawW) - (int)(272 * uiScale) - totalIconsWidth;
                int iStartY = hY + hDrawH + 8;
                for(int i = 0; i < MAX_KI_SHOTS; i++) {
                    int renderIndex = (playerID == 1) ? i : (MAX_KI_SHOTS - 1 - i);
                    int dX = iStartX + (renderIndex * iSpacing), dX1 = dX, dX2 = dX + iW;
                    if (playerID == 2) { int temp = dX1; dX1 = dX2; dX2 = temp; }
                    if (i < kiShotsAvailable) g2d.drawImage(kiBlastImage, dX1, iStartY, dX2, iStartY + iH, 3, 3, 3 + 253, 3 + 124, null);
                    else g2d.drawImage(resM.kiblastGray, dX1, iStartY, dX2, iStartY + iH, 0, 0, 253, 124, null);
                }
            }
        }
    }
}