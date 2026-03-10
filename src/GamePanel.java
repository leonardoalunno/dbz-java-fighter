import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;

public class GamePanel extends JPanel implements Runnable {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;

    int FPS = 60;
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();
    ResourceManager rm = ResourceManager.getInstance();

    public int gameState = -1; // -1: Splash, 0: Loading, 1: Main Menu...
    public int stateTimer = 0;

    // --- MAIN MENU ---
    private int mainMenuOption = 0;

    // --- LOADING ---
    private int loadSpriteFrame = 1;

    // --- BATTLE PHASES ---
    public int battlePhase = 0;

    // --- MENU SELEZIONE ---
    private int p1Cursor = 0, p2Cursor = 4;
    private boolean p1Ready = false, p2Ready = false;

    // Nomi corretti senza punti
    private String[] charNames = {"Goku", "Vegeta", "Trunks", "Broly", "Sup Kai"};

    private int stageCursor = 0;
    private String[] stageNames = {"Canyon Sunset", "Tournament Day", "Tournament Sunset"};

    private int menuCooldown = 0;
    private final int COOLDOWN_TIME = 12;

    private Fighter player1, player2;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void update() {
        if (menuCooldown > 0) menuCooldown--;

        switch(gameState) {
            case -1: // SPLASH SCREEN
                stateTimer++;
                // Dopo 3 secondi (180 frame) passa al Loading
                if (stateTimer > 180) {
                    gameState = 0;
                    stateTimer = 0;
                }
                break;

            case 0: // LOADING
                stateTimer++;
                if (stateTimer % 15 == 0) {
                    loadSpriteFrame = (loadSpriteFrame == 1) ? 2 : 1;
                }
                if (stateTimer > 180) {
                    gameState = 1;
                    stateTimer = 0;
                }
                break;

            case 1: // MAIN MENU
                if (menuCooldown == 0) {
                    // Scorre GIÙ: accetta sia 'S' (p1_down) sia 'Freccia Giù' (p2_down)
                    if (keyH.p1_down || keyH.p2_down) {
                        mainMenuOption = (mainMenuOption + 1) % 5;
                        menuCooldown = COOLDOWN_TIME;
                    }
                    // Scorre SÙ: accetta sia 'W' (p1_up) sia 'Freccia Sù' (p2_up)
                    if (keyH.p1_up || keyH.p2_up) {
                        mainMenuOption = (mainMenuOption + 4) % 5;
                        menuCooldown = COOLDOWN_TIME;
                    }
                    // SELEZIONA: accetta sia 'F' (p1_punch) sia 'INVIO' (enterPressed)
                    if (keyH.p1_punch || keyH.enterPressed) {
                        if (mainMenuOption == 0) {
                            gameState = 2; // Z BATTLE VS
                        } else if (mainMenuOption == 2) {
                            gameState = 6; // COMMANDS
                        } else if (mainMenuOption == 3) {
                            gameState = 7; // CREDITS
                        } else if (mainMenuOption == 4) {
                            System.exit(0); // Exit
                        }
                        menuCooldown = COOLDOWN_TIME;
                    }
                }
                break;

            case 2: // CHAR SELECT
                updateCharacterMenu();
                break;

            case 3: // STAGE SELECT
                updateStageMenu();
                break;

            case 4: // VS SCREEN
                stateTimer++;
                if (stateTimer > 180) {
                    gameState = 5;
                    battlePhase = 0;
                    stateTimer = 0;
                }
                break;

            case 5: // BATTLE
                if (battlePhase == 0 || battlePhase == 1) {
                    stateTimer++;
                    if (battlePhase == 0 && stateTimer > 60) { battlePhase = 1; stateTimer = 0; }
                    if (battlePhase == 1 && stateTimer > 40) { battlePhase = 2; stateTimer = 0; }
                }

                // --- LA MAGIA È QUI: Aggiorniamo i personaggi in TUTTE le fasi (2, 3 e 4) ---
                if (battlePhase >= 2 && player1 != null && player2 != null) {
                    player1.update(keyH, player2);
                    player2.update(keyH, player1);
                }

                if (battlePhase == 2) {
                    // Controllo del K.O. solo durante il combattimento attivo
                    if (player1.hp <= 0 || player2.hp <= 0) {
                        battlePhase = 3;
                        stateTimer = 0;
                    }
                }
                else if (battlePhase == 3) {
                    // FASE 3: Mostra l'icona K.O.
                    // Mentre il timer scorre, i personaggi dietro continuano ad animarsi!
                    stateTimer++;
                    if (stateTimer > 120) { // Aspetta 2 secondi
                        battlePhase = 4;
                    }
                }
                else if (battlePhase == 4) {
                    // FASE 4: Schermata Vincitore
                    if (keyH.p1_block || keyH.p2_block) {
                        gameState = 1; // Ritorna al Main Menu
                        menuCooldown = COOLDOWN_TIME;

                        // Resetta le variabili per la prossima partita
                        p1Ready = false;
                        p2Ready = false;
                        player1 = null;
                        player2 = null;
                    }
                }
                break;

            case 6: // COMMANDS
            case 7: // CREDITS
                if (menuCooldown == 0 && (keyH.p1_punch || keyH.p1_block)) {
                    gameState = 1;
                    menuCooldown = COOLDOWN_TIME;
                }
                break;
        }
    }

