import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.AlphaComposite;

public class Vegeta extends Fighter {

    private int beamEndX = -1;

    public Vegeta(int x, int y, int playerID) {
        super(x, y, playerID, ResourceManager.getInstance().vegetaSpriteSheet);

        this.auraImage    = ResourceManager.getInstance().auraYellow;
        this.kiBlastImage = ResourceManager.getInstance().kiblastYellow;
        this.facingRight  = (playerID == 1);
        this.portraitSrcY = 300;

        // Scala e dimensioni — allineate a Goku (scale 1.0)
        this.scale      = 1.0;
        this.baseWidth  = (int)(72 * scale);
        this.baseHeight = (int)(163 * scale);

        // Universal floor
        int universalFloorY = y + 111;
        this.y       = universalFloorY - this.baseHeight;
        this.groundY = this.y;

        // Fisica — identica a Goku
        this.speed        = (int)(4 * scale);
        this.jumpStrength = -16 * scale;
        this.gravity      = 0.35 * scale;

        // Ki
        this.ki            = MAX_KI;
        this.kiBlastKiCost = 70.0;  // Vegeta spara più spesso
        this.kiOnHitReward = 18.0;  // Vegeta guadagna più Ki sui colpi

        // Danni — Vegeta è più aggressivo sui Ki Blast
        this.kiBlastDamage = 12;
        this.specialDamage = 50;

        // Special (Final Flash)
        this.MAX_SPECIAL_ENERGY = 2400;
        this.SPECIAL_CHARGE     = 90;
        this.SPECIAL_DURATION   = 100;
        this.specialDrainRate   = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;

        // Aura
        this.auraColor = new Color(255, 238, 0);

        // Definisci le combo routes
        this.comboRoutes = defineComboRoutes();
    }

