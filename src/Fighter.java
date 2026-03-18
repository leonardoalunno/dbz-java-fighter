import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Fighter {

    protected int x, y;
    protected int playerID;
    protected BufferedImage spriteSheet;
    protected BufferedImage kiBlastImage;
    protected BufferedImage auraImage;
    protected int auraFrame = 0;
    protected int auraAnimTimer = 0;

    protected int hudSrcY = 0;
    protected int portraitSrcY = 0; // Y del ritratto nell'hud.png (300 per personaggio)
    public double scale = 1.0;

    public int hp = 100;
    protected int maxHP = 100;
    protected int speed = 4;
    protected int baseWidth = 48;
    protected int baseHeight = 86;

    public Color auraColor = Color.WHITE;

    protected boolean facingRight;
    protected int groundY;

    public int knockbackSpeed = 0;
    protected int customOffsetX = 0;

    // --- VARIABILI DI STATO ---
    protected boolean isMoving = false, isJumping = false, isCrouching = false;
    protected boolean isFlying = false, isBlocking = false, isAttacking = false;
    protected boolean isTeleporting = false, isChargingAura = false, isAuraActive = false;
    protected boolean isWinner = false;

    public float alpha = 1.0f;
    public int teleportTimer = 0;

    protected int spriteCounter = 0, spriteNum = 1;
    protected int crouchTimer = 0, flyCooldown = 0, flyNum = 1;
    protected int flyMoveTimer = 0;
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

    // Mantenuti per compatibilità con le sottoclassi (non usati nella logica di gioco)
    protected int kiShotsAvailable = 3;
    protected int MAX_KI_SHOTS = 3;

    protected int shotCooldown = 0;

    protected int punchDamage = 5, kickDamage = 8;
    protected int kiBlastDamage, specialDamage;
    protected boolean hasHit = false;

    protected boolean isHit = false;
    protected int hitTimer = 0;

    protected boolean isInvincible = false;
    protected int invincibleTimer = 0;

    protected int blockTimer = 0;

    // --- VARIABILI DI RENDERING UNIVERSALI ---
    protected int srcX, srcY, srcW, srcH;
    protected int drawW, drawH, drawY, shiftX;

    // --- INPUT UNIVERSALI ---
    protected boolean inUp, inDown, inLeft, inRight, inPunch, inKick, inKiBlast, inSpecial, inFly, inAura, inBlock;

    protected ArrayList<VisualEffect> activeEffects = new ArrayList<>();
    protected ArrayList<KiBlastProjectile> activeBlasts = new ArrayList<>();

    // =========================================================
    // SISTEMA 1 — KI: Barra continua che sostituisce i colpi discreti
    //
    // 'ki' si rigenera passivamente ogni frame (kiRegen/frame).
    // Con l'Aura attiva la rigenerazione raddoppia.
    // Sparare un Ki Blast costa 'kiBlastKiCost'.
    // Se ki arriva a 0 si attiva il Ki Break (penalità KI_BREAK_DURATION frame):
    //   durante il Break è impossibile sparare, usare la special o fare counter.
    // =========================================================
    protected double ki = 0;
    protected double MAX_KI = 300.0;
    protected double kiBlastKiCost = 80.0;
    protected double kiRegen = 0.5;
    protected int kiBreakTimer = 0;
    protected final int KI_BREAK_DURATION = 60;

    // =========================================================
    // SISTEMA 2 — COMBO: Catena di colpi con scaling del danno
    //
    // Ogni colpo che atterra entro COMBO_WINDOW frame dal precedente
    // incrementa comboCount.
    // Il moltiplicatore comboDamageScale scende dell'8% per ogni hit
    // oltre il primo, con un minimo del 30%.
    // =========================================================
    protected int comboCount = 0;
    protected int comboTimer = 0;
    protected final int COMBO_WINDOW = 28;
    protected double comboDamageScale = 1.0;

    // =========================================================
    // SISTEMA 3 — GUARDIA & COUNTER: Timing e Guard Crush
    //
    // PERFECT GUARD: se si blocca entro PERFECT_GUARD_WINDOW frame
    //   dall'inizio di un attacco fisico in arrivo → danno azzerato,
    //   si apre la finestra Counter (COUNTER_WINDOW_DURATION frame).
    // COUNTER: il prossimo attacco di questo fighter fa 1.5x danno.
    // GUARD HEALTH: si consuma bloccando colpi fisici.
    //   A 0 scatta il Guard Crush: stordimento GUARD_CRUSH_DURATION frame.
    //   Dopo il crush, guardHealth si ripristina a metà.
    // =========================================================
    protected int guardHealth = 100;
    protected final int MAX_GUARD_HEALTH = 100;
    protected boolean isGuardCrushed = false;
    protected int guardCrushTimer = 0;
    protected final int GUARD_CRUSH_DURATION = 45;
    protected final int PERFECT_GUARD_WINDOW = 8;
    protected int blockActiveTimer = 0;
    protected boolean isCountering = false;
    protected int counterWindow = 0;
    protected final int COUNTER_WINDOW_DURATION = 22;

    // =========================================================
    // SISTEMA 4 — KNOCKBACK & HITSTUN: Stordimento scalato e Wall Bounce
    //
    // hitstunDuration: calcolato dal danno ricevuto (12–55 frame).
    //   Sostituisce il vecchio valore fisso di 20 frame.
    // wallBounced: true se il fighter ha colpito un bordo schermo
    //   durante il knockback → permette un follow-up dell'avversario.
    // isTumbling: true se hp < 20% del massimo → animazione di barcollamento
    //   (usata dalle sottoclassi in draw() se vogliono distinguerla).
    // =========================================================
    protected int hitstunDuration = 20;
    public boolean wallBounced = false;
    protected boolean isTumbling = false;

    // =========================================================
    // COSTRUTTORE
    // =========================================================
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

    protected abstract void spawnKiBlastVFX();
    protected abstract void fireKiBlastProjectile();
    protected abstract void onSpecialAttackHit(Fighter opponent);

    // =========================================================
    // takeDamage — Riscritta con i 4 sistemi integrati
    // =========================================================
    public void takeDamage(int amount, int appliedKnockback, boolean isEnergyAttack) {
        if (isInvincible) return;
        if (isGuardCrushed) return;
        // Blocca colpi doppi nello stesso hitstun, a meno che non sia un wall bounce
        if (isHit && !wallBounced) return;

        if (isBlocking) {
            // --- GUARDIA PERFETTA ---
            // Condizioni: attacco fisico, blocco attivato da meno di PERFECT_GUARD_WINDOW frame
            boolean isPerfectGuard = (blockActiveTimer <= PERFECT_GUARD_WINDOW) && !isEnergyAttack;

            if (isPerfectGuard) {
                // Nessun danno, finestra counter aperta
                isCountering  = true;
                counterWindow = COUNTER_WINDOW_DURATION;
                // Piccola ricompensa: recupero Ki
                ki = Math.min(MAX_KI, ki + 30);
                knockbackSpeed = Math.max(1, appliedKnockback / 5);

            } else {
                // --- GUARDIA NORMALE ---
                if (isEnergyAttack) {
                    // Attacchi energetici penetrano parzialmente la guardia
                    hp -= Math.max(1, amount / 4);
                }
                // La guardia si deteriora assorbendo colpi fisici
                guardHealth -= amount / 2;
                knockbackSpeed = appliedKnockback / 3;
                blockTimer++;

                // --- GUARD CRUSH ---
                if (guardHealth <= 0) {
                    guardHealth     = 0;
                    isGuardCrushed  = true;
                    guardCrushTimer = 0;
                    isBlocking      = false;
                    blockActiveTimer = 0;
                    isCountering    = false;
                    counterWindow   = 0;
                }
            }

        } else {
            // --- COLPO RICEVUTO SENZA GUARDIA ---
            hp -= amount;
            isHit        = true;
            hitTimer     = 0;
            wallBounced  = false;

            // Hitstun scalato con il danno: min 12, max 55 frame
            hitstunDuration = Math.max(12, Math.min(55, amount * 2));

            this.knockbackSpeed = appliedKnockback;
            isAttacking      = false;
            isChargingAura   = false;
            isTeleporting    = false;
            blockTimer       = 0;
            isCountering     = false;
            counterWindow    = 0;
            blockActiveTimer = 0;

            // Tumble: hp sotto il 20%
            isTumbling = (hp > 0 && hp <= (int)(maxHP * 0.20));
        }

        if (hp < 0) hp = 0;
    }

    protected void startTeleport(int offX, int offY, boolean faceRight) {
        isTeleporting = true; teleportPhase = 1; teleportFrame = 6; teleportCounter = 0;
        targetOffsetX = offX; targetOffsetY = offY; facingRight = faceRight;
    }

    // =========================================================
    // update — Motore principale aggiornato
    // =========================================================
    public void update(KeyHandler keyH, Fighter opponent) {

        // Aggiornamento effetti visivi
        for (int i = 0; i < activeEffects.size(); i++) {
            VisualEffect eff = activeEffects.get(i);
            eff.update();
            if (eff.isDead) { activeEffects.remove(i); i--; }
        }

        // --- MORTE ---
        if (hp <= 0) {
            isAttacking = false; isChargingAura = false; isAuraActive = false;
            isBlocking  = false; isGuardCrushed = false; isTumbling   = false;
            comboCount  = 0;     comboTimer     = 0;
            if (y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer > 10) { if (endFrame < 7) endFrame++; endTimer = 0; }
            return;
        }

        // --- VINCITORE ---
        if (opponent != null && opponent.hp <= 0) {
            isWinner    = true;
            isAttacking = false; isChargingAura = false; isAuraActive  = false;
            isBlocking  = false; isGuardCrushed = false;
            if (!isFlying && y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer > 15) { endFrame = (endFrame == 1) ? 2 : 1; endTimer = 0; }
            return;
        } else isWinner = false;

        // =========================================================
        // SISTEMA 4 — KNOCKBACK con Wall Bounce
        // =========================================================
        if (knockbackSpeed > 0) {
            x += facingRight ? -knockbackSpeed : knockbackSpeed;
            knockbackSpeed = Math.max(0, knockbackSpeed - 2);

            boolean hitLeft  = (x <= 0);
            boolean hitRight = (x >= GamePanel.SCREEN_WIDTH - baseWidth);

            if ((hitLeft || hitRight) && !wallBounced && knockbackSpeed > 3) {
                wallBounced    = true;
                x              = hitLeft ? 0 : GamePanel.SCREEN_WIDTH - baseWidth;
                knockbackSpeed = knockbackSpeed / 2 + 3;
                facingRight    = !facingRight;
                // Wall bounce accorcia leggermente lo stordimento
                hitstunDuration = Math.max(8, hitstunDuration - 6);
            } else {
                if (x < 0) x = 0;
                if (x > GamePanel.SCREEN_WIDTH - baseWidth) x = GamePanel.SCREEN_WIDTH - baseWidth;
            }
        }

        // =========================================================
        // SISTEMA 4 — HITSTUN variabile
        // =========================================================
        if (isHit) {
            hitTimer++;
            if (hitTimer > hitstunDuration) {
                isHit       = false;
                wallBounced = false;
                isInvincible    = true;
                invincibleTimer = 0;
            }
        }

        if (isInvincible) {
            invincibleTimer++;
            if (invincibleTimer > 40) isInvincible = false;
        }

        // =========================================================
        // SISTEMA 3 — GUARD CRUSH timer
        // =========================================================
        if (isGuardCrushed) {
            guardCrushTimer++;
            isBlocking  = false;
            isAttacking = false;
            // Gravità durante lo stordimento
            if (!isFlying && y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            if (guardCrushTimer >= GUARD_CRUSH_DURATION) {
                isGuardCrushed  = false;
                guardHealth     = MAX_GUARD_HEALTH / 2; // recupero parziale
                isInvincible    = true;
                invincibleTimer = 0;
            }
            prevW = inUp; prevA = inLeft; prevS = inDown; prevD = inRight;
            return; // fighter stordito: skip il resto dell'update
        }

        // Recupero lento della guardia mentre non si blocca
        if (!isBlocking && !isHit && guardHealth < MAX_GUARD_HEALTH) {
            guardHealth = Math.min(MAX_GUARD_HEALTH, guardHealth + 1);
        }

        isMoving = false;

        // Lettura input
        inUp      = (playerID == 1) ? keyH.p1_up    : keyH.p2_up;
        inDown    = (playerID == 1) ? keyH.p1_down  : keyH.p2_down;
        inLeft    = (playerID == 1) ? keyH.p1_left  : keyH.p2_left;
        inRight   = (playerID == 1) ? keyH.p1_right : keyH.p2_right;
        inPunch   = (playerID == 1) ? keyH.p1_punch      : keyH.p2_punch;
        inKick    = (playerID == 1) ? keyH.p1_kick       : keyH.p2_kick;
        inKiBlast = (playerID == 1) ? keyH.p1_kiBlast    : keyH.p2_kiBlast;
        inSpecial = (playerID == 1) ? keyH.p1_kamehameha : keyH.p2_kamehameha;
        inFly     = (playerID == 1) ? keyH.p1_fly        : keyH.p2_fly;
        inAura    = (playerID == 1) ? keyH.p1_aura       : keyH.p2_aura;
        inBlock   = (playerID == 1) ? keyH.p1_block      : keyH.p2_block;

        if (!isAttacking && !isTeleporting && !isChargingAura && opponent != null)
            this.facingRight = (this.x <= opponent.getX());

        // =========================================================
        // SISTEMA 3 — BLOCCO con tracking della durata
        // =========================================================
        boolean wasBlocking = isBlocking;
        if (inBlock && !isAttacking && !isTeleporting && !isChargingAura && !isHit)
            isBlocking = true;
        else
            isBlocking = false;

        if (isBlocking) {
            if (!wasBlocking) blockActiveTimer = 0; // prima frame del blocco
            blockActiveTimer++;
        } else {
            blockActiveTimer = 0;
        }

        // Counter window scade nel tempo
        if (counterWindow > 0) {
            counterWindow--;
            if (counterWindow == 0) isCountering = false;
        }

        // =========================================================
        // SISTEMA 1 — KI: rigenerazione e Ki Break
        // =========================================================
        if (kiBreakTimer > 0) {
            kiBreakTimer--;
        } else {
            double regenMult = isAuraActive ? 2.0 : 1.0;
            ki = Math.min(MAX_KI, ki + kiRegen * regenMult);
        }

        // =========================================================
        // SISTEMA 2 — COMBO timer: azzera la catena se scade
        // =========================================================
        if (comboTimer > 0) {
            comboTimer--;
        } else if (comboCount > 0) {
            comboCount        = 0;
            comboDamageScale  = 1.0;
        }

        // --- AURA ---
        if (inAura && auraEnergy >= MAX_AURA_ENERGY && !isAuraActive && !isChargingAura
                && !isAttacking && !isTeleporting && !isBlocking) {
            isChargingAura = true; auraChargeTimer = 0; velocityY = 0;
        }
        if (isChargingAura) {
            auraChargeTimer++;
            if (auraChargeTimer >= AURA_CHARGE_DURATION) {
                isChargingAura = false; isAuraActive = true;
                hp += 25; if (hp > maxHP) hp = maxHP;
            }
        }
        if (isAuraActive) {
            speed = (int)(8 * scale); jumpStrength = -15 * scale;
            auraEnergy -= AURA_DRAIN_RATE;
            if (auraEnergy <= 0) { auraEnergy = 0; isAuraActive = false; }
        } else {
            speed = (int)(4 * scale); jumpStrength = -12 * scale;
            if (auraEnergy < MAX_AURA_ENERGY && !isChargingAura) auraEnergy++;
        }

        if (isChargingAura || isAuraActive) {
            auraAnimTimer++;
            if (auraAnimTimer > 4) { auraFrame++; if (auraFrame > 3) auraFrame = 0; auraAnimTimer = 0; }
            if (auraImage != null && Math.random() < 0.15) {
                int rX = x + (int)(Math.random() * baseWidth * 1.5) - (int)(baseWidth * 0.25);
                int rY = y - (int)(Math.random() * baseHeight);
                activeEffects.add(new VisualEffect(auraImage, rX, rY,
                        new int[]{129, 197}, new int[]{985, 894}, new int[]{66, 72}, new int[]{106, 79}, 3, scale * 0.6));
            }
        } else { auraFrame = 0; }

        if (shotCooldown > 0) shotCooldown--;

        // Special drena energia durante l'uso
        if (isAttacking && attackType == 6) { specialEnergy -= specialDrainRate; if (specialEnergy < 0) specialEnergy = 0; }
        else if (specialEnergy < MAX_SPECIAL_ENERGY) specialEnergy++;

        if (tapTimerW > 0) tapTimerW--; if (tapTimerA > 0) tapTimerA--;
        if (tapTimerS > 0) tapTimerS--; if (tapTimerD > 0) tapTimerD--;

        boolean newW = inUp && !prevW, newA = inLeft && !prevA;
        boolean newS = inDown && !prevS, newD = inRight && !prevD;

        if (!isChargingAura && !isBlocking && !isHit) {

            if (!isTeleporting && !isAttacking) {
                // Teleport con doppio tap
                if (newA) { if (tapTimerA > 0) { startTeleport((int)(-DASH_DISTANCE * scale), 0, facingRight); tapTimerA = 0; } else tapTimerA = DOUBLE_TAP_WINDOW; }
                if (newD) { if (tapTimerD > 0) { startTeleport((int)( DASH_DISTANCE * scale), 0, facingRight); tapTimerD = 0; } else tapTimerD = DOUBLE_TAP_WINDOW; }
                if (newW) { if (tapTimerW > 0) { startTeleport(0, (int)(-DASH_DISTANCE * scale), facingRight); tapTimerW = 0; } else tapTimerW = DOUBLE_TAP_WINDOW; }
                if (newS && isFlying) { if (tapTimerS > 0) { startTeleport(0, (int)(DASH_DISTANCE * scale), facingRight); tapTimerS = 0; } else tapTimerS = DOUBLE_TAP_WINDOW; }

                // Attacchi corpo a corpo
                if (!isFlying && !isJumping && !isCrouching) {
                    if (inPunch) { isAttacking = true; attackTimer = 0; attackType = 1; }
                    else if (inKick) { isAttacking = true; attackTimer = 0; attackType = 2; }
                } else if (isFlying) {
                    if (inKick)       { isAttacking = true; attackTimer = 0; attackType = 3; }
                    else if (inPunch) { isAttacking = true; attackTimer = 0; attackType = 4; }
                }

                // =============================================
                // SISTEMA 1 — KI BLAST usa la barra Ki continua
                // =============================================
                if (inKiBlast && ki >= kiBlastKiCost && shotCooldown == 0 && kiBreakTimer == 0) {
                    isAttacking = true; attackTimer = 0; attackType = 5;
                    ki -= kiBlastKiCost;
                    if (ki <= 0) { ki = 0; kiBreakTimer = KI_BREAK_DURATION; }
                    shotCooldown = 30;
                    spawnKiBlastVFX();
                }

                // Special: richiede energy piena + Ki minimo (non usabile durante Ki Break)
                if (inSpecial && specialEnergy >= MAX_SPECIAL_ENERGY && ki >= 50 && kiBreakTimer == 0) {
                    isAttacking = true; attackTimer = 0; attackType = 6;
                    ki -= 50;
                }
            }

            if (isAttacking) {
                if (attackTimer == 0) hasHit = false;
                attackTimer++;

                if (attackType == 5 && attackTimer == 7) fireKiBlastProjectile();

                // Rilevamento collisione e danno
                if (!hasHit && opponent != null) {
                    Rectangle hitbox = getAttackHitbox();
                    if (hitbox != null && hitbox.intersects(opponent.getBounds())) {
                        int damage = 0, kb = 0;

                        if (attackType == 1 || attackType == 4) { damage = punchDamage; kb = (int)(8  * scale); }
                        else if (attackType == 2 || attackType == 3) { damage = kickDamage;  kb = (int)(10 * scale); }
                        else if (attackType == 6) { damage = specialDamage; kb = (int)(40 * scale); }

                        if (damage > 0) {
                            // =============================================
                            // SISTEMA 2 — SCALING DANNO COMBO
                            // =============================================
                            if (comboTimer > 0) comboCount++;
                            else                comboCount = 1;
                            comboTimer       = COMBO_WINDOW;
                            comboDamageScale = Math.max(0.30, 1.0 - (comboCount - 1) * 0.08);
                            int finalDamage  = (int)(damage * comboDamageScale);

                            // =============================================
                            // SISTEMA 3 — BONUS COUNTER ATTACK
                            // =============================================
                            if (isCountering) {
                                finalDamage   = (int)(finalDamage * 1.5);
                                isCountering  = false;
                                counterWindow = 0;
                            }

                            boolean isEnergy = (attackType == 6);
                            opponent.takeDamage(finalDamage, kb, isEnergy);
                            hasHit = true;
                            if (attackType == 6) onSpecialAttackHit(opponent);
                        }
                    }
                }

                int currentDuration = (attackType == 6) ? SPECIAL_DURATION : ATTACK_DURATION;
                if (attackTimer >= currentDuration) { isAttacking = false; attackType = 0; }
            }

            // Ki Blast projectiles: update e collisione
            for (int i = 0; i < activeBlasts.size(); i++) {
                KiBlastProjectile blast = activeBlasts.get(i);
                blast.update(activeEffects);
                Rectangle blastHitbox = new Rectangle(blast.px, blast.py - (int)(10 * scale), (int)(40 * scale), (int)(20 * scale));
                if (opponent != null && blastHitbox.intersects(opponent.getBounds())) {
                    opponent.takeDamage(kiBlastDamage, (int)(15 * scale), true);
                    int impX = blast.pFacingRight ? opponent.getX() + 10 : opponent.getX() + opponent.baseWidth - 10;
                    int impY = opponent.y + (opponent.baseHeight / 2);
                    opponent.activeEffects.add(new VisualEffect(blast.img, impX, impY,
                            new int[]{260, 0, 86}, new int[]{135, 448, 448}, new int[]{126, 70, 70}, new int[]{109, 64, 64},
                            new int[]{0, 0, 0}, new int[]{-40, -40, -40}, 6, 0.5 * scale));
                    activeBlasts.remove(i); i--; continue;
                }
                if (blast.isDead) { activeBlasts.remove(i); i--; }
            }

            // --- Teletrasporto ---
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
                        if (teleportFrame > 6) { isTeleporting = false; teleportPhase = 1; teleportFrame = 6; }
                    }
                }
            } else {
                // Volo e movimento
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
                        if (inLeft) {
                            x -= speed;
                            if (opponent != null && getBounds().intersects(opponent.getBounds())) x += speed;
                            flyMoveTimer++;
                            flyNum = facingRight
                                    ? (flyMoveTimer > 8 ? 5 : 4)  // indietro: prep → pieno
                                    : (flyMoveTimer > 8 ? 3 : 2);  // avanti: prep → pieno
                        } else if (inRight) {
                            x += speed;
                            if (opponent != null && getBounds().intersects(opponent.getBounds())) x -= speed;
                            flyMoveTimer++;
                            flyNum = facingRight
                                    ? (flyMoveTimer > 8 ? 3 : 2)   // avanti: prep → pieno
                                    : (flyMoveTimer > 8 ? 5 : 4);  // indietro: prep → pieno
                        } else {
                            flyMoveTimer = 0;
                            flyNum = 1; // stazionario
                        }
                    }
                } else {
                    if (!isCrouching && !isAttacking) {
                        if (inLeft)  { x -= speed; if (opponent != null && getBounds().intersects(opponent.getBounds())) x += speed; isMoving = true; }
                        if (inRight) { x += speed; if (opponent != null && getBounds().intersects(opponent.getBounds())) x -= speed; isMoving = true; }
                    }
                    if (inUp && !isJumping && !isCrouching && !isAttacking) { isCrouching = true; crouchTimer = 0; }
                    if (isCrouching) { crouchTimer++; if (crouchTimer >= CROUCH_DURATION) { isCrouching = false; isJumping = true; velocityY = jumpStrength; } }
                    if (isJumping) { velocityY += gravity; y += (int)velocityY; }
                }

                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; isFlying = false; }
                if (y < 0)  { y = 0; velocityY = 0; }
                if (x < 0)  x = 0;
                if (x > GamePanel.SCREEN_WIDTH - baseWidth) x = GamePanel.SCREEN_WIDTH - baseWidth;
            }
        } else {
            // Gravità durante hitstun o blocco
            if (!isFlying && y < groundY) {
                isJumping = true; velocityY += gravity; y += (int)velocityY;
                if (y >= groundY) { y = groundY; velocityY = 0; isJumping = false; }
            }
        }

        prevW = inUp; prevA = inLeft; prevS = inDown; prevD = inRight;
    }

    // =========================================================
    // RENDERING — drawFighterSprite con effetti Guard Crush
    // =========================================================
    protected void drawFighterSprite(Graphics2D g2d) {
        drawW = (int)(srcW * scale);
        drawH = (int)(srcH * scale);
        drawY = y - (drawH - baseHeight);
        shiftX = (baseWidth - drawW) / 2;
        if (isAttacking && !facingRight) shiftX = -(drawW - baseWidth);
        shiftX += customOffsetX;

        drawShadow(g2d);
        drawAura(g2d);

        float currentAlpha = 1.0f;
        if (isTeleporting) {
            currentAlpha = Math.max(0.0f, Math.min(1.0f, (float)teleportFrame / 6.0f));
        } else if (isInvincible && invincibleTimer % 10 < 5) {
            currentAlpha = 0.4f;
        } else if (isGuardCrushed && guardCrushTimer % 8 < 4) {
            currentAlpha = 0.65f;
        }

        // Tinta rossa lampeggiante durante Guard Crush
        if (isGuardCrushed) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
            g2d.setColor(Color.RED);
            g2d.fillRect(x + shiftX, drawY, drawW, drawH);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));

        int sX1 = x + shiftX, sX2 = sX1 + drawW;
        if (facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
        g2d.drawImage(spriteSheet, sX1, drawY, sX2, drawY + drawH, srcX, srcY, srcX + srcW, srcY + srcH, null);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    protected void drawPlayerPin(Graphics2D g2d, int drawX, int drawY, int drawW) {
        ResourceManager resM = ResourceManager.getInstance();
        BufferedImage pin = (playerID == 1) ? resM.pinP1 : resM.pinP2;
        if (pin != null) {
            int pinW = 16, pinH = (pinW * pin.getHeight()) / pin.getWidth();
            int pinX = drawX + (drawW - pinW) / 2;
            int pinY = drawY - pinH - 12;
            g2d.drawImage(pin, pinX, pinY, pinW, pinH, null);
        }
    }

    protected void drawUniversalHUD(Graphics2D g2d, String specialName) {
        ResourceManager resM = ResourceManager.getInstance();
        if (resM.hudFull == null) return;

        // --- COSTANTI LAYOUT ---
        int portraitSize = 80;
        int sideMargin   = 20;
        int pad          = 14;
        int centerGap    = 80; // aumentato per evitare sovrapposizione barre HP

        // Larghezza massima delle barre (dal bordo del portrait al centro schermo)
        int barsAreaStart = (playerID == 1)
                ? sideMargin + portraitSize + pad
                : GamePanel.SCREEN_WIDTH / 2 + centerGap;
        int barsAreaEnd   = (playerID == 1)
                ? GamePanel.SCREEN_WIDTH / 2 - centerGap
                : GamePanel.SCREEN_WIDTH - sideMargin - portraitSize - pad;
        int maxBarW = barsAreaEnd - barsAreaStart;

        // Larghezze proporzionali per ogni barra
        int hpBarW    = maxBarW;
        int spBarW    = (int)(maxBarW * 0.80);
        int auraBarW  = (int)(maxBarW * 0.65);
        int kiBarW    = (int)(maxBarW * 0.60);
        int guardBarW = (int)(maxBarW * 0.45);

        int barH        = 16;
        int barGap      = 22;
        int firstBarY   = 20;
        int radius      = 5;
        int labelMargin = 8;

        // --- RITRATTO CIRCOLARE ---
        int portraitX = (playerID == 1) ? sideMargin : GamePanel.SCREEN_WIDTH - portraitSize - sideMargin;
        int portraitY = firstBarY;

        java.awt.geom.Ellipse2D.Float clip = new java.awt.geom.Ellipse2D.Float(
                portraitX, portraitY, portraitSize, portraitSize);
        java.awt.Shape oldClip = g2d.getClip();
        g2d.setClip(clip);
        g2d.drawImage(resM.hudFull,
                portraitX, portraitY,
                portraitX + portraitSize, portraitY + portraitSize,
                0, portraitSrcY, 254, portraitSrcY + 254, null);
        g2d.setClip(oldClip);

        g2d.setColor((playerID == 1) ? UIManager.COLOR_P1 : UIManager.COLOR_P2);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawOval(portraitX, portraitY, portraitSize, portraitSize);

        // --- NOME GIOCATORE ---
        if (resM.saiyanFont != null) {
            g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 22f));
            String pText = (playerID == 1) ? "PLAYER ONE" : "PLAYER TWO";
            g2d.setColor((playerID == 1) ? UIManager.COLOR_P1 : UIManager.COLOR_P2);
            int nameX = (playerID == 1)
                    ? barsAreaStart
                    : barsAreaEnd - g2d.getFontMetrics().stringWidth(pText);
            g2d.drawString(pText, nameX, firstBarY + 14);
        }

        int barsStartY = firstBarY + 24;

        // --- BARRA HP ---
        double hpPercent = (double) hp / maxHP;
        Color hpColor = hpPercent > 0.50 ? new Color(60, 210, 60)
                : hpPercent >= 0.21      ? new Color(230, 140, 20)
                :                          new Color(210, 40, 40);
        drawBar(g2d, barsAreaStart, barsAreaEnd, barsStartY,
                hpBarW, barH, radius, hpPercent,
                new Color(20, 20, 20), hpColor, "HP", hpColor, resM, labelMargin);

        // --- BARRA SPECIAL ---
        double sPercent = specialEnergy / MAX_SPECIAL_ENERGY;
        drawBar(g2d, barsAreaStart, barsAreaEnd, barsStartY + barGap,
                spBarW, barH, radius, sPercent,
                new Color(10, 30, 40), new Color(0, 200, 220),
                specialName, new Color(0, 200, 220), resM, labelMargin);

        // --- BARRA AURA ---
        double aPercent = auraEnergy / MAX_AURA_ENERGY;
        drawBar(g2d, barsAreaStart, barsAreaEnd, barsStartY + barGap * 2,
                auraBarW, barH, radius, aPercent,
                new Color(40, 30, 5), auraColor,
                "AURA", auraColor, resM, labelMargin);

        // --- BARRA KI ---
        double kiPercent = ki / MAX_KI;
        Color kiColor = kiBreakTimer > 0
                ? (kiBreakTimer % 10 < 5 ? new Color(255, 130, 0) : new Color(90, 40, 0))
                : (isAuraActive ? new Color(160, 220, 255) : new Color(70, 150, 255));
        String kiLabel = kiBreakTimer > 0 ? "KI BREAK!" : "KI";
        Color kiLabelColor = kiBreakTimer > 0 ? new Color(255, 130, 0) : new Color(70, 150, 255);
        drawBar(g2d, barsAreaStart, barsAreaEnd, barsStartY + barGap * 3,
                kiBarW, barH, radius, kiPercent,
                new Color(15, 15, 40), kiColor, kiLabel, kiLabelColor, resM, labelMargin);

        // --- BARRA GUARD ---
        double guardPercent = (double) guardHealth / MAX_GUARD_HEALTH;
        Color guardColor = isGuardCrushed     ? new Color(200, 30, 30)
                : guardPercent < 0.30         ? new Color(220, 100, 30)
                :                               new Color(175, 175, 185);
        String guardLabel = isGuardCrushed ? "BROKEN" : "GUARD";
        Color guardLabelColor = isGuardCrushed ? new Color(220, 50, 50) : new Color(150, 150, 165);
        drawBar(g2d, barsAreaStart, barsAreaEnd, barsStartY + barGap * 4,
                guardBarW, barH, radius, guardPercent,
                new Color(35, 10, 10), guardColor, guardLabel, guardLabelColor, resM, labelMargin);

        // --- BADGE ---
        if (isCountering && counterWindow > 0 && counterWindow % 10 < 6)
            drawCounterBadge(g2d, portraitX, portraitSize);
        if (comboCount >= 2)
            drawComboBadge(g2d);
    }

    private void drawBar(Graphics2D g2d, int areaStart, int areaEnd, int y,
                         int w, int h, int radius, double percent,
                         Color bgColor, Color fillColor,
                         String label, Color labelColor,
                         ResourceManager resM, int labelMargin) {

        percent = Math.max(0, Math.min(1, percent));
        int fillW = (int)(w * percent);

        if (playerID == 1) {
            int x = areaStart;

            g2d.setColor(bgColor);
            g2d.fillRoundRect(x, y, w, h, radius, radius);

            if (fillW > 0) {
                g2d.setColor(fillColor);
                g2d.fillRoundRect(x, y, fillW, h, radius, radius);
            }

            g2d.setColor(fillColor.darker());
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(x, y, w, h, radius, radius);

            // Label fuori a destra
            if (resM.saiyanFont != null) {
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 14f));
                g2d.setColor(labelColor);
                g2d.drawString(label, x + w + labelMargin, y + h - 2);
            }

        } else {
            int x = areaEnd - w;

            g2d.setColor(bgColor);
            g2d.fillRoundRect(x, y, w, h, radius, radius);

            // Fill da destra verso sinistra
            if (fillW > 0) {
                g2d.setColor(fillColor);
                g2d.fillRoundRect(x + w - fillW, y, fillW, h, radius, radius);
            }

            g2d.setColor(fillColor.darker());
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(x, y, w, h, radius, radius);

            // Label FUORI a sinistra della barra (speculare a P1)
            if (resM.saiyanFont != null) {
                g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 14f));
                g2d.setColor(labelColor);
                int labelW = g2d.getFontMetrics().stringWidth(label);
                g2d.drawString(label, x - labelW - labelMargin, y + h - 2);
            }
        }
    }

    // Badge "COUNTER!" lampeggiante vicino all'HUD del fighter
    private void drawCounterBadge(Graphics2D g2d, int portraitX, int portraitSize) {
        ResourceManager resM = ResourceManager.getInstance();
        if (resM.saiyanFont == null) return;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(resM.saiyanFont.deriveFont(Font.BOLD, 22f));
        String text = "COUNTER!";
        int tw = g2d.getFontMetrics().stringWidth(text);
        int tx = (playerID == 1)
                ? portraitX + portraitSize + 5
                : portraitX - g2d.getFontMetrics().stringWidth(text) - 5;
        int ty = 205;
        // Ombra
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(text, tx + 2, ty + 2);
        // Testo giallo-oro
        g2d.setColor(new Color(255, 215, 40));
        g2d.drawString(text, tx, ty);
    }

    // Badge combo numerica che galleggia sopra il personaggio
    private void drawComboBadge(Graphics2D g2d) {
        ResourceManager resM = ResourceManager.getInstance();
        if (resM.saiyanFont == null) return;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int badgeCX = x + shiftX + drawW / 2;
        int badgeY  = drawY - 32;

        // Font scala col numero di hit (max 44px)
        int fontSize = Math.min(44, 22 + comboCount * 2);
        g2d.setFont(resM.saiyanFont.deriveFont(Font.BOLD, (float)fontSize));

        String hitText  = comboCount + " HIT";
        String scaleStr = String.format("x%.0f%%", comboDamageScale * 100);
        int tw = g2d.getFontMetrics().stringWidth(hitText);

        // Ombra
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(hitText, badgeCX - tw / 2 + 2, badgeY + 2);

        // Colore: bianco → giallo → arancione → rosso caldo
        Color comboColor;
        if      (comboCount <= 3)  comboColor = new Color(255, 255, 255);
        else if (comboCount <= 6)  comboColor = new Color(255, 230, 70);
        else if (comboCount <= 10) comboColor = new Color(255, 135, 25);
        else                       comboColor = new Color(255, 45, 45);

        g2d.setColor(comboColor);
        g2d.drawString(hitText, badgeCX - tw / 2, badgeY);

        // Percentuale scaling sotto, più piccola
        g2d.setFont(resM.saiyanFont.deriveFont(Font.PLAIN, 14f));
        g2d.setColor(new Color(200, 200, 255, 210));
        int sw = g2d.getFontMetrics().stringWidth(scaleStr);
        g2d.drawString(scaleStr, badgeCX - sw / 2, badgeY + 17);
    }

    // =========================================================
    // RENDERING — Aura e Ombra (invariate)
    // =========================================================
    protected void drawAura(Graphics2D g2d) {
        if ((isChargingAura || isAuraActive) && auraImage != null) {
            int[] aX = {3, 357, 4, 368}, aY = {4, 4, 454, 440};
            int[] aW = {350, 345, 359, 356}, aH = {446, 432, 437, 417};
            double auraDrawScale = 0.45 * scale;
            int dAW = (int)(aW[auraFrame] * auraDrawScale), dAH = (int)(aH[auraFrame] * auraDrawScale);
            int dAX = x + (baseWidth - dAW) / 2;
            int dAY = y - (dAH - baseHeight) + (int)(20 * scale);
            g2d.drawImage(auraImage, dAX, dAY, dAX + dAW, dAY + dAH,
                    aX[auraFrame], aY[auraFrame], aX[auraFrame] + aW[auraFrame], aY[auraFrame] + aH[auraFrame], null);
        }
    }

    protected void drawShadow(Graphics2D g2d) {
        int dist = Math.max(0, groundY - y);
        float maxDist  = (float)(250 * scale);
        float sScale   = Math.max(0.2f, 1.0f - (dist / maxDist));
        int shadowW    = (int)(baseWidth * 1.4 * sScale);
        int shadowH    = (int)(22 * scale * sScale);
        int centerX    = x + (baseWidth / 2);
        int shadowX    = centerX - (shadowW / 2);
        int floorY     = groundY + baseHeight;
        int shadowY    = floorY - (shadowH / 2) - (int)(4 * scale);
        float alphaVal = 0.6f * sScale;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaVal));
        g2d.setColor(Color.BLACK);
        g2d.fillOval(shadowX, shadowY, shadowW, shadowH);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}