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

                // L — jab singolo
                new ComboRoute("goku_L",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_jab", 5, 3, 7, 5, 8)
                        },
                        "light_1"
                ),

                // L → L — doppio jab
                new ComboRoute("goku_LL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_jab",  5, 3, 7, 5, 8),
                                new AttackData("goku_jab2", 4, 3, 6, 5, 8)
                        },
                        "light_2"
                ),

                // L → L → L — triplo jab
                new ComboRoute("goku_LLL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_jab",  5, 3, 7, 5,  8),
                                new AttackData("goku_jab2", 4, 3, 6, 5,  8),
                                new AttackData("goku_kick", 6, 4, 8, 8, 10)
                        },
                        "light_3"
                ),

                // L → L → H — launcher
                new ComboRoute("goku_LLH",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_jab",      5, 3,  7,  5,  8),
                                new AttackData("goku_jab2",     4, 3,  6,  5,  8),
                                new AttackData("goku_launcher", 8, 4, 12, 15, 20,
                                        false, false, true, false, 0.0, 15.0)
                        },
                        "light_launcher"
                ),

                // H — smash singolo
                new ComboRoute("goku_H",
                        new int[]{ ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_smash", 7, 4, 10, 12, 12)
                        },
                        "heavy_1"
                ),

                // L → L → L — versione aerea
                new ComboRoute("goku_LLL_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_air_jab",  4, 3, 6, 5,  8),
                                new AttackData("goku_air_jab2", 4, 3, 6, 5,  8),
                                new AttackData("goku_air_kick", 5, 4, 8, 8, 10)
                        },
                        "air_light_3",
                        false, true, false  // requiresAura=false, requiresAir=true
                ),

                // L → L → H — launcher aereo
                new ComboRoute("goku_LLH_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("goku_air_jab",     4, 3,  6,  5,  8),
                                new AttackData("goku_air_jab2",    4, 3,  6,  5,  8),
                                new AttackData("goku_air_smash",   7, 4, 10, 12, 18,
                                        false, false, false, true, 0.0, 15.0)
                        },
                        "air_heavy_launcher",
                        false, true, false
                ),

                // Surprise Attack — L unblockable (solo con Aura)
                new ComboRoute("goku_surprise",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("goku_surprise", 3, 5, 10, 8, 12,
                                        false, true, false, false, 0.0, 20.0)
                        },
                        "surprise",
                        true, false, false  // requiresAura=true
                )
        };
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
        int handX = facingRight ? x + (int)(40 * scale) : x - (int)(10 * scale);
        int handY = y + (int)(25 * scale);
        activeEffects.add(new VisualEffect(kiBlastImage, handX, handY,
                new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60},
                7, 0.6 * scale));
    }

    @Override
    protected void fireKiBlastProjectile() {
        activeBlasts.add(new KiBlastProjectile(
                facingRight ? x + (int)(50 * scale) : x - (int)(10 * scale),
                y + (int)(25 * scale), facingRight, kiBlastImage, scale));
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
            if (spriteCounter > (state == FighterState.AURA_ACTIVE ? 3 : 5)) {
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

        // Default: stance base
        srcX = 455; srcY = 553; srcW = 72; srcH = 163;

        switch (state) {

            case KO -> {
                srcW = 90; srcH = 91; srcY = 1450;
                int[] koX = {0, 187, 300, 400, 505, 600, 710};
                srcX = koX[Math.min(endFrame - 1, 6)];
            }

            case WINNER -> {
                if (isFlying()) {
                    srcW = 40; srcH = 100; srcY = 1642;
                    int[] wfX = {0, 46};
                    srcX = wfX[Math.min(endFrame - 1, 1)];
                } else {
                    srcW = 33; srcH = 90; srcY = 1649;
                    int[] wgX = {89, 128};
                    srcX = wgX[Math.min(endFrame - 1, 1)];
                }
            }

            case HIT_STUN, TUMBLING -> {
                srcY = 1450; srcW = 90; srcH = 91;
                srcX = (hitTimer <= hitstunDuration / 2) ? 0 : 300;
            }

            case CHARGING_KI -> {
                srcW = 42; srcH = 90; srcX = 0; srcY = 821;
                shiftX += (int)(Math.random() * 3) - 1;
            }

            case AURA_ACTIVE -> {
                // Usa la stance ma con aura visiva attiva
                srcX = 455; srcY = 553; srcW = 72; srcH = 163;
            }

            case BLOCKING -> {
                int blockFrame = Math.min(blockActiveTimer / 5, 2);
                srcY = 1015; srcH = 132; srcW = 95;
                int[] bX = {3, 102, 201};
                srcX = bX[blockFrame];
            }

            case BLOCKING_AIR -> {
                int blockFrame = Math.min(blockActiveTimer / 5, 2);
                srcY = 1268; srcH = 140; srcW = 70;
                int[] bX = {7, 81, 163};
                srcX = bX[blockFrame];
            }

            case TELEPORTING -> {
                srcW = 30; srcH = 91; srcY = 1748;
                int[] tX = {3, 38, 72, 111, 147, 185};
                srcX = tX[Math.min(teleportFrame - 1, 5)];
            }

            case COMBO_LIGHT, COMBO_HEAVY -> {
                if (activeRoute != null) {
                    drawComboSprite(g2d);
                }
            }

            case SPECIAL_STARTUP, SPECIAL_ACTIVE -> {
                srcY = 972; srcW = 65; srcH = 84;
                srcX = (specialTimer <= 7) ? 202 : 300;
            }

            case ULTIMATE_STARTUP -> {
                srcY = 1065; srcW = 54; srcH = 77; srcX = 0;
            }

            case ULTIMATE_ACTIVE -> {
                srcY = 1065; srcW = 54; srcH = 77; srcX = 59;
            }

            case CROUCHING -> {
                srcY = 1411; srcH = 162; srcW = 99; srcX = 8;
            }

            case JUMPING -> {
                srcY = 1411; srcH = 162; srcW = 99;
                int[] jumpX = {122, 224, 328, 435, 550, 670, 780};
                srcX = jumpX[Math.min(spriteNum - 1, 6)];
            }

            case FLYING_IDLE -> {
                srcY = 8691; srcH = 156; srcW = 104; srcX = 0;
            }
            case FLYING_FORWARD -> {
                srcY = 8691; srcH = 156; srcW = 104; srcX = 150;
            }
            case FLYING_FORWARD_FULL -> {
                srcY = 8691; srcH = 156; srcW = 104; srcX = 300;
            }
            case FLYING_BACKWARD -> {
                srcY = 8691; srcH = 156; srcW = 104; srcX = 450;
            }
            case FLYING_BACKWARD_FULL -> {
                srcY = 8691; srcH = 156; srcW = 104; srcX = 600;
            }

            case WALKING -> {
                srcY = 885; srcH = 125; srcW = 106;
                int[] walkX = {3, 112, 221, 329, 437, 546};
                srcX = walkX[Math.min(spriteNum - 1, 5)];
            }

            default -> {
                // IDLE — stance base già impostata sopra
            }
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
        AttackData atk = activeRoute.attacks[comboStep];
        boolean isStartup = (attackTimer <= atk.startup);

        switch (key) {
            case "light_1", "light_2", "light_3", "light_launcher" -> {
                // Attacchi leggeri a terra
                if (comboStep == 2 && key.equals("light_launcher")) {
                    // Launcher = calcio alto
                    srcY = 439; srcW = 80; srcH = 89;
                    srcX = isStartup ? 242 : 325;
                } else {
                    // Jab/pugno
                    srcY = 442; srcW = 87; srcH = 85;
                    srcX = isStartup ? 0 : 130;
                }
            }
            case "heavy_1" -> {
                srcY = 439; srcW = 80; srcH = 89;
                srcX = isStartup ? 242 : 325;
            }
            case "air_light_3", "air_heavy_launcher" -> {
                // Attacchi aerei
                srcY = 536; srcW = 65; srcH = 88;
                srcX = isStartup ? 0 : 100;
            }
            case "surprise" -> {
                srcY = 442; srcW = 87; srcH = 85;
                srcX = isStartup ? 0 : 130;
            }
        }
    }

    // =============================================
    // HELPER — disegno beam Kamehameha
    // =============================================
    private void drawKamehameha(Graphics2D g2d) {
        int bodySrcX = 126, bodySrcY = 1069, bodyW = 146, bodyH = 64;
        int headSrcX = 339, headSrcY = 1069, headW = 86,  headH = 64;
        int drawBodyH = (int)(bodyH * scale);
        int drawHeadW = (int)(headW * scale);
        int drawHeadH = (int)(headH * scale);
        int beamY  = drawY + (int)(6 * scale);
        int targetX = (beamEndX != -1) ? beamEndX : (facingRight ? GamePanel.SCREEN_WIDTH : 0);

        if (facingRight) {
            int startX = x + shiftX + drawW;
            if (targetX - drawHeadW > startX) {
                g2d.drawImage(spriteSheet, startX, beamY, targetX - drawHeadW, beamY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                g2d.drawImage(spriteSheet, targetX - drawHeadW, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        } else {
            int startX = x + shiftX;
            if (targetX + drawHeadW < startX) {
                g2d.drawImage(spriteSheet, startX, beamY, targetX + drawHeadW, beamY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                g2d.drawImage(spriteSheet, targetX + drawHeadW, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet, startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        }
    }
}