    private void updateCharacterMenu() {
        // --- LOGICA ANNULLA / TORNA AL MENU ---
        // Se nessuno è pronto e viene premuto il tasto Parata (Back), torna al Main Menu
        if (!p1Ready && !p2Ready && menuCooldown == 0) {
            if (keyH.p1_block || keyH.p2_block) {
                gameState = 1; // Torna al Main Menu
                menuCooldown = COOLDOWN_TIME;
                return; // Esce subito per evitare altri input
            }
        }

        // --- PLAYER 1 SELECTION ---
        if (!p1Ready && menuCooldown == 0) {
            if (keyH.p1_right) { p1Cursor = (p1Cursor + 1) % 5; menuCooldown = COOLDOWN_TIME; }
            if (keyH.p1_left) { p1Cursor = (p1Cursor + 4) % 5; menuCooldown = COOLDOWN_TIME; }
            if (keyH.p1_punch) { p1Ready = true; menuCooldown = COOLDOWN_TIME; }
        }
        // Annulla scelta P1 (già presente, ma assicuriamoci sia coerente)
        if (p1Ready && keyH.p1_block && menuCooldown == 0) {
            p1Ready = false;
            menuCooldown = COOLDOWN_TIME;
        }

        // --- PLAYER 2 SELECTION ---
        if (!p2Ready && menuCooldown == 0) {
            if (keyH.p2_right) { p2Cursor = (p2Cursor + 1) % 5; menuCooldown = COOLDOWN_TIME; }
            if (keyH.p2_left) { p2Cursor = (p2Cursor + 4) % 5; menuCooldown = COOLDOWN_TIME; }
            if (keyH.p2_punch) { p2Ready = true; menuCooldown = COOLDOWN_TIME; }
        }
        // Annulla scelta P2
        if (p2Ready && keyH.p2_block && menuCooldown == 0) {
            p2Ready = false;
            menuCooldown = COOLDOWN_TIME;
        }

        if (p1Ready && p2Ready) {
            gameState = 3;
            menuCooldown = 20;
        }
    }

    private void updateStageMenu() {
        if (menuCooldown == 0) {
            // --- TORNA A CHARACTER SELECT ---
            if (keyH.p1_block || keyH.p2_block) {
                gameState = 2; // Torna a Char Select
                p1Ready = false; // Resetta lo stato di pronto per permettere modifiche
                p2Ready = false;
                menuCooldown = COOLDOWN_TIME;
                return;
            }

            if (keyH.p1_right) { stageCursor = (stageCursor + 1) % 3; menuCooldown = COOLDOWN_TIME; }
            if (keyH.p1_left) { stageCursor = (stageCursor + 2) % 3; menuCooldown = COOLDOWN_TIME; }
            if (keyH.p1_punch) {
                initBattle();
                gameState = 4;
                stateTimer = 0;
            }
        }
    }

