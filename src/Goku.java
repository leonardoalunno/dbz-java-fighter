import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.AlphaComposite;

public class Goku extends Fighter {

    private int beamEndX = -1;

    public Goku(int x, int y, int playerID) {
        super(x, y, playerID, ResourceManager.getInstance().gokuSpriteSheet);

        this.auraImage   = ResourceManager.getInstance().auraBlue;
        this.kiBlastImage = ResourceManager.getInstance().kiblastBlue;
        this.facingRight  = (playerID == 1);
        this.portraitSrcY = 0;

        // Scala e dimensioni
        this.scale      = 1.0;
        this.baseWidth  = (int)(72 * scale);
        this.baseHeight = (int)(163 * scale);

        // Universal floor
        int universalFloorY = y + 111;
        this.y       = universalFloorY - this.baseHeight;
        this.groundY = this.y;

        // Fisica
        this.speed        = (int)(4 * scale);
        this.jumpStrength = -16 * scale;
        this.gravity      = 0.35 * scale;

        // Ki
        this.ki            = MAX_KI;
        this.kiBlastKiCost = 80.0;
        this.kiOnHitReward = 15.0;

        // Danni
        this.kiBlastDamage = 10;
        this.specialDamage = 35;

        // Special (Kamehameha)
        this.MAX_SPECIAL_ENERGY = 2400;
        this.SPECIAL_CHARGE     = 40;
        this.SPECIAL_DURATION   = 90;
        this.specialDrainRate   = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;

        // Aura
        this.auraColor = new Color(0, 118, 255);

        // Definisci le combo routes
        this.comboRoutes = defineComboRoutes();
    }

