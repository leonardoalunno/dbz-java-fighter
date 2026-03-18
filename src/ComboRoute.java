public class ComboRoute {

    // =============================================
    // COSTANTI INPUT — usate nell'inputSequence
    // =============================================
    public static final int LIGHT    = 1;
    public static final int HEAVY    = 2;
    public static final int SPECIAL  = 3;
    public static final int ULTIMATE = 4;

    // =============================================
    // IDENTIFICAZIONE
    // =============================================
    public final String id; // es. "goku_LLL", "vegeta_LLH"

    // =============================================
    // SEQUENZA DI INPUT
    // Ogni intero corrisponde a una costante qui sopra.
    // Es: {LIGHT, LIGHT, HEAVY} = L → L → H
    // =============================================
    public final int[] inputSequence;

    // =============================================
    // DATI DEI COLPI
    // Un AttackData per ogni hit della sequenza.
    // attacks.length deve essere == inputSequence.length
    // =============================================
    public final AttackData[] attacks;

    // =============================================
    // CHIAVE ANIMAZIONE
    // Usata nel draw() delle subclass per sapere
    // quale sprite mostrare per ogni step.
    // Es: "combo_LLL" → Goku.draw() sa quali srcX usare
    // =============================================
    public final String animationKey;

    // =============================================
    // PROPRIETA' DELLA ROUTE
    // =============================================
    public final boolean requiresAura;   // true = solo con Aura attiva
    public final boolean requiresAir;    // true = solo in aria
    public final boolean requiresGround; // true = solo a terra

    // =============================================
    // COSTRUTTORE COMPLETO
    // =============================================
    public ComboRoute(String id,
                      int[] inputSequence,
                      AttackData[] attacks,
                      String animationKey,
                      boolean requiresAura,
                      boolean requiresAir,
                      boolean requiresGround) {

        if (inputSequence.length != attacks.length) {
            throw new IllegalArgumentException(
                    "ComboRoute '" + id + "': inputSequence e attacks devono avere la stessa lunghezza!"
            );
        }

        this.id             = id;
        this.inputSequence  = inputSequence;
        this.attacks        = attacks;
        this.animationKey   = animationKey;
        this.requiresAura   = requiresAura;
        this.requiresAir    = requiresAir;
        this.requiresGround = requiresGround;
    }

    // =============================================
    // COSTRUTTORE SEMPLIFICATO
    // Per combo a terra, senza requisiti speciali
    // =============================================
    public ComboRoute(String id,
                      int[] inputSequence,
                      AttackData[] attacks,
                      String animationKey) {
        this(id, inputSequence, attacks, animationKey, false, false, false);
    }

    // =============================================
    // HELPER — lunghezza della combo
    // =============================================
    public int length() {
        return inputSequence.length;
    }

    // =============================================
    // HELPER — danno totale della combo (senza scaling)
    // =============================================
    public int totalDamage() {
        int total = 0;
        for (AttackData a : attacks) total += a.damage;
        return total;
    }

    // =============================================
    // HELPER — verifica se questa route è eseguibile
    // in base allo stato corrente del fighter
    // =============================================
    public boolean isExecutable(boolean auraActive, boolean inAir) {
        if (requiresAura && !auraActive)  return false;
        if (requiresAir  && !inAir)       return false;
        if (requiresGround && inAir)      return false;
        return true;
    }
}