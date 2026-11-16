import javax.swing.*;
import java.awt.*;

/** Hosts all application pages inside one top-level window. */
public final class AppWindow {
    private static JFrame frame;
    private static JDesktopPane desktop;
    private static DashboardFrame dashboard;
    private AppWindow() {}

    public static void openDashboard() {
        if (frame == null) {
            frame = new JFrame("Digital Certificate Repository");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1000, 700));
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            desktop = new JDesktopPane();
            desktop.setBackground(AppTheme.BG);
            frame.setContentPane(desktop);
        }
        if (dashboard == null || dashboard.isClosed()) dashboard = new DashboardFrame();
        if (dashboard.getParent() == null) desktop.add(dashboard);
        dashboard.setBounds(0, 0, desktop.getWidth() > 0 ? desktop.getWidth() : 1200, desktop.getHeight() > 0 ? desktop.getHeight() : 800);
        configureInternalFrame(dashboard, false);
        dashboard.setVisible(true);
        frame.setVisible(true);
        try { dashboard.setSelected(true); } catch (java.beans.PropertyVetoException ignored) {}
    }

    public static void showPage(JInternalFrame page) {
        if (frame == null) { openDashboard(); }
        configureInternalFrame(page, true);
        desktop.add(page);
        page.setBounds(0, 0, Math.max(900, desktop.getWidth()), Math.max(600, desktop.getHeight()));
        page.setVisible(true);
        try { page.setMaximum(true); page.setSelected(true); } catch (java.beans.PropertyVetoException ignored) {}
        page.moveToFront();
    }

    private static void configureInternalFrame(JInternalFrame page, boolean closable) {
        page.setClosable(closable);
        page.setResizable(true);
        page.setMaximizable(true);
        page.setIconifiable(false);
        if (closable && !(page.getRootPane().getClientProperty("backBarAdded") instanceof Boolean)) {
            Container original = page.getContentPane();
            JPanel wrapper = new JPanel(new BorderLayout());
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
            top.setBackground(AppTheme.BG);
            JButton back = AppTheme.button("← Back to Dashboard");
            back.addActionListener(e -> page.dispose());
            top.add(back);
            wrapper.add(top, BorderLayout.NORTH);
            page.setContentPane(wrapper);
            wrapper.add(original, BorderLayout.CENTER);
            page.getRootPane().putClientProperty("backBarAdded", Boolean.TRUE);
        }
    }

    public static void logout() {
        if (frame != null) { frame.dispose(); frame = null; desktop = null; dashboard = null; }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