    // =============================================
    // COMBO ROUTES — sequenze di Vegeta
    // =============================================
    public ComboRoute[] defineComboRoutes() {
        return new ComboRoute[] {

                // ============================
                // A TERRA (requiresGround=true)
                // ============================

                new ComboRoute("vegeta_L",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_light", 4, 4, 8, 5, 8)
                        },
                        "light_1",
                        false, false, true
                ),

                new ComboRoute("vegeta_LL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_light",  4, 4, 8, 5, 8),
                                new AttackData("vegeta_light2", 4, 4, 8, 5, 8)
                        },
                        "light_2",
                        false, false, true
                ),

                new ComboRoute("vegeta_LLL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_light",  4, 4, 8, 5,  8),
                                new AttackData("vegeta_light2", 4, 4, 8, 5,  8),
                                new AttackData("vegeta_light3", 4, 4, 8, 8, 10)
                        },
                        "light_3",
                        false, false, true
                ),

                new ComboRoute("vegeta_LLH",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_light",    4, 4,  8,  5,  8),
                                new AttackData("vegeta_light2",   4, 4,  8,  5,  8),
                                new AttackData("vegeta_launcher", 4, 4, 16, 15, 20,
                                        false, true, true, false,
                                        0.0, 15.0)
                        },
                        "light_launcher",
                        false, false, true
                ),

                new ComboRoute("vegeta_H",
                        new int[]{ ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_guard_break", 12, 4, 8, 12, 12,
                                        false, false, false, false, true,
                                        0.0, 15.0)
                        },
                        "heavy_standalone",
                        false, false, true
                ),

                // ============================
                // IN ARIA (requiresAir=true)
                // ============================

                new ComboRoute("vegeta_L_air",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_air_light", 4, 4, 8, 5, 8)
                        },
                        "air_light_1",
                        false, true, false
                ),

                new ComboRoute("vegeta_LL_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_air_light",  4, 4, 8, 5, 8),
                                new AttackData("vegeta_air_light2", 4, 4, 8, 5, 8)
                        },
                        "air_light_2",
                        false, true, false
                ),

                new ComboRoute("vegeta_LLL_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_air_light",  4, 4, 8, 5,  8),
                                new AttackData("vegeta_air_light2", 4, 4, 8, 5,  8),
                                new AttackData("vegeta_air_light3", 4, 4, 8, 8, 10)
                        },
                        "air_light_3",
                        false, true, false
                ),

                new ComboRoute("vegeta_LLH_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_air_light",    4, 4,  8,  5,  8),
                                new AttackData("vegeta_air_light2",   4, 4,  8,  5,  8),
                                new AttackData("vegeta_air_launcher", 4, 4, 16, 15, 20,
                                        false, true, true, false,
                                        0.0, 15.0)
                        },
                        "air_heavy_launcher",
                        false, true, false
                ),

                new ComboRoute("vegeta_LLH_air_spike",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_air_light",  4, 4,  8,  5,  8),
                                new AttackData("vegeta_air_light2", 4, 4,  8,  5,  8),
                                new AttackData("vegeta_air_spike", 16, 4,  8, 15, 20,
                                        false, true, false, true,
                                        0.0, 15.0)
                        },
                        "air_heavy_spike",
                        false, true, false
                ),

                new ComboRoute("vegeta_H_air",
                        new int[]{ ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_air_guard_break", 8, 4, 12, 12, 12,
                                        false, false, false, false, true,
                                        0.0, 15.0)
                        },
                        "air_heavy_standalone",
                        false, true, false
                ),

                // ============================
                // SPECIALE (requiresAura=true)
                // ============================

                new ComboRoute("vegeta_surprise",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_surprise", 3, 5, 10, 8, 12,
                                        false, true, false, false,
                                        0.0, 20.0)
                        },
                        "surprise",
                        true, false, false
                )
        };
    }


    // =============================================
    // FIND MATCHING CHAIN ROUTE — override per spike/launcher
    // Se in aria e combo L→L→H:
    //   avversario sotto metà schermo → launcher (manda su)
    //   avversario sopra metà schermo → spike (manda giù)
    // =============================================
    @Override
    protected ComboRoute findMatchingChainRoute(Fighter opponent) {
        if (comboRoutes == null || comboHistoryLength == 0) return null;
        boolean inAir = isAttacking() ? wasFlying
                : (isFlying() || state == FighterState.JUMPING);
        boolean aura  = auraBoostActive;
        int midScreen = GamePanel.SCREEN_HEIGHT / 2;

        ComboRoute best = null;
        for (ComboRoute route : comboRoutes) {
            if (!route.isExecutable(aura, inAir)) continue;

            // Spike/launcher: selezione in base alla Y dell'avversario
            if (opponent != null && inAir) {
                if (route.id.equals("vegeta_LLH_air") && opponent.y < midScreen)
                    continue; // avversario in alto → skip launcher, usa spike
                if (route.id.equals("vegeta_LLH_air_spike") && opponent.y >= midScreen)
                    continue; // avversario in basso → skip spike, usa launcher
            }

            // Match esatto con la chain accumulata
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
    // HITBOX
    // =============================================
    @Override
    public Rectangle getSpecialHitbox() { return null; }

    @Override
    public Rectangle getUltimateHitbox() {
        if (specialTimer <= SPECIAL_CHARGE) return null;
        int reach   = (int)(GamePanel.SCREEN_WIDTH * scale);
        int boxH    = (int)(40 * scale);
        int offsetY = (int)(20 * scale);
        int hX = facingRight ? x + baseWidth : x - reach;
        return new Rectangle(hX, y + offsetY, reach, boxH);
    }

    // =============================================
    // VFX
    // =============================================
    @Override
    protected void spawnKiBlastVFX() {
        // Frame 1: scintilla accanto alla testa, lato mano tesa
        int handX = facingRight ? x + baseWidth + (int)(65 * scale) : x + (int)(65 * scale);
        int handY = y + (int)(45 * scale);
        activeEffects.add(new VisualEffect(kiBlastImage, handX, handY,
                new int[]{391}, new int[]{133}, new int[]{61}, new int[]{62},
                3, 0.8 * scale));
    }

    @Override
    protected void fireKiBlastProjectile() {
        // Lancio spostato più avanti nella direzione di sguardo
        int startX = facingRight ? x + baseWidth + (int)(50 * scale) : x - (int)(110 * scale);
        int startY = y + (int)(50 * scale);
        KiBlastProjectile blast = new KiBlastProjectile(startX, startY, facingRight, kiBlastImage, scale);
        blast.sound = SoundManager.getInstance().playAndReturn("kiblast");
        activeBlasts.add(blast);
    }

    @Override
    protected void onSpecialHit(Fighter opponent) {}

    @Override
    protected void onUltimateHit(Fighter opponent) {
        int expX = opponent.getX() + (opponent.baseWidth / 2);
        int expY = opponent.y + (opponent.baseHeight / 2);
        opponent.activeEffects.add(new VisualEffect(
                ResourceManager.getInstance().commonVfx, expX, expY,
                new int[]{0, 200}, new int[]{0, 0},
                new int[]{142, 142}, new int[]{120, 120},
                6, 1.2 * scale));
    }

    @Override protected String ultimateChargeSound() { return "final_flash"; }

    // =============================================
    // UPDATE
    // =============================================
    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        super.update(keyH, opponent);

        if (state == FighterState.SPECIAL_STARTUP && specialTimer == 4) {
            // Frame 2: centrato sul corpo, leggermente più in basso
            int hX = x + (baseWidth / 2) - (int)(8 * scale);
            int hY = y + (int)(60 * scale);
            activeEffects.add(new VisualEffect(kiBlastImage, hX, hY,
                    new int[]{391}, new int[]{133}, new int[]{61}, new int[]{62},
                    3, 0.8 * scale));
        }
        if (state == FighterState.SPECIAL_STARTUP && specialTimer == 8) {
            // Frame 3: a ridosso della mano, più in basso
            int bX = facingRight ? x + baseWidth + (int)(20 * scale) : x - (int)(50 * scale);
            int bY = y + (int)(55 * scale);
            activeEffects.add(new VisualEffect(kiBlastImage, bX, bY,
                    new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60},
                    3, 0.6 * scale));
        }

        // Beam end per Final Flash
        if (state == FighterState.ULTIMATE_ACTIVE) {
            beamEndX = facingRight ? GamePanel.SCREEN_WIDTH : 0;
            Rectangle hitbox = getUltimateHitbox();
            if (hitbox != null && opponent != null
                    && hitbox.intersects(opponent.getBounds()))
                beamEndX = facingRight
                        ? opponent.getX()
                        : opponent.getX() + opponent.baseWidth;
        }

        // Animazione walking
        if (state == FighterState.WALKING) {
            spriteCounter++;
            if (spriteCounter > (auraBoostActive ? 3 : 5)) {
                spriteNum++;
                if (spriteNum > 6) spriteNum = 1;
                spriteCounter = 0;
            }
        } else if (state == FighterState.JUMPING) {
            spriteCounter++;
            if (spriteCounter > 8) {
                spriteNum++;
                if (spriteNum > 6) spriteNum = 6;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 1;
        }
    }

    // =============================================
    // DRAW — switch sulla FSM
    // =============================================
    @Override
    public void draw(Graphics2D g2d) {

        // Default: stance base (IDLE)
        srcX = 407; srcY = 833; srcW = 79; srcH = 149;

        switch (state) {

            case KO -> {
                if (koFromAir) {
                    if (koPhase == 0) {
                        // Caduta dall'aria (2 frame) — riusa discesa LAUNCHED
                        srcW = 102; srcH = 112; srcY = 7459;
                        int[] kfX = {210, 314};
                        srcX = kfX[Math.min(koFrame, 1)];
                    } else {
                        // A terra (3 frame) — riusa recovery LAUNCHED
                        srcW = 153; srcH = 89; srcY = 7575;
                        int[] kgX = {2, 157, 312};
                        srcX = kgX[Math.min(koFrame, 2)];
                    }
                } else {
                    if (koPhase == 0) {
                        // Colpo a terra (2 frame) — riusa salita LAUNCHED
                        srcW = 105; srcH = 117; srcY = 7338;
                        int[] k0X = {216, 323};
                        srcX = k0X[Math.min(koFrame, 1)];
                    } else if (koPhase == 1) {
                        // Transizione (1 frame) — discesa LAUNCHED
                        srcW = 102; srcH = 112; srcY = 7459;
                        srcX = 210;
                    } else {
                        // A terra (3 frame) — recovery LAUNCHED
                        srcW = 153; srcH = 89; srcY = 7575;
                        int[] k2X = {2, 157, 312};
                        srcX = k2X[Math.min(koFrame, 2)];
                    }
                }
            }

            case WINNER -> {
                srcW = 66; srcH = 148; srcY = 7818;
                int[] wX = {2, 70, 138, 206, 274, 342, 410};
                srcX = wX[Math.min(endFrame - 1, 6)];
            }

            case HIT_STUN, TUMBLING -> {
                srcW = 99; srcH = 116; srcY = 6569;
                srcX = 103;
            }

            case LAUNCHED -> {
                if (launchPhase == 0) {
                    // Airborne
                    if (launchedUp) {
                        // Launcher verso l'alto: 2 frame salita + 2 frame discesa
                        if (velocityY < 0) {
                            // Salendo
                            srcW = 105; srcH = 117; srcY = 7338;
                            int[] upX = {216, 323};
                            srcX = upX[Math.min(launchFrame, 1)];
                        } else {
                            // Scendendo
                            srcW = 102; srcH = 112; srcY = 7459;
                            int[] downX = {210, 314};
                            srcX = downX[Math.min(launchFrame - 2, 1)];
                        }
                    } else {
                        // Spike verso il basso — riusa i frame di discesa
                        srcW = 102; srcH = 112; srcY = 7459;
                        int[] spikeX = {210, 314};
                        srcX = spikeX[Math.min(launchFrame, 1)];
                    }
                } else {
                    // Ground recovery (8 frame)
                    srcW = 153; srcH = 89; srcY = 7575;
                    int[] recX = {2, 157, 312, 467, 622, 777, 932, 1087};
                    srcX = recX[Math.min(launchFrame, 7)];
                }
            }

            case CHARGING_KI -> {
                srcW = 92; srcH = 143; srcY = 7970;
                // Alterna 2 frame della trasformazione per effetto vibrazione (come Goku)
                srcX = (spriteCounter % 8 < 4) ? 848 : 2;
                shiftX += (int)(Math.random() * 3) - 1;
            }

            case AURA_ACTIVE -> {
                srcX = 9; srcY = 6; srcW = 43; srcH = 75;
            }

            case BLOCKING -> {
                int blockFrame = Math.min(blockActiveTimer / 5, 2);
                srcY = 1331; srcH = 119; srcW = 87;
                int[] bX = {2, 91, 180};
                srcX = bX[blockFrame];
            }

            case BLOCKING_AIR -> {
                int blockFrame = Math.min(blockActiveTimer / 5, 2);
                srcY = 1563; srcH = 130; srcW = 83;
                int[] bX = {2, 87, 172};
                srcX = bX[blockFrame];
            }

            case TELEPORTING -> {
                // Nessuno sprite dedicato: riusa la posa di volo (movimento rapido)
                srcW = 88; srcH = 150; srcX = 92; srcY = 1697;
            }

            case COMBO_LIGHT, COMBO_HEAVY -> {
                if (activeRoute != null) drawComboSprite(g2d);
            }

            case SPECIAL_STARTUP, SPECIAL_ACTIVE -> {
                srcW = 106; srcH = 151; srcY = 5852;
                int spFrame = Math.min(specialTimer / FRAME_SPEED, 5);
                int[] spX = {2, 110, 218, 326, 542, 974};
                srcX = spX[spFrame];
            }

            case ULTIMATE_STARTUP -> {
                srcW = 123; srcH = 140; srcY = 4939;
                int[] ultX = {2, 127, 252, 377, 502};
                int ultFrame = Math.min(specialTimer * ultX.length / SPECIAL_CHARGE, 4);
                srcX = ultX[ultFrame];
            }

            case ULTIMATE_ACTIVE -> {
                srcW = 123; srcH = 140; srcY = 4939;
                srcX = 502; // frame più frontale, riusato per lo sparo
            }

            case CROUCHING -> {
                srcW = 88; srcH = 150; srcX = 2; srcY = 1697;
            }

            case JUMPING -> {
                srcW = 88; srcH = 150; srcY = 1697;
                int[] jumpX = {92, 182, 272, 362, 452, 542};
                srcX = jumpX[Math.min(spriteNum - 1, 5)];
            }

            case FLYING_IDLE -> {
                srcW = 88; srcH = 150; srcX = 92; srcY = 1697;
            }
            case FLYING_FORWARD -> {
                srcW = 106; srcH = 128; srcX = 2; srcY = 8117;
            }
            case FLYING_FORWARD_FULL -> {
                srcW = 106; srcH = 128; srcX = 110; srcY = 8117;
            }
            case FLYING_BACKWARD -> {
                srcW = 89; srcH = 129; srcX = 2; srcY = 8249;
            }
            case FLYING_BACKWARD_FULL -> {
                srcW = 89; srcH = 129; srcX = 93; srcY = 8249;
            }

            case WALKING -> {
                srcW = 98; srcH = 117; srcY = 1210;
                int[] walkX = {2, 102, 202, 302, 402, 502};
                srcX = walkX[Math.min(spriteNum - 1, 5)];
            }

            default -> {
                // IDLE — stance base già impostata sopra
            }
        }

        // Flash visivo quando colpito da un light (sovrascrive lo sprite temporaneamente)
        if (lightHitFlash > 0 && !isInState(FighterState.HIT_STUN, FighterState.TUMBLING,
                FighterState.LAUNCHED, FighterState.KO)) {
            srcW = 99; srcH = 116; srcY = 6569; srcX = 103;
        }

        drawFighterSprite(g2d);

        drawFighterSprite(g2d);

        // Final Flash beam
        if (state == FighterState.ULTIMATE_ACTIVE) {
            drawFinalFlash(g2d);
        }

        for (VisualEffect eff : activeEffects) eff.draw(g2d);
        for (KiBlastProjectile blast : activeBlasts) blast.draw(g2d);

        drawPlayerPin(g2d, x + shiftX, drawY, drawW);
        drawUniversalHUD(g2d, "FINAL FLASH");

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }


    // =============================================
    // INTRO TRANSFORM — 7 frame (da X=378 in poi), base->SSJ (riga Y=7970)
    // Nota: l'ultimo frame (SSJ) sta a X=2, all'inizio della riga
    // =============================================
    @Override
    public void drawIntroTransform(Graphics2D g2d, double progress) {
        final int FRAMES = 7;
        int frame = (int)(progress * FRAMES);
        if (frame >= FRAMES) frame = FRAMES - 1;

        srcW = 92; srcH = 143; srcY = 7970;
        int[] tX = {378, 472, 566, 660, 754, 848, 2};
        srcX = tX[frame];

        // Aura visiva solo sull'ultimo frame — estetica pura, nessun bonus
        introAuraOverride = (frame == FRAMES - 1);
        drawFighterSprite(g2d);
        introAuraOverride = false;

        for (VisualEffect eff : activeEffects) eff.draw(g2d);
        drawPlayerPin(g2d, x + shiftX, drawY, drawW);
    }


    // =============================================
    // HELPER — sprite della combo in base alla route
    // =============================================
    private void drawComboSprite(Graphics2D g2d) {
        String key = activeRoute.animationKey;
        int frameIndex = attackTimer / FRAME_SPEED;

        switch (key) {

            // === TERRA: Light (4 frame) ===
            case "light_1", "light_2", "light_3" -> {
                frameIndex = Math.min(frameIndex, 3);
                srcY = 2500; srcW = 133; srcH = 109;
                int[] xFrames = {2, 137, 272, 407};
                srcX = xFrames[frameIndex];
            }

            // === TERRA: Launcher L→L→H (6 frame) ===
            case "light_launcher" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 4805; srcW = 123; srcH = 130;
                int[] xFrames = {2, 127, 252, 377, 502, 627};
                srcX = xFrames[frameIndex];
            }

            // === TERRA: Heavy standalone guard breaker (6 frame) ===
            case "heavy_standalone" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 5543; srcW = 105; srcH = 148;
                int[] xFrames = {2, 109, 216, 323, 430, 537};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Light (4 frame) ===
            case "air_light_1", "air_light_2", "air_light_3" -> {
                frameIndex = Math.min(frameIndex, 3);
                srcY = 4015; srcW = 131; srcH = 127;
                int[] xFrames = {2, 135, 268, 401};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Heavy standalone guard breaker (7 frame) ===
            case "air_heavy_standalone" -> {
                frameIndex = Math.min(frameIndex, 6);
                srcY = 5695; srcW = 144; srcH = 153;
                int[] xFrames = {732, 878, 1024, 1170, 1316, 1462, 1608};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Launcher — stesso sprite del launcher a terra (5 frame) ===
            case "air_heavy_launcher" -> {
                frameIndex = Math.min(frameIndex, 4);
                srcY = 2613; srcW = 99; srcH = 136;
                int[] xFrames = {2, 103, 204, 305, 406};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Spike — calcio dall'alto verso il basso (7 frame) ===
            case "air_heavy_spike" -> {
                frameIndex = Math.min(frameIndex, 6);
                srcY = 5083; srcW = 124; srcH = 153;
                int[] xFrames = {2, 128, 254, 380, 506, 632, 758};
                srcX = xFrames[frameIndex];
            }

            // === SURPRISE ATTACK (usa sprite light a terra) ===
            case "surprise" -> {
                frameIndex = Math.min(frameIndex, 3);
                srcY = 2500; srcW = 133; srcH = 109;
                int[] xFrames = {2, 137, 272, 407};
                srcX = xFrames[frameIndex];
            }
        }
    }


    private void drawFinalFlash(Graphics2D g2d) {
        // Coordinate beam sprite (nuovo foglio)
        int headSrcX = 1260, headSrcY = 3751, headW = 86, headH = 80;
        int bodySrcX = 1221, bodySrcY = 3780, bodyW = 36, bodyH = 22;

        int drawBodyH = (int)(bodyH * scale);
        int drawHeadW = (int)(headW * scale);
        int drawHeadH = (int)(headH * scale);
        int beamY  = drawY + (int)(6 * scale);
        int bodyOffsetY = (drawHeadH - drawBodyH) / 2;
        int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? GamePanel.SCREEN_WIDTH : 0);

        // Head nativo punta a SINISTRA. Origine: flip se facingRight. Impatto: flip se !facingRight.

        if (facingRight) {
            int startX = x + shiftX + drawW - (int)(20 * scale);
            if (targetX - drawHeadW > startX + drawHeadW) {
                // Testa origine (flippata: bocca verso sinistra)
                g2d.drawImage(spriteSheet,
                        startX + drawHeadW, beamY, startX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                // Body (si allunga)
                g2d.drawImage(spriteSheet,
                        startX + drawHeadW, beamY + bodyOffsetY,
                        targetX - drawHeadW, beamY + bodyOffsetY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                // Testa impatto (normale: bocca verso destra)
                g2d.drawImage(spriteSheet,
                        targetX - drawHeadW, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        } else {
            int startX = x + shiftX + (int)(20 * scale);
            if (targetX + drawHeadW < startX - drawHeadW) {
                // Testa origine (normale: bocca verso destra)
                g2d.drawImage(spriteSheet,
                        startX - drawHeadW, beamY, startX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                // Body (si allunga)
                g2d.drawImage(spriteSheet,
                        startX - drawHeadW, beamY + bodyOffsetY,
                        targetX + drawHeadW, beamY + bodyOffsetY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                // Testa impatto (flippata: bocca verso sinistra)
                g2d.drawImage(spriteSheet,
                        targetX + drawHeadW, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        }
    }


}