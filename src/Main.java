import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        AppTheme.apply();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
