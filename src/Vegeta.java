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

        // Scala e dimensioni
        this.scale      = 2.0;
        this.baseWidth  = (int)(48 * scale);
        this.baseHeight = (int)(86 * scale);

        // Universal floor
        int universalFloorY = y + 111;
        this.y       = universalFloorY - this.baseHeight;
        this.groundY = this.y;

        // Fisica
        this.speed        = (int)(4 * scale);
        this.jumpStrength = -12 * scale;
        this.gravity      = 0.5 * scale;

        // Ki
        this.ki            = MAX_KI;
        this.kiBlastKiCost = 70.0;  // Vegeta spara più spesso
        this.kiOnHitReward = 18.0;  // Vegeta guadagna più Ki sui colpi

        // Danni — Vegeta è più aggressivo sui Ki Blast
        this.kiBlastDamage = 12;
        this.specialDamage = 50;

        // Special (Final Flash) — più lenta ma devastante
        this.MAX_SPECIAL_ENERGY = 3600;
        this.SPECIAL_CHARGE     = 65;
        this.SPECIAL_DURATION   = 100;
        this.specialDrainRate   = MAX_SPECIAL_ENERGY / SPECIAL_DURATION;

        // Aura
        this.auraColor = new Color(255, 238, 0);

        // Definisci le combo routes
        this.comboRoutes = defineComboRoutes();
    }

    // =============================================
    // COMBO ROUTES — sequenze di Vegeta
    // Stile più aggressivo e diretto rispetto a Goku
    // =============================================
    @Override
    public ComboRoute[] defineComboRoutes() {
        return new ComboRoute[] {

                // L — jab singolo
                new ComboRoute("vegeta_L",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_jab", 4, 3, 6, 5, 8)
                        },
                        "light_1"
                ),

                // L → L — doppio jab
                new ComboRoute("vegeta_LL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_jab",  4, 3, 6, 5, 8),
                                new AttackData("vegeta_jab2", 4, 3, 6, 6, 8)
                        },
                        "light_2"
                ),

                // L → L → L — triplo jab con calcio finale
                new ComboRoute("vegeta_LLL",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_jab",  4, 3, 6,  5,  8),
                                new AttackData("vegeta_jab2", 4, 3, 6,  6,  8),
                                new AttackData("vegeta_kick", 6, 4, 8, 10, 12)
                        },
                        "light_3"
                ),

                // L → L → H — launcher con smash
                new ComboRoute("vegeta_LLH",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_jab",      4, 3,  6,  5,  8),
                                new AttackData("vegeta_jab2",     4, 3,  6,  6,  8),
                                new AttackData("vegeta_launcher", 7, 4, 12, 16, 22,
                                        false, false, true, false, 0.0, 18.0)
                        },
                        "light_launcher"
                ),

                // H — smash diretto (più potente di Goku)
                new ComboRoute("vegeta_H",
                        new int[]{ ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_smash", 7, 4, 12, 14, 14)
                        },
                        "heavy_1"
                ),

                // H → H — doppio smash
                new ComboRoute("vegeta_HH",
                        new int[]{ ComboRoute.HEAVY, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_smash",  7, 4, 12, 14, 14),
                                new AttackData("vegeta_smash2", 8, 4, 14, 16, 18)
                        },
                        "heavy_2"
                ),

                // Combo aerea L → L → L
                new ComboRoute("vegeta_LLL_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_air_jab",  4, 3, 6,  5,  8),
                                new AttackData("vegeta_air_jab2", 4, 3, 6,  6,  8),
                                new AttackData("vegeta_air_kick", 5, 4, 8, 10, 12)
                        },
                        "air_light_3",
                        false, true, false
                ),

                // Combo aerea L → L → H — spike (sbatte a terra)
                new ComboRoute("vegeta_LLH_air",
                        new int[]{ ComboRoute.LIGHT, ComboRoute.LIGHT, ComboRoute.HEAVY },
                        new AttackData[]{
                                new AttackData("vegeta_air_jab",   4, 3,  6,  5,  8),
                                new AttackData("vegeta_air_jab2",  4, 3,  6,  6,  8),
                                new AttackData("vegeta_air_spike", 7, 4, 12, 14, 20,
                                        false, false, false, true, 0.0, 18.0)
                        },
                        "air_spike",
                        false, true, false
                ),

                // Surprise Attack — overhead unblockable (solo con Aura)
                new ComboRoute("vegeta_surprise",
                        new int[]{ ComboRoute.LIGHT },
                        new AttackData[]{
                                new AttackData("vegeta_overhead", 3, 5, 10, 10, 14,
                                        false, true, false, false, 0.0, 22.0)
                        },
                        "surprise",
                        true, false, false
                )
        };
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
        // Vegeta alza il braccio — sfera sopra la testa
        int handX = facingRight
                ? x + (int)(22 * scale)
                : x + baseWidth - (int)(40 * scale);
        int handY = y - (int)(10 * scale);
        activeEffects.add(new VisualEffect(kiBlastImage, handX, handY,
                new int[]{390}, new int[]{198}, new int[]{62}, new int[]{60},
                7, 0.6 * scale));
    }

    @Override
    protected void fireKiBlastProjectile() {
        int startX = facingRight
                ? x + (int)(55 * scale)
                : x - (int)(15 * scale);
        int startY = y + (int)(20 * scale);
        activeBlasts.add(new KiBlastProjectile(startX, startY,
                facingRight, kiBlastImage, scale));
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
            if (spriteCounter > (state == FighterState.AURA_ACTIVE ? 3 : 5)) {
                spriteNum++;
                if (spriteNum > 3) spriteNum = 1;
                spriteCounter = 0;
            }
        } else if (state == FighterState.JUMPING) {
            spriteCounter++;
            if (spriteCounter > 6) {
                spriteNum++;
                if (spriteNum > 4) spriteNum = 4;
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
        srcX = 9; srcY = 6; srcW = 43; srcH = 75;

        switch (state) {

            case KO -> {
                srcW = 75; srcH = 79; srcY = 784;
                int[] koX = {0, 200, 300, 400, 500, 600, 725};
                srcX = koX[Math.min(endFrame - 1, 6)];
            }

            case WINNER -> {
                srcW = 32; srcH = 80; srcY = 885;
                int[] wX = {12, 53};
                srcX = wX[Math.min(endFrame - 1, 1)];
            }

            case HIT_STUN, TUMBLING -> {
                srcY = 784; srcW = 75; srcH = 79;
                srcX = (hitTimer <= hitstunDuration / 2) ? 0 : 200;
            }

            case CHARGING_KI -> {
                srcW = 39; srcH = 77; srcX = 444; srcY = 627;
                shiftX += (int)(Math.random() * 3) - 1;
            }

            case AURA_ACTIVE -> {
                srcX = 9; srcY = 6; srcW = 43; srcH = 75;
            }

            case BLOCKING -> {
                srcW = 43; srcH = 73; srcX = 11; srcY = 983;
            }

            case BLOCKING_AIR -> {
                srcW = 31; srcH = 76; srcX = 277; srcY = 103;
            }

            case TELEPORTING -> {
                if (isFlying() || prevState == FighterState.JUMPING) {
                    srcW = 41; srcH = 79; srcX = 210; srcY = 6;
                } else {
                    srcW = 43; srcH = 73; srcX = 11; srcY = 10;
                }
            }

            case COMBO_LIGHT, COMBO_HEAVY -> {
                if (activeRoute != null) drawComboSprite(g2d);
            }

            case SPECIAL_STARTUP, SPECIAL_ACTIVE -> {
                srcY = 615; srcW = 67; srcH = 80;
                srcX = (specialTimer <= 7) ? 124 : 220;
            }

            case ULTIMATE_STARTUP -> {
                srcY = 1067; srcW = 53; srcH = 76; srcX = 10;
            }

            case ULTIMATE_ACTIVE -> {
                srcY = 1067; srcW = 53; srcH = 76; srcX = 80;
            }

            case CROUCHING -> {
                srcW = 39; srcH = 83; srcX = 5; srcY = 176;
            }

            case JUMPING -> {
                srcW = 39; srcH = 83;
                int[] jumpX = {50, 90, 130, 170};
                srcX = jumpX[Math.min(spriteNum - 1, 3)];
                srcY = 176;
            }

            case FLYING_IDLE -> {
                srcW = 41; srcH = 79; srcX = 210; srcY = 6;
            }
            case FLYING_FORWARD -> {
                srcW = 41; srcH = 79; srcX = 231; srcY = 190;
            }
            case FLYING_FORWARD_FULL -> {
                srcW = 41; srcH = 79; srcX = 231; srcY = 190;
            }
            case FLYING_BACKWARD -> {
                srcW = 41; srcH = 79; srcX = 186; srcY = 190;
            }
            case FLYING_BACKWARD_FULL -> {
                srcW = 41; srcH = 79; srcX = 186; srcY = 190;
            }

            case WALKING -> {
                if (spriteNum == 1 || spriteNum == 3) {
                    srcW = 39; srcH = 82; srcX = 7; srcY = 89;
                } else {
                    srcW = 39; srcH = 82; srcX = 55; srcY = 89;
                }
            }

            default -> {
                // IDLE — stance base già impostata sopra
            }
        }

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
    // HELPER — sprite della combo in base alla route
    // =============================================
    private void drawComboSprite(Graphics2D g2d) {
        String key     = activeRoute.animationKey;
        AttackData atk = activeRoute.attacks[comboStep];
        boolean isStartup = (attackTimer <= atk.startup);

        switch (key) {
            case "light_1", "light_2", "light_3", "light_launcher" -> {
                if (comboStep == 2 && key.equals("light_launcher")) {
                    srcY = 451; srcW = 41; srcH = 75;
                    srcX = isStartup ? 8 : 70;
                } else {
                    srcY = 274; srcW = 70; srcH = 74;
                    srcX = isStartup ? 4 : 81;
                }
            }
            case "heavy_1", "heavy_2" -> {
                srcY = 451; srcW = 41; srcH = 75;
                srcX = isStartup ? 8 : 70;
            }
            case "air_light_3", "air_spike" -> {
                srcY = 282; srcW = 66; srcH = 72;
                srcX = isStartup ? 208 : 290;
            }
            case "surprise" -> {
                srcY = 274; srcW = 70; srcH = 74;
                srcX = isStartup ? 4 : 81;
            }
        }
    }

    // =============================================
    // HELPER — disegno beam Final Flash
    // =============================================
    private void drawFinalFlash(Graphics2D g2d) {
        int bodySrcX = 323, bodySrcY = 1278, bodyW = 36, bodyH = 22;
        int headSrcX = 362, headSrcY = 1249, headW = 86, headH = 80;
        int drawBodyH  = (int)(bodyH * scale);
        int drawHeadW  = (int)(headW * scale);
        int drawHeadH  = (int)(headH * scale);
        int beamY      = drawY;
        int bodyOffsetY = (drawHeadH - drawBodyH) / 2;
        int targetX    = (beamEndX != -1) ? beamEndX
                : (facingRight ? GamePanel.SCREEN_WIDTH : 0);

        if (facingRight) {
            int startX = x + shiftX + drawW;
            if (targetX - drawHeadW > startX) {
                g2d.drawImage(spriteSheet,
                        startX, beamY + bodyOffsetY,
                        targetX - drawHeadW, beamY + bodyOffsetY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                g2d.drawImage(spriteSheet,
                        targetX - drawHeadW, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet,
                        startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        } else {
            int startX = x + shiftX;
            if (targetX + drawHeadW < startX) {
                g2d.drawImage(spriteSheet,
                        startX, beamY + bodyOffsetY,
                        targetX + drawHeadW, beamY + bodyOffsetY + drawBodyH,
                        bodySrcX, bodySrcY, bodySrcX + bodyW, bodySrcY + bodyH, null);
                g2d.drawImage(spriteSheet,
                        targetX + drawHeadW, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            } else {
                g2d.drawImage(spriteSheet,
                        startX, beamY, targetX, beamY + drawHeadH,
                        headSrcX, headSrcY, headSrcX + headW, headSrcY + headH, null);
            }
        }
    }
}