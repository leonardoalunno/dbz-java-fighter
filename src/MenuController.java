public class MenuController {

    private GamePanel gp;

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
    }

    private void updateMainMenu() {
        if (gp.menuCooldown == 0) {
            if (gp.keyH.p1_down || gp.keyH.p2_down) {
                gp.mainMenuOption = (gp.mainMenuOption + 1) % 5;
                gp.menuCooldown = gp.COOLDOWN_TIME;
            }
            if (gp.keyH.p1_up || gp.keyH.p2_up) {
                gp.mainMenuOption = (gp.mainMenuOption + 4) % 5;
                gp.menuCooldown = gp.COOLDOWN_TIME;
            }
            if (gp.keyH.p1_punch || gp.keyH.enterPressed) {
                if (gp.mainMenuOption == 0) gp.gameState = 2; // Z BATTLE VS
                else if (gp.mainMenuOption == 2) gp.gameState = 6; // COMMANDS
                else if (gp.mainMenuOption == 3) gp.gameState = 7; // CREDITS
                else if (gp.mainMenuOption == 4) System.exit(0); // Exit
                gp.menuCooldown = gp.COOLDOWN_TIME;
            }
        }
    }

    private void updateCharacterMenu() {
        if (!gp.p1Ready && !gp.p2Ready && gp.menuCooldown == 0) {
            if (gp.keyH.p1_block || gp.keyH.p2_block) {
                gp.gameState = 1;
                gp.menuCooldown = gp.COOLDOWN_TIME;
                return;
            }
        }

        // P1
        if (!gp.p1Ready && gp.menuCooldown == 0) {
            if (gp.keyH.p1_right) { gp.p1Cursor = (gp.p1Cursor + 1) % 5; gp.menuCooldown = gp.COOLDOWN_TIME; }
            if (gp.keyH.p1_left) { gp.p1Cursor = (gp.p1Cursor + 4) % 5; gp.menuCooldown = gp.COOLDOWN_TIME; }
            if (gp.keyH.p1_punch) { gp.p1Ready = true; gp.menuCooldown = gp.COOLDOWN_TIME; }
        }
        if (gp.p1Ready && gp.keyH.p1_block && gp.menuCooldown == 0) {
            gp.p1Ready = false; gp.menuCooldown = gp.COOLDOWN_TIME;
        }

        // P2
        if (!gp.p2Ready && gp.menuCooldown == 0) {
            if (gp.keyH.p2_right) { gp.p2Cursor = (gp.p2Cursor + 1) % 5; gp.menuCooldown = gp.COOLDOWN_TIME; }
            if (gp.keyH.p2_left) { gp.p2Cursor = (gp.p2Cursor + 4) % 5; gp.menuCooldown = gp.COOLDOWN_TIME; }
            if (gp.keyH.p2_punch) { gp.p2Ready = true; gp.menuCooldown = gp.COOLDOWN_TIME; }
        }
        if (gp.p2Ready && gp.keyH.p2_block && gp.menuCooldown == 0) {
            gp.p2Ready = false; gp.menuCooldown = gp.COOLDOWN_TIME;
        }

        // Entrambi pronti
        if (gp.p1Ready && gp.p2Ready) {
            gp.gameState = 3;
            gp.menuCooldown = 20;
        }
    }

    private void updateStageMenu() {
        if (gp.menuCooldown == 0) {
            if (gp.keyH.p1_block || gp.keyH.p2_block) {
                gp.gameState = 2;
                gp.p1Ready = false; gp.p2Ready = false;
                gp.menuCooldown = gp.COOLDOWN_TIME;
                return;
            }

            if (gp.keyH.p1_right) { gp.stageCursor = (gp.stageCursor + 1) % 3; gp.menuCooldown = gp.COOLDOWN_TIME; }
            if (gp.keyH.p1_left) { gp.stageCursor = (gp.stageCursor + 2) % 3; gp.menuCooldown = gp.COOLDOWN_TIME; }
            if (gp.keyH.p1_punch) {
                gp.initBattle();
                gp.gameState = 4;
                gp.stateTimer = 0;
            }
        }
    }

    private void updateCommandsOrCredits() {
        if (gp.menuCooldown == 0 && (gp.keyH.p1_punch || gp.keyH.p1_block)) {
            gp.gameState = 1;
            gp.menuCooldown = gp.COOLDOWN_TIME;
        }
    }
}