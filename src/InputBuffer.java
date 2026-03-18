public class InputBuffer {

    // =============================================
    // COSTANTI
    // =============================================
    private static final int BUFFER_SIZE   = 8;  // ultimi N input registrati
    public  static final int INPUT_WINDOW  = 14; // frame entro cui un input è valido

    // Input speciali
    public static final int NONE     = 0;
    public static final int LIGHT    = ComboRoute.LIGHT;
    public static final int HEAVY    = ComboRoute.HEAVY;
    public static final int SPECIAL  = ComboRoute.SPECIAL;
    public static final int ULTIMATE = ComboRoute.ULTIMATE;

    // =============================================
    // STATO INTERNO
    // =============================================
    private final int[] inputs;      // sequenza circolare degli input
    private final int[] timestamps;  // frame in cui ogni input è stato registrato
    private int head;                // indice della posizione più recente
    private int size;                // quanti input sono attualmente nella coda
    private int currentFrame;        // frame corrente del gioco

    // Traccia i tasti premuti nel frame precedente
    // per rilevare solo i nuovi press (edge detection)
    private boolean prevLight    = false;
    private boolean prevHeavy    = false;
    private boolean prevSpecial  = false;
    private boolean prevUltimate = false;

    // =============================================
    // COSTRUTTORE
    // =============================================
    public InputBuffer() {
        this.inputs     = new int[BUFFER_SIZE];
        this.timestamps = new int[BUFFER_SIZE];
        this.head       = 0;
        this.size       = 0;
        this.currentFrame = 0;
    }

    // =============================================
    // UPDATE — chiamato ogni frame
    // Registra solo i nuovi press (non il tasto tenuto)
    // =============================================
    public void update(boolean light, boolean heavy,
                       boolean special, boolean ultimate) {
        currentFrame++;

        // Edge detection: registra solo il momento in cui
        // il tasto viene premuto, non mentre è tenuto
        if (light    && !prevLight)    record(LIGHT);
        if (heavy    && !prevHeavy)    record(HEAVY);
        if (special  && !prevSpecial)  record(SPECIAL);
        if (ultimate && !prevUltimate) record(ULTIMATE);

        prevLight    = light;
        prevHeavy    = heavy;
        prevSpecial  = special;
        prevUltimate = ultimate;
    }

    // =============================================
    // RECORD — aggiunge un input alla coda circolare
    // =============================================
    private void record(int input) {
        head = (head + 1) % BUFFER_SIZE;
        inputs[head]     = input;
        timestamps[head] = currentFrame;
        if (size < BUFFER_SIZE) size++;
    }

    // =============================================
    // MATCHES — verifica se la sequenza corrisponde
    // agli ultimi N input ancora dentro la finestra
    // =============================================
    public boolean matches(int[] sequence) {
        int seqLen = sequence.length;
        if (seqLen > size) return false;

        // Raccogliamo gli ultimi seqLen input validi
        // (ancora dentro la finestra temporale)
        int[] recent = getRecentValid(seqLen);
        if (recent == null) return false;

        // Confronto esatto
        for (int i = 0; i < seqLen; i++) {
            if (recent[i] != sequence[i]) return false;
        }
        return true;
    }

    // =============================================
    // GET RECENT VALID — restituisce gli ultimi N
    // input se tutti ancora dentro INPUT_WINDOW
    // =============================================
    private int[] getRecentValid(int count) {
        if (count > size) return null;

        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            // Leggiamo dalla posizione più recente all'indietro
            int idx = (head - i + BUFFER_SIZE) % BUFFER_SIZE;

            // Verifica finestra temporale
            if (currentFrame - timestamps[idx] > INPUT_WINDOW) return null;

            // Riempiamo il risultato al contrario
            // (così result[0] = più vecchio, result[count-1] = più recente)
            result[count - 1 - i] = inputs[idx];
        }
        return result;
    }

    // =============================================
    // CONSUME — svuota il buffer dopo una combo
    // riconosciuta, per evitare match doppi
    // =============================================
    public void consume() {
        size = 0;
        head = 0;
        // Non resettiamo currentFrame — il tempo continua
    }

    // =============================================
    // CLEAR — reset completo (usato al KO, fine match)
    // =============================================
    public void clear() {
        size         = 0;
        head         = 0;
        currentFrame = 0;
        prevLight    = false;
        prevHeavy    = false;
        prevSpecial  = false;
        prevUltimate = false;
    }

    // =============================================
    // HELPER — ultimo input registrato
    // =============================================
    public int lastInput() {
        if (size == 0) return NONE;
        return inputs[head];
    }

    // =============================================
    // HELPER — quanti input sono nella coda
    // =============================================
    public int size() {
        return size;
    }
}