    private void initBattle() {
        int startDistance = 100; // Distanza dai bordi
        int charWidth = 48;      // Larghezza base di Goku

        // Player 1: inizia a 200px da sinistra
        if (p1Cursor == 0) {
            player1 = new Goku(startDistance, 480, 1);
        } else {
            player1 = new Goku(startDistance, 480, 1);
        }

        // Player 2: deve finire a 200px da destra
        // Calcolo: 800 (schermo) - 200 (distanza) - 48 (larghezza personaggio) = 552
        int p2StartX = SCREEN_WIDTH - startDistance - charWidth;

        if (p2Cursor == 0) {
            player2 = new Goku(p2StartX, 480, 2);
        } else {
            player2 = new Goku(p2StartX, 480, 2);
        }
    }

    // --- HELPER METHOD FONT SAIYAN ---
    private void setCustomFont(Graphics2D g2d, float size) {
        if (rm.saiyanFont != null) {
            g2d.setFont(rm.saiyanFont.deriveFont(Font.PLAIN, size));
        } else {
            g2d.setFont(new Font("Arial", Font.BOLD, (int)size));
        }
    }

    // --- HELPER METHOD FONT BANGERS ---
    private void setBangersFont(Graphics2D g2d, float size) {
        if (rm.bangersFont != null) {
            g2d.setFont(rm.bangersFont.deriveFont(Font.PLAIN, size));
        } else {
            g2d.setFont(new Font("Arial", Font.BOLD, (int)size));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        switch(gameState) {
            case -1: drawSplash(g2d); break;
            case 0: drawLoading(g2d); break;
            case 1: drawMainMenu(g2d); break;
            case 2: drawCharacterMenu(g2d); break;
            case 3: drawStageMenu(g2d); break;
            case 4: drawVsScreen(g2d); break;
            case 5: drawBattle(g2d); break;
            case 6: drawCommands(g2d); break;
            case 7: drawCredits(g2d); break;
        }
        g2d.dispose();
    }

    private void drawSplash(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (rm.splashLogo != null) {
            float alpha = 1.0f; // Trasparenza base

            // Primi 60 frame (1 secondo): Fade In (Appare)
            if (stateTimer < 60) {
                alpha = (float) stateTimer / 60.0f;
            }
            // Ultimi 60 frame (1 secondo): Fade Out (Scompare)
            else if (stateTimer > 120) {
                alpha = 1.0f - ((float)(stateTimer - 120) / 60.0f);
            }

            // Sicurezza per evitare errori di calcolo in Java
            if (alpha < 0.0f) alpha = 0.0f;
            if (alpha > 1.0f) alpha = 1.0f;

            // Applica la trasparenza
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Disegna il logo centrato
            int logoW = 500;
            int logoH = (logoW * rm.splashLogo.getHeight()) / rm.splashLogo.getWidth();
            int logoX = (SCREEN_WIDTH - logoW) / 2;
            int logoY = (SCREEN_HEIGHT - logoH) / 2;

            g2d.drawImage(rm.splashLogo, logoX, logoY, logoW, logoH, null);

            // IMPORTANTE: Resetta la trasparenza al 100% per non rendere invisibile il resto del gioco!
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private void drawLoading(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        BufferedImage loadSprite = (loadSpriteFrame == 1) ? rm.load1 : rm.load2;
        if (loadSprite != null) {
            double scale = 2.5;
            int spriteW = (int)(loadSprite.getWidth() * scale);
            int spriteH = (int)(loadSprite.getHeight() * scale);
            int spriteX = (SCREEN_WIDTH - spriteW) / 2;
            int spriteY = (SCREEN_HEIGHT - spriteH) / 2 - 50;
            g2d.drawImage(loadSprite, spriteX, spriteY, spriteW, spriteH, null);
        }

        g2d.setColor(Color.WHITE);
        setCustomFont(g2d, 40f);

        int textX = 320;
        int textY = 530;
        g2d.drawString("LOADING", textX, textY);

        int numDots = (stateTimer / 15) % 4;
        int textWidth = g2d.getFontMetrics().stringWidth("LOADING");
        int dotStartX = textX + textWidth + 8;
        int dotSize = 8;
        int dotSpacing = 15;

        for (int i = 0; i < numDots; i++) {
            g2d.fillOval(dotStartX + (i * dotSpacing), textY - dotSize, dotSize, dotSize);
        }
    }

    private void drawMainMenu(Graphics2D g2d) {
        // 1. Sfondo e filtro scuro
        if (rm.mainMenuBg != null) {
            g2d.drawImage(rm.mainMenuBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // 2. Logo DBZ
        if (rm.dbzLogo != null) {
            int logoW = 400;
            int logoH = (logoW * rm.dbzLogo.getHeight()) / rm.dbzLogo.getWidth();
            g2d.drawImage(rm.dbzLogo, (SCREEN_WIDTH - logoW)/2, 30, logoW, logoH, null);
        }

        // 3. Voci del Menu scritte con il Font
        String[] menuOptions = {"Z BATTLE VS", "TRAINING", "COMMANDS", "CREDITS", "EXIT"};

        int startY = 270;  // Altezza di partenza
        int spacing = 60;  // Spazio tra una voce e l'altra

        setCustomFont(g2d, 50f); // Font Saiyan bello grande

        for (int i = 0; i < menuOptions.length; i++) {
            String text = menuOptions[i];

            // Calcolo matematico per il centro esatto dello schermo
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            int textX = (SCREEN_WIDTH - textWidth) / 2;
            int textY = startY + (i * spacing);

            // Se l'opzione è quella selezionata...
            if (i == mainMenuOption) {
                g2d.setColor(Color.YELLOW); // Diventa Gialla

                // Disegna il cursore a destra della parola
                if (rm.menuCursor != null) {
                    int cSize = 30;
                    int cx = textX + textWidth + 15;
                    int cy = textY - (cSize / 2) - 15; // Centrato rispetto all'altezza del testo
                    g2d.drawImage(rm.menuCursor, cx, cy, cSize, cSize, null);
                }
            } else {
                g2d.setColor(Color.WHITE); // Altrimenti resta Bianca
            }

            g2d.drawString(text, textX, textY);
        }
    }

    private void drawCharacterMenu(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 50));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2d.setColor(Color.YELLOW);
        setCustomFont(g2d, 60f);
        g2d.drawString("CHARACTER SELECT", 170, 100);

        // Array icone aggiornato: Broly è al quarto posto, Sup Kai al quinto
        BufferedImage[] icons = {rm.iconGoku, rm.iconVegeta, rm.iconFutureTrunks, rm.iconBroly, rm.iconSupremeKai};

        int size = 90; // Icone giganti! (prima era 60)
        int spacing = 120; // Aumentato lo spazio tra di loro
        // Calcolo matematico per centrare le 5 icone perfettamente nello schermo
        int startX = (SCREEN_WIDTH - (spacing * 4 + size)) / 2;
        int startY = 230; // Leggermente rialzato per far respirare il layout

        for(int i = 0; i < 5; i++) {
            int x = startX + (i * spacing);
            if (icons[i] != null) g2d.drawImage(icons[i], x, startY, size, size, null);

            g2d.setColor(Color.WHITE);
            setCustomFont(g2d, 22f);
            int textWidth = g2d.getFontMetrics().stringWidth(charNames[i]);
            int textX = x + (size / 2) - (textWidth / 2);
            // Nome distanziato in modo dinamico in base alla grandezza dell'icona
            g2d.drawString(charNames[i], textX, startY + size + 35);
        }

        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(Color.RED);
        int p1X = startX + (p1Cursor * spacing);
        g2d.drawRect(p1X - 5, startY - 5, size + 10, size + 10);

        if(p1Ready) {
            setBangersFont(g2d, 26f);
            int p1Width = g2d.getFontMetrics().stringWidth("P1 READY");
            int p1TextX = p1X + (size / 2) - (p1Width / 2);
            g2d.drawString("P1 READY", p1TextX, startY - 15);
        }

        g2d.setColor(new Color(50, 150, 255));
        int p2X = startX + (p2Cursor * spacing);
        if (p1Cursor == p2Cursor) g2d.drawRect(p2X - 10, startY - 10, size + 20, size + 20);
        else g2d.drawRect(p2X - 5, startY - 5, size + 10, size + 10);

        if(p2Ready) {
            setBangersFont(g2d, 26f);
            int p2Width = g2d.getFontMetrics().stringWidth("P2 READY");
            int p2TextX = p2X + (size / 2) - (p2Width / 2);
            g2d.drawString("P2 READY", p2TextX, startY - 15);
        }
    }

    private void drawStageMenu(Graphics2D g2d) {
        g2d.setColor(new Color(20, 20, 30));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Titolo Giallo
        g2d.setColor(Color.YELLOW);
        setCustomFont(g2d, 60f);
        int titleWidth = g2d.getFontMetrics().stringWidth("STAGE SELECT");
        g2d.drawString("STAGE SELECT", (SCREEN_WIDTH - titleWidth) / 2, 120);

        BufferedImage[] stageIcons = {rm.canyonIcon, rm.tournamentDayIcon, rm.tournamentSunsetIcon};

        // --- MODIFICA: ICONE QUADRATE ---
        int iconSize = 120; // Dimensione fissa per W e H
        int spacing = 200;  // Spazio tra i centri delle icone
        int startX = (SCREEN_WIDTH - (spacing * 2 + iconSize)) / 2;
        int y = 240; // Leggermente più su per bilanciare il testo in basso

        for (int i = 0; i < 3; i++) {
            int x = startX + (i * spacing);

            // Disegno l'immagine quadrata
            if (stageIcons[i] != null) {
                g2d.drawImage(stageIcons[i], x, y, iconSize, iconSize, null);
            }

            if (i == stageCursor) {
                // Bordo giallo per la selezione (quadrato)
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(5));
                g2d.drawRect(x - 8, y - 8, iconSize + 16, iconSize + 16);

                // Nome dello stage
                g2d.setColor(Color.WHITE);
                setCustomFont(g2d, 35f);
                int textWidth = g2d.getFontMetrics().stringWidth(stageNames[i]);
                int textX = (SCREEN_WIDTH - textWidth) / 2;
                g2d.drawString(stageNames[i], textX, 480);
            } else {
                // Bordo grigio scuro per gli altri
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(x - 8, y - 8, iconSize + 16, iconSize + 16);
            }
        }
    }

    private void drawVsScreen(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Array icone aggiornato con Broly al posto giusto!
        BufferedImage[] icons = {rm.iconGoku, rm.iconVegeta, rm.iconFutureTrunks, rm.iconBroly, rm.iconSupremeKai};

        int iconSize = 150;
        int p1X = 150, p1Y = 220;
        int p2X = 500, p2Y = 220;

        // Impostiamo il bordo bianco (4 pixel di spessore)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(4));

        // DISEGNO P1 (Bordo e Icona)
        if (icons[p1Cursor] != null) {
            g2d.drawRect(p1X - 4, p1Y - 4, iconSize + 8, iconSize + 8);
            g2d.drawImage(icons[p1Cursor], p1X, p1Y, iconSize, iconSize, null);
        }

        // DISEGNO P2 (Bordo e Icona)
        if (icons[p2Cursor] != null) {
            g2d.drawRect(p2X - 4, p2Y - 4, iconSize + 8, iconSize + 8);
            g2d.drawImage(icons[p2Cursor], p2X, p2Y, iconSize, iconSize, null);
        }

        // --- NOMI SOPRA LE ICONE (Font Saiyan) ---
        setCustomFont(g2d, 40f);
        g2d.setColor(Color.WHITE);

        String nameP1 = charNames[p1Cursor];
        int nameP1Width = g2d.getFontMetrics().stringWidth(nameP1);
        int nameP1X = p1X + (iconSize / 2) - (nameP1Width / 2); // Centrato matematicamente rispetto all'icona
        g2d.drawString(nameP1, nameP1X, p1Y - 20);

        String nameP2 = charNames[p2Cursor];
        int nameP2Width = g2d.getFontMetrics().stringWidth(nameP2);
        int nameP2X = p2X + (iconSize / 2) - (nameP2Width / 2);
        g2d.drawString(nameP2, nameP2X, p2Y - 20);

        // Logo VS centrale
        if (rm.vsIcon != null) {
            int vsTargetW = 120;
            int vsTargetH = (vsTargetW * rm.vsIcon.getHeight()) / rm.vsIcon.getWidth();
            int vsX = (SCREEN_WIDTH - vsTargetW) / 2;
            int vsY = 220 + (iconSize / 2) - (vsTargetH / 2);

            g2d.drawImage(rm.vsIcon, vsX, vsY, vsTargetW, vsTargetH, null);
        }
    }

    private void drawBattle(Graphics2D g2d) {
        BufferedImage sky = null, floor = null;
        switch(stageCursor) {
            case 0: sky = rm.canyonSky; floor = rm.canyonFloor; break;
            case 1: sky = rm.tournamentDaySky; floor = rm.tournamentDayFloor; break;
            case 2: sky = rm.tournamentSunsetSky; floor = rm.tournamentSunsetFloor; break;
        }

        if (sky != null) g2d.drawImage(sky, 0, 0, SCREEN_WIDTH, 300, null);
        if (floor != null) g2d.drawImage(floor, 0, 300, SCREEN_WIDTH, 300, null);

        if (player1 != null) player1.draw(g2d);
        if (player2 != null) player2.draw(g2d);

        BufferedImage currentStatusIcon = null;
        int targetH = 100;

        if (battlePhase == 0) {
            currentStatusIcon = rm.readyIcon;
        }
        else if (battlePhase == 1) {
            currentStatusIcon = rm.fightIcon;
        }
        else if (battlePhase == 3) {
            currentStatusIcon = rm.koIcon; // Mostra l'icona K.O.
        }

        // Disegna l'icona corrente (Ready, Fight o KO)
        if (currentStatusIcon != null) {
            int drawW = (targetH * currentStatusIcon.getWidth()) / currentStatusIcon.getHeight();
            int drawX = (SCREEN_WIDTH - drawW) / 2;
            int drawY = 220;
            g2d.drawImage(currentStatusIcon, drawX, drawY, drawW, targetH, null);
        }

        // --- NUOVO: SCHERMATA VINCITORE (Fase 4) ---
        if (battlePhase == 4) {
            // 1. Oscura la schermata con un nero semi-trasparente (150 su 255)
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            // 2. Determina chi ha vinto e imposta Testo + Colore
            String winnerText;
            Color winnerColor;

            if (player2.hp <= 0) {
                winnerText = "PLAYER ONE WINS";
                winnerColor = Color.RED; // Stesso rosso del cursore P1
            } else {
                winnerText = "PLAYER TWO WINS";
                winnerColor = new Color(50, 150, 255); // Stesso azzurro del cursore P2
            }

            // 3. Stampa la scritta della vittoria al centro
            g2d.setColor(winnerColor);
            setCustomFont(g2d, 70f);
            int textX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth(winnerText)) / 2;
            g2d.drawString(winnerText, textX, 250);

            // 4. Scritta grigia per tornare al menu (uguale a Credits/Commands)
            g2d.setColor(Color.GRAY);
            setCustomFont(g2d, 25f);
            String returnText = "Press BLOCK to return to Menu";
            int retX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth(returnText)) / 2;
            g2d.drawString(returnText, retX, 560);
        }
    }

