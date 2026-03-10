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

    // --- KAMEHAMEHA ---
    private final int SPECIAL_CHARGE = 40;
    private final int SPECIAL_DURATION = 90;
    private int beamEndX = -1; // Usato per calcolare dove si ferma il raggio

    // --- KI BLAST SPECIFICI ---
    private ArrayList<KiBlastProjectile> activeBlasts = new ArrayList<>();

    // --- EFFETTI VISIVI (Esplosioni) ---
    private ArrayList<VisualEffect> activeEffects = new ArrayList<>();

    // --- K.O. E VITTORIA ---
    private boolean isWinner = false;
    private int endFrame = 1;
    private int endTimer = 0;

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

    private class VisualEffect {
        int ex, ey, type; // type 1 = KiBlast, type 2 = Kamehameha
        int frame = 0, timer = 0;
        boolean isDead = false;

        public VisualEffect(int x, int y, int type) {
            this.ex = x; this.ey = y; this.type = type;
        }

        public void update() {
            timer++;
            int speed = (type == 1) ? 4 : 6; // Velocità animazione (Ki = veloce, Kame = un po' più lenta)
            if (timer > speed) {
                frame++;
                timer = 0;
                int maxFrames = (type == 1) ? 3 : 2;
                if (frame >= maxFrames) isDead = true; // L'animazione è finita
            }
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
            reach = 36; offsetY = 15; // Prima reach era 30
        } else if (attackType == 2 || attackType == 3) { // Calcio a terra o in volo
            reach = 30; offsetY = 35; // Prima reach era 40
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
        // --- 1. AGGIORNA GLI EFFETTI VISIVI (Sempre attivo, anche a fine match!) ---
        for (int i = 0; i < activeEffects.size(); i++) {
            VisualEffect eff = activeEffects.get(i);
            eff.update();
            if (eff.isDead) { activeEffects.remove(i); i--; }
        }

        // --- K.O. (SCONFITTA) ---
        if (hp <= 0) {
            isAttacking = false; isChargingAura = false; isAuraActive = false; isBlocking = false;
            // Se è in volo, lo facciamo cadere a terra con la gravità
            if (y < groundY) { velocityY += gravity; y += (int) velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }

            endTimer++;
            if (endTimer > 10) { // Velocità caduta: cambia frame ogni 10 tick
                if (endFrame < 6) endFrame++;
                endTimer = 0;
            }
            return; // Blocca tutti gli input per non farlo muovere!
        }

        // --- VITTORIA ---
        if (opponent != null && opponent.hp <= 0) {
            isWinner = true;
            isAttacking = false; isChargingAura = false; isAuraActive = false; isBlocking = false;
            // Se NON è in volo e sta cadendo, lo stabilizziamo a terra
            if (!isFlying && y < groundY) { velocityY += gravity; y += (int) velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }

            endTimer++;
            if (endTimer > 15) { // Velocità esultanza: un po' più lenta (15 tick)
                endFrame = (endFrame == 1) ? 2 : 1; // Alterna solo tra 1 e 2
                endTimer = 0;
            }
            return; // Blocca tutti gli input!
        } else {
            isWinner = false; // Resetta in caso di rematch
        }

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

                // --- NUOVO: Calcolo lunghezza Kamehameha ---
                if (attackType == 6 && attackTimer > SPECIAL_CHARGE) {
                    beamEndX = facingRight ? 800 : 0; // Di base va fino in fondo
                    Rectangle hitbox = getAttackHitbox();
                    // Se l'hitbox tocca l'avversario, accorciamo il raggio!
                    if (hitbox != null && opponent != null && hitbox.intersects(opponent.getBounds())) {
                        beamEndX = facingRight ? opponent.getX() : opponent.getX() + opponent.baseWidth;
                    }
                }

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
                            // Spawna l'esplosione gigante se è la Kamehameha
                            if (attackType == 6) {
                                // Lo centriamo addosso all'avversario (142x120 la dim dell'esplosione)
                                int expX = opponent.getX() + (48 / 2) - (142 / 2);
                                int expY = opponent.y + (86 / 2) - (120 / 2);
                                activeEffects.add(new VisualEffect(expX, expY, 2));
                            }
                        }
                    }
                }

                int currentDuration = ATTACK_DURATION;
                if (attackType == 6) currentDuration = SPECIAL_DURATION;
                if (attackTimer >= currentDuration) { isAttacking = false; attackType = 0; }
            }

            // Calcolo Danni e Movimento Ki Blast
            for (int i = 0; i < activeBlasts.size(); i++) {
                KiBlastProjectile blast = activeBlasts.get(i);
                blast.update();

                Rectangle blastHitbox = new Rectangle(blast.px, blast.py, 24, 16);
                if (opponent != null && blastHitbox.intersects(opponent.getBounds())) {
                    opponent.takeDamage(kiBlastDamage);

                    // Effetto scintille (centrato rispetto alla Ki Blast)
                    activeEffects.add(new VisualEffect(blast.px - 5, blast.py - 8, 1));

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

        // --- DISEGNO EFFETTI VISIVI (VFX) ---
        for (VisualEffect eff : activeEffects) {
            if (eff.type == 1) { // KiBlast Impact
                int sY = 989, sW = 35, sH = 32;
                int[] xFrames = {459, 499, 542};
                int sX = xFrames[Math.min(eff.frame, 2)];
                g2d.drawImage(spriteSheet, eff.ex, eff.ey, eff.ex + sW, eff.ey + sH, sX, sY, sX + sW, sY + sH, null);
            } else if (eff.type == 2) { // Kamehameha Explosion
                int sY = 1034, sW = 142, sH = 120;
                int[] xFrames = {458, 658};
                int sX = xFrames[Math.min(eff.frame, 1)];
                g2d.drawImage(spriteSheet, eff.ex, eff.ey, eff.ex + sW, eff.ey + sH, sX, sY, sX + sW, sY + sH, null);
            }
        }

        for (KiBlastProjectile blast : activeBlasts) {
            int sW = 24, sH = 16, sX = 367, sY = 989;
            int bX1 = blast.px, bX2 = bX1 + sW;
            if (!blast.pFacingRight) { bX1 = bX2; bX2 = blast.px; }
            g2d.drawImage(spriteSheet, bX1, blast.py, bX2, blast.py + sH, sX, sY, sX + sW, sY + sH, null);
        }

        if (isAuraActive && !isChargingAura) {
            int auraSrcX = 200, auraSrcY = 800, auraSrcW = 78, auraSrcH = 111;

            // --- LA MAGIA DELLA SCALA ---
            // 1.0 = Grandezza originale
            double scale = 1.25;

            int drawAuraW = (int)(auraSrcW * scale);
            int drawAuraH = (int)(auraSrcH * scale);

            // Ricalcoliamo la X e la Y per tenerla sempre centrata su Goku
            int drawAuraX = x + (baseWidth - drawAuraW) / 2;
            int drawAuraY = y - (drawAuraH - baseHeight);

            g2d.drawImage(spriteSheet,
                    facingRight ? drawAuraX : drawAuraX + drawAuraW, drawAuraY,
                    facingRight ? drawAuraX + drawAuraW : drawAuraX, drawAuraY + drawAuraH,
                    auraSrcX, auraSrcY, auraSrcX + auraSrcW, auraSrcY + auraSrcH, null);
        }


        // --- DISEGNO K.O. E VITTORIA ---
        if (hp <= 0) {
            drawW = 90; drawH = 91; srcY = 1450;
            int[] koX = {187, 300, 400, 505, 600, 710};
            srcX = koX[Math.min(endFrame - 1, 5)]; // Evita errori di indice
            drawY = y - (drawH - baseHeight);
            shiftX = (baseWidth - drawW) / 2;
        }
        else if (isWinner) {
            if (isFlying) {
                // Esultanza in volo
                drawW = 40; drawH = 100; srcY = 1642;
                int[] winFlyX = {0, 46};
                srcX = winFlyX[endFrame - 1];
            } else {
                // Esultanza a terra
                drawW = 33; drawH = 90; srcY = 1649;
                int[] winGndX = {89, 128};
                srcX = winGndX[endFrame - 1];
            }
            drawY = y - (drawH - baseHeight);
            shiftX = (baseWidth - drawW) / 2;
        }

        else if (isChargingAura) { drawW = 78; drawH = 111; srcX = 0; srcY = 800; drawY = y - (drawH - baseHeight); shiftX = (baseWidth - drawW) / 2; }
        else if (isBlocking) {
            if (isFlying || isJumping) {
                // Parata in volo
                drawW = 37; drawH = 87; srcX = 160; srcY = 2;
            } else {
                // Parata a terra
                drawW = 41; drawH = 78; srcX = 0; srcY = 972;
            }
            drawY = y - (drawH - baseHeight);
            shiftX = (baseWidth - drawW) / 2;
        }
        else if (isTeleporting) { drawW = 30; drawH = 91; srcY = 1748; int[] tX = {3, 38, 72, 111, 147, 185}; srcX = tX[Math.min(teleportFrame - 1, 5)]; drawY = y - (drawH - baseHeight); shiftX = (baseWidth - drawW) / 2; }
        else if (isAttacking) {
            if (attackType == 1) { srcY = 442; drawW = 87; drawH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 0 : 130; }
            else if (attackType == 2) { srcY = 439; drawW = 80; drawH = 89; srcX = (attackTimer <= KICK_STARTUP) ? 242 : 325; }
            else if (attackType == 3) { srcY = 536; drawW = 65; drawH = 88; srcX = (attackTimer <= KICK_STARTUP) ? 0 : 100; }
            else if (attackType == 4) { srcY = 535; drawW = 64; drawH = 85; srcX = (attackTimer <= PUNCH_STARTUP) ? 204 : 300; }
            else if (attackType == 5) { srcY = 972; drawW = 65; drawH = 84; srcX = (attackTimer <= 7) ? 202 : 300; }
            else if (attackType == 6) {
                if (attackTimer <= SPECIAL_CHARGE) { srcY = 1065; drawW = 54; drawH = 77; srcX = 0; }
                else {
                    srcY = 1065; drawW = 54; drawH = 77; srcX = 59;
                    int bodySrcX = 126, bodySrcY = 1069, bodyW = 146, bodyH = 64, headSrcX = 339, headSrcY = 1069, headW = 86, headH = 64, beamY = drawY + 6;

                    // Capisce dove fermarsi in base al calcolo di prima
                    int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? 800 : 0);

                    if (facingRight) {
                        int startX = x + shiftX + drawW;
                        if (targetX - headW > startX) {
                            g2d.drawImage(spriteSheet, startX, beamY, targetX - headW, beamY + bodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                            g2d.drawImage(spriteSheet, targetX - headW, beamY, targetX, beamY + headH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                        } else {
                            // Se il nemico è troppo vicino, disegna solo la testa
                            g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + headH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                        }
                    } else {
                        int startX = x + shiftX;
                        if (targetX + headW < startX) {
                            g2d.drawImage(spriteSheet, startX, beamY, targetX + headW, beamY + bodyH, bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                            g2d.drawImage(spriteSheet, targetX + headW, beamY, targetX, beamY + headH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                        } else {
                            g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + headH, headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                        }
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

        drawPlayerPin(g2d, x + shiftX, drawY, drawW);

        drawUniversalHUD(g2d, "KAMEHAMEHA");
    }
}