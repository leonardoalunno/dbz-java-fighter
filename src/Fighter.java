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

    // =============================================
    // POSIZIONE E IDENTITA'
    // =============================================
    protected int x, y;
    protected int playerID;
    protected BufferedImage spriteSheet;
    protected BufferedImage kiBlastImage;
    protected BufferedImage auraImage;
    protected int portraitSrcY = 0;
    public double scale = 1.0;

    // =============================================
    // STATISTICHE BASE
    // =============================================
    public int hp = 100;
    protected int maxHP = 100;
    protected int speed = 4;
    protected int baseWidth = 48;
    protected int baseHeight = 86;
    public Color auraColor = Color.WHITE;

    // =============================================
    // FISICA
    // =============================================
    protected boolean facingRight;
    protected int groundY;
    protected double velocityY = 0;
    protected double gravity = 0.5;
    protected double jumpStrength = -12;
    protected final int CROUCH_DURATION = 3;
    protected int crouchTimer = 0;

    // =============================================
    // FSM — Stato corrente e precedente
    // =============================================
    protected FighterState state     = FighterState.IDLE;
    protected FighterState prevState = FighterState.IDLE;

    // =============================================
    // INPUT
    // =============================================
    protected boolean inUp, inDown, inLeft, inRight;
    protected boolean inLight, inHeavy, inSpecial, inUltimate;
    protected boolean inBlock, inFly, inCharge;
    protected boolean prevUp, prevLeft, prevRight, prevDown;

    // =============================================
    // INPUT BUFFER — Neo Combo
    // =============================================
    protected InputBuffer inputBuffer;

    // =============================================
    // COMBO SYSTEM
    // =============================================
    protected ComboRoute[] comboRoutes;   // definite nelle subclass
    protected ComboRoute   activeRoute;   // combo in esecuzione
    protected int comboStep    = 0;       // posizione nella sequenza
    protected int attackTimer  = 0;       // frame dentro l'AttackData corrente
    protected int comboCount   = 0;       // hit totali nella catena
    protected int comboWindowTimer = 0;   // finestra per continuare la combo
    protected final int COMBO_WINDOW = 30;
    protected double comboDamageScale = 1.0;
    protected boolean hasHit = false;     // ha già colpito in questo step

    // =============================================
    // ATTACCO SPECIALE / ULTIMATE
    // =============================================
    protected double specialEnergy  = 0;
    protected double MAX_SPECIAL_ENERGY = 2400;
    protected double specialDrainRate;
    protected int SPECIAL_CHARGE   = 40;
    protected int SPECIAL_DURATION = 90;
    protected int specialTimer     = 0;   // frame dentro lo special/ultimate

    // =============================================
    // KI SYSTEM
    // =============================================
    protected double ki         = 0;
    protected double MAX_KI     = 300.0;
    protected double kiRegen    = 0.5;
    protected double kiBlastKiCost  = 80.0;
    protected double kiOnHitReward  = 15.0;
    protected int kiBreakTimer  = 0;
    protected final int KI_BREAK_DURATION = 60;
    // Carica manuale: +kiChargeRate per frame tenendo C
    protected double kiChargeRate = 3.0;

    // =============================================
    // AURA SYSTEM
    // =============================================
    protected int    auraFrame      = 0;
    protected int    auraAnimTimer  = 0;
    protected int    auraChargeTimer = 0;
    protected final int AURA_CHARGE_DURATION = 30;
    protected final int AURA_DURATION        = 600;
    protected double auraEnergy     = 0;
    protected double MAX_AURA_ENERGY = 1200;
    protected double AURA_DRAIN_RATE = MAX_AURA_ENERGY / (double)600;

    // =============================================
    // GUARD SYSTEM
    // =============================================
    protected int guardHealth        = 100;
    protected final int MAX_GUARD_HEALTH     = 100;
    protected int guardCrushTimer    = 0;
    protected final int GUARD_CRUSH_DURATION = 45;
    protected final int PERFECT_GUARD_WINDOW = 8;
    protected int blockActiveTimer   = 0;
    protected boolean isCountering   = false;
    protected int counterWindow      = 0;
    protected final int COUNTER_WINDOW_DURATION = 22;
    protected int blockTimer         = 0;

    // =============================================
    // KNOCKBACK & HITSTUN
    // =============================================
    public int knockbackSpeed    = 0;
    protected int hitstunDuration = 20;
    protected int hitTimer        = 0;
    public boolean wallBounced    = false;
    protected int invincibleTimer = 0;

    // =============================================
    // Z-CANCEL
    // =============================================
    protected final double Z_CANCEL_COST = 60.0;
    // Z-Cancel disponibile dopo il primo hit della combo
    protected boolean zCancelAvailable = false;

    // =============================================
    // VOLO
    // =============================================
    protected int flyNum       = 1;
    protected int flyMoveTimer = 0;
    protected int flyCooldown  = 0;

    // =============================================
    // TELEPORT
    // =============================================
    protected final int DASH_DISTANCE   = 150;
    protected final int DOUBLE_TAP_WINDOW = 15;
    protected int tapTimerW = 0, tapTimerA = 0, tapTimerS = 0, tapTimerD = 0;
    protected int teleportPhase = 1, teleportFrame = 6, teleportCounter = 0;
    protected int targetOffsetX = 0, targetOffsetY = 0;

    // =============================================
    // FINE MATCH
    // =============================================
    protected int endFrame = 1, endTimer = 0;

    // =============================================
    // DANNI
    // =============================================
    protected int kiBlastDamage = 10;
    protected int specialDamage = 35;

    // =============================================
    // ANIMAZIONE SPRITE
    // =============================================
    protected int srcX, srcY, srcW, srcH;
    protected int drawW, drawH, drawY, shiftX;
    protected int spriteCounter = 0, spriteNum = 1;
    protected int customOffsetX = 0;

    // =============================================
    // EFFETTI VISIVI
    // =============================================
    protected ArrayList<VisualEffect>      activeEffects = new ArrayList<>();
    protected ArrayList<KiBlastProjectile> activeBlasts  = new ArrayList<>();

    // =============================================
    // COSTRUTTORE
    // =============================================
    public Fighter(int x, int y, int playerID, BufferedImage spriteSheet) {
        this.x          = x;
        this.y          = y;
        this.playerID   = playerID;
        this.spriteSheet = spriteSheet;
        this.inputBuffer = new InputBuffer();
    }

    // =============================================
    // METODI ASTRATTI — implementati nelle subclass
    // =============================================
    public abstract void draw(Graphics2D g2d);
    public abstract ComboRoute[] defineComboRoutes();
    public abstract Rectangle getSpecialHitbox();
    public abstract Rectangle getUltimateHitbox();
    protected abstract void spawnKiBlastVFX();
    protected abstract void fireKiBlastProjectile();
    protected abstract void onSpecialHit(Fighter opponent);
    protected abstract void onUltimateHit(Fighter opponent);

    // =============================================
    // HELPER FSM — controllo stato
    // =============================================
    public boolean isInState(FighterState... states) {
        for (FighterState s : states) if (this.state == s) return true;
        return false;
    }

    protected void setState(FighterState newState) {
        prevState = state;
        state     = newState;
    }

    // =============================================
    // HELPER — retrocompatibilita' con GamePanel
    // =============================================
    public boolean isAttacking() {
        return isInState(FighterState.COMBO_LIGHT,
                FighterState.COMBO_HEAVY,
                FighterState.SPECIAL_STARTUP,
                FighterState.SPECIAL_ACTIVE,
                FighterState.ULTIMATE_STARTUP,
                FighterState.ULTIMATE_ACTIVE);
    }
    public boolean isInHitStun()    { return state == FighterState.HIT_STUN;      }
    public boolean isBlocking()     { return isInState(FighterState.BLOCKING,
            FighterState.BLOCKING_AIR); }
    public boolean isFlying()       { return isInState(FighterState.FLYING_IDLE,
            FighterState.FLYING_FORWARD,
            FighterState.FLYING_FORWARD_FULL,
            FighterState.FLYING_BACKWARD,
            FighterState.FLYING_BACKWARD_FULL); }
    public boolean isJumping()      { return state == FighterState.JUMPING;        }
    public boolean isGrounded()     { return isInState(FighterState.IDLE,
            FighterState.WALKING,
            FighterState.CROUCHING);   }

    // =============================================
    // BOUNDS — hitbox fisica del personaggio
    // =============================================
    public int getX() { return x; }
    public Rectangle getBounds() {
        return new Rectangle(x, y, baseWidth, baseHeight);
    }

    // =============================================
    // HITBOX ATTACCO CORRENTE
    // Usata per il rilevamento colpi nel update()
    // =============================================
    public Rectangle getCurrentAttackHitbox() {
        if (activeRoute == null || comboStep >= activeRoute.length()) return null;
        AttackData atk = activeRoute.attacks[comboStep];

        // Hitbox attiva solo nella finestra 'active'
        if (attackTimer < atk.startup || attackTimer >= atk.startup + atk.active)
            return null;

        int reach    = (int)(40 * scale);
        int boxH     = (int)(20 * scale);
        int offsetY  = (int)(20 * scale);
        int hX = facingRight ? x + baseWidth : x - reach;
        return new Rectangle(hX, y + offsetY, reach, boxH);
    }

    // =============================================
    // START TELEPORT
    // =============================================
    protected void startTeleport(int offX, int offY, boolean faceRight) {
        setState(FighterState.TELEPORTING);
        teleportPhase   = 1;
        teleportFrame   = 6;
        teleportCounter = 0;
        targetOffsetX   = offX;
        targetOffsetY   = offY;
        facingRight     = faceRight;
    }

    // =============================================
    // TAKE DAMAGE — integra tutti i sistemi
    // =============================================
    public void takeDamage(int amount, int appliedKnockback,
                           boolean isEnergy, boolean isUnblockable) {
        // Immunità
        if (state == FighterState.KO)           return;
        if (invincibleTimer > 0)                return;
        if (state == FighterState.GUARD_CRUSHED) return;
        if (isInHitStun() && !wallBounced)      return;

        // Teleport = i-frames
        if (state == FighterState.TELEPORTING)  return;

        if (isBlocking() && !isUnblockable) {
            // --- PERFECT GUARD ---
            boolean isPerfect = (blockActiveTimer <= PERFECT_GUARD_WINDOW) && !isEnergy;
            if (isPerfect) {
                isCountering  = true;
                counterWindow = COUNTER_WINDOW_DURATION;
                ki = Math.min(MAX_KI, ki + 30);
                knockbackSpeed = Math.max(1, appliedKnockback / 5);
                return;
            }

            // --- GUARDIA NORMALE ---
            if (isEnergy) hp -= Math.max(1, amount / 4);
            guardHealth   -= amount / 2;
            knockbackSpeed = appliedKnockback / 3;
            blockTimer++;

            // --- GUARD CRUSH ---
            if (guardHealth <= 0) {
                guardHealth      = 0;
                blockActiveTimer = 0;
                isCountering     = false;
                counterWindow    = 0;
                setState(FighterState.GUARD_CRUSHED);
                guardCrushTimer  = 0;
            }

        } else {
            // --- COLPO SENZA GUARDIA ---
            hp -= amount;
            if (hp < 0) hp = 0;

            hitTimer         = 0;
            wallBounced      = false;
            hitstunDuration  = Math.max(12, Math.min(55, amount * 2));
            knockbackSpeed   = appliedKnockback;

            isCountering     = false;
            counterWindow    = 0;
            blockActiveTimer = 0;
            zCancelAvailable = false;

            // Interrompi qualsiasi azione
            activeRoute  = null;
            comboStep    = 0;
            attackTimer  = 0;
            specialTimer = 0;

            // Launcher — manda in aria
            if (isGrounded()) {
                velocityY = -8.0;
            }

            setState(hp <= 0 ? FighterState.KO : FighterState.HIT_STUN);

            // Tumbling sotto 20% HP
            if (hp > 0 && hp <= (int)(maxHP * 0.20))
                setState(FighterState.TUMBLING);
        }
    }

    // Overload retrocompatibile (isUnblockable = false di default)
    public void takeDamage(int amount, int appliedKnockback, boolean isEnergy) {
        takeDamage(amount, appliedKnockback, isEnergy, false);
    }

    // =============================================
    // UPDATE — motore principale
    // =============================================
    public void update(KeyHandler keyH, Fighter opponent) {

        // Aggiornamento effetti visivi
        for (int i = 0; i < activeEffects.size(); i++) {
            VisualEffect eff = activeEffects.get(i);
            eff.update();
            if (eff.isDead) { activeEffects.remove(i); i--; }
        }

        // --- KO ---
        if (state == FighterState.KO) {
            activeRoute = null; comboStep = 0;
            inputBuffer.clear();
            if (y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer > 10) { if (endFrame < 7) endFrame++; endTimer = 0; }
            return;
        }

        // --- VINCITORE ---
        if (opponent != null && opponent.state == FighterState.KO) {
            setState(FighterState.WINNER);
            activeRoute = null; comboStep = 0;
            inputBuffer.clear();
            if (!isFlying() && y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer > 15) { endFrame = (endFrame == 1) ? 2 : 1; endTimer = 0; }
            return;
        }

        // --- KNOCKBACK con Wall Bounce ---
        if (knockbackSpeed > 0) {
            x += facingRight ? -knockbackSpeed : knockbackSpeed;
            knockbackSpeed = Math.max(0, knockbackSpeed - 2);
            boolean hitLeft  = (x <= 0);
            boolean hitRight = (x >= GamePanel.SCREEN_WIDTH - baseWidth);
            if ((hitLeft || hitRight) && !wallBounced && knockbackSpeed > 3) {
                wallBounced     = true;
                x               = hitLeft ? 0 : GamePanel.SCREEN_WIDTH - baseWidth;
                knockbackSpeed  = knockbackSpeed / 2 + 3;
                facingRight     = !facingRight;
                hitstunDuration = Math.max(8, hitstunDuration - 6);
            } else {
                if (x < 0) x = 0;
                if (x > GamePanel.SCREEN_WIDTH - baseWidth) x = GamePanel.SCREEN_WIDTH - baseWidth;
            }
        }

        // --- HIT STUN ---
        if (state == FighterState.HIT_STUN || state == FighterState.TUMBLING) {
            hitTimer++;
            // Gravità durante hitstun
            if (!isFlying() && y < groundY) {
                velocityY += gravity; y += (int)velocityY;
                if (y >= groundY) { y = groundY; velocityY = 0; }
            }
            if (hitTimer > hitstunDuration) {
                wallBounced      = false;
                invincibleTimer  = 40;
                setState(y < groundY ? FighterState.JUMPING : FighterState.IDLE);
            }
            return;
        }

        // --- INVINCIBILITA' post-hitstun ---
        if (invincibleTimer > 0) invincibleTimer--;

        // --- GUARD CRUSH ---
        if (state == FighterState.GUARD_CRUSHED) {
            guardCrushTimer++;
            if (!isFlying() && y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            if (guardCrushTimer >= GUARD_CRUSH_DURATION) {
                guardHealth     = MAX_GUARD_HEALTH / 2;
                invincibleTimer = 40;
                setState(FighterState.IDLE);
            }
            return;
        }

        // Recupero guardia passivo
        if (!isBlocking() && guardHealth < MAX_GUARD_HEALTH)
            guardHealth = Math.min(MAX_GUARD_HEALTH, guardHealth + 1);

        // --- LETTURA INPUT ---
        inUp      = (playerID == 1) ? keyH.p1_up      : keyH.p2_up;
        inDown    = (playerID == 1) ? keyH.p1_down    : keyH.p2_down;
        inLeft    = (playerID == 1) ? keyH.p1_left    : keyH.p2_left;
        inRight   = (playerID == 1) ? keyH.p1_right   : keyH.p2_right;
        inLight   = (playerID == 1) ? keyH.p1_light   : keyH.p2_light;
        inHeavy   = (playerID == 1) ? keyH.p1_heavy   : keyH.p2_heavy;
        inSpecial = (playerID == 1) ? keyH.p1_special : keyH.p2_special;
        inUltimate= (playerID == 1) ? keyH.p1_ultimate: keyH.p2_ultimate;
        inBlock   = (playerID == 1) ? keyH.p1_block   : keyH.p2_block;
        inFly     = (playerID == 1) ? keyH.p1_fly     : keyH.p2_fly;
        inCharge  = (playerID == 1) ? keyH.p1_charge  : keyH.p2_charge;

        // Aggiorna buffer input
        inputBuffer.update(inLight, inHeavy, inSpecial, inUltimate);

        // Facing automatico
        if (!isAttacking() && state != FighterState.TELEPORTING
                && state != FighterState.CHARGING_KI && opponent != null)
            facingRight = (x <= opponent.getX());

        // --- BLOCCO ---
        boolean wasBlocking = isBlocking();
        if (inBlock && !isAttacking() && state != FighterState.TELEPORTING
                && state != FighterState.CHARGING_KI) {
            boolean inAir = isFlying() || state == FighterState.JUMPING;
            setState(inAir ? FighterState.BLOCKING_AIR : FighterState.BLOCKING);
        } else if (isBlocking()) {
            setState(prevState == FighterState.JUMPING || prevState == FighterState.FLYING_IDLE
                    ? FighterState.IDLE : FighterState.IDLE);
        }
        if (isBlocking()) {
            if (!wasBlocking) blockActiveTimer = 0;
            blockActiveTimer++;
        } else {
            blockActiveTimer = 0;
        }

        // Counter window scade
        if (counterWindow > 0) { counterWindow--; if (counterWindow == 0) isCountering = false; }

        // --- KI: rigenerazione e Ki Break ---
        if (kiBreakTimer > 0) {
            kiBreakTimer--;
        } else {
            double regenMult = (state == FighterState.AURA_ACTIVE) ? 2.0 : 1.0;
            ki = Math.min(MAX_KI, ki + kiRegen * regenMult);
        }

        // Carica Ki manuale
        if (inCharge && !isAttacking() && !isBlocking()
                && state != FighterState.TELEPORTING) {
            setState(FighterState.CHARGING_KI);
            ki = Math.min(MAX_KI, ki + kiChargeRate);
        } else if (state == FighterState.CHARGING_KI) {
            setState(FighterState.IDLE);
        }

        // --- COMBO WINDOW timer ---
        if (comboWindowTimer > 0) {
            comboWindowTimer--;
        } else if (comboCount > 0 && !isAttacking()) {
            comboCount        = 0;
            comboDamageScale  = 1.0;
            activeRoute       = null;
            comboStep         = 0;
        }

        // --- AURA ---
        if (inCharge && auraEnergy >= MAX_AURA_ENERGY
                && state != FighterState.AURA_ACTIVE
                && !isAttacking() && !isBlocking()) {
            setState(FighterState.CHARGING_KI);
            auraChargeTimer = 0;
            velocityY       = 0;
        }
        if (state == FighterState.CHARGING_KI && auraEnergy >= MAX_AURA_ENERGY) {
            auraChargeTimer++;
            if (auraChargeTimer >= AURA_CHARGE_DURATION) {
                setState(FighterState.AURA_ACTIVE);
                hp += 25; if (hp > maxHP) hp = maxHP;
            }
        }
        if (state == FighterState.AURA_ACTIVE) {
            speed        = (int)(8 * scale);
            jumpStrength = -15 * scale;
            auraEnergy  -= AURA_DRAIN_RATE;
            if (auraEnergy <= 0) { auraEnergy = 0; setState(FighterState.IDLE); }
        } else {
            speed        = (int)(4 * scale);
            jumpStrength = -12 * scale;
            if (auraEnergy < MAX_AURA_ENERGY && state != FighterState.CHARGING_KI)
                auraEnergy++;
        }

        // Animazione aura
        if (state == FighterState.CHARGING_KI || state == FighterState.AURA_ACTIVE) {
            auraAnimTimer++;
            if (auraAnimTimer > 4) { auraFrame++; if (auraFrame > 3) auraFrame = 0; auraAnimTimer = 0; }
            if (auraImage != null && Math.random() < 0.15) {
                int rX = x + (int)(Math.random() * baseWidth * 1.5) - (int)(baseWidth * 0.25);
                int rY = y - (int)(Math.random() * baseHeight);
                activeEffects.add(new VisualEffect(auraImage, rX, rY,
                        new int[]{129, 197}, new int[]{985, 894},
                        new int[]{66, 72}, new int[]{106, 79}, 3, scale * 0.6));
            }
        } else { auraFrame = 0; }

        // Special energy
        if (isInState(FighterState.SPECIAL_ACTIVE, FighterState.ULTIMATE_ACTIVE)) {
            specialEnergy -= specialDrainRate; if (specialEnergy < 0) specialEnergy = 0;
        } else if (specialEnergy < MAX_SPECIAL_ENERGY) specialEnergy++;

        // Doppio tap teleport
        if (tapTimerW > 0) tapTimerW--; if (tapTimerA > 0) tapTimerA--;
        if (tapTimerS > 0) tapTimerS--; if (tapTimerD > 0) tapTimerD--;
        boolean newA = inLeft  && !prevLeft;
        boolean newD = inRight && !prevRight;
        boolean newW = inUp    && !prevUp;
        boolean newS = inDown  && !prevDown;

        // =============================================
        // LOGICA PRINCIPALE — solo se non bloccati
        // =============================================
        if (!isBlocking() && !isInHitStun()
                && state != FighterState.CHARGING_KI
                && state != FighterState.GUARD_CRUSHED) {

            // --- TELEPORT ---
            if (!isAttacking() && state != FighterState.TELEPORTING) {
                if (newA) { if (tapTimerA > 0) { startTeleport((int)(-DASH_DISTANCE * scale), 0, facingRight); tapTimerA = 0; } else tapTimerA = DOUBLE_TAP_WINDOW; }
                if (newD) { if (tapTimerD > 0) { startTeleport((int)( DASH_DISTANCE * scale), 0, facingRight); tapTimerD = 0; } else tapTimerD = DOUBLE_TAP_WINDOW; }
                if (newW) { if (tapTimerW > 0) { startTeleport(0, (int)(-DASH_DISTANCE * scale), facingRight); tapTimerW = 0; } else tapTimerW = DOUBLE_TAP_WINDOW; }
                if (newS && isFlying()) { if (tapTimerS > 0) { startTeleport(0, (int)(DASH_DISTANCE * scale), facingRight); tapTimerS = 0; } else tapTimerS = DOUBLE_TAP_WINDOW; }
            }

            // --- TELEPORT update ---
            if (state == FighterState.TELEPORTING) {
                teleportCounter++;
                if (teleportCounter > 2) {
                    teleportCounter = 0;
                    if (teleportPhase == 1) {
                        teleportFrame--;
                        if (teleportFrame < 1) {
                            x += targetOffsetX; y += targetOffsetY;
                            if (y > groundY) y = groundY;
                            if (isFlying() && y > groundY - (int)(40 * scale)) y = groundY - (int)(40 * scale);
                            teleportPhase = 2; teleportFrame = 1;
                        }
                    } else {
                        teleportFrame++;
                        if (teleportFrame > 6) { setState(FighterState.IDLE); teleportPhase = 1; teleportFrame = 6; }
                    }
                }
            }

            // --- NEO COMBO ---
            if (!isAttacking() && state != FighterState.TELEPORTING) {

                // Z-CANCEL: interrompe la combo corrente spendendo Ki
                if (isAttacking() && zCancelAvailable
                        && inBlock && ki >= Z_CANCEL_COST) {
                    ki -= Z_CANCEL_COST;
                    activeRoute      = null;
                    comboStep        = 0;
                    attackTimer      = 0;
                    zCancelAvailable = false;
                    setState(FighterState.IDLE);
                }

                // Cerca una combo che matchi l'input buffer
                if (inLight || inHeavy) {
                    ComboRoute matched = findMatchingRoute();
                    if (matched != null) {
                        activeRoute  = matched;
                        comboStep    = matched.length() - 1; // step corrente
                        attackTimer  = 0;
                        hasHit       = false;
                        setState(inputBuffer.lastInput() == ComboRoute.HEAVY
                                ? FighterState.COMBO_HEAVY
                                : FighterState.COMBO_LIGHT);
                        inputBuffer.consume();
                    }
                }

                // SPECIAL (Ki Blast)
                if (inSpecial && ki >= kiBlastKiCost
                        && kiBreakTimer == 0
                        && state != FighterState.SPECIAL_STARTUP) {
                    ki -= kiBlastKiCost;
                    if (ki <= 0) { ki = 0; kiBreakTimer = KI_BREAK_DURATION; }
                    setState(FighterState.SPECIAL_STARTUP);
                    specialTimer = 0;
                    spawnKiBlastVFX();
                }

                // ULTIMATE
                if (inUltimate && specialEnergy >= MAX_SPECIAL_ENERGY
                        && ki >= 50 && kiBreakTimer == 0) {
                    ki -= 50;
                    setState(FighterState.ULTIMATE_STARTUP);
                    specialTimer = 0;
                }
            }

            // --- ESECUZIONE COMBO ---
            if (isInState(FighterState.COMBO_LIGHT, FighterState.COMBO_HEAVY)) {
                attackTimer++;
                AttackData atk = activeRoute.attacks[comboStep];

                // Hit detection
                if (!hasHit && opponent != null) {
                    Rectangle hitbox = getCurrentAttackHitbox();
                    if (hitbox != null && hitbox.intersects(opponent.getBounds())) {
                        // Combo scaling
                        comboCount++;
                        comboWindowTimer  = COMBO_WINDOW;
                        comboDamageScale  = Math.max(0.30, 1.0 - (comboCount - 1) * 0.08);
                        int finalDamage   = (int)(atk.damage * comboDamageScale);

                        // Counter bonus
                        if (isCountering) {
                            finalDamage   = (int)(finalDamage * 1.5);
                            isCountering  = false;
                            counterWindow = 0;
                        }

                        opponent.takeDamage(finalDamage, atk.knockback,
                                atk.isEnergy, atk.isUnblockable);
                        ki = Math.min(MAX_KI, ki + kiOnHitReward);
                        hasHit           = true;
                        zCancelAvailable = true;
                    }
                }

                // Fine di questo step della combo
                if (attackTimer >= atk.totalDuration()) {
                    attackTimer = 0;
                    hasHit      = false;

                    // Avanza al prossimo step se c'è
                    if (comboStep + 1 < activeRoute.length()) {
                        comboStep++;
                        // Aspetta il prossimo input nel COMBO_WINDOW
                        setState(FighterState.IDLE);
                    } else {
                        // Combo finita
                        activeRoute      = null;
                        comboStep        = 0;
                        zCancelAvailable = false;
                        setState(isFlying() ? FighterState.FLYING_IDLE : FighterState.IDLE);
                    }
                }
            }

            // --- SPECIAL update ---
            if (state == FighterState.SPECIAL_STARTUP) {
                specialTimer++;
                if (specialTimer == 7) fireKiBlastProjectile();
                if (specialTimer >= 15) setState(FighterState.IDLE);
            }

            // --- ULTIMATE update ---
            if (state == FighterState.ULTIMATE_STARTUP) {
                specialTimer++;
                if (specialTimer >= SPECIAL_CHARGE) setState(FighterState.ULTIMATE_ACTIVE);
            }
            if (state == FighterState.ULTIMATE_ACTIVE) {
                specialTimer++;
                Rectangle hitbox = getUltimateHitbox();
                if (hitbox != null && opponent != null
                        && hitbox.intersects(opponent.getBounds())) {
                    int finalDamage = specialDamage;
                    if (isCountering) { finalDamage = (int)(finalDamage * 1.5); isCountering = false; }
                    opponent.takeDamage(finalDamage, (int)(40 * scale), true);
                    onUltimateHit(opponent);
                }
                if (specialTimer >= SPECIAL_CHARGE + SPECIAL_DURATION) {
                    setState(FighterState.IDLE);
                    specialTimer = 0;
                }
            }

            // --- KI BLAST projectiles ---
            for (int i = 0; i < activeBlasts.size(); i++) {
                KiBlastProjectile blast = activeBlasts.get(i);
                blast.update(activeEffects);
                Rectangle blastHitbox = new Rectangle(blast.px, blast.py - (int)(10 * scale),
                        (int)(40 * scale), (int)(20 * scale));
                if (opponent != null && blastHitbox.intersects(opponent.getBounds())) {
                    opponent.takeDamage(kiBlastDamage, (int)(15 * scale), true);
                    int impX = blast.pFacingRight ? opponent.getX() + 10 : opponent.getX() + opponent.baseWidth - 10;
                    int impY = opponent.y + (opponent.baseHeight / 2);
                    opponent.activeEffects.add(new VisualEffect(blast.img, impX, impY,
                            new int[]{260, 0, 86}, new int[]{135, 448, 448},
                            new int[]{126, 70, 70}, new int[]{109, 64, 64},
                            new int[]{0, 0, 0}, new int[]{-40, -40, -40}, 6, 0.5 * scale));
                    activeBlasts.remove(i); i--; continue;
                }
                if (blast.isDead) { activeBlasts.remove(i); i--; }
            }

            // --- VOLO ---
            if (!isAttacking() && state != FighterState.TELEPORTING) {
                if (flyCooldown > 0) flyCooldown--;
                if (inFly && flyCooldown == 0) {
                    if (!isFlying()) {
                        setState(FighterState.FLYING_IDLE);
                        flyCooldown = 20;
                        velocityY   = 0;
                        if (y >= groundY) y -= (int)(40 * scale);
                    } else {
                        setState(FighterState.JUMPING);
                        flyCooldown = 20;
                    }
                }
            }

            if (isFlying()) {
                if (inUp)   y -= speed;
                if (inDown) { y += speed; if (y > groundY - (int)(40 * scale)) y = groundY - (int)(40 * scale); }
                if (!isAttacking()) {
                    if (inLeft) {
                        x -= speed;
                        if (opponent != null && getBounds().intersects(opponent.getBounds())) x += speed;
                        flyMoveTimer++;
                        setState(facingRight
                                ? (flyMoveTimer > 8 ? FighterState.FLYING_BACKWARD_FULL : FighterState.FLYING_BACKWARD)
                                : (flyMoveTimer > 8 ? FighterState.FLYING_FORWARD_FULL  : FighterState.FLYING_FORWARD));
                    } else if (inRight) {
                        x += speed;
                        if (opponent != null && getBounds().intersects(opponent.getBounds())) x -= speed;
                        flyMoveTimer++;
                        setState(facingRight
                                ? (flyMoveTimer > 8 ? FighterState.FLYING_FORWARD_FULL  : FighterState.FLYING_FORWARD)
                                : (flyMoveTimer > 8 ? FighterState.FLYING_BACKWARD_FULL : FighterState.FLYING_BACKWARD));
                    } else {
                        flyMoveTimer = 0;
                        setState(FighterState.FLYING_IDLE);
                    }
                }
            } else if (!isAttacking() && state != FighterState.TELEPORTING) {
                // Movimento a terra
                if (state != FighterState.CROUCHING) {
                    if (inLeft)  { x -= speed; if (opponent != null && getBounds().intersects(opponent.getBounds())) x += speed; setState(FighterState.WALKING); }
                    else if (inRight) { x += speed; if (opponent != null && getBounds().intersects(opponent.getBounds())) x -= speed; setState(FighterState.WALKING); }
                    else if (state == FighterState.WALKING) setState(FighterState.IDLE);
                }

                // Salto
                if (inUp && state != FighterState.JUMPING && state != FighterState.CROUCHING) {
                    setState(FighterState.CROUCHING); crouchTimer = 0;
                }
                if (state == FighterState.CROUCHING) {
                    crouchTimer++;
                    if (crouchTimer >= CROUCH_DURATION) {
                        setState(FighterState.JUMPING);
                        velocityY = jumpStrength;
                    }
                }
                if (state == FighterState.JUMPING) {
                    velocityY += gravity;
                    y += (int)velocityY;
                }
            }

            // Limiti schermo e pavimento
            if (y >= groundY) { y = groundY; velocityY = 0; if (isFlying() || state == FighterState.JUMPING) setState(FighterState.IDLE); }
            if (y < 0)  { y = 0; velocityY = 0; }
            if (x < 0)  x = 0;
            if (x > GamePanel.SCREEN_WIDTH - baseWidth) x = GamePanel.SCREEN_WIDTH - baseWidth;
        }

        prevUp    = inUp;
        prevLeft  = inLeft;
        prevRight = inRight;
        prevDown  = inDown;
    }

    // =============================================
    // FIND MATCHING ROUTE — cerca la combo
    // che matcha l'input buffer corrente
    // =============================================
    protected ComboRoute findMatchingRoute() {
        if (comboRoutes == null) return null;
        boolean inAir = isFlying() || state == FighterState.JUMPING;
        boolean aura  = state == FighterState.AURA_ACTIVE;

        // Priorità: route più lunghe prima (più specifiche)
        ComboRoute best = null;
        for (ComboRoute route : comboRoutes) {
            if (!route.isExecutable(aura, inAir)) continue;
            if (inputBuffer.matches(route.inputSequence)) {
                if (best == null || route.length() > best.length())
                    best = route;
            }
        }
        return best;
    }

    // =============================================
    // RENDERING
    // =============================================
    protected void drawFighterSprite(Graphics2D g2d) {
        drawW  = (int)(srcW * scale);
        drawH  = (int)(srcH * scale);
        drawY  = y - (drawH - baseHeight);
        shiftX = (baseWidth - drawW) / 2;
        if (isAttacking() && !facingRight) shiftX = -(drawW - baseWidth);
        shiftX += customOffsetX;

        drawShadow(g2d);
        drawAura(g2d);

        float currentAlpha = 1.0f;
        if (state == FighterState.TELEPORTING) {
            currentAlpha = Math.max(0.0f, Math.min(1.0f, (float)teleportFrame / 6.0f));
        } else if (invincibleTimer > 0 && invincibleTimer % 10 < 5) {
            currentAlpha = 0.4f;
        } else if (state == FighterState.GUARD_CRUSHED && guardCrushTimer % 8 < 4) {
            currentAlpha = 0.65f;
        }

        if (state == FighterState.GUARD_CRUSHED) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
            g2d.setColor(Color.RED);
            g2d.fillRect(x + shiftX, drawY, drawW, drawH);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
        int sX1 = x + shiftX, sX2 = sX1 + drawW;
        if (facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
        g2d.drawImage(spriteSheet, sX1, drawY, sX2, drawY + drawH,
                srcX, srcY, srcX + srcW, srcY + srcH, null);
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

    protected void drawAura(Graphics2D g2d) {
        if ((state == FighterState.CHARGING_KI || state == FighterState.AURA_ACTIVE) && auraImage != null) {
            int[] aX = {3, 357, 4, 368}, aY = {4, 4, 454, 440};
            int[] aW = {350, 345, 359, 356}, aH = {446, 432, 437, 417};
            double auraDrawScale = 0.45 * scale;
            int dAW = (int)(aW[auraFrame] * auraDrawScale), dAH = (int)(aH[auraFrame] * auraDrawScale);
            int dAX = x + (baseWidth - dAW) / 2;
            int dAY = y - (dAH - baseHeight) + (int)(20 * scale);
            g2d.drawImage(auraImage, dAX, dAY, dAX + dAW, dAY + dAH,
                    aX[auraFrame], aY[auraFrame],
                    aX[auraFrame] + aW[auraFrame], aY[auraFrame] + aH[auraFrame], null);
        }
    }

    protected void drawShadow(Graphics2D g2d) {
        int dist       = Math.max(0, groundY - y);
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

    // HUD — invariato dalla versione precedente
    // (drawUniversalHUD, drawBar, drawCounterBadge, drawComboBadge)
    // Copia il blocco completo dal Fighter.java precedente —
    // non va modificato, funziona già con le nuove variabili.


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
                : (state == FighterState.AURA_ACTIVE ? new Color(160, 220, 255) : new Color(70, 150, 255));
        String kiLabel = kiBreakTimer > 0 ? "KI BREAK!" : "KI";
        Color kiLabelColor = kiBreakTimer > 0 ? new Color(255, 130, 0) : new Color(70, 150, 255);
        drawBar(g2d, barsAreaStart, barsAreaEnd, barsStartY + barGap * 3,
                kiBarW, barH, radius, kiPercent,
                new Color(15, 15, 40), kiColor, kiLabel, kiLabelColor, resM, labelMargin);

        // --- BARRA GUARD ---
        double guardPercent = (double) guardHealth / MAX_GUARD_HEALTH;
        Color guardColor = (state == FighterState.GUARD_CRUSHED) ? new Color(200, 30, 30)
                : guardPercent < 0.30         ? new Color(220, 100, 30)
                :                               new Color(175, 175, 185);
        String guardLabel = (state == FighterState.GUARD_CRUSHED) ? "BROKEN" : "GUARD";
        Color guardLabelColor = (state == FighterState.GUARD_CRUSHED)
                ? new Color(220, 50, 50)
                : new Color(150, 150, 165);
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


}