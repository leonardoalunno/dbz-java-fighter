public class AttackData {

    // =============================================
    // IDENTIFICAZIONE
    // =============================================
    public final String id; // es. "goku_light_1", "vegeta_heavy_launcher"

    // =============================================
    // TIMING (in frame a 60fps)
    // =============================================
    public final int startup;   // frame prima che il colpo sia attivo
    public final int active;    // frame in cui la hitbox è attiva
    public final int recovery;  // frame di recovery dopo il colpo

    // =============================================
    // DANNO E KNOCKBACK
    // =============================================
    public final int damage;        // danno base
    public final int knockback;     // forza della spinta

    // =============================================
    // PROPRIETA' SPECIALI
    // =============================================
    public final boolean isEnergy;       // Ki Blast / Ultimate → chip damage sulla guardia
    public final boolean isUnblockable;  // Surprise Attack → bypassa BLOCKING
    public final boolean isLauncher;     // manda l'avversario in aria
    public final boolean causesCrumple;  // spike verso il basso / caduta a terra
    public final boolean isGuardBreaker; // spezza la guardia istantaneamente senza fare danno HP

    // =============================================
    // KI
    // =============================================
    public final double kiCost;     // Ki consumato per eseguire questo attacco
    public final double kiOnHit;    // Ki guadagnato se il colpo va a segno

    // =============================================
    // COSTRUTTORE COMPLETO
    // =============================================
    public AttackData(String id,
                      int startup, int active, int recovery,
                      int damage, int knockback,
                      boolean isEnergy, boolean isUnblockable,
                      boolean isLauncher, boolean causesCrumple,
                      boolean isGuardBreaker,
                      double kiCost, double kiOnHit) {
        this.id            = id;
        this.startup       = startup;
        this.active        = active;
        this.recovery      = recovery;
        this.damage        = damage;
        this.knockback     = knockback;
        this.isEnergy      = isEnergy;
        this.isUnblockable = isUnblockable;
        this.isLauncher    = isLauncher;
        this.causesCrumple = causesCrumple;
        this.isGuardBreaker = isGuardBreaker;
        this.kiCost        = kiCost;
        this.kiOnHit       = kiOnHit;
    }

    // =============================================
    // COSTRUTTORE SENZA GUARD BREAKER — retrocompatibile
    // (isGuardBreaker = false)
    // =============================================
    public AttackData(String id,
                      int startup, int active, int recovery,
                      int damage, int knockback,
                      boolean isEnergy, boolean isUnblockable,
                      boolean isLauncher, boolean causesCrumple,
                      double kiCost, double kiOnHit) {
        this(id, startup, active, recovery, damage, knockback,
                isEnergy, isUnblockable, isLauncher, causesCrumple,
                false, kiCost, kiOnHit);
    }

    // =============================================
    // COSTRUTTORE SEMPLIFICATO — per attacchi fisici standard
    // (tutti i flag a false, kiCost=0, kiOnHit=15)
    // =============================================
    public AttackData(String id,
                      int startup, int active, int recovery,
                      int damage, int knockback) {
        this(id, startup, active, recovery, damage, knockback,
                false, false, false, false, false, 0.0, 15.0);
    }

    // =============================================
    // DURATA TOTALE — utile per sapere quando finisce l'attacco
    // =============================================
    public int totalDuration() {
        return startup + active + recovery;
    }
}