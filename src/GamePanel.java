import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.AlphaComposite;

public class GamePanel extends JPanel implements Runnable {

    // --- NUOVA RISOLUZIONE 16:9 ---
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    int FPS = 60;
    Thread gameThread;

    // --- COMPONENTI DEL MOTORE DI GIOCO ---
    public KeyHandler keyH = new KeyHandler();
    public UIManager uiManager;
    public MenuController menuController;

    // --- VARIABILI DI STATO PUBBLICHE ---
    public int gameState = -1;
    public int stateTimer = 0;
    public int mainMenuOption = 0;
    public int loadSpriteFrame = 1;
    public int battlePhase = 0;

    public int p1Cursor = 0, p2Cursor = 4;
    public boolean p1Ready = false, p2Ready = false;
    public String[] charNames = {"Goku", "Vegeta", "Trunks", "Broly", "Beerus"};

    public int stageCursor = 0;

    // --- NUOVI 17 STAGE ---
    public String[] stageNames = {
            "Cavern", "Cell Games Arena", "Galactic Arena", "Gravity Training",
            "Hyperbolic Time Chamber", "Islands", "King Kai's Planet", "Land of the Kais",
            "Planet Namek 1", "Planet Namek 2", "Planet Namek Destroyed", "Rocky Field",
            "Rocky Field Evening", "Space", "Wasteland", "West City Destroyed", "West City"
    };

    public int menuCooldown = 0;
    public final int COOLDOWN_TIME = 12;

    public Fighter player1, player2;

    // --- NUOVE VARIABILI SCREEN SHAKE ---
    public int shakeTimer = 0;
    public int shakeMagnitude = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        uiManager = new UIManager(this);
        menuController = new MenuController(this);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Metodo helper per innescare il terremoto
    public void startShake(int duration, int magnitude) {
        this.shakeTimer = duration;
        this.shakeMagnitude = magnitude;
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

        // --- DECREMENTO DELLO SHAKE ---
        if (shakeTimer > 0) shakeTimer--;

        switch(gameState) {
            case -1: // SPLASH SCREEN
                stateTimer++;
                if (stateTimer > 180) { gameState = 0; stateTimer = 0; }
                break;

            case 0: // LOADING
                stateTimer++;
                if (stateTimer % 15 == 0) loadSpriteFrame = (loadSpriteFrame == 1) ? 2 : 1;
                if (stateTimer > 180) { gameState = 1; stateTimer = 0; }
                break;

            case 1: case 2: case 3: case 6: case 7: // MENU VARI
                menuController.updateMenus();
                break;

            case 4: // VS SCREEN
                stateTimer++;
                if (stateTimer > 180) { gameState = 5; battlePhase = 0; stateTimer = 0; }
                break;

            case 5: // BATTLE
                if (battlePhase == 0 || battlePhase == 1) {
                    stateTimer++;
                    if (battlePhase == 0 && stateTimer > 60) { battlePhase = 1; stateTimer = 0; }
                    if (battlePhase == 1 && stateTimer > 40) { battlePhase = 2; stateTimer = 0; }
                }

                if (battlePhase >= 2 && player1 != null && player2 != null) {
                    player1.update(keyH, player2);
                    player2.update(keyH, player1);

                    // --- TRUCCO: LEGGIAMO L'IMPATTO PER GENERARE LO SHAKE! ---
                    // Se un giocatore viene colpito (hitTimer = 1 è il primo frame dell'impatto)
                    // La magnitudo dipende dalla violenza del Knockback!
                    if (player1.isHit && player1.hitTimer == 1) {
                        startShake(12, Math.max(2, player1.knockbackSpeed / 2));
                    }
                    if (player2.isHit && player2.hitTimer == 1) {
                        startShake(12, Math.max(2, player2.knockbackSpeed / 2));
                    }
                }

                if (battlePhase == 2) {
                    if (player1.hp <= 0 || player2.hp <= 0) { battlePhase = 3; stateTimer = 0; }
                }
                else if (battlePhase == 3) {
                    stateTimer++;
                    if (stateTimer > 120) battlePhase = 4;
                }
                else if (battlePhase == 4) {
                    if (keyH.p1_block || keyH.p2_block) {
                        gameState = 1;
                        menuCooldown = COOLDOWN_TIME;
                        p1Ready = false; p2Ready = false;
                        player1 = null; player2 = null;
                    }
                }
                break;
        }
    }

    public Fighter createFighter(int cursorIndex, int x, int y, int playerID) {
        switch (cursorIndex) {
            case 0: return new Goku(x, y, playerID);
            case 1: return new Vegeta(x, y, playerID);
            case 2: return new Goku(x, y, playerID); // return new FutureTrunks(x, y, playerID);
            case 3: return new Goku(x, y, playerID); // return new Broly(x, y, playerID);
            case 4: return new Goku(x, y, playerID); // return new Beerus(x, y, playerID);
            default: return new Goku(x, y, playerID);
        }
    }

    public void initBattle() {
        int startDistance = 250;
        int spawnY = 575;

        int p1StartX = startDistance;
        int p2StartX = SCREEN_WIDTH - startDistance - 48;

        player1 = createFighter(p1Cursor, p1StartX, spawnY, 1);
        player2 = createFighter(p2Cursor, p2StartX, spawnY, 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // --- APPLICAZIONE DELLO SCREEN SHAKE ALLA TELECAMERA ---
        int currentShakeX = 0;
        int currentShakeY = 0;

        if (shakeTimer > 0) {
            // Generiamo un offset casuale tra -magnitude e +magnitude
            currentShakeX = (int)(Math.random() * shakeMagnitude * 2) - shakeMagnitude;
            currentShakeY = (int)(Math.random() * shakeMagnitude * 2) - shakeMagnitude;

            // Spostiamo letteralmente tutta la tela del gioco (Personaggi, Sfondo, HUD)!
            g2d.translate(currentShakeX, currentShakeY);
        }

        // Disegniamo tutto tramite lo UIManager
        uiManager.draw(g2d);

        // --- RIPRISTINO TELECAMERA ---
        if (shakeTimer > 0) {
            // Se non lo riportiamo indietro, al frame successivo il gioco si "rompe" traslando all'infinito
            g2d.translate(-currentShakeX, -currentShakeY);
        }

        g2d.dispose();
    }
}