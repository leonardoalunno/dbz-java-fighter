import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // =========================
    // --- COMANDI MAIN MENU ---
    // =========================
    public boolean enterPressed;

    // ==========================================
    // --- COMANDI GIOCATORE 1 (Sinistra) ---
    // WASD = movimento
    // F = Light Attack
    // R = Heavy Attack
    // E = Special (Ki Blast)
    // Z = Ultimate
    // Q = Block
    // V = Fly
    // C = Charge Ki (manuale)
    // ==========================================
    public boolean p1_up, p1_down, p1_left, p1_right;
    public boolean p1_light;    // F
    public boolean p1_heavy;    // R
    public boolean p1_special;  // E
    public boolean p1_ultimate; // Z
    public boolean p1_block;    // Q
    public boolean p1_fly;      // V
    public boolean p1_charge;   // C

    // ==========================================
    // --- COMANDI GIOCATORE 2 (Destra) ---
    // Frecce = movimento
    // L = Light Attack
    // P = Heavy Attack
    // O = Special (Ki Blast)
    // K = Ultimate
    // U = Block
    // N = Fly
    // M = Charge Ki (manuale)
    // ==========================================
    public boolean p2_up, p2_down, p2_left, p2_right;
    public boolean p2_light;    // L
    public boolean p2_heavy;    // P
    public boolean p2_special;  // O
    public boolean p2_ultimate; // K
    public boolean p2_block;    // U
    public boolean p2_fly;      // N
    public boolean p2_charge;   // M

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // --- MAIN MENU ---
        if (code == KeyEvent.VK_ENTER) enterPressed = true;

        // --- INPUT GIOCATORE 1 ---
        if (code == KeyEvent.VK_W) p1_up    = true;
        if (code == KeyEvent.VK_S) p1_down  = true;
        if (code == KeyEvent.VK_A) p1_left  = true;
        if (code == KeyEvent.VK_D) p1_right = true;

        if (code == KeyEvent.VK_F) p1_light    = true;
        if (code == KeyEvent.VK_R) p1_heavy    = true;
        if (code == KeyEvent.VK_E) p1_special  = true;
        if (code == KeyEvent.VK_Z) p1_ultimate = true;
        if (code == KeyEvent.VK_Q) p1_block    = true;
        if (code == KeyEvent.VK_V) p1_fly      = true;
        if (code == KeyEvent.VK_C) p1_charge   = true;

        // --- INPUT GIOCATORE 2 ---
        if (code == KeyEvent.VK_UP)    p2_up    = true;
        if (code == KeyEvent.VK_DOWN)  p2_down  = true;
        if (code == KeyEvent.VK_LEFT)  p2_left  = true;
        if (code == KeyEvent.VK_RIGHT) p2_right = true;

        if (code == KeyEvent.VK_L) p2_light    = true;
        if (code == KeyEvent.VK_P) p2_heavy    = true;
        if (code == KeyEvent.VK_O) p2_special  = true;
        if (code == KeyEvent.VK_K) p2_ultimate = true;
        if (code == KeyEvent.VK_U) p2_block    = true;
        if (code == KeyEvent.VK_N) p2_fly      = true;
        if (code == KeyEvent.VK_M) p2_charge   = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // --- MAIN MENU ---
        if (code == KeyEvent.VK_ENTER) enterPressed = false;

        // --- RILASCIO GIOCATORE 1 ---
        if (code == KeyEvent.VK_W) p1_up    = false;
        if (code == KeyEvent.VK_S) p1_down  = false;
        if (code == KeyEvent.VK_A) p1_left  = false;
        if (code == KeyEvent.VK_D) p1_right = false;

        if (code == KeyEvent.VK_F) p1_light    = false;
        if (code == KeyEvent.VK_R) p1_heavy    = false;
        if (code == KeyEvent.VK_E) p1_special  = false;
        if (code == KeyEvent.VK_Z) p1_ultimate = false;
        if (code == KeyEvent.VK_Q) p1_block    = false;
        if (code == KeyEvent.VK_V) p1_fly      = false;
        if (code == KeyEvent.VK_C) p1_charge   = false;

        // --- RILASCIO GIOCATORE 2 ---
        if (code == KeyEvent.VK_UP)    p2_up    = false;
        if (code == KeyEvent.VK_DOWN)  p2_down  = false;
        if (code == KeyEvent.VK_LEFT)  p2_left  = false;
        if (code == KeyEvent.VK_RIGHT) p2_right = false;

        if (code == KeyEvent.VK_L) p2_light    = false;
        if (code == KeyEvent.VK_P) p2_heavy    = false;
        if (code == KeyEvent.VK_O) p2_special  = false;
        if (code == KeyEvent.VK_K) p2_ultimate = false;
        if (code == KeyEvent.VK_U) p2_block    = false;
        if (code == KeyEvent.VK_N) p2_fly      = false;
        if (code == KeyEvent.VK_M) p2_charge   = false;
    }
}