public enum FighterState {

    // =============================================
    // STATI BASE — Movimento e riposo
    // =============================================
    IDLE,           // Fermo a terra (stance)
    WALKING,        // Camminata a terra
    CROUCHING,      // Preparazione salto (breve)
    JUMPING,        // In aria dopo salto
    FLYING_IDLE,    // Volo stazionario
    FLYING_FORWARD, // Volo in avanti (prep)
    FLYING_FORWARD_FULL, // Volo in avanti (pieno regime)
    FLYING_BACKWARD,     // Volo all'indietro (prep)
    FLYING_BACKWARD_FULL,// Volo all'indietro (pieno regime)

    // =============================================
    // STATI COMBATTIMENTO — Attacchi
    // =============================================
    COMBO_LIGHT,    // Attacco leggero (Neo Combo)
    COMBO_HEAVY,    // Attacco pesante
    SPECIAL_STARTUP,// Caricamento Ki Blast
    SPECIAL_ACTIVE, // Ki Blast in volo
    ULTIMATE_STARTUP, // Caricamento mossa Ultimate
    ULTIMATE_ACTIVE,  // Ultimate attiva

    // =============================================
    // STATI DIFESA — Parata e movimento evasivo
    // =============================================
    BLOCKING,       // Parata a terra
    BLOCKING_AIR,   // Parata in aria
    TELEPORTING,    // Dash/Teletrasporto (con i-frames)

    // =============================================
    // STATI KI — Gestione energia
    // =============================================
    CHARGING_KI,    // Carica Ki manuale (tasto sostenuto)
    AURA_ACTIVE,    // Power-up Aura attivo

    // =============================================
    // STATI DANNO — Reazione ai colpi
    // =============================================
    HIT_STUN,       // Stordimento dopo colpo ricevuto
    GUARD_CRUSHED,  // Guardia rotta (stordimento lungo)
    TUMBLING,       // HP sotto 20% — barcollamento

    // =============================================
    // STATI FINALI — Fine match
    // =============================================
    KO,             // Sconfitto
    WINNER          // Vincitore
}