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
    protected static final int FRAME_SPEED = 4; // ticks per sprite frame (a 60fps)
    protected double comboDamageScale = 1.0;
    protected boolean hasHit = false;     // ha già colpito in questo step

    // Chain progressiva: tiene traccia degli input accumulati nella combo
    protected int[] comboInputHistory = new int[8];
    protected int comboHistoryLength = 0;
    protected boolean wasFlying = false;   // era in volo prima della combo
    protected boolean prevInLight = false;  // edge detection per combo chain
    protected boolean prevInHeavy = false;

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
    protected double auraDamageMultiplier = 1.3;   // +30% danno melee con aura
    protected double auraKiRecharge      = 150.0;  // Ki ricaricato all'attivazione
    protected int    auraHPRecover       = 25;     // HP recuperati all'attivazione
    protected boolean auraBoostActive    = false;  // true finché l'aura è attiva (persiste tra stati)

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
    protected boolean hitWhileFlying = false; // resta in aria durante hitstun
    protected int invincibleTimer = 0;
    protected int hitCooldown     = 0;        // frame minimi fra un hit e il successivo
    protected final int HIT_COOLDOWN_FRAMES = 4;
    protected int lightHitFlash   = 0;        // frame visivi di reazione al light (no stato)
    protected final int LIGHT_HIT_FLASH_DURATION = 6;

    // =============================================
    // LAUNCHED STATE
    // =============================================
    protected boolean launchedUp    = true;  // true=launcher(su), false=spike(giù)
    protected int launchPhase       = 0;     // 0=airborne, 1=ground recovery
    protected int launchFrame       = 0;     // frame corrente nella fase
    protected int launchAnimTimer   = 0;     // tick per avanzare frame

    // =============================================
    // KO STATE
    // =============================================
    protected boolean koFromAir     = false; // true se il KO è avvenuto in aria
    protected int koPhase           = 0;     // fasi dell'animazione KO
    protected int koFrame           = 0;     // frame corrente
    protected int koAnimTimer       = 0;     // tick per avanzare frame

    // =============================================
    // CINEMATIC ULTIMATE
    // =============================================
    protected int cinematicPhase    = 0;     // 0=darkFadeIn 1=transform 2=beam 3=flash 4=fadeOut
    protected int cinematicTimer    = 0;     // tick nel passo corrente
    protected int cinematicFrame    = 0;     // frame animazione corrente
    protected float cinematicAlpha  = 0f;    // alpha overlay (0-1)
    protected boolean cinematicActive = false;
    protected int cinematicSavedX   = 0;
    protected int cinematicSavedY   = 0;
    protected int cinematicOpponentSavedX = 0;
    protected int cinematicOpponentSavedY = 0;
    protected int cinematicHitsDealt = 0;
    protected int cinematicStep     = 0;     // step nella sequenza di trasformazione
    protected int cinematicVibrateX = 0;     // offset vibrazione orizzontale
    protected boolean cinematicShowAura     = false;
    protected boolean cinematicShowLightning = false;
    protected boolean cinematicShowExplosion = false;
    protected boolean cinematicPostBeam   = false;  // true = Goku in SSJ3 sul campo dopo il beam
    protected int cinematicPostFrame      = 0;      // frame SSJ3 durante post-beam
    public static final int CINEMATIC_TOTAL_DAMAGE = 50;
    public static final int CINEMATIC_NUM_HITS = 5;

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
    protected boolean flyingBeforeAction = false; // preserva volo durante teleport/charge/block

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
                FighterState.ULTIMATE_ACTIVE,
                FighterState.CINEMATIC_ULTIMATE);
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
        flyingBeforeAction = isFlying();
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
    // TAKE DAMAGE — versione completa con tutti i flag
    // =============================================
    public void takeDamage(int amount, int appliedKnockback,
                           boolean isEnergy, boolean isUnblockable,
                           boolean isGuardBreaker, boolean isLauncher,
                           boolean isSpikeDown) {
        // Immunità
        if (state == FighterState.KO)           return;
        if (state == FighterState.GUARD_CRUSHED) return;
        if (invincibleTimer > 0)                return;
        if (hitCooldown > 0)                    return;

        // Teleport = i-frames
        if (state == FighterState.TELEPORTING)  return;

        if (isBlocking() && !isUnblockable) {

            // --- GUARD BREAKER: crush istantaneo, 0 danno HP ---
            if (isGuardBreaker) {
                guardHealth      = 0;
                blockActiveTimer = 0;
                isCountering     = false;
                counterWindow    = 0;
                setState(FighterState.GUARD_CRUSHED);
                guardCrushTimer  = 0;
                knockbackSpeed   = Math.max(1, appliedKnockback / 4);
                return;
            }

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

            // --- GUARD CRUSH (da accumulo) ---
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

            // KO check immediato
            if (hp <= 0) {
                hitTimer        = 0;
                hitCooldown     = HIT_COOLDOWN_FRAMES;
                knockbackSpeed  = appliedKnockback;
                activeRoute     = null; comboStep = 0;
                attackTimer     = 0;    specialTimer = 0;
                comboHistoryLength = 0; wasFlying = false;
                koFromAir       = (y < groundY) || isFlying() || wasFlying;
                koPhase         = 0; koFrame = 0; koAnimTimer = 0;
                if (koFromAir) velocityY = 2.0; // cade lentamente
                setState(FighterState.KO);
                return;
            }

            // =============================================
            // COLPO "PESANTE" — launcher, spike, guard breaker
            // Causa HIT_STUN pieno, sbalzo, nessuna azione possibile
            // =============================================
            boolean isHeavyHit = isLauncher || isSpikeDown || isGuardBreaker;

            if (isHeavyHit) {
                hitCooldown      = HIT_COOLDOWN_FRAMES;
                hitTimer         = 0;
                wallBounced      = false;
                hitstunDuration  = Math.max(12, Math.min(55, amount * 2));
                knockbackSpeed   = appliedKnockback;

                hitWhileFlying   = isFlying() || wasFlying;

                isCountering     = false;
                counterWindow    = 0;
                blockActiveTimer = 0;
                zCancelAvailable = false;

                // Interrompi qualsiasi azione
                activeRoute        = null;
                comboStep          = 0;
                attackTimer        = 0;
                specialTimer       = 0;
                comboHistoryLength = 0;
                wasFlying          = false;

                // Launcher — manda in aria con forza
                if (isLauncher) {
                    velocityY = -12.0;
                    hitWhileFlying = false;
                    launchedUp = true;
                    launchPhase = 0; launchFrame = 0; launchAnimTimer = 0;
                    setState(FighterState.LAUNCHED);
                }
                // Spike — schiaccia verso il basso
                else if (isSpikeDown) {
                    velocityY = 10.0;
                    hitWhileFlying = false;
                    launchedUp = false;
                    launchPhase = 0; launchFrame = 0; launchAnimTimer = 0;
                    setState(FighterState.LAUNCHED);
                }
                // Guard breaker fuori block — stun breve
                else if (isGuardBreaker) {
                    hitstunDuration = 25;
                    setState(FighterState.HIT_STUN);
                }

                // Tumbling sotto 20% HP (solo per HIT_STUN, non LAUNCHED)
                if (state == FighterState.HIT_STUN && hp > 0 && hp <= (int)(maxHP * 0.20))
                    setState(FighterState.TUMBLING);
            }
            // =============================================
            // COLPO "LEGGERO" — light attacks
            // Solo danno, NESSUN pushback, NESSUN stato, NESSUN cooldown
            // L'avversario resta fermo e puo' essere colpito dal prossimo light
            // =============================================
            else {
                // Nessun hitCooldown, nessun knockback, nessun cambio di stato
                lightHitFlash = LIGHT_HIT_FLASH_DURATION;
            }
        }
    }

    // Overload retrocompatibile a 4 parametri
    public void takeDamage(int amount, int appliedKnockback,
                           boolean isEnergy, boolean isUnblockable) {
        takeDamage(amount, appliedKnockback, isEnergy, isUnblockable,
                false, false, false);
    }

    // Overload retrocompatibile a 3 parametri
    public void takeDamage(int amount, int appliedKnockback, boolean isEnergy) {
        takeDamage(amount, appliedKnockback, isEnergy, false,
                false, false, false);
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

        // --- CINEMATIC ULTIMATE ---
        if (cinematicActive) {
            updateCinematicUltimate(opponent);
            return; // Blocca TUTTO il resto durante la cinematica
        }
        // Se L'AVVERSARIO ha la cinematica attiva, questo fighter è congelato
        if (state == FighterState.CINEMATIC_ULTIMATE) {
            return;
        }

        // --- KO ---
        if (state == FighterState.KO) {
            activeRoute = null; comboStep = 0;
            comboHistoryLength = 0; wasFlying = false;
            inputBuffer.clear();
            koAnimTimer++;

            if (koFromAir) {
                // KO dall'aria: fase 0 = caduta, fase 1 = a terra
                if (koPhase == 0) {
                    velocityY += gravity;
                    y += (int)velocityY;
                    if (koAnimTimer % FRAME_SPEED == 0) koFrame++;
                    if (koFrame > 1) koFrame = 1; // max 2 frame caduta
                    if (y >= groundY) {
                        y = groundY; velocityY = 0;
                        koPhase = 1; koFrame = 0; koAnimTimer = 0;
                    }
                } else {
                    // A terra: 3 frame poi stop
                    if (koAnimTimer % FRAME_SPEED == 0) koFrame++;
                    if (koFrame > 2) koFrame = 2; // ferma all'ultimo
                }
            } else {
                // KO a terra: fase 0 = colpo (2f), fase 1 = transizione (1f), fase 2 = a terra (3f)
                if (koPhase == 0) {
                    if (koAnimTimer % FRAME_SPEED == 0) koFrame++;
                    if (koFrame >= 2) { koPhase = 1; koFrame = 0; koAnimTimer = 0; }
                } else if (koPhase == 1) {
                    if (koAnimTimer % FRAME_SPEED == 0) koFrame++;
                    if (koFrame >= 1) { koPhase = 2; koFrame = 0; koAnimTimer = 0; }
                } else {
                    if (koAnimTimer % FRAME_SPEED == 0) koFrame++;
                    if (koFrame > 2) koFrame = 2; // ferma all'ultimo
                }
            }
            return;
        }

        // --- VINCITORE ---
        if (opponent != null && opponent.state == FighterState.KO) {
            setState(FighterState.WINNER);
            activeRoute = null; comboStep = 0;
            comboHistoryLength = 0; wasFlying = false;
            inputBuffer.clear();
            if (!isFlying() && y < groundY) { velocityY += gravity; y += (int)velocityY; if (y >= groundY) { y = groundY; velocityY = 0; } }
            endTimer++;
            if (endTimer % 15 == 0) { // avanza frame ogni 15 tick (~0.25s)
                if (endFrame < 7) endFrame++;
            }
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

        // --- HIT COOLDOWN — protegge da multi-hit per frame (beam) ---
        if (hitCooldown > 0) hitCooldown--;
        if (lightHitFlash > 0) lightHitFlash--;

        // --- HIT STUN ---
        if (state == FighterState.HIT_STUN || state == FighterState.TUMBLING) {
            hitTimer++;
            // Fisica durante hitstun — solo se NON era in volo stazionario
            if (!hitWhileFlying) {
                velocityY += gravity;
                y += (int)velocityY;
                if (y >= groundY) { y = groundY; velocityY = 0; }
            }
            if (hitTimer > hitstunDuration) {
                wallBounced      = false;
                invincibleTimer  = 40;
                if (hitWhileFlying) {
                    setState(FighterState.FLYING_IDLE); // torna in volo
                    hitWhileFlying = false;
                } else {
                    setState(FighterState.IDLE);
                }
            }
            return;
        }

        // --- LAUNCHED — lanciato in aria da launcher/spike ---
        // Fase 0: airborne (gravità), Fase 1: recovery a terra (7 frame)
        if (state == FighterState.LAUNCHED) {
            launchAnimTimer++;

            if (launchPhase == 0) {
                // Airborne — gravità attiva
                velocityY += gravity;
                y += (int)velocityY;
                // Avanza frame ogni FRAME_SPEED tick
                if (launchAnimTimer % FRAME_SPEED == 0) launchFrame++;
                // Atterra
                if (y >= groundY) {
                    y = groundY;
                    velocityY = 0;
                    launchPhase = 1;
                    launchFrame = 0;
                    launchAnimTimer = 0;
                }
            } else {
                // Ground recovery — 7 frame animati
                if (launchAnimTimer % FRAME_SPEED == 0) launchFrame++;
                if (launchFrame >= 7) {
                    invincibleTimer = 40;
                    setState(FighterState.IDLE);
                }
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
            prevUp    = inUp;
            prevLeft  = inLeft;
            prevRight = inRight;
            prevDown  = inDown;
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

        // --- BLOCCO (non durante hitstun/tumbling) ---
        // Blocco in aria permesso SOLO se si sta volando, non durante salto/lancio
        boolean wasBlocking = isBlocking();
        boolean canBlock = inBlock && !isAttacking()
                && state != FighterState.TELEPORTING
                && state != FighterState.CHARGING_KI
                && state != FighterState.HIT_STUN
                && state != FighterState.TUMBLING
                && state != FighterState.LAUNCHED;

        if (canBlock) {
            if (isFlying()) {
                flyingBeforeAction = true;
                setState(FighterState.BLOCKING_AIR);
            } else if (y >= groundY) {
                flyingBeforeAction = false;
                setState(FighterState.BLOCKING); // solo a terra
            }
            // Se in aria da salto/lancio: NON può bloccare
        } else if (isBlocking()) {
            // Uscita dal blocco: ripristina stato corretto
            if (flyingBeforeAction) {
                setState(FighterState.FLYING_IDLE);
                flyingBeforeAction = false;
            } else if (y < groundY) {
                setState(FighterState.JUMPING); // continua a cadere
            } else {
                setState(FighterState.IDLE);
            }
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
            double regenMult = auraBoostActive ? 2.0 : 1.0;
            ki = Math.min(MAX_KI, ki + kiRegen * regenMult);
        }

        // --- COMBO WINDOW timer ---
        if (comboWindowTimer > 0) {
            comboWindowTimer--;
        } else if ((comboCount > 0 || comboHistoryLength > 0) && !isAttacking()) {
            comboCount        = 0;
            comboDamageScale  = 1.0;
            activeRoute       = null;
            comboStep         = 0;
            comboHistoryLength = 0;
            wasFlying         = false;
        }

        // --- AURA ACTIVATION (tasto C, istantaneo quando barra piena) ---
        if (inCharge && auraEnergy >= MAX_AURA_ENERGY
                && !auraBoostActive
                && state != FighterState.CHARGING_KI
                && state != FighterState.TELEPORTING
                && !isAttacking() && !isBlocking()) {
            flyingBeforeAction = isFlying();
            // Attivazione ISTANTANEA: boost + bonus subito
            auraBoostActive = true;
            hp = Math.min(maxHP, hp + auraHPRecover);
            ki = Math.min(MAX_KI, ki + auraKiRecharge);
            // Animazione di carica (solo visuale)
            setState(FighterState.CHARGING_KI);
            auraChargeTimer = 0;
            velocityY       = 0;
        }
        // Animazione CHARGING_KI: pura estetica, il boost è già attivo
        if (state == FighterState.CHARGING_KI) {
            auraChargeTimer++;
            if (auraChargeTimer >= AURA_CHARGE_DURATION) {
                if (flyingBeforeAction) {
                    setState(FighterState.FLYING_IDLE);
                    flyingBeforeAction = false;
                } else {
                    setState(FighterState.IDLE);
                }
            }
        }
        // Aura boost: drain e buff attivi finché auraBoostActive è true
        if (auraBoostActive) {
            speed        = (int)(8 * scale);
            jumpStrength = -18 * scale;
            auraEnergy  -= AURA_DRAIN_RATE;
            if (auraEnergy <= 0) {
                auraEnergy       = 0;
                auraBoostActive  = false;
            }
        } else if (state != FighterState.CHARGING_KI) {
            speed        = (int)(4 * scale);
            jumpStrength = -12 * scale;
            if (auraEnergy < MAX_AURA_ENERGY)
                auraEnergy++;
        }

        // Animazione aura (visibile durante qualsiasi azione finché il boost è attivo)
        if (state == FighterState.CHARGING_KI || auraBoostActive) {
            auraAnimTimer++;
            if (auraAnimTimer > 4) { auraFrame++; if (auraFrame > 3) auraFrame = 0; auraAnimTimer = 0; }
            if (auraImage != null && Math.random() < 0.10) {
                // Particelle intorno al corpo
                int rX = x + (int)(Math.random() * baseWidth) - (int)(baseWidth * 0.1);
                int rY = y + (int)(baseHeight * 0.2) + (int)(Math.random() * baseHeight * 0.6);
                // Alterna tra fulmine grande e piccolo
                if (Math.random() < 0.5) {
                    // Fulmine 1 (più grande)
                    activeEffects.add(new VisualEffect(auraImage, rX, rY,
                            new int[]{129}, new int[]{895},
                            new int[]{66}, new int[]{107}, 4, scale * 0.5));
                } else {
                    // Fulmine 2 (più piccolo)
                    activeEffects.add(new VisualEffect(auraImage, rX, rY,
                            new int[]{199}, new int[]{894},
                            new int[]{72}, new int[]{79}, 4, scale * 0.5));
                }
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
                && state != FighterState.TUMBLING
                && state != FighterState.LAUNCHED
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
                            if (flyingBeforeAction && y > groundY - (int)(40 * scale))
                                y = groundY - (int)(40 * scale);
                            teleportPhase = 2; teleportFrame = 1;
                        }
                    } else {
                        teleportFrame++;
                        if (teleportFrame > 6) {
                            if (flyingBeforeAction) {
                                setState(FighterState.FLYING_IDLE);
                            } else if (y < groundY) {
                                setState(FighterState.JUMPING);
                            } else {
                                setState(FighterState.IDLE);
                            }
                            flyingBeforeAction = false;
                            teleportPhase = 1; teleportFrame = 6;
                        }
                    }
                }
            }

            // --- Z-CANCEL: interrompe la combo corrente spendendo Ki ---
            if (isAttacking() && zCancelAvailable
                    && inBlock && ki >= Z_CANCEL_COST) {
                ki -= Z_CANCEL_COST;
                activeRoute      = null;
                comboStep        = 0;
                attackTimer      = 0;
                zCancelAvailable = false;
                comboHistoryLength = 0;
                setState(wasFlying ? FighterState.FLYING_IDLE
                        : (y < groundY ? FighterState.JUMPING : FighterState.IDLE));
                wasFlying = false;
            }

            // --- NEO COMBO (Chain Progressiva) ---
            if (!isAttacking() && state != FighterState.TELEPORTING) {

                // Edge detection: solo nuove pressioni, non tasto tenuto
                boolean newLightPress = inLight && !prevInLight;
                boolean newHeavyPress = inHeavy && !prevInHeavy;

                if (newLightPress || newHeavyPress) {
                    int newInput = newHeavyPress ? ComboRoute.HEAVY : ComboRoute.LIGHT;

                    // Estendi la chain se siamo nella finestra, altrimenti ricomincia
                    if (comboWindowTimer > 0 && comboHistoryLength > 0) {
                        if (comboHistoryLength < comboInputHistory.length) {
                            comboInputHistory[comboHistoryLength] = newInput;
                            comboHistoryLength++;
                        }
                    } else {
                        comboInputHistory[0] = newInput;
                        comboHistoryLength = 1;
                        wasFlying = false;
                    }

                    // Cerca la route che matcha esattamente la chain accumulata
                    ComboRoute matched = findMatchingChainRoute(opponent);

                    // Se non matcha, prova con solo il nuovo input (nuova chain)
                    if (matched == null && comboHistoryLength > 1) {
                        comboInputHistory[0] = newInput;
                        comboHistoryLength = 1;
                        wasFlying = false;
                        matched = findMatchingChainRoute(opponent);
                    }

                    if (matched != null) {
                        // Salva lo stato volo SOLO al primo attacco della chain
                        if (!isAttacking() && comboHistoryLength == 1) {
                            wasFlying = isFlying();
                        }
                        activeRoute  = matched;
                        comboStep    = matched.length() - 1; // esegui l'ultimo step
                        attackTimer  = 0;
                        hasHit       = false;
                        setState(newInput == ComboRoute.HEAVY
                                ? FighterState.COMBO_HEAVY
                                : FighterState.COMBO_LIGHT);
                    }
                }

                // SPECIAL (Ki Blast)
                if (inSpecial && ki >= kiBlastKiCost
                        && kiBreakTimer == 0
                        && state != FighterState.SPECIAL_STARTUP) {
                    ki -= kiBlastKiCost;
                    if (ki <= 0) { ki = 0; kiBreakTimer = KI_BREAK_DURATION; }
                    flyingBeforeAction = isFlying();
                    setState(FighterState.SPECIAL_STARTUP);
                    specialTimer = 0;
                    spawnKiBlastVFX();
                }

                // CINEMATIC ULTIMATE (condizioni speciali: tutte e 3 le barre piene + avversario lanciato)
                if (inUltimate && ki >= MAX_KI
                        && specialEnergy >= MAX_SPECIAL_ENERGY
                        && auraEnergy >= MAX_AURA_ENERGY
                        && opponent != null
                        && opponent.state == FighterState.LAUNCHED
                        && opponent.launchedUp
                        && !cinematicActive) {
                    startCinematicUltimate(opponent);
                }
                // ULTIMATE (Kamehameha normale)
                else if (inUltimate && specialEnergy >= MAX_SPECIAL_ENERGY
                        && ki >= 50 && kiBreakTimer == 0
                        && !cinematicActive) {
                    ki -= 50;
                    flyingBeforeAction = isFlying();
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

                        // Aura boost: +30% danno melee
                        if (auraBoostActive) finalDamage = (int)(finalDamage * auraDamageMultiplier);

                        // Counter bonus
                        if (isCountering) {
                            finalDamage   = (int)(finalDamage * 1.5);
                            isCountering  = false;
                            counterWindow = 0;
                        }

                        opponent.takeDamage(finalDamage, atk.knockback,
                                atk.isEnergy, atk.isUnblockable,
                                atk.isGuardBreaker, atk.isLauncher,
                                atk.causesCrumple);
                        ki = Math.min(MAX_KI, ki + kiOnHitReward);
                        hasHit           = true;
                        zCancelAvailable = true;
                    }
                }

                // Fine di questo step della combo
                if (attackTimer >= atk.totalDuration()) {
                    attackTimer      = 0;
                    hasHit           = false;
                    activeRoute      = null;
                    comboStep        = 0;
                    zCancelAvailable = false;
                    comboWindowTimer = COMBO_WINDOW; // finestra per estendere la chain

                    // Ripristina lo stato corretto
                    if (wasFlying) {
                        setState(FighterState.FLYING_IDLE);
                    } else if (y < groundY) {
                        setState(FighterState.JUMPING); // era in salto, gravità riprende
                    } else {
                        setState(FighterState.IDLE);
                    }
                }
            }

            // --- SPECIAL update ---
            if (state == FighterState.SPECIAL_STARTUP) {
                specialTimer++;
                if (specialTimer == 17) fireKiBlastProjectile(); // 5° frame (4*4+1)
                if (specialTimer >= 24) {
                    if (flyingBeforeAction) {
                        setState(FighterState.FLYING_IDLE);
                        flyingBeforeAction = false;
                    } else {
                        setState(FighterState.IDLE);
                    }
                }
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
                    // Multi-hit: 4 colpi da 12 HP distribuiti nella durata del beam
                    int beamTick = specialTimer - SPECIAL_CHARGE;
                    int hitInterval = SPECIAL_DURATION / 4; // ~22 frame tra un hit e l'altro
                    if (beamTick % hitInterval == 0 && beamTick <= SPECIAL_DURATION) {
                        int finalDamage = 12;
                        if (isCountering) { finalDamage = (int)(finalDamage * 1.5); isCountering = false; }
                        if (auraBoostActive) finalDamage = (int)(finalDamage * auraDamageMultiplier);
                        opponent.takeDamage(finalDamage, (int)(8 * scale), true);
                        onUltimateHit(opponent);
                    }
                }
                if (specialTimer >= SPECIAL_CHARGE + SPECIAL_DURATION) {
                    if (flyingBeforeAction) {
                        setState(FighterState.FLYING_IDLE);
                        flyingBeforeAction = false;
                    } else {
                        setState(FighterState.IDLE);
                    }
                    specialTimer = 0;
                }
            }

            // --- KI BLAST projectiles ---
            for (int i = 0; i < activeBlasts.size(); i++) {
                KiBlastProjectile blast = activeBlasts.get(i);
                blast.update(activeEffects);
                Rectangle blastHitbox = new Rectangle(blast.px, blast.py - (int)(30 * scale),
                        (int)(80 * scale), (int)(60 * scale));
                if (opponent != null && blastHitbox.intersects(opponent.getBounds())) {
                    opponent.takeDamage(kiBlastDamage, (int)(15 * scale), true);
                    // Impatto al punto reale del blast, non al centro dell'avversario
                    int impX = blast.pFacingRight
                            ? opponent.getX()                          // colpisce dal lato sinistro
                            : opponent.getX() + opponent.baseWidth;    // colpisce dal lato destro
                    int impY = blast.py;
                    opponent.activeEffects.add(new VisualEffect(blast.img, impX, impY,
                            new int[]{260, 0, 86}, new int[]{135, 448, 448},
                            new int[]{126, 70, 70}, new int[]{109, 64, 64},
                            new int[]{0, 0, 0}, new int[]{0, 0, 0}, 5, 0.8 * scale,
                            blast.pFacingRight)); // flip in base alla direzione del blast
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
                // Movimento a terra / orizzontale in salto
                if (state == FighterState.JUMPING) {
                    // In salto: consenti movimento orizzontale senza cambiare stato
                    if (inLeft)  { x -= speed; if (opponent != null && getBounds().intersects(opponent.getBounds())) x += speed; }
                    if (inRight) { x += speed; if (opponent != null && getBounds().intersects(opponent.getBounds())) x -= speed; }
                } else if (state != FighterState.CROUCHING) {
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

        prevUp      = inUp;
        prevLeft    = inLeft;
        prevRight   = inRight;
        prevDown    = inDown;
        prevInLight = inLight;
        prevInHeavy = inHeavy;
    }

    // =============================================
    // FIND MATCHING CHAIN ROUTE — cerca la combo
    // che matcha la chain di input accumulata
    // =============================================
    protected ComboRoute findMatchingChainRoute(Fighter opponent) {
        if (comboRoutes == null || comboHistoryLength == 0) return null;

        // Se siamo già in una combo chain, usa wasFlying per determinare aria/terra
        boolean inAir = isAttacking() ? wasFlying
                : (isFlying() || state == FighterState.JUMPING);
        boolean aura  = auraBoostActive;

        ComboRoute best = null;
        for (ComboRoute route : comboRoutes) {
            if (!route.isExecutable(aura, inAir)) continue;

            // Match esatto: la route deve avere la stessa lunghezza della chain
            if (route.length() != comboHistoryLength) continue;

            boolean matches = true;
            for (int i = 0; i < comboHistoryLength; i++) {
                if (route.inputSequence[i] != comboInputHistory[i]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
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

    // =============================================
    // CINEMATIC ULTIMATE — metodi
    // =============================================
    protected void startCinematicUltimate(Fighter opponent) {
        cinematicActive = true;
        cinematicPhase  = 0;
        cinematicTimer  = 0;
        cinematicFrame  = 0;
        cinematicAlpha  = 0f;
        cinematicHitsDealt = 0;
        cinematicStep   = 0;
        cinematicVibrateX = 0;
        cinematicShowAura = false;
        cinematicShowLightning = false;
        cinematicShowExplosion = false;
        setState(FighterState.CINEMATIC_ULTIMATE);
        // Salva posizioni
        cinematicSavedX = x;
        cinematicSavedY = y;
        cinematicOpponentSavedX = opponent.x;
        cinematicOpponentSavedY = opponent.y;
        // Consuma tutte le barre
        ki = 0;
        specialEnergy = 0;
        auraEnergy = 0;
        auraBoostActive = false;
        // Congela l'avversario
        opponent.setState(FighterState.CINEMATIC_ULTIMATE);
        opponent.velocityY = 0;
    }

    protected void updateCinematicUltimate(Fighter opponent) {
        cinematicTimer++;

        switch (cinematicPhase) {
            case 0: // DARK FADE IN (50 tick)
                cinematicAlpha = Math.min(1f, cinematicTimer / 50f);
                if (cinematicTimer >= 50) {
                    cinematicPhase = 1;
                    cinematicTimer = 0;
                    cinematicStep  = 0;
                    cinematicFrame = 0;
                }
                break;

            case 1: // SSJ3 TRANSFORMATION — sequenza a step
                switch (cinematicStep) {
                    case 0: // Frame 0 — posa iniziale
                        cinematicFrame = 0;
                        cinematicShowExplosion = false;
                        cinematicShowLightning = false;
                        cinematicShowAura = false;
                        cinematicVibrateX = 0;
                        if (cinematicTimer >= 20) { cinematicStep = 1; cinematicTimer = 0; }
                        break;
                    case 1: // Esplosione
                        cinematicShowExplosion = true;
                        cinematicFrame = 0;
                        if (cinematicTimer >= 25) { cinematicStep = 2; cinematicTimer = 0; cinematicShowExplosion = false; }
                        break;
                    case 2: // Frame 1 — carica aura, vibra, fulmini
                        cinematicFrame = 1;
                        cinematicShowLightning = true;
                        cinematicVibrateX = (cinematicTimer % 4 < 2) ? -4 : 4;
                        if (cinematicTimer >= 50) { cinematicStep = 3; cinematicTimer = 0; cinematicVibrateX = 0; }
                        break;
                    case 3: // Frame 2
                        cinematicFrame = 2;
                        if (cinematicTimer >= 20) { cinematicStep = 4; cinematicTimer = 0; }
                        break;
                    case 4: // Frame 3-4 alternati — SSJ3, aura + vibra
                        cinematicShowAura = true;
                        cinematicFrame = (cinematicTimer % 8 < 4) ? 3 : 4;
                        cinematicVibrateX = (cinematicTimer % 4 < 2) ? -3 : 3;
                        if (cinematicTimer >= 70) { cinematicStep = 5; cinematicTimer = 0; cinematicShowAura = false; cinematicVibrateX = 0; }
                        break;
                    case 5: // Frame 5
                        cinematicFrame = 5;
                        if (cinematicTimer >= 20) { cinematicStep = 6; cinematicTimer = 0; }
                        break;
                    case 6: // Frame 6
                        cinematicFrame = 6;
                        if (cinematicTimer >= 20) { cinematicStep = 7; cinematicTimer = 0; }
                        break;
                    case 7: // Frame 7 — carica onda, NO fulmini
                        cinematicFrame = 7;
                        cinematicShowLightning = false;
                        if (cinematicTimer >= 70) { cinematicStep = 8; cinematicTimer = 0; }
                        break;
                    case 8: // Frame 8
                        cinematicFrame = 8;
                        if (cinematicTimer >= 20) { cinematicStep = 9; cinematicTimer = 0; }
                        break;
                    case 9: // Frame 9
                        cinematicFrame = 9;
                        if (cinematicTimer >= 20) { cinematicStep = 10; cinematicTimer = 0; }
                        break;
                    case 10: // Frame 10
                        cinematicFrame = 10;
                        if (cinematicTimer >= 20) { cinematicStep = 11; cinematicTimer = 0; }
                        break;
                    case 11: // Frame 11 — ultimo
                        cinematicFrame = 11;
                        if (cinematicTimer >= 60) {
                            cinematicPhase = 2; cinematicTimer = 0;
                            cinematicShowLightning = false;
                            cinematicShowAura = false;
                            cinematicVibrateX = 0;
                        }
                        break;
                }
                break;

            case 2: // BEAM + MULTI HIT (90 tick)
                cinematicAlpha = 1f;
                int hitInterval = 120 / CINEMATIC_NUM_HITS;
                if (cinematicTimer % hitInterval == 0 && cinematicHitsDealt < CINEMATIC_NUM_HITS) {
                    int dmg = CINEMATIC_TOTAL_DAMAGE / CINEMATIC_NUM_HITS;
                    opponent.hp -= dmg;
                    if (opponent.hp < 0) opponent.hp = 0;
                    cinematicHitsDealt++;
                }
                if (cinematicTimer >= 120) { cinematicPhase = 3; cinematicTimer = 0; }
                break;

            case 3: // WHITE FLASH (60 tick)
                if (cinematicTimer < 30) cinematicAlpha = 1f;
                else cinematicAlpha = 1f - ((cinematicTimer - 30) / 30f);
                if (cinematicTimer >= 60) {
                    cinematicPhase = 4;
                    cinematicTimer = 0;
                    cinematicPostBeam = true;
                    cinematicPostFrame = 5;
                    // Ripristina posizioni sul campo
                    x = cinematicSavedX;
                    y = cinematicSavedY;
                    opponent.x = cinematicOpponentSavedX;
                    opponent.y = cinematicOpponentSavedY;
                    // Avversario visibile: lanciato o KO
                    if (opponent.hp <= 0) {
                        opponent.setState(FighterState.KO);
                        opponent.koFromAir = true;
                        opponent.koPhase = 0; opponent.koFrame = 0; opponent.koAnimTimer = 0;
                    } else {
                        opponent.setState(FighterState.LAUNCHED);
                        opponent.launchedUp = false;
                        opponent.launchPhase = 0; opponent.launchFrame = 0; opponent.launchAnimTimer = 0;
                        opponent.velocityY = 0; // fermo durante fase 4
                    }
                }
                break;

            case 4: // SSJ3 ON BATTLEFIELD — poi flash bianco — poi SSJ2
                cinematicAlpha = 0f;
                cinematicPostBeam = true; // resta true per tutte le sotto-fasi
                if (cinematicTimer < 50) {
                    cinematicPostFrame = 5;       // SSJ3
                } else if (cinematicTimer < 65) {
                    cinematicPostFrame = 99;      // flash bianco SSJ2
                } else {
                    cinematicPostFrame = -1;      // SSJ2 normale (idle)
                }
                if (cinematicTimer >= 80) {
                    cinematicPhase = 5;
                    cinematicTimer = 0;
                    cinematicPostBeam = false;
                }
                break;

            case 5: // RETURN TO GAMEPLAY (20 tick)
                cinematicAlpha = 0f;
                if (cinematicTimer >= 20) endCinematicUltimate(opponent);
                break;
        }
    }

    protected void endCinematicUltimate(Fighter opponent) {
        cinematicActive = false;
        cinematicPostBeam = false;
        setState(FighterState.IDLE);
        if (y < groundY) setState(FighterState.JUMPING);

        // Avversario: se ancora in LAUNCHED, riattiva gravità
        if (opponent.state == FighterState.LAUNCHED) {
            opponent.velocityY = 8;
            opponent.invincibleTimer = 60;
        }
    }

    // Getter per GamePanel
    public boolean isCinematicActive() { return cinematicActive; }
    public int getCinematicPhase() { return cinematicPhase; }
    public float getCinematicAlpha() { return cinematicAlpha; }
    public int getCinematicFrame() { return cinematicFrame; }
    public boolean isCinematicShowExplosion() { return cinematicShowExplosion; }

    // Override in sottoclassi per sprite specifici
    public void drawCinematicSprite(Graphics2D g2d) {}
    public void drawCinematicBeamScene(Graphics2D g2d) {}

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
        if ((state == FighterState.CHARGING_KI || auraBoostActive) && auraImage != null) {
            int[] aX = {3, 357, 4, 368}, aY = {4, 4, 454, 440};
            int[] aW = {350, 345, 359, 356}, aH = {446, 432, 437, 417};
            double auraDrawScale = 0.65 * scale; // ingrandita da 0.45 a 0.65
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
                : (auraBoostActive ? new Color(160, 220, 255) : new Color(70, 150, 255));
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