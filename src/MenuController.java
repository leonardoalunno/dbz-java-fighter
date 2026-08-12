public class MenuController {

    private GamePanel gp;


    // --- Edge detection: stato dei tasti nel frame precedente ---
    private boolean prevP1Left, prevP1Right, prevP1Confirm, prevP1Cancel;
    private boolean prevP2Left, prevP2Right, prevP2Confirm, prevP2Cancel;

    public MenuController(GamePanel gp) {
        this.gp = gp;
    }

    public void updateMenus() {
        switch(gp.gameState) {
            case 1: updateMainMenu(); break;
            case 2: updateCharacterMenu(); break;
            case 3: updateStageMenu(); break;
            case 6:
            case 7: updateCommandsOrCredits(); break;
        }
        updatePrevInputs();   // sempre in fondo, dopo lo switch
    }

    private void updatePrevInputs() {
        prevP1Left    = gp.keyH.p1_left;
        prevP1Right   = gp.keyH.p1_right;
        prevP1Confirm = gp.keyH.p1_light;
        prevP1Cancel  = gp.keyH.p1_block;

        prevP2Left    = gp.keyH.p2_left;
        prevP2Right   = gp.keyH.p2_right;
        prevP2Confirm = gp.keyH.p2_light;
        prevP2Cancel  = gp.keyH.p2_block;
    }

    private void updateMainMenu() {
        if (gp.menuCooldown == 0) {
            if (gp.keyH.p1_down || gp.keyH.p2_down) {
                gp.mainMenuOption = (gp.mainMenuOption + 1) % 5;
                gp.menuCooldown = gp.COOLDOWN_TIME;
                SoundManager.getInstance().play("select");
            }
            if (gp.keyH.p1_up || gp.keyH.p2_up) {
                gp.mainMenuOption = (gp.mainMenuOption + 4) % 5;
                gp.menuCooldown = gp.COOLDOWN_TIME;
                SoundManager.getInstance().play("select");
            }
            if (gp.keyH.p1_light || gp.keyH.enterPressed) {
                SoundManager.getInstance().play("confirm");
                if (gp.mainMenuOption == 0) { gp.trainingMode = false; gp.gameState = 2; } // Z BATTLE VS
                else if (gp.mainMenuOption == 1) { gp.trainingMode = true; gp.stageCursor = gp.trainingStages[0]; gp.gameState = 2; } // TRAINING
                else if (gp.mainMenuOption == 2) gp.gameState = 6; // COMMANDS
                else if (gp.mainMenuOption == 3) gp.gameState = 7; // CREDITS
                else if (gp.mainMenuOption == 4) System.exit(0); // Exit
                gp.menuCooldown = gp.COOLDOWN_TIME;
            }
        }
    }

    private void updateCharacterMenu() {
        int n = gp.charNames.length;

        // --- Indietro al menu principale (solo se nessuno ha confermato) ---
        if (!gp.p1Ready && !gp.p2Ready) {
            boolean cancel = (gp.keyH.p1_block && !prevP1Cancel)
                    || (gp.keyH.p2_block && !prevP2Cancel);
            if (cancel) {
                SoundManager.getInstance().play("cancel");
                gp.gameState = 1;
                gp.menuCooldown = gp.COOLDOWN_TIME;
                return;
            }
        }

        // --- P1 ---
        if (!gp.p1Ready) {
            if (gp.keyH.p1_right && !prevP1Right)   { gp.p1Cursor = (gp.p1Cursor + 1) % n; SoundManager.getInstance().play("select"); }
            if (gp.keyH.p1_left  && !prevP1Left)    { gp.p1Cursor = (gp.p1Cursor + n - 1) % n; SoundManager.getInstance().play("select"); }
            if (gp.keyH.p1_light && !prevP1Confirm) { gp.p1Ready = true; SoundManager.getInstance().play("confirm"); }
        } else {
            if (gp.keyH.p1_block && !prevP1Cancel)  { gp.p1Ready = false; SoundManager.getInstance().play("cancel"); }
        }

        // --- P2 ---
        if (!gp.p2Ready) {
            if (gp.keyH.p2_right && !prevP2Right)   { gp.p2Cursor = (gp.p2Cursor + 1) % n; SoundManager.getInstance().play("select"); }
            if (gp.keyH.p2_left  && !prevP2Left)    { gp.p2Cursor = (gp.p2Cursor + n - 1) % n; SoundManager.getInstance().play("select"); }
            if (gp.keyH.p2_light && !prevP2Confirm) { gp.p2Ready = true; SoundManager.getInstance().play("confirm"); }
        } else {
            if (gp.keyH.p2_block && !prevP2Cancel)  { gp.p2Ready = false; SoundManager.getInstance().play("cancel"); }
        }

        // --- Entrambi pronti → Stage Select ---
        if (gp.p1Ready && gp.p2Ready) {
            gp.stateTimer++;
            if (gp.stateTimer > 45) {
                gp.gameState = 3;
                gp.stateTimer = 0;
                gp.menuCooldown = gp.COOLDOWN_TIME;
            }
        } else {
            gp.stateTimer = 0;
        }
    }

    private void updateStageMenu() {
        if (gp.menuCooldown == 0) {
            // --- Tasto per tornare indietro (Menu Selezione Personaggi) ---
            if (gp.keyH.p1_block || gp.keyH.p2_block) {
                SoundManager.getInstance().play("cancel");
                gp.gameState = 2;
                gp.p1Ready = false; gp.p2Ready = false;
                gp.menuCooldown = gp.COOLDOWN_TIME;
                return;
            }

            // Recuperiamo il numero totale di stage (ora sono 17!)
            int totalStages = gp.stageNames.length;

            if (gp.trainingMode) {
                // --- TRAINING: solo i 3 stage consentiti ---
                int pos = 0;
                for (int i = 0; i < gp.trainingStages.length; i++)
                    if (gp.trainingStages[i] == gp.stageCursor) pos = i;

                if (gp.keyH.p1_right) {
                    pos = (pos + 1) % gp.trainingStages.length;
                    gp.stageCursor = gp.trainingStages[pos];
                    gp.menuCooldown = gp.COOLDOWN_TIME;
                    SoundManager.getInstance().play("select");
                }
                if (gp.keyH.p1_left) {
                    pos = (pos + gp.trainingStages.length - 1) % gp.trainingStages.length;
                    gp.stageCursor = gp.trainingStages[pos];
                    gp.menuCooldown = gp.COOLDOWN_TIME;
                    SoundManager.getInstance().play("select");
                }
            } else {
                // --- Scorrimento a DESTRA ---
                if (gp.keyH.p1_right) {
                    gp.stageCursor = (gp.stageCursor + 1) % totalStages;
                    gp.menuCooldown = gp.COOLDOWN_TIME;
                    SoundManager.getInstance().play("select");
                }

                // --- Scorrimento a SINISTRA ---
                // Aggiungiamo totalStages prima di sottrarre 1 per evitare numeri negativi nel modulo
                if (gp.keyH.p1_left) {
                    gp.stageCursor = (gp.stageCursor + totalStages - 1) % totalStages;
                    gp.menuCooldown = gp.COOLDOWN_TIME;
                    SoundManager.getInstance().play("select");
                }
            }

            // --- Tasto per avviare la battaglia ---
            if (gp.keyH.p1_light) {
                SoundManager.getInstance().play("confirm");
                gp.initBattle();
                gp.gameState = 4;
                gp.stateTimer = 0;
            }
        }
    }

    private void updateCommandsOrCredits() {
        if (gp.menuCooldown == 0 && (gp.keyH.p1_light || gp.keyH.p1_block)) {
            gp.gameState = 1;
            gp.menuCooldown = gp.COOLDOWN_TIME;
        }
    }
}