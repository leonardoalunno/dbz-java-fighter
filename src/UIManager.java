import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class UIManager {

    private GamePanel gp;
    private ResourceManager rm;

    public UIManager(GamePanel gp) {
        this.gp = gp;
        this.rm = ResourceManager.getInstance();
    }

    public void draw(Graphics2D g2d) {
        switch(gp.gameState) {
            case -1: drawSplash(g2d); break;
            case 0: drawLoading(g2d); break;
            case 1: drawMainMenu(g2d); break;
            case 2: drawCharacterMenu(g2d); break;
            case 3: drawStageMenu(g2d); break;
            case 4: drawVsScreen(g2d); break;
            case 5: drawBattle(g2d); break;
            case 6: drawCommands(g2d); break;
            case 7: drawCredits(g2d); break;
        }
    }

    private void setCustomFont(Graphics2D g2d, float size) {
        if (rm.saiyanFont != null) g2d.setFont(rm.saiyanFont.deriveFont(Font.PLAIN, size));
        else g2d.setFont(new Font("Arial", Font.BOLD, (int)size));
    }

    private void setBangersFont(Graphics2D g2d, float size) {
        if (rm.bangersFont != null) g2d.setFont(rm.bangersFont.deriveFont(Font.PLAIN, size));
        else g2d.setFont(new Font("Arial", Font.BOLD, (int)size));
    }

    private void drawSplash(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        if (rm.splashLogo != null) {
            float alpha = 1.0f;
            if (gp.stateTimer < 60) alpha = (float) gp.stateTimer / 60.0f;
            else if (gp.stateTimer > 120) alpha = 1.0f - ((float)(gp.stateTimer - 120) / 60.0f);

            if (alpha < 0.0f) alpha = 0.0f;
            if (alpha > 1.0f) alpha = 1.0f;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int logoW = 600;
            int logoH = (logoW * rm.splashLogo.getHeight()) / rm.splashLogo.getWidth();
            int logoX = (GamePanel.SCREEN_WIDTH - logoW) / 2;
            int logoY = (GamePanel.SCREEN_HEIGHT - logoH) / 2;
            g2d.drawImage(rm.splashLogo, logoX, logoY, logoW, logoH, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private void drawLoading(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        BufferedImage loadSprite = (gp.loadSpriteFrame == 1) ? rm.load1 : rm.load2;
        if (loadSprite != null) {
            double scale = 3.0;
            int spriteW = (int)(loadSprite.getWidth() * scale), spriteH = (int)(loadSprite.getHeight() * scale);
            int spriteX = (GamePanel.SCREEN_WIDTH - spriteW) / 2, spriteY = (GamePanel.SCREEN_HEIGHT - spriteH) / 2 - 50;
            g2d.drawImage(loadSprite, spriteX, spriteY, spriteW, spriteH, null);
        }

        g2d.setColor(Color.WHITE);
        setCustomFont(g2d, 45f);
        int textWidth = g2d.getFontMetrics().stringWidth("LOADING");
        int textX = (GamePanel.SCREEN_WIDTH - textWidth) / 2 - 20;
        g2d.drawString("LOADING", textX, 600);

        int numDots = (gp.stateTimer / 15) % 4;
        for (int i = 0; i < numDots; i++) g2d.fillOval(textX + textWidth + 8 + (i * 15), 600 - 8, 8, 8);
    }

    private void drawMainMenu(Graphics2D g2d) {
        if (rm.mainMenuBg != null) g2d.drawImage(rm.mainMenuBg, 0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null);

        int boxCenterX = 400;
        if (rm.dbzLogo != null) {
            int logoW = 470, logoH = (logoW * rm.dbzLogo.getHeight()) / rm.dbzLogo.getWidth();
            g2d.drawImage(rm.dbzLogo, boxCenterX - (logoW / 2), 20, logoW, logoH, null);
        }

        String[] menuOptions = {"Z BATTLE VS", "TRAINING", "COMMANDS", "CREDITS", "EXIT"};
        setCustomFont(g2d, 50f);

        for (int i = 0; i < menuOptions.length; i++) {
            String text = menuOptions[i];
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            int textX = boxCenterX - (textWidth / 2);
            int textY = 260 + (i * 65);

            g2d.setColor(Color.BLACK);
            g2d.drawString(text, textX - 2, textY - 2); g2d.drawString(text, textX + 2, textY - 2);
            g2d.drawString(text, textX - 2, textY + 2); g2d.drawString(text, textX + 2, textY + 2);

            if (i == gp.mainMenuOption) {
                g2d.setColor(Color.YELLOW);
                if (rm.menuCursor != null) g2d.drawImage(rm.menuCursor, textX + textWidth + 15, textY - 30, 30, 30, null);
            } else g2d.setColor(Color.WHITE);
            g2d.drawString(text, textX, textY);
        }
    }

    private void drawCharacterMenu(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 50)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        g2d.setColor(Color.YELLOW); setCustomFont(g2d, 70f);
        g2d.drawString("CHARACTER SELECT", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("CHARACTER SELECT")) / 2, 100);

        BufferedImage[] icons = {rm.iconGoku, rm.iconVegeta, rm.iconFutureTrunks, rm.iconBroly, rm.iconSupremeKai};
        int size = 110, spacing = 160, startX = (GamePanel.SCREEN_WIDTH - (spacing * 4 + size)) / 2, startY = 300;

        for(int i = 0; i < 5; i++) {
            int x = startX + (i * spacing);
            if (icons[i] != null) g2d.drawImage(icons[i], x, startY, size, size, null);
            g2d.setColor(Color.WHITE); setCustomFont(g2d, 28f);
            g2d.drawString(gp.charNames[i], x + (size / 2) - (g2d.getFontMetrics().stringWidth(gp.charNames[i]) / 2), startY + size + 45);
        }

        g2d.setStroke(new BasicStroke(5));

        // --- DISEGNO CURSORE E SCRITTA P1 ---
        g2d.setColor(Color.RED);
        int p1X = startX + (gp.p1Cursor * spacing);
        g2d.drawRect(p1X - 6, startY - 6, size + 12, size + 12);

        if(gp.p1Ready) {
            setBangersFont(g2d, 32f);
            g2d.drawString("P1 READY", p1X + (size / 2) - (g2d.getFontMetrics().stringWidth("P1 READY") / 2), startY - 20);
        }

        // --- DISEGNO CURSORE E SCRITTA P2 ---
        g2d.setColor(new Color(50, 150, 255));
        int p2X = startX + (gp.p2Cursor * spacing);

        if (gp.p1Cursor == gp.p2Cursor) {
            g2d.drawRect(p2X - 12, startY - 12, size + 24, size + 24);
        } else {
            g2d.drawRect(p2X - 6, startY - 6, size + 12, size + 12);
        }

        if(gp.p2Ready) {
            setBangersFont(g2d, 32f);
            int p2ReadyY = startY - 20;

            // TRUCCO: Se P1 e P2 hanno scelto lo stesso personaggio, alziamo la scritta P2 di 35 pixel!
            if (gp.p1Cursor == gp.p2Cursor) {
                p2ReadyY -= 35;
            }

            g2d.drawString("P2 READY", p2X + (size / 2) - (g2d.getFontMetrics().stringWidth("P2 READY") / 2), p2ReadyY);
        }
    }

    private void drawStageMenu(Graphics2D g2d) {
        g2d.setColor(new Color(20, 20, 30)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        g2d.setColor(Color.YELLOW); setCustomFont(g2d, 70f);
        g2d.drawString("STAGE SELECT", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("STAGE SELECT")) / 2, 120);

        BufferedImage[] stageIcons = {rm.canyonIcon, rm.tournamentDayIcon, rm.tournamentSunsetIcon};
        int iconSize = 180, spacing = 300, startX = (GamePanel.SCREEN_WIDTH - (spacing * 2 + iconSize)) / 2, y = 280;

        for (int i = 0; i < 3; i++) {
            int x = startX + (i * spacing);
            if (stageIcons[i] != null) g2d.drawImage(stageIcons[i], x, y, iconSize, iconSize, null);

            if (i == gp.stageCursor) {
                g2d.setColor(Color.YELLOW); g2d.setStroke(new BasicStroke(6));
                g2d.drawRect(x - 8, y - 8, iconSize + 16, iconSize + 16);
                g2d.setColor(Color.WHITE); setCustomFont(g2d, 40f);
                g2d.drawString(gp.stageNames[i], (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth(gp.stageNames[i])) / 2, 550);
            } else {
                g2d.setColor(Color.DARK_GRAY); g2d.setStroke(new BasicStroke(4));
                g2d.drawRect(x - 8, y - 8, iconSize + 16, iconSize + 16);
            }
        }
    }

    private void drawVsScreen(Graphics2D g2d) {
        g2d.setColor(Color.BLACK); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        BufferedImage[] icons = {rm.iconGoku, rm.iconVegeta, rm.iconFutureTrunks, rm.iconBroly, rm.iconSupremeKai};
        int iconSize = 200, p1X = 250, p2X = GamePanel.SCREEN_WIDTH - 250 - iconSize, yPos = 260;

        g2d.setColor(Color.WHITE); g2d.setStroke(new BasicStroke(5));
        if (icons[gp.p1Cursor] != null) { g2d.drawRect(p1X - 5, yPos - 5, iconSize + 10, iconSize + 10); g2d.drawImage(icons[gp.p1Cursor], p1X, yPos, iconSize, iconSize, null); }
        if (icons[gp.p2Cursor] != null) { g2d.drawRect(p2X - 5, yPos - 5, iconSize + 10, iconSize + 10); g2d.drawImage(icons[gp.p2Cursor], p2X, yPos, iconSize, iconSize, null); }

        setCustomFont(g2d, 50f); g2d.setColor(Color.WHITE);
        g2d.drawString(gp.charNames[gp.p1Cursor], p1X + (iconSize / 2) - (g2d.getFontMetrics().stringWidth(gp.charNames[gp.p1Cursor]) / 2), yPos - 30);
        g2d.drawString(gp.charNames[gp.p2Cursor], p2X + (iconSize / 2) - (g2d.getFontMetrics().stringWidth(gp.charNames[gp.p2Cursor]) / 2), yPos - 30);

        if (rm.vsIcon != null) {
            int vsTargetW = 160, vsTargetH = (vsTargetW * rm.vsIcon.getHeight()) / rm.vsIcon.getWidth();
            g2d.drawImage(rm.vsIcon, (GamePanel.SCREEN_WIDTH - vsTargetW) / 2, yPos + (iconSize / 2) - (vsTargetH / 2), vsTargetW, vsTargetH, null);
        }
    }

    private void drawBattle(Graphics2D g2d) {
        // --- DISEGNO BACKGROUND CORRETTO ---
        BufferedImage bg = null;
        switch(gp.stageCursor) {
            case 0: bg = rm.canyonBg; break;
            case 1: bg = rm.tournamentDayBg; break;
            case 2: bg = rm.tournamentSunsetBg; break;
        }
        if (bg != null) {
            g2d.drawImage(bg, 0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null);
        }

        if (gp.player1 != null && gp.player2 != null) {
            if (gp.player2.isAttacking && !gp.player1.isAttacking) { gp.player1.draw(g2d); gp.player2.draw(g2d); }
            else { gp.player2.draw(g2d); gp.player1.draw(g2d); }
        }

        BufferedImage currentStatusIcon = null; int targetH = 120;
        if (gp.battlePhase == 0) currentStatusIcon = rm.readyIcon;
        else if (gp.battlePhase == 1) currentStatusIcon = rm.fightIcon;
        else if (gp.battlePhase == 3) currentStatusIcon = rm.koIcon;

        if (currentStatusIcon != null) {
            int drawW = (targetH * currentStatusIcon.getWidth()) / currentStatusIcon.getHeight();
            g2d.drawImage(currentStatusIcon, (GamePanel.SCREEN_WIDTH - drawW) / 2, 250, drawW, targetH, null);
        }

        if (gp.battlePhase == 4) {
            g2d.setColor(new Color(0, 0, 0, 150)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
            String winnerText = (gp.player2.hp <= 0) ? "PLAYER ONE WINS" : "PLAYER TWO WINS";
            g2d.setColor((gp.player2.hp <= 0) ? Color.RED : new Color(50, 150, 255));
            setCustomFont(g2d, 90f); g2d.drawString(winnerText, (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth(winnerText)) / 2, 300);

            g2d.setColor(Color.GRAY); setCustomFont(g2d, 35f);
            g2d.drawString("Press BLOCK to return to Menu", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Press BLOCK to return to Menu")) / 2, 600);
        }
    }

    private void drawCommands(Graphics2D g2d) {
        if (rm.mainMenuBg != null) { g2d.drawImage(rm.mainMenuBg, 0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null); g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT); }
        else { g2d.setColor(new Color(20, 20, 40)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT); }

        g2d.setColor(Color.YELLOW); setCustomFont(g2d, 80f); g2d.drawString("COMMANDS", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("COMMANDS")) / 2, 100);

        int startY = 180, spacing = 45, p1X = 150, p2X = GamePanel.SCREEN_WIDTH - 450;
        g2d.setColor(Color.CYAN); setCustomFont(g2d, 50f); g2d.drawString("PLAYER ONE", p1X, startY);
        g2d.setFont(new Font("Arial", Font.BOLD, 22)); g2d.setColor(Color.WHITE);
        g2d.drawString("W, A, S, D - Move / Jump", p1X, startY + spacing); g2d.drawString("W, A, S, D (Double Tap) - Teleport", p1X, startY + spacing * 2); g2d.drawString("F - Punch / Smash / Select", p1X, startY + spacing * 3); g2d.drawString("R - Kick", p1X, startY + spacing * 4); g2d.drawString("E - Ki Blast", p1X, startY + spacing * 5); g2d.drawString("Z - Special Attack", p1X, startY + spacing * 6); g2d.drawString("V - Fly", p1X, startY + spacing * 7); g2d.drawString("X - Charge Aura", p1X, startY + spacing * 8); g2d.drawString("Q - Block", p1X, startY + spacing * 9);

        g2d.setColor(Color.RED); setCustomFont(g2d, 50f); g2d.drawString("PLAYER TWO", p2X, startY);
        g2d.setFont(new Font("Arial", Font.BOLD, 22)); g2d.setColor(Color.WHITE);
        g2d.drawString("Arrows - Move / Jump", p2X, startY + spacing); g2d.drawString("Arrows (Double Tap) - Teleport", p2X, startY + spacing * 2); g2d.drawString("L - Punch / Smash / Select", p2X, startY + spacing * 3); g2d.drawString("P - Kick", p2X, startY + spacing * 4); g2d.drawString("O - Ki Blast", p2X, startY + spacing * 5); g2d.drawString("K - Special Attack", p2X, startY + spacing * 6); g2d.drawString("N - Fly", p2X, startY + spacing * 7); g2d.drawString("M - Charge Aura", p2X, startY + spacing * 8); g2d.drawString("U - Block", p2X, startY + spacing * 9);

        g2d.setColor(Color.GRAY); setCustomFont(g2d, 35f); g2d.drawString("Press BLOCK to return to Menu", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Press BLOCK to return to Menu")) / 2, 680);
    }

    private void drawCredits(Graphics2D g2d) {
        if (rm.mainMenuBg != null) { g2d.drawImage(rm.mainMenuBg, 0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null); g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT); }
        else { g2d.setColor(new Color(40, 20, 20)); g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT); }

        g2d.setColor(Color.YELLOW); setCustomFont(g2d, 100f); g2d.drawString("CREDITS", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("CREDITS")) / 2, 180);
        g2d.setColor(Color.WHITE); setCustomFont(g2d, 45f); g2d.drawString("Developed by", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Developed by")) / 2, 300);
        g2d.setColor(Color.CYAN); setCustomFont(g2d, 60f); g2d.drawString("Leonardo Alunno", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Leonardo Alunno")) / 2, 380);
        g2d.setColor(Color.WHITE); setCustomFont(g2d, 35f); g2d.drawString("Based on Dragon Ball Z assets", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Based on Dragon Ball Z assets")) / 2, 500);
        g2d.setColor(Color.GRAY); setCustomFont(g2d, 30f); g2d.drawString("Press BLOCK to return to Menu", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("Press BLOCK to return to Menu")) / 2, 650);
    }
}