    // =============================================
    // COMBO ROUTES — sequenze di Goku
    // =============================================
    @Override
    public ComboRoute[] defineComboRoutes() {
        return new ComboRoute[] {

                // ============================
                // A TERRA (requiresGround=true)
                // ============================

                // L — light singolo (4 frame, danno al 2°)
                new ComboRoute("goku_L",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_light", 4, 4, 8, 5, 8)
                        },
                        "light_1",
                        false, false, true  // requiresGround=true
                ),

                // L → L — doppio light (4 frame, danno al 2°)
                new ComboRoute("goku_LL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_light",  4, 4, 8, 5, 8),
                                new AttackData("goku_light2", 4, 4, 8, 5, 8)
                        },
                        "light_2",
                        false, false, true
                ),

                // L → L → L — triplo light (4 frame, danno al 2°)
                new ComboRoute("goku_LLL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_light",  4, 4, 8, 5,  8),
                                new AttackData("goku_light2", 4, 4, 8, 5,  8),
                                new AttackData("goku_light3", 4, 4, 8, 8, 10)
                        },
                        "light_3",
                        false, false, true
                ),

                // L → L → H — launcher (6 frame, danno al 2°, lancia in alto)
                new ComboRoute("goku_LLH",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_light",    4, 4,  8,  5,  8),
                                new AttackData("goku_light2",   4, 4,  8,  5,  8),
                                new AttackData("goku_launcher", 4, 4, 16, 15, 20,
                                        false, true, true, false,
                                        0.0, 15.0)
                        },
                        "light_launcher",
                        false, false, true
                ),

                // H — heavy standalone guard breaker (6 frame, danno al 4°)
                // In block: spezza la guardia senza danno HP
                // Fuori block: danno pieno
                new ComboRoute("goku_H",
                        new int[]{ ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_guard_break", 12, 4, 8, 12, 12,
                                        false, false, false, false, true,
                                        0.0, 15.0)
                        },
                        "heavy_standalone",
                        false, false, true
                ),

                // ============================
                // IN ARIA (requiresAir=true)
                // ============================

                // L — air light singolo (4 frame, danno al 2°)
                new ComboRoute("goku_L_air",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_air_light", 4, 4, 8, 5, 8)
                        },
                        "air_light_1",
                        false, true, false
                ),

                // L → L — doppio air light (4 frame, danno al 2°)
                new ComboRoute("goku_LL_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_air_light",  4, 4, 8, 5, 8),
                                new AttackData("goku_air_light2", 4, 4, 8, 5, 8)
                        },
                        "air_light_2",
                        false, true, false
                ),

                // L → L → L — triplo air light (4 frame, danno al 2°)
                new ComboRoute("goku_LLL_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_air_light",  4, 4, 8, 5,  8),
                                new AttackData("goku_air_light2", 4, 4, 8, 5,  8),
                                new AttackData("goku_air_light3", 4, 4, 8, 8, 10)
                        },
                        "air_light_3",
                        false, true, false
                ),

                // L → L → H — air launcher (6 frame, danno al 2°, lancia in alto)
                // Selezionato quando l'avversario è SOTTO metà schermo
                new ComboRoute("goku_LLH_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_air_light",    4, 4,  8,  5,  8),
                                new AttackData("goku_air_light2",   4, 4,  8,  5,  8),
                                new AttackData("goku_air_launcher", 4, 4, 16, 15, 20,
                                        false, true, true, false,
                                        0.0, 15.0)
                        },
                        "air_heavy_launcher",
                        false, true, false
                ),

                // L → L → H — air spike (6 frame, danno al 4°, schiaccia in basso)
                // Selezionato quando l'avversario è SOPRA metà schermo
                new ComboRoute("goku_LLH_air_spike",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_air_light",  4, 4,  8,  5,  8),
                                new AttackData("goku_air_light2", 4, 4,  8,  5,  8),
                                new AttackData("goku_air_spike", 12, 4,  8, 15, 20,
                                        false, true, false, true,
                                        0.0, 15.0)
                        },
                        "air_heavy_spike",
                        false, true, false
                ),

                // H — air heavy standalone guard breaker (6 frame, danno al 3°)
                new ComboRoute("goku_H_air",
                        new int[]{ ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_air_guard_break", 8, 4, 12, 12, 12,
                                        false, false, false, false, true,
                                        0.0, 15.0)
                        },
                        "air_heavy_standalone",
                        false, true, false
                ),

                // ============================
                // SPECIALE (requiresAura=true)
                // ============================

                // Surprise Attack — L unblockable (solo con Aura)
                new ComboRoute("goku_surprise",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_surprise", 3, 5, 10, 8, 12,
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
                if (route.id.equals("goku_LLH_air") && opponent.y < midScreen)
                    continue; // avversario in alto → skip launcher, usa spike
                if (route.id.equals("goku_LLH_air_spike") && opponent.y >= midScreen)
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
    // HITBOX SPECIAL (Kamehameha)
    // =============================================
    @Override
    public Rectangle getSpecialHitbox() { return null; } // gestita dai proiettili

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
        // Scintilla frame 1 — dura esattamente 1 sprite frame (4 tick)
        int sparkX = facingRight ? x + baseWidth - (int)(12 * scale) : x - (int)(18 * scale);
        int sparkY = y + (int)(20 * scale);
        activeEffects.add(new VisualEffect(kiBlastImage, sparkX, sparkY,
                new int[]{391}, new int[]{133}, new int[]{61}, new int[]{62},
                3, 0.8 * scale)); // speed=3 → vive 4 tick = 1 frame
    }

    @Override
    protected void fireKiBlastProjectile() {
        // Punto di partenza allineato al centro della sferetta
        int startX = facingRight ? x + baseWidth + (int)(30 * scale) : x - (int)(60 * scale);
        int startY = y + (int)(33 * scale); // stessa Y del centro sferetta
        activeBlasts.add(new KiBlastProjectile(startX, startY, facingRight, kiBlastImage, scale));
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

    // =============================================
    // UPDATE
    // =============================================
    @Override
    public void update(KeyHandler keyH, Fighter opponent) {
        super.update(keyH, opponent);

        // Scintilla frame 2 (tick 4-7) — più in basso e più a dx
        if (state == FighterState.SPECIAL_STARTUP && specialTimer == 4) {
            int spark2X = facingRight ? x + baseWidth + (int)(8 * scale) : x - (int)(38 * scale);
            int spark2Y = y + (int)(28 * scale);
            activeEffects.add(new VisualEffect(kiBlastImage, spark2X, spark2Y,
                    new int[]{391}, new int[]{133}, new int[]{61}, new int[]{62},
                    3, 0.8 * scale));
        }
        // Sferetta frame 3 (tick 8-11) — centro allineato con ki blast
        if (state == FighterState.SPECIAL_STARTUP && specialTimer == 8) {
            int ballX = facingRight ? x + baseWidth + (int)(30 * scale) : x - (int)(60 * scale);
            int ballY = y + (int)(33 * scale);
            activeEffects.add(new VisualEffect(kiBlastImage, ballX, ballY,
                    new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60},
                    3, 0.6 * scale));
        }

        // Beam end per Kamehameha
        if (state == FighterState.ULTIMATE_ACTIVE) {
            beamEndX = facingRight ? GamePanel.SCREEN_WIDTH : 0;
            Rectangle hitbox = getUltimateHitbox();
            if (hitbox != null && opponent != null && hitbox.intersects(opponent.getBounds()))
                beamEndX = facingRight ? opponent.getX() : opponent.getX() + opponent.baseWidth;
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
                if (spriteNum > 7) spriteNum = 7;
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

        // Durante la cinematica zoom (fasi 0-3), il rendering è gestito da drawCinematicSprite via UIManager
        // Ma l'HUD si disegna SEMPRE dalle fasi 4+ in poi
        if (cinematicActive && !cinematicPostBeam) {
            if (state == FighterState.CINEMATIC_ULTIMATE) {
                // Fase 0-3: niente personaggio, niente HUD (schermo nero/zoom)
                return;
            }
        }

        // Post-beam: disegna SSJ3 o frame transizione a scala di gioco
        if (cinematicPostBeam) {
            // Aura dietro
            drawAura(g2d);

            if (cinematicPostFrame == 99) {
                int tW = (int)(107 * scale), tH = (int)(165 * scale);
                int tX = x + (baseWidth - tW) / 2;
                int tY = y + baseHeight - tH;
                int sX1 = tX, sX2 = tX + tW;
                if (facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
                g2d.drawImage(spriteSheet, sX1, tY, sX2, tY + tH,
                        874, 552, 874 + 107, 552 + 165, null);
            } else if (cinematicPostFrame == -1) {
                int tW = (int)(107 * scale), tH = (int)(165 * scale);
                int tX = x + (baseWidth - tW) / 2;
                int tY = y + baseHeight - tH;
                int sX1 = tX, sX2 = tX + tW;
                if (facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
                g2d.drawImage(spriteSheet, sX1, tY, sX2, tY + tH,
                        438, 552, 438 + 107, 552 + 165, null);
            } else {
                int[] ssj3X = {1784, 1946, 2108, 2270, 2432, 2594, 2756, 812, 650, 488, 974, 1136};
                int ssj3W = 160, ssj3H = 172, ssj3Y = 5096;
                int pf = Math.min(cinematicPostFrame, 11);
                int dW = (int)(ssj3W * scale);
                int dH = (int)(ssj3H * scale);
                int dX = x + (baseWidth - dW) / 2;
                int dY = y + baseHeight - dH;
                int sX1 = dX, sX2 = dX + dW;
                if (facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
                g2d.drawImage(spriteSheet, sX1, dY, sX2, dY + dH,
                        ssj3X[pf], ssj3Y, ssj3X[pf] + ssj3W, ssj3Y + ssj3H, null);
            }

            drawShadow(g2d);
            drawUniversalHUD(g2d, "KAMEHAMEHA"); // HUD visibile durante post-beam!
            return;
        }

        // Se l'avversario sta facendo la cinematica, non disegnare
        if (state == FighterState.CINEMATIC_ULTIMATE) return;

        // Default: IDLE stance
        srcX = 438; srcY = 552; srcW = 107; srcH = 165;

        switch (state) {

            case KO -> {
                if (koFromAir) {
                    if (koPhase == 0) {
                        // Caduta dall'aria (2 frame)
                        srcW = 134; srcH = 126; srcY = 7791;
                        int[] kfX = {274, 410};
                        srcX = kfX[Math.min(koFrame, 1)];
                    } else {
                        // A terra (3 frame, stop all'ultimo)
                        srcW = 155; srcH = 121; srcY = 7921;
                        int[] kgX = {2, 159, 316};
                        srcX = kgX[Math.min(koFrame, 2)];
                    }
                } else {
                    if (koPhase == 0) {
                        // Colpo a terra (2 frame)
                        srcW = 117; srcH = 130; srcY = 7657;
                        int[] k0X = {2, 121};
                        srcX = k0X[Math.min(koFrame, 1)];
                    } else if (koPhase == 1) {
                        // Transizione (1 frame)
                        srcW = 134; srcH = 126; srcY = 7791;
                        srcX = 2;
                    } else {
                        // A terra (3 frame, stop all'ultimo)
                        srcW = 155; srcH = 121; srcY = 7921;
                        int[] k2X = {2, 159, 316};
                        srcX = k2X[Math.min(koFrame, 2)];
                    }
                }
            }

            case WINNER -> {
                srcW = 66; srcH = 163; srcY = 8046;
                int[] wX = {2, 70, 138, 206, 274, 342, 410};
                srcX = wX[Math.min(endFrame - 1, 6)];
            }

            case HIT_STUN, TUMBLING -> {
                srcW = 101; srcH = 137; srcY = 6780;
                srcX = 105;
            }

            case LAUNCHED -> {
                if (launchPhase == 0) {
                    // Airborne
                    if (launchedUp) {
                        // Launcher verso l'alto: 2 frame salita + 2 frame discesa
                        if (velocityY < 0) {
                            // Salendo
                            srcW = 117; srcH = 130; srcY = 7657;
                            int[] upX = {240, 359};
                            srcX = upX[Math.min(launchFrame, 1)];
                        } else {
                            // Scendendo
                            srcW = 134; srcH = 126; srcY = 7791;
                            int[] downX = {274, 410};
                            srcX = downX[Math.min(launchFrame - 2, 1)];
                        }
                    } else {
                        // Spike verso il basso (2 frame)
                        srcW = 134; srcH = 126; srcY = 7791;
                        int[] spikeX = {274, 410};
                        srcX = spikeX[Math.min(launchFrame, 1)];
                    }
                } else {
                    // Ground recovery (7 frame)
                    srcW = 155; srcH = 121; srcY = 7921;
                    int[] recX = {2, 159, 316, 473, 630, 787, 944};
                    srcX = recX[Math.min(launchFrame, 6)];
                }
            }

            case CHARGING_KI -> {
                srcW = 94; srcH = 162; srcY = 386;
                // Alterna tra 2 frame per effetto vibrazione
                srcX = (spriteCounter % 8 < 4) ? 482 : 386;
                shiftX += (int)(Math.random() * 3) - 1;
            }

            case AURA_ACTIVE -> {
                // Stessa stance di IDLE con aura visiva
                srcX = 438; srcY = 552; srcW = 107; srcH = 165;
            }

            case BLOCKING -> {
                int blockFrame = Math.min(blockActiveTimer / 5, 2);
                srcY = 1014; srcH = 133; srcW = 97;
                int[] bX = {2, 101, 200};
                srcX = bX[blockFrame];
            }

            case BLOCKING_AIR -> {
                int blockFrame = Math.min(blockActiveTimer / 5, 2);
                srcY = 1267; srcH = 141; srcW = 76;
                int[] bX = {2, 80, 158};
                srcX = bX[blockFrame];
            }

            case TELEPORTING -> {
                srcW = 95; srcH = 151; srcY = 5916; srcX = 2;
            }

            case COMBO_LIGHT, COMBO_HEAVY -> {
                if (activeRoute != null) {
                    drawComboSprite(g2d);
                }
            }

            case SPECIAL_STARTUP, SPECIAL_ACTIVE -> {
                srcW = 117; srcH = 161; srcY = 3873;
                int spFrame = Math.min(specialTimer / FRAME_SPEED, 5);
                int[] spX = {2, 121, 240, 359, 478, 597};
                srcX = spX[spFrame];
            }

            case ULTIMATE_STARTUP -> {
                srcW = 135; srcH = 139; srcY = 6071;
                int ultFrame = Math.min(specialTimer / FRAME_SPEED, 3);
                int[] ultX = {550, 413, 276, 139};
                srcX = ultX[ultFrame];
            }

            case ULTIMATE_ACTIVE -> {
                srcW = 135; srcH = 139; srcY = 6071;
                srcX = 2; // beam frame
            }

            case CROUCHING -> {
                srcY = 1412; srcH = 165; srcW = 106; srcX = 2;
            }

            case JUMPING -> {
                srcY = 1412; srcH = 165; srcW = 106;
                int[] jumpX = {110, 218, 326, 434, 542, 650, 758};
                srcX = jumpX[Math.min(spriteNum - 1, 6)];
            }

            case FLYING_IDLE -> {
                srcY = 1412; srcH = 165; srcW = 106; srcX = 218;
            }
            case FLYING_FORWARD -> {
                srcY = 8547; srcH = 146; srcW = 109; srcX = 2;
            }
            case FLYING_FORWARD_FULL -> {
                srcY = 8547; srcH = 146; srcW = 109; srcX = 113;
            }
            case FLYING_BACKWARD -> {
                srcY = 8697; srcH = 148; srcW = 96; srcX = 2;
            }
            case FLYING_BACKWARD_FULL -> {
                srcY = 8697; srcH = 148; srcW = 96; srcX = 100;
            }

            case WALKING -> {
                srcY = 884; srcH = 126; srcW = 108;
                int[] walkX = {2, 112, 222, 332, 442, 552};
                srcX = walkX[Math.min(spriteNum - 1, 5)];
            }

            default -> {
                // IDLE — stance base già impostata sopra
            }
        }

        // Flash visivo quando colpito da un light (sovrascrive lo sprite temporaneamente)
        if (lightHitFlash > 0 && !isInState(FighterState.HIT_STUN, FighterState.TUMBLING,
                FighterState.LAUNCHED, FighterState.KO)) {
            srcW = 101; srcH = 137; srcY = 6780; srcX = 105;
        }

        drawFighterSprite(g2d);

        // Kamehameha beam
        if (state == FighterState.ULTIMATE_ACTIVE) {
            drawKamehameha(g2d);
        }

        for (VisualEffect eff : activeEffects) eff.draw(g2d);
        for (KiBlastProjectile blast : activeBlasts) blast.draw(g2d);

        drawPlayerPin(g2d, x + shiftX, drawY, drawW);
        drawUniversalHUD(g2d, "KAMEHAMEHA");

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
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
                srcY = 2251; srcW = 145; srcH = 127;
                int[] xFrames = {2, 149, 296, 443};
                srcX = xFrames[frameIndex];
            }

            // === TERRA: Launcher combo L→L→H (6 frame) ===
            case "light_launcher" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 2685; srcW = 115; srcH = 179;
                int[] xFrames = {2, 119, 236, 353, 470, 587};
                srcX = xFrames[frameIndex];
            }

            // === TERRA: Heavy standalone guard breaker (6 frame) ===
            case "heavy_standalone" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 3131; srcW = 152; srcH = 150;
                int[] xFrames = {2, 156, 310, 464, 618, 772};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Light (4 frame) ===
            case "air_light_1", "air_light_2", "air_light_3" -> {
                frameIndex = Math.min(frameIndex, 3);
                srcY = 3429; srcW = 127; srcH = 146;
                int[] xFrames = {2, 131, 260, 389};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Heavy standalone guard breaker (6 frame) ===
            case "air_heavy_standalone" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 4175; srcW = 145; srcH = 160;
                int[] xFrames = {149, 296, 443, 590, 737, 884};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Launcher — stesso sprite del launcher a terra (6 frame) ===
            case "air_heavy_launcher" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 2685; srcW = 115; srcH = 179;
                int[] xFrames = {2, 119, 236, 353, 470, 587};
                srcX = xFrames[frameIndex];
            }

            // === ARIA: Spike — calcio dall'alto verso il basso (6 frame) ===
            case "air_heavy_spike" -> {
                frameIndex = Math.min(frameIndex, 5);
                srcY = 3579; srcW = 134; srcH = 151;
                int[] xFrames = {2, 138, 274, 410, 546, 682};
                srcX = xFrames[frameIndex];
            }

            // === SURPRISE ATTACK (usa sprite light a terra) ===
            case "surprise" -> {
                frameIndex = Math.min(frameIndex, 3);
                srcY = 2251; srcW = 145; srcH = 127;
                int[] xFrames = {2, 149, 296, 443};
                srcX = xFrames[frameIndex];
            }
        }
    }

    // =============================================
    // CINEMATIC ULTIMATE — SSJ3 Sprite
    // =============================================
    @Override
    public void drawCinematicSprite(Graphics2D g2d) {
        if (spriteSheet == null) return;

        // 12 frame SSJ3: W=160, H=172, Y=5096
        int[] ssj3X = {1784, 1946, 2108, 2270, 2432, 2594, 2756, 812, 650, 488, 974, 1136};
        int ssj3W = 160, ssj3H = 172, ssj3Y = 5096;

        int frame = Math.min(cinematicFrame, 11);

        // Sprite grande centrato sullo schermo + vibrazione
        double cinematicScale = 3.5;
        int dW = (int)(ssj3W * cinematicScale);
        int dH = (int)(ssj3H * cinematicScale);
        int dX = (GamePanel.SCREEN_WIDTH - dW) / 2 + cinematicVibrateX;
        int dY = (GamePanel.SCREEN_HEIGHT - dH) / 2 + 50;

        int centerX = dX + dW / 2;
        int centerY = dY + dH / 2;

        // --- AURA (solo quando cinematicShowAura è true) ---
        if (auraImage != null && cinematicShowAura) {
            int[] aX = {3, 357, 4, 368}, aY = {4, 4, 454, 440};
            int[] aW = {350, 345, 359, 356}, aH = {446, 432, 437, 417};
            int af = (cinematicTimer / 3) % 4;
            double auraScale = cinematicScale * 0.7;
            int aDrawW = (int)(aW[af] * auraScale);
            int aDrawH = (int)(aH[af] * auraScale);
            int aDrawX = centerX - aDrawW / 2;
            // Base dell'aura appena sotto i piedi di Goku
            int feetY = dY + dH;
            int aDrawY = feetY - aDrawH + (int)(42 * cinematicScale);
            g2d.drawImage(auraImage, aDrawX, aDrawY, aDrawX + aDrawW, aDrawY + aDrawH,
                    aX[af], aY[af], aX[af] + aW[af], aY[af] + aH[af], null);
        }

        // --- SSJ3 Sprite ---
        int sX1 = dX, sX2 = dX + dW;
        if (facingRight) { int t = sX1; sX1 = sX2; sX2 = t; }
        g2d.drawImage(spriteSheet, sX1, dY, sX2, dY + dH,
                ssj3X[frame], ssj3Y, ssj3X[frame] + ssj3W, ssj3Y + ssj3H, null);

        // --- ESPLOSIONE davanti a Goku (sprite reale da aura_blue.png) ---
        if (cinematicShowExplosion && auraImage != null) {
            double expProgress = cinematicTimer / 20.0;
            double expScale = cinematicScale * (0.3 + expProgress * 0.7);
            int expW = (int)(211 * expScale);
            int expH = (int)(206 * expScale);
            // Centro dell'esplosione sulle coordinate del corpo (64/160, 96/172 del frame)
            int expCX = facingRight ? dX + dW - (int)(dW * 64.0 / 160) : dX + (int)(dW * 64.0 / 160);
            int expCY = dY + (int)(dH * 96.0 / 172);
            int expX = expCX - expW / 2;
            int expY = expCY - expH / 2;
            float expAlpha = Math.min(1f, (float)expProgress);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, expAlpha));
            g2d.drawImage(auraImage, expX, expY, expX + expW, expY + expH,
                    813, 818, 813 + 211, 818 + 206, null);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }

        // --- CARICA KAMEHAMEHA tra le mani (step 7-10, NON step 11) ---
        int sphereCX = 0, sphereCY = 0;
        boolean sphereVisible = false;
        if (cinematicStep >= 7 && cinematicStep <= 10 && cinematicPhase == 1) {
            sphereVisible = true;
            float progress;
            if (cinematicStep == 7) {
                progress = Math.min(1f, cinematicTimer / 50f);
            } else {
                progress = 1f;
            }

            int baseSize = (int)(30 * cinematicScale * (0.2 + progress * 0.8));
            double pulse = 1.0 + Math.sin(cinematicTimer * 0.3) * 0.1;
            int sphereSize = (int)(baseSize * pulse);

            double handFracX = 0, handFracY = 0;
            switch (cinematicStep) {
                case 7:  handFracX = 127.0 / 160; handFracY = 98.0 / 172; break;
                case 8:  handFracX = 121.0 / 160; handFracY = 99.0 / 172; break;
                case 9:  handFracX = 79.0 / 160;  handFracY = 99.0 / 172; break;
                case 10: handFracX = 87.0 / 160;  handFracY = 94.0 / 172; break;
            }
            if (facingRight) {
                sphereCX = dX + dW - (int)(dW * handFracX);
            } else {
                sphereCX = dX + (int)(dW * handFracX);
            }
            sphereCY = dY + (int)(dH * handFracY);

            // Glow esterno
            int glowSize = (int)(sphereSize * 2.5);
            java.awt.RadialGradientPaint glowPaint = new java.awt.RadialGradientPaint(
                    sphereCX, sphereCY, glowSize / 2f,
                    new float[]{0f, 0.4f, 1f},
                    new java.awt.Color[]{
                            new java.awt.Color(100, 180, 255, (int)(120 * progress)),
                            new java.awt.Color(50, 120, 255, (int)(60 * progress)),
                            new java.awt.Color(0, 50, 150, 0)
                    });
            g2d.setPaint(glowPaint);
            g2d.fillOval(sphereCX - glowSize / 2, sphereCY - glowSize / 2, glowSize, glowSize);

            // Sfera media
            java.awt.RadialGradientPaint spherePaint = new java.awt.RadialGradientPaint(
                    sphereCX, sphereCY, sphereSize / 2f,
                    new float[]{0f, 0.5f, 1f},
                    new java.awt.Color[]{
                            new java.awt.Color(220, 240, 255, (int)(255 * progress)),
                            new java.awt.Color(80, 160, 255, (int)(230 * progress)),
                            new java.awt.Color(30, 80, 220, (int)(100 * progress))
                    });
            g2d.setPaint(spherePaint);
            g2d.fillOval(sphereCX - sphereSize / 2, sphereCY - sphereSize / 2, sphereSize, sphereSize);

            // Nucleo bianco
            int coreSize = (int)(sphereSize * 0.4);
            java.awt.RadialGradientPaint corePaint = new java.awt.RadialGradientPaint(
                    sphereCX, sphereCY, coreSize / 2f,
                    new float[]{0f, 0.6f, 1f},
                    new java.awt.Color[]{
                            new java.awt.Color(255, 255, 255, (int)(255 * progress)),
                            new java.awt.Color(200, 230, 255, (int)(200 * progress)),
                            new java.awt.Color(100, 180, 255, 0)
                    });
            g2d.setPaint(corePaint);
            g2d.fillOval(sphereCX - coreSize / 2, sphereCY - coreSize / 2, coreSize, coreSize);
            g2d.setPaint(null);
            g2d.setColor(java.awt.Color.WHITE);
        }

        // --- LAUNCH BEAM allo step 11 (head alle mani, body fino all'angolo) ---
        if (cinematicStep == 11 && cinematicPhase == 1 && spriteSheet != null) {
            // Mani di Goku SSJ3 frame 11: frac (19/160, 32/172)
            double handFX = 19.0 / 160, handFY = 32.0 / 172;
            int handPX, handPY;
            if (facingRight) {
                handPX = dX + dW - (int)(dW * handFX);
            } else {
                handPX = dX + (int)(dW * handFX);
            }
            handPY = dY + (int)(dH * handFY);

            float launchProgress = Math.min(1f, cinematicTimer / 40f);
            double lScale = cinematicScale * 0.5;

            int lHeadW = (int)(173 * lScale), lHeadH = (int)(143 * lScale);
            int lBodyW = (int)(127 * lScale), lBodyH = (int)(101 * lScale);

            // Punto di ancoraggio della head: frac (11/173, 110/143)
            int anchorOffX = (int)(lHeadW * 11.0 / 173);
            int anchorOffY = (int)(lHeadH * 110.0 / 143);

            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, launchProgress));

            // Direzione diagonale esatta dello schermo
            double ratio = 720.0 / 1280.0;

            // Head posizionata con anchor sul punto delle mani
            int headDrawX, headDrawY;
            if (facingRight) {
                headDrawX = handPX - anchorOffX;
            } else {
                headDrawX = handPX - (lHeadW - anchorOffX);
            }
            headDrawY = handPY - anchorOffY;

            int hx1 = headDrawX, hx2 = headDrawX + lHeadW;
            if (!facingRight) { int t = hx1; hx1 = hx2; hx2 = t; }
            g2d.drawImage(spriteSheet, hx1, headDrawY, hx2, headDrawY + lHeadH,
                    1543, 6254, 1543 + 173, 6254 + 143, null);

            // Body: step con sovrapposizione (triangolo vuoto 23x39 source)
            int lOverlapX = (int)(23 * lScale);
            int lOverlapY = (int)(39 * lScale);
            int bStepX = facingRight ? (lBodyW - lOverlapX) : -(lBodyW - lOverlapX);
            int bStepY = -(lBodyH - lOverlapY);

            // Punto di partenza body: collegato alla head (con sovrapposizione)
            int bStartX = facingRight ? headDrawX + lHeadW - lOverlapX : headDrawX - lBodyW + lOverlapX;
            int bStartY = headDrawY - (lBodyH - lOverlapY);

            int numBodies = (int)(8 * launchProgress);
            for (int i = 0; i < numBodies; i++) {
                int bx = bStartX + bStepX * i;
                int by = bStartY + bStepY * i;
                int b1 = bx, b2 = bx + lBodyW;
                if (!facingRight) { b1 = bx + lBodyW; b2 = bx; }
                g2d.drawImage(spriteSheet, b1, by, b2, by + lBodyH,
                        1805, 6217, 1805 + 127, 6217 + 101, null);
            }

            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }

        // --- FULMINI intorno al personaggio ---
        if (auraImage != null && cinematicShowLightning) {
            java.util.Random rng = new java.util.Random(cinematicTimer * 7L);
            int bodyW, bodyCenter, bodyCenterY;
            if (frame >= 3) {
                // SSJ3: capelli larghi, centrato
                bodyW = (int)(dW * 0.7);
                bodyCenter = centerX;
                bodyCenterY = centerY;
            } else {
                // Pre-SSJ3: coordinate esatte del centro corpo
                double bodyFracX, bodyFracY;
                if (frame <= 1) {
                    bodyFracX = 64.0 / 160; bodyFracY = 104.0 / 172; // step 2, frame 1
                } else {
                    bodyFracX = 65.0 / 160; bodyFracY = 92.0 / 172;  // step 3, frame 2
                }
                bodyW = (int)(dW * 0.5);
                if (facingRight) {
                    bodyCenter = dX + dW - (int)(dW * bodyFracX);
                } else {
                    bodyCenter = dX + (int)(dW * bodyFracX);
                }
                bodyCenterY = dY + (int)(dH * bodyFracY);
            }
            int bodyLeft = bodyCenter - bodyW / 2;
            int minY = (frame >= 3) ? dY - (int)(dH * 0.05) : bodyCenterY - (int)(dH * 0.45);
            int maxY = (frame >= 3) ? dY + (int)(dH * 0.65) : bodyCenterY + (int)(dH * 0.25);
            int numBolts = (frame >= 3) ? 2 + rng.nextInt(2) : 2;
            for (int i = 0; i < numBolts; i++) {
                int boltX = bodyLeft + rng.nextInt(bodyW);
                int boltY = minY + rng.nextInt(maxY - minY);
                double boltScale = cinematicScale * (0.20 + rng.nextDouble() * 0.20);
                if (rng.nextBoolean()) {
                    int bW = (int)(66 * boltScale), bH = (int)(107 * boltScale);
                    g2d.drawImage(auraImage, boltX, boltY, boltX + bW, boltY + bH,
                            129, 895, 129 + 66, 895 + 107, null);
                } else {
                    int bW = (int)(72 * boltScale), bH = (int)(79 * boltScale);
                    g2d.drawImage(auraImage, boltX, boltY, boltX + bW, boltY + bH,
                            199, 894, 199 + 72, 894 + 79, null);
                }
            }
        }

        // --- FULMINI attorno alla sfera kamehameha (1 solo, piccolo, vicino) ---
        if (auraImage != null && sphereVisible && cinematicTimer % 3 == 0) {
            java.util.Random rng2 = new java.util.Random(cinematicTimer * 13L);
            int boltX = sphereCX - (int)(10 * cinematicScale) + rng2.nextInt((int)(20 * cinematicScale));
            int boltY = sphereCY - (int)(10 * cinematicScale) + rng2.nextInt((int)(20 * cinematicScale));
            double boltScale = cinematicScale * (0.10 + rng2.nextDouble() * 0.10);
            int bW = (int)(66 * boltScale), bH = (int)(107 * boltScale);
            g2d.drawImage(auraImage, boltX, boltY, boltX + bW, boltY + bH,
                    129, 895, 129 + 66, 895 + 107, null);
        }
    }

    // =============================================
    // CINEMATIC BEAM SCENE — Kamehameha diagonale
    // =============================================
    @Override
    public void drawCinematicBeamScene(Graphics2D g2d) {
        if (spriteSheet == null) return;

        int W = GamePanel.SCREEN_WIDTH;
        int H = GamePanel.SCREEN_HEIGHT;

        double beamScale = 2.0;
        int headW = (int)(173 * beamScale), headH = (int)(143 * beamScale);
        int bodyW = (int)(127 * beamScale), bodyH = (int)(101 * beamScale);

        // Sovrapposizione body (triangolo vuoto 23x39 source)
        int overlapX = (int)(23 * beamScale);
        int overlapY = (int)(39 * beamScale);
        int bStepX = bodyW - overlapX;
        int bStepY = bodyH - overlapY;

        // Angoli di riferimento: il CENTRO del beam passa esattamente per i corners
        // facingRight: basso-sx (0,H) → alto-dx (W,0)
        // !facingRight: basso-dx (W,H) → alto-sx (0,0)
        int cornerStartX = facingRight ? 0 : W;
        int cornerStartY = H;
        int cornerEndX = facingRight ? W : 0;
        int cornerEndY = 0;

        // Numero totale di segmenti per coprire la diagonale
        int totalSegs = 18;

        // === SUB-FASE 1 (tick 0-60): beam attraversa lo schermo (rallentato) ===
        if (cinematicTimer <= 60) {
            float progress = cinematicTimer / 60f;
            int numSegs = (int)(totalSegs * progress);

            for (int i = 0; i < numSegs; i++) {
                float frac = (float)i / totalSegs;
                int cx = cornerStartX + (int)((cornerEndX - cornerStartX) * frac);
                int cy = cornerStartY + (int)((cornerEndY - cornerStartY) * frac);
                int bx = cx - bodyW / 2;
                int by = cy - bodyH / 2;
                int b1 = bx, b2 = bx + bodyW;
                if (!facingRight) { int t = b1; b1 = b2; b2 = t; }
                g2d.drawImage(spriteSheet, b1, by, b2, by + bodyH,
                        1805, 6217, 1805 + 127, 6217 + 101, null);
            }

            // Head alla punta — flippata H e V, collegata all'ultimo body
            float headFrac = (float)numSegs / totalSegs;
            int hcx = cornerStartX + (int)((cornerEndX - cornerStartX) * headFrac);
            int hcy = cornerStartY + (int)((cornerEndY - cornerStartY) * headFrac);
            int hx = hcx - headW / 2;
            int hy = hcy - headH / 2;
            // Flip H+V
            int h1 = hx, h2 = hx + headW;
            int hv1 = hy, hv2 = hy + headH;
            if (facingRight) { int t = h1; h1 = h2; h2 = t; }
            int tv = hv1; hv1 = hv2; hv2 = tv;
            g2d.drawImage(spriteSheet, h1, hv1, h2, hv2,
                    1543, 6254, 1543 + 173, 6254 + 143, null);
        }

        // === SUB-FASE 2 (tick 60-90): schermo nero + avversario ===
        // === SUB-FASE 3 (tick 90-120): beam sbuca e oltrepassa avversario ===
        if (cinematicTimer > 60) {
            double oppScale = 2.5;
            int oppW = (int)(117 * oppScale);
            int oppH = (int)(130 * oppScale);
            int oppX = (W - oppW) / 2;
            int oppY = (H - oppH) / 2 - 30;

            // Beam arriva dall'angolo (dopo tick 90)
            if (cinematicTimer > 90) {
                float beamProg = Math.min(1f, (cinematicTimer - 90) / 25f);
                int numSegs = (int)(totalSegs * beamProg);

                for (int i = 0; i < numSegs; i++) {
                    float frac = (float)i / totalSegs;
                    int cx = cornerStartX + (int)((cornerEndX - cornerStartX) * frac);
                    int cy = cornerStartY + (int)((cornerEndY - cornerStartY) * frac);
                    int bx = cx - bodyW / 2;
                    int by = cy - bodyH / 2;
                    int b1 = bx, b2 = bx + bodyW;
                    if (!facingRight) { int t = b1; b1 = b2; b2 = t; }
                    g2d.drawImage(spriteSheet, b1, by, b2, by + bodyH,
                            1805, 6217, 1805 + 127, 6217 + 101, null);
                }

                // Head alla punta
                float headFrac = (float)numSegs / totalSegs;
                int hcx = cornerStartX + (int)((cornerEndX - cornerStartX) * headFrac);
                int hcy = cornerStartY + (int)((cornerEndY - cornerStartY) * headFrac);
                int hx = hcx - headW / 2;
                int hy = hcy - headH / 2;
                int h1 = hx, h2 = hx + headW;
                int hv1 = hy, hv2 = hy + headH;
                if (facingRight) { int t = h1; h1 = h2; h2 = t; }
                int tv = hv1; hv1 = hv2; hv2 = tv;
                g2d.drawImage(spriteSheet, h1, hv1, h2, hv2,
                        1543, 6254, 1543 + 173, 6254 + 143, null);

                // Flash quando colpisce
                if (beamProg >= 0.5f && (cinematicTimer / 3) % 2 == 0) {
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(java.awt.Color.WHITE);
                    g2d.fillRect(0, 0, W, H);
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
                }
            }

            // Avversario SOPRA il beam
            float oppAlpha = Math.min(1f, (cinematicTimer - 60) / 10f);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, oppAlpha));
            int oS1 = oppX, oS2 = oppX + oppW;
            if (facingRight) { int t = oS1; oS1 = oS2; oS2 = t; }
            g2d.drawImage(spriteSheet, oS1, oppY, oS2, oppY + oppH,
                    359, 7657, 359 + 117, 7657 + 130, null);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }

        // Glow azzurro
        float glowAlpha = 0.06f + 0.04f * (float)Math.sin(cinematicTimer * 0.4);
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, glowAlpha));
        g2d.setColor(new java.awt.Color(100, 180, 255));
        g2d.fillRect(0, 0, W, H);
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
    }

    // =============================================
    // HELPER — disegno beam Kamehameha
    // =============================================
    private void drawKamehameha(Graphics2D g2d) {
        // Coordinate beam sprite
        int headSrcX = 1542, headSrcY = 6083, headW = 172, headH = 128;
        int bodySrcX = 1724, bodySrcY = 6123, bodyW = 120, bodyH = 48;

        int drawBodyH = (int)(bodyH * scale);
        int drawHeadW = (int)(headW * scale);
        int drawHeadH = (int)(headH * scale);
        int beamY  = drawY + (int)(6 * scale);
        int bodyOffsetY = (drawHeadH - drawBodyH) / 2;
        int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? GamePanel.SCREEN_WIDTH : 0);

        // Lo sprite della testa nativo punta a SINISTRA.
        // Origine: verso in cui spara → flip se facingRight
        // Impatto: verso opposto → flip se !facingRight

        if (facingRight) {
            int startX = x + shiftX + drawW;
            if (targetX - drawHeadW > startX + drawHeadW) {
                // Testa origine (normale: bocca verso destra)
                g2d.drawImage(spriteSheet,
                        startX, beamY, startX + drawHeadW, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                // Body (stretches)
                g2d.drawImage(spriteSheet,
                        startX + drawHeadW, beamY + bodyOffsetY,
                        targetX - drawHeadW, beamY + bodyOffsetY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                // Testa impatto (flippata: bocca verso sinistra)
                g2d.drawImage(spriteSheet,
                        targetX, beamY, targetX - drawHeadW, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        } else {
            int startX = x + shiftX;
            if (targetX + drawHeadW < startX - drawHeadW) {
                // Testa origine (flippata: bocca verso sinistra)
                g2d.drawImage(spriteSheet,
                        startX, beamY, startX - drawHeadW, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
                // Body (stretches)
                g2d.drawImage(spriteSheet,
                        startX - drawHeadW, beamY + bodyOffsetY,
                        targetX + drawHeadW, beamY + bodyOffsetY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                // Testa impatto (normale: bocca verso destra)
                g2d.drawImage(spriteSheet,
                        targetX, beamY, targetX + drawHeadW, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        }
    }
}