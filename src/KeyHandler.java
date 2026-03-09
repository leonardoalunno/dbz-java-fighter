import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // =========================
    // --- COMANDI MAIN MANU ---
    // =========================
    public boolean enterPressed;

    // ==========================================
    // --- COMANDI GIOCATORE 1 (Sinistra) ---
    // ==========================================
    public boolean p1_up, p1_down, p1_left, p1_right;
    public boolean p1_punch;     // F
    public boolean p1_kick;      // R
    public boolean p1_kiBlast;   // E
    public boolean p1_kamehameha;// Z
    public boolean p1_fly;       // V
    public boolean p1_aura;      // X
    public boolean p1_block;     // Q

    // ==========================================
    // --- COMANDI GIOCATORE 2 (Destra) ---
    // ==========================================
    public boolean p2_up, p2_down, p2_left, p2_right;
    public boolean p2_punch;     // L
    public boolean p2_kick;      // P
    public boolean p2_kiBlast;   // O  <-- MODIFICATO COME RICHIESTO
    public boolean p2_kamehameha;// K  <-- MODIFICATO COME RICHIESTO
    public boolean p2_fly;       // N  <-- SPOSTATO QUI PER EVITARE IL DOPPIO 'M'
    public boolean p2_aura;      // M  <-- MODIFICATO COME RICHIESTO
    public boolean p2_block;     // U

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // --- MAIN MENU ---
        if (code == KeyEvent.VK_ENTER) enterPressed = true;

        // --- INPUT GIOCATORE 1 ---
        if (code == KeyEvent.VK_W) p1_up = true;
        if (code == KeyEvent.VK_S) p1_down = true;
        if (code == KeyEvent.VK_A) p1_left = true;
        if (code == KeyEvent.VK_D) p1_right = true;

        if (code == KeyEvent.VK_F) p1_punch = true;
        if (code == KeyEvent.VK_R) p1_kick = true;
        if (code == KeyEvent.VK_E) p1_kiBlast = true;
        if (code == KeyEvent.VK_Z) p1_kamehameha = true;
        if (code == KeyEvent.VK_V) p1_fly = true;
        if (code == KeyEvent.VK_X) p1_aura = true;
        if (code == KeyEvent.VK_Q) p1_block = true;

        // --- INPUT GIOCATORE 2 ---
        if (code == KeyEvent.VK_UP) p2_up = true;
        if (code == KeyEvent.VK_DOWN) p2_down = true;
        if (code == KeyEvent.VK_LEFT) p2_left = true;
        if (code == KeyEvent.VK_RIGHT) p2_right = true;

        if (code == KeyEvent.VK_L) p2_punch = true;
        if (code == KeyEvent.VK_P) p2_kick = true;
        if (code == KeyEvent.VK_O) p2_kiBlast = true;    // <-- O
        if (code == KeyEvent.VK_K) p2_kamehameha = true; // <-- K
        if (code == KeyEvent.VK_N) p2_fly = true;        // <-- N
        if (code == KeyEvent.VK_M) p2_aura = true;       // <-- M
        if (code == KeyEvent.VK_U) p2_block = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // --- MAIN MENU ---
        if (code == KeyEvent.VK_ENTER) enterPressed = false;

        // --- RILASCIO GIOCATORE 1 ---
        if (code == KeyEvent.VK_W) p1_up = false;
        if (code == KeyEvent.VK_S) p1_down = false;
        if (code == KeyEvent.VK_A) p1_left = false;
        if (code == KeyEvent.VK_D) p1_right = false;

        if (code == KeyEvent.VK_F) p1_punch = false;
        if (code == KeyEvent.VK_R) p1_kick = false;
        if (code == KeyEvent.VK_E) p1_kiBlast = false;
        if (code == KeyEvent.VK_Z) p1_kamehameha = false;
        if (code == KeyEvent.VK_V) p1_fly = false;
        if (code == KeyEvent.VK_X) p1_aura = false;
        if (code == KeyEvent.VK_Q) p1_block = false;

        // --- RILASCIO GIOCATORE 2 ---
        if (code == KeyEvent.VK_UP) p2_up = false;
        if (code == KeyEvent.VK_DOWN) p2_down = false;
        if (code == KeyEvent.VK_LEFT) p2_left = false;
        if (code == KeyEvent.VK_RIGHT) p2_right = false;

        if (code == KeyEvent.VK_L) p2_punch = false;
        if (code == KeyEvent.VK_P) p2_kick = false;
        if (code == KeyEvent.VK_O) p2_kiBlast = false;
        if (code == KeyEvent.VK_K) p2_kamehameha = false;
        if (code == KeyEvent.VK_N) p2_fly = false;
        if (code == KeyEvent.VK_M) p2_aura = false;
        if (code == KeyEvent.VK_U) p2_block = false;
    }
}