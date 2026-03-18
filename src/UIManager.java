import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class UIManager {

    private GamePanel gp;
    private ResourceManager rm;

    public static final Color COLOR_P1 = new Color(129, 4, 4);   // Rosso estratto da vs_left
    public static final Color COLOR_P2 = new Color(0, 141, 141); // Ciano estratto da vs_right

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

        BufferedImage[] icons = {rm.iconGoku, rm.iconVegeta, rm.iconFutureTrunks, rm.iconBroly, rm.iconBeerus};
        int size = 110, spacing = 160, startX = (GamePanel.SCREEN_WIDTH - (spacing * 4 + size)) / 2, startY = 300;

        for(int i = 0; i < 5; i++) {
            int x = startX + (i * spacing);
            if (icons[i] != null) g2d.drawImage(icons[i], x, startY, size, size, null);
            g2d.setColor(Color.WHITE); setCustomFont(g2d, 28f);
            g2d.drawString(gp.charNames[i], x + (size / 2) - (g2d.getFontMetrics().stringWidth(gp.charNames[i]) / 2), startY + size + 45);
        }

        g2d.setStroke(new BasicStroke(5));

        // --- DISEGNO CURSORE E SCRITTA P1 ---
        g2d.setColor(COLOR_P1); // Rosso profondo (129, 4, 4)
        int p1X = startX + (gp.p1Cursor * spacing);
        g2d.drawRect(p1X - 6, startY - 6, size + 12, size + 12);

        if(gp.p1Ready) {
            setBangersFont(g2d, 32f);
            g2d.drawString("P1 READY", p1X + (size / 2) - (g2d.getFontMetrics().stringWidth("P1 READY") / 2), startY - 20);
        }

        // --- DISEGNO CURSORE E SCRITTA P2 ---
        g2d.setColor(COLOR_P2); // Ciano tecnologico (0, 141, 141)
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
        ResourceManager rm = ResourceManager.getInstance();

        // Sfondo scuro
        g2d.setColor(new Color(20, 20, 30));
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        g2d.setColor(Color.YELLOW);
        setCustomFont(g2d, 70f);
        g2d.drawString("STAGE SELECT", (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth("STAGE SELECT")) / 2, 100);

        // --- DISEGNO PREVIEW DELLO STAGE CENTRALE (DIMENSIONI CORRETTE) ---
        BufferedImage currentIcon = rm.stageIcons[gp.stageCursor];
        if (currentIcon != null) {
            // Fissiamo l'altezza massima per non uscire dallo schermo!
            int iconH = 380;
            // Calcoliamo la larghezza in base alle proporzioni dell'immagine
            int iconW = (iconH * currentIcon.getWidth()) / currentIcon.getHeight();

            int iconX = (GamePanel.SCREEN_WIDTH - iconW) / 2;
            int iconY = 160; // Alzato un pochino per centrarlo meglio

            // Bordo bianco per farla risaltare
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(iconX - 6, iconY - 6, iconW + 12, iconH + 12);

            g2d.drawImage(currentIcon, iconX, iconY, iconW, iconH, null);

            // Frecce colorate a lato per indicare lo scorrimento
            setBangersFont(g2d, 80f);
            g2d.setColor(Color.WHITE); // Sostituito P1/P2 color con Bianco
            g2d.drawString("<", iconX - 80, iconY + (iconH / 2) + 25);
            g2d.drawString(">", iconX + iconW + 40, iconY + (iconH / 2) + 25);
        }

        // --- NOME DELLO STAGE (Sotto l'immagine) ---
        g2d.setColor(Color.WHITE);
        setCustomFont(g2d, 50f);
        String sName = gp.stageNames[gp.stageCursor];
        // Scriviamo il nome a Y = 620 (così sta perfettamente sotto l'immagine che finisce a 540)
        g2d.drawString(sName, (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth(sName)) / 2, 620);
    }


    private void drawVsScreen(Graphics2D g2d) {
        ResourceManager rm = ResourceManager.getInstance();

        // 1. Sfondo nero di base
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // --- CALCOLO ANIMAZIONE SLIDE-IN ---
        // Usiamo lo stateTimer per farli scivolare dentro nei primi 30 frame (mezzo secondo)
        int slideAnim = Math.max(0, 30 - gp.stateTimer) * 40;

        // 2. Disegniamo gli sfondi diagonali (vs_left e vs_right) con l'animazione
        if (rm.vsLeft != null) g2d.drawImage(rm.vsLeft, -slideAnim, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null);
        if (rm.vsRight != null) g2d.drawImage(rm.vsRight, slideAnim, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null);

        // 3. DISEGNO DEI PORTRAIT CON SCALA DINAMICA
        BufferedImage p1Img = rm.portraits[gp.p1Cursor];
        BufferedImage p2Img = rm.portraits[gp.p2Cursor];

        int targetHeight = 650; // Altezza fissa universale per allinearli tutti perfettamente!

        // --- PORTRAIT P1 (Sinistra) ---
        if (p1Img != null) {
            int w1 = (targetHeight * p1Img.getWidth()) / p1Img.getHeight();
            // Ancorato in basso, scivola da sinistra
            int x1 = 50 - slideAnim;
            int y1 = GamePanel.SCREEN_HEIGHT - targetHeight;
            g2d.drawImage(p1Img, x1, y1, w1, targetHeight, null);
        }

        // --- PORTRAIT P2 (Destra) - SPECCHIATO! ---
        if (p2Img != null) {
            int w2 = (targetHeight * p2Img.getWidth()) / p2Img.getHeight();
            // Ancorato in basso, scivola da destra
            int x2 = GamePanel.SCREEN_WIDTH - 50 + slideAnim;
            int y2 = GamePanel.SCREEN_HEIGHT - targetHeight;

            // Per specchiare l'immagine, scambiamo i punti finali di X del rettangolo di destinazione
            g2d.drawImage(p2Img,
                    x2, y2, x2 - w2, y2 + targetHeight, // Destinazione (disegnata al contrario)
                    0, 0, p2Img.getWidth(), p2Img.getHeight(), // Sorgente originale
                    null);
        }

        // 4. BAGLIORE VS ROTANTE IN CENTRO
        if (rm.vsBg != null) {
            // --- MODIFICATO: Rimpiccioliamo il sole ---
            int glowSize = 480; // Era 700. Ora è molto più compatto e meno invasivo.

            int cx = GamePanel.SCREEN_WIDTH / 2;
            int cy = GamePanel.SCREEN_HEIGHT / 2;

            java.awt.geom.AffineTransform oldXForm = g2d.getTransform();
            g2d.translate(cx, cy);
            g2d.rotate(Math.toRadians(gp.stateTimer * 3));
            g2d.drawImage(rm.vsBg, -glowSize / 2, -glowSize / 2, glowSize, glowSize, null);
            g2d.setTransform(oldXForm);
        }

        // 5. ICONA VS CENTRALE
        if (rm.vsIcon != null) {
            // --- MODIFICATO: Rimpiccioliamo le lettere VS ---
            int iconW = 240; // Era 350. Ora è proporzionato al nuovo sole centrale.

            int iconH = (iconW * rm.vsIcon.getHeight()) / rm.vsIcon.getWidth();
            if (gp.stateTimer < 15) {
                iconW += (15 - gp.stateTimer) * 10;
                iconH += (15 - gp.stateTimer) * 10;
            }
            g2d.drawImage(rm.vsIcon, (GamePanel.SCREEN_WIDTH - iconW) / 2, (GamePanel.SCREEN_HEIGHT - iconH) / 2, iconW, iconH, null);
        }

        // 6. NOMI DEI LOTTATORI
        g2d.setColor(Color.WHITE);
        setCustomFont(g2d, 55f);

        // Nome P1 (Spostato un po' più al centro)
        g2d.drawString(gp.charNames[gp.p1Cursor], 80 - slideAnim, GamePanel.SCREEN_HEIGHT - 40);

        // Nome P2
        String p2Name = gp.charNames[gp.p2Cursor];
        g2d.drawString(p2Name, GamePanel.SCREEN_WIDTH - 80 - g2d.getFontMetrics().stringWidth(p2Name) + slideAnim, GamePanel.SCREEN_HEIGHT - 40);
    }

    private void drawBattle(Graphics2D g2d) {
        // --- DISEGNO BACKGROUND DINAMICO (USANDO L'ARRAY DEI 17 STAGE) ---
        BufferedImage bg = rm.stageBgs[gp.stageCursor];
        if (bg != null) {
            g2d.drawImage(bg, 0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null);
        }

        // --- DISEGNO PERSONAGGI (Gestione Z-Index per chi attacca) ---
        if (gp.player1 != null && gp.player2 != null) {
            if (gp.player2.isAttacking() && !gp.player1.isAttacking()) {
                gp.player1.draw(g2d);
                gp.player2.draw(g2d);
            } else {
                gp.player2.draw(g2d);
                gp.player1.draw(g2d);
            }
        }

        // --- STATUS ICONS (Ready, Fight, KO) ---
        BufferedImage currentStatusIcon = null; int targetH = 120;
        if (gp.battlePhase == 0) currentStatusIcon = rm.readyIcon;
        else if (gp.battlePhase == 1) currentStatusIcon = rm.fightIcon;
        else if (gp.battlePhase == 3) currentStatusIcon = rm.koIcon;

        if (currentStatusIcon != null) {
            int drawW = (targetH * currentStatusIcon.getWidth()) / currentStatusIcon.getHeight();
            g2d.drawImage(currentStatusIcon, (GamePanel.SCREEN_WIDTH - drawW) / 2, 250, drawW, targetH, null);
        }

        // --- SCHERMATA VITTORIA (Fase 4) ---
        if (gp.battlePhase == 4) {
            // Overlay scuro
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

            boolean p2ko = (gp.player2.state == FighterState.KO);
            String winnerText = p2ko ? "PLAYER ONE WINS" : "PLAYER TWO WINS";
            g2d.setColor(p2ko ? COLOR_P1 : COLOR_P2);

            setCustomFont(g2d, 90f);
            g2d.drawString(winnerText, (GamePanel.SCREEN_WIDTH - g2d.getFontMetrics().stringWidth(winnerText)) / 2, 300);

            g2d.setColor(Color.GRAY);
            setCustomFont(g2d, 35f);
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
        g2d.drawString("F - Light Attack", p1X, startY + spacing * 3);
        g2d.drawString("R - Heavy Attack", p1X, startY + spacing * 4);
        g2d.drawString("E - Ki Blast (Special)", p1X, startY + spacing * 5);
        g2d.drawString("Z - Ultimate", p1X, startY + spacing * 6);
        g2d.drawString("V - Fly", p1X, startY + spacing * 7);
        g2d.drawString("C - Charge Ki", p1X, startY + spacing * 8);
        g2d.drawString("Q - Block / Z-Cancel", p1X, startY + spacing * 9);

        g2d.setColor(Color.RED); setCustomFont(g2d, 50f); g2d.drawString("PLAYER TWO", p2X, startY);
        g2d.setFont(new Font("Arial", Font.BOLD, 22)); g2d.setColor(Color.WHITE);
        g2d.drawString("L - Light Attack", p2X, startY + spacing * 3);
        g2d.drawString("P - Heavy Attack", p2X, startY + spacing * 4);
        g2d.drawString("O - Ki Blast (Special)", p2X, startY + spacing * 5);
        g2d.drawString("K - Ultimate", p2X, startY + spacing * 6);
        g2d.drawString("N - Fly", p2X, startY + spacing * 7);
        g2d.drawString("M - Charge Ki", p2X, startY + spacing * 8);
        g2d.drawString("U - Block / Z-Cancel", p2X, startY + spacing * 9);

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