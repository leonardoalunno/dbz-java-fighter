import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // In Swing, la GUI dovrebbe sempre essere creata nell'Event Dispatch Thread (EDT)

        SwingUtilities.invokeLater(() -> {

            // Creiamo la finestra
            JFrame window = new JFrame("Dragon Ball Z: Super Butōden 3 - Progetto d'Esame");

            // Diciamo al programma di chiudersi quando premiamo la X
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Impediamo all'utente di ridimensionare la finestra e sballare le proporzioni
            window.setResizable(false);

            // Creiamo la nostra "tela" e la aggiungiamo alla finestra
            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);

            // Pack dice alla finestra di "restringersi" attorno alle dimensioni del GamePanel
            window.pack();

            // Centriamo la finestra al centro dello schermo del tuo Mac
            window.setLocationRelativeTo(null);

            // Finalmente, rendiamo tutto visibile!
            window.setVisible(true);

            // ACCENDIAMO IL MOTORE!
            gamePanel.startGameThread();

        });
    }
}