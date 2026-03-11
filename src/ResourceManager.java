import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ResourceManager {

    private static ResourceManager instance;

    // --- FONT ---
    public Font saiyanFont;
    public Font bangersFont;

    // --- SPRITESHEETS ---
    public BufferedImage gokuSpriteSheet;
    public BufferedImage vegetaSpriteSheet;
    public BufferedImage futureTrunksSpriteSheet;
    public BufferedImage supremeKaiSpriteSheet;
    public BufferedImage brolySpriteSheet;

    // --- ICONE PERSONAGGI ---
    public BufferedImage iconGoku, iconVegeta, iconFutureTrunks, iconSupremeKai, iconBroly;

    // --- RISORSE STAGE (Aggiornate per 16:9 con file unico bg.png) ---
    public BufferedImage canyonBg, canyonIcon;
    public BufferedImage tournamentDayBg, tournamentDayIcon;
    public BufferedImage tournamentSunsetBg, tournamentSunsetIcon;

    // --- MENU E UI ---
    public BufferedImage splashLogo, dbzLogo, fightIcon, koIcon, load1, load2;
    public BufferedImage mainMenuBg, readyIcon, vsIcon, winnerIcon;
    public BufferedImage menuCursor;
    public BufferedImage hudFull;
    public BufferedImage pinP1, pinP2;

    // --- EFFETTI VISIVI (VFX) ---
    public BufferedImage kiblastBlue, kiblastYellow, kiblastRed, kiblastGreen, kiblastPurple, kiblastGray;
    public BufferedImage commonVfx;

    // --- AURE ---
    public BufferedImage auraBlue, auraRed, auraPurple, auraGreen, auraYellow;

    private ResourceManager() {
        loadImages();
    }

    public static ResourceManager getInstance() {
        if (instance == null) instance = new ResourceManager();
        return instance;
    }

    private void loadImages() {
        try {
            // 0A. CARICAMENTO DEL FONT SAIYAN (Tramite InputStream per i JAR)
            java.io.InputStream saiyanStream = getClass().getResourceAsStream("/assets/fonts/SaiyanSans.ttf");
            if (saiyanStream != null) {
                saiyanFont = Font.createFont(Font.TRUETYPE_FONT, saiyanStream);
                System.out.println("Font SaiyanSans caricato con successo!");
            } else {
                System.out.println("Attenzione: File SaiyanSans.ttf non trovato nel classpath!");
            }

            // 0B. CARICAMENTO DEL FONT BANGERS
            java.io.InputStream bangersStream = getClass().getResourceAsStream("/assets/fonts/Bangers.ttf");
            if (bangersStream != null) {
                bangersFont = Font.createFont(Font.TRUETYPE_FONT, bangersStream);
                System.out.println("Font Bangers caricato con successo!");
            } else {
                System.out.println("Attenzione: File Bangers.ttf non trovato nel classpath!");
            }

            // 1. CARICAMENTO SPRITESHEETS
            gokuSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/characters/spritesheets/goku_spritesheet.png"));
            vegetaSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/characters/spritesheets/vegeta_spritesheet.png"));
            futureTrunksSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/characters/spritesheets/future_trunks_spritesheet.png"));
            supremeKaiSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/characters/spritesheets/supreme_kai_spritesheet.png"));
            brolySpriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/characters/spritesheets/broly_spritesheet.png"));

            // 2. CARICAMENTO ICONE PERSONAGGI
            iconGoku = ImageIO.read(getClass().getResourceAsStream("/assets/characters/characters_icons/icon_goku.png"));
            iconVegeta = ImageIO.read(getClass().getResourceAsStream("/assets/characters/characters_icons/icon_vegeta.png"));
            iconFutureTrunks = ImageIO.read(getClass().getResourceAsStream("/assets/characters/characters_icons/icon_future_trunks.png"));
            iconSupremeKai = ImageIO.read(getClass().getResourceAsStream("/assets/characters/characters_icons/icon_supreme_kai.png"));
            iconBroly = ImageIO.read(getClass().getResourceAsStream("/assets/characters/characters_icons/icon_broly.png"));

            // 3. CARICAMENTO DEI 3 STAGE
            canyonBg = ImageIO.read(getClass().getResourceAsStream("/assets/stages/canyon_sunset/bg.png"));
            canyonIcon = ImageIO.read(getClass().getResourceAsStream("/assets/stages/canyon_sunset/icon.png"));

            tournamentDayBg = ImageIO.read(getClass().getResourceAsStream("/assets/stages/tournament_day/bg.png"));
            tournamentDayIcon = ImageIO.read(getClass().getResourceAsStream("/assets/stages/tournament_day/icon.png"));

            tournamentSunsetBg = ImageIO.read(getClass().getResourceAsStream("/assets/stages/tournament_sunset/bg.png"));
            tournamentSunsetIcon = ImageIO.read(getClass().getResourceAsStream("/assets/stages/tournament_sunset/icon.png"));

            // 4. CARICAMENTO MENU E UI ORIGINALI
            splashLogo = ImageIO.read(getClass().getResourceAsStream("/assets/menu/splash_logo.png"));
            dbzLogo = ImageIO.read(getClass().getResourceAsStream("/assets/menu/dbz_logo.png"));
            fightIcon = ImageIO.read(getClass().getResourceAsStream("/assets/menu/fight_icon.png"));
            koIcon = ImageIO.read(getClass().getResourceAsStream("/assets/menu/ko_icon.png"));
            load1 = ImageIO.read(getClass().getResourceAsStream("/assets/menu/load_1.png"));
            load2 = ImageIO.read(getClass().getResourceAsStream("/assets/menu/load_2.png"));
            mainMenuBg = ImageIO.read(getClass().getResourceAsStream("/assets/menu/main_menu_bg.png"));
            readyIcon = ImageIO.read(getClass().getResourceAsStream("/assets/menu/ready_icon.png"));
            vsIcon = ImageIO.read(getClass().getResourceAsStream("/assets/menu/vs_icon.png"));
            winnerIcon = ImageIO.read(getClass().getResourceAsStream("/assets/menu/winner_icon.png"));
            menuCursor = ImageIO.read(getClass().getResourceAsStream("/assets/menu/cursor.png"));
            hudFull = ImageIO.read(getClass().getResourceAsStream("/assets/menu/hud.png"));
            pinP1 = ImageIO.read(getClass().getResourceAsStream("/assets/menu/pin_player_one.png"));
            pinP2 = ImageIO.read(getClass().getResourceAsStream("/assets/menu/pin_player_two.png"));

            // 5. CARICAMENTO EFFETTI VISIVI (VFX)
            kiblastBlue = ImageIO.read(getClass().getResourceAsStream("/assets/effects/kiblast_blue.png"));
            kiblastYellow = ImageIO.read(getClass().getResourceAsStream("/assets/effects/kiblast_yellow.png"));
            kiblastRed = ImageIO.read(getClass().getResourceAsStream("/assets/effects/kiblast_red.png"));
            kiblastGreen = ImageIO.read(getClass().getResourceAsStream("/assets/effects/kiblast_green.png"));
            kiblastPurple = ImageIO.read(getClass().getResourceAsStream("/assets/effects/kiblast_purple.png"));
            kiblastGray = ImageIO.read(getClass().getResourceAsStream("/assets/effects/kiblast_gray.png"));

            commonVfx = ImageIO.read(getClass().getResourceAsStream("/assets/effects/common_vfx.png"));

            // CARICAMENTO AURE
            auraBlue = ImageIO.read(getClass().getResourceAsStream("/assets/effects/aura_blue.png"));
            auraYellow = ImageIO.read(getClass().getResourceAsStream("/assets/effects/aura_yellow.png"));
            auraRed = ImageIO.read(getClass().getResourceAsStream("/assets/effects/aura_red.png"));
            auraGreen = ImageIO.read(getClass().getResourceAsStream("/assets/effects/aura_green.png"));
            auraPurple = ImageIO.read(getClass().getResourceAsStream("/assets/effects/aura_purple.png"));

            System.out.println("Risorse caricate con successo (Formato JAR-Ready)!");

        } catch (Exception e) {
            System.err.println("ERRORE: Impossibile trovare uno o più file immagine o font.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}