    private void drawCommands(Graphics2D g2d) {
        if (rm.mainMenuBg != null) {
            g2d.drawImage(rm.mainMenuBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        } else {
            g2d.setColor(new Color(20, 20, 40));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        g2d.setColor(Color.YELLOW);
        setCustomFont(g2d, 70f);
        g2d.drawString("COMMANDS", 270, 80);

        int startY = 140;
        int spacing = 35;
        int p1X = 40;
        int p2X = 440;

        // --- P1 ---
        g2d.setColor(Color.CYAN);
        setCustomFont(g2d, 40f);
        g2d.drawString("PLAYER ONE", p1X, startY);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("W, A, S, D - Move / Jump", p1X, startY + spacing);
        g2d.drawString("W, A, S, D (Double Tap) - Teleport", p1X, startY + spacing * 2);
        g2d.drawString("F - Punch / Smash / Select", p1X, startY + spacing * 3);
        g2d.drawString("R - Kick", p1X, startY + spacing * 4);
        g2d.drawString("E - Ki Blast", p1X, startY + spacing * 5);
        g2d.drawString("Z - Special Attack", p1X, startY + spacing * 6);
        g2d.drawString("V - Fly", p1X, startY + spacing * 7);
        g2d.drawString("X - Charge Aura", p1X, startY + spacing * 8);
        g2d.drawString("Q - Block", p1X, startY + spacing * 9);

        // --- P2 ---
        g2d.setColor(Color.RED);
        setCustomFont(g2d, 40f);
        g2d.drawString("PLAYER TWO", p2X, startY);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Arrows - Move / Jump", p2X, startY + spacing);
        g2d.drawString("Arrows (Double Tap) - Teleport", p2X, startY + spacing * 2);
        g2d.drawString("L - Punch / Smash / Select", p2X, startY + spacing * 3);
        g2d.drawString("P - Kick", p2X, startY + spacing * 4);
        g2d.drawString("O - Ki Blast", p2X, startY + spacing * 5);
        g2d.drawString("K - Special Attack", p2X, startY + spacing * 6);
        g2d.drawString("N - Fly", p2X, startY + spacing * 7);
        g2d.drawString("M - Charge Aura", p2X, startY + spacing * 8);
        g2d.drawString("U - Block", p2X, startY + spacing * 9);

        g2d.setColor(Color.GRAY);
        setCustomFont(g2d, 25f);
        g2d.drawString("Press BLOCK to return to Menu", 250, 560);
    }

    private void drawCredits(Graphics2D g2d) {
        if (rm.mainMenuBg != null) {
            g2d.drawImage(rm.mainMenuBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        } else {
            g2d.setColor(new Color(40, 20, 20));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        int textX;

        g2d.setColor(Color.YELLOW);
        setCustomFont(g2d, 80f);
        textX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("CREDITS")) / 2;
        g2d.drawString("CREDITS", textX, 150);

        g2d.setColor(Color.WHITE);
        setCustomFont(g2d, 40f);
        textX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Developed by")) / 2;
        g2d.drawString("Developed by", textX, 250);

        g2d.setColor(Color.CYAN);
        setCustomFont(g2d, 50f);
        textX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Leonardo Alunno")) / 2;
        g2d.drawString("Leonardo Alunno", textX, 310);

        g2d.setColor(Color.WHITE);
        setCustomFont(g2d, 30f);
        textX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Based on Dragon Ball Z assets")) / 2;
        g2d.drawString("Based on Dragon Ball Z assets", textX, 420);

        g2d.setColor(Color.GRAY);
        setCustomFont(g2d, 25f);
        textX = (SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Press BLOCK to return to Menu")) / 2;
        g2d.drawString("Press BLOCK to return to Menu", textX, 550);
    }
}