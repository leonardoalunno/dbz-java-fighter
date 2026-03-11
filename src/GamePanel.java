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

    // --- VARIABILI DI STATO PUBBLICHE (Per farle leggere al Controller e UI) ---
    public int gameState = -1; // -1: Splash, 0: Loading, 1: Main Menu...
    public int stateTimer = 0;
    public int mainMenuOption = 0;
    public int loadSpriteFrame = 1;
    public int battlePhase = 0;

    public int p1Cursor = 0, p2Cursor = 4;
    public boolean p1Ready = false, p2Ready = false;
    public String[] charNames = {"Goku", "Vegeta", "Trunks", "Broly", "Sup Kai"};

    public int stageCursor = 0;
    public String[] stageNames = {"Canyon Sunset", "Tournament Day", "Tournament Sunset"};

    public int menuCooldown = 0;
    public final int COOLDOWN_TIME = 12;

    public Fighter player1, player2;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        // Inizializziamo i nostri Manager
        uiManager = new UIManager(this);
        menuController = new MenuController(this);
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
                if (stateTimer > 180) { gameState = 0; stateTimer = 0; }
                break;

            case 0: // LOADING
                stateTimer++;
                if (stateTimer % 15 == 0) loadSpriteFrame = (loadSpriteFrame == 1) ? 2 : 1;
                if (stateTimer > 180) { gameState = 1; stateTimer = 0; }
                break;

            case 1: // MAIN MENU
            case 2: // CHAR SELECT
            case 3: // STAGE SELECT
            case 6: // COMMANDS
            case 7: // CREDITS
                // IL MENU CONTROLLER GESTISCE TUTTI GLI INPUT DEL MENU!
                menuController.updateMenus();
                break;

            case 4: // VS SCREEN (Transizione Automatica)
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

    // --- FACTORY UNIVERSALE PER I PERSONAGGI ---
    public Fighter createFighter(int cursorIndex, int x, int y, int playerID) {
        switch (cursorIndex) {
            case 0: return new Goku(x, y, playerID);
            case 1: return new Vegeta(x, y, playerID);
            case 2: return new Goku(x, y, playerID); // return new FutureTrunks(x, y, playerID);
            case 3: return new Goku(x, y, playerID); // return new Broly(x, y, playerID);
            case 4: return new Goku(x, y, playerID); // return new SupremeKai(x, y, playerID);
            default: return new Goku(x, y, playerID);
        }
    }

    public void initBattle() {
        int startDistance = 250;
        int spawnY = 550;

        int p1StartX = startDistance;
        int p2StartX = SCREEN_WIDTH - startDistance - 48;

        player1 = createFighter(p1Cursor, p1StartX, spawnY, 1);
        player2 = createFighter(p2Cursor, p2StartX, spawnY, 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Tutta la grafica è stata delegata allo UIManager!
        uiManager.draw(g2d);

        g2d.dispose();
    }
}