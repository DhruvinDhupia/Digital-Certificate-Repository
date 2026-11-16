import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Small custom Swing chart; no charting dependency is used. */
public class AnalyticsPanel extends JPanel {
    private final List<String> departments = new ArrayList<>();
    private final List<Integer> counts = new ArrayList<>();
    private int verified;
    private int pending;

    public AnalyticsPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(450, 230));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        refreshData();
    }

    public void refreshData() {
        departments.clear(); counts.clear(); verified = 0; pending = 0;
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement()) {
            try (ResultSet r = s.executeQuery("SELECT status, COUNT(*) AS total FROM certificates GROUP BY status")) {
                while (r.next()) { if ("Verified".equalsIgnoreCase(r.getString("status"))) verified = r.getInt("total"); else pending = r.getInt("total"); }
            }
            try (ResultSet r = s.executeQuery("SELECT department, COUNT(*) AS total FROM certificates GROUP BY department ORDER BY total DESC LIMIT 6")) {
                while (r.next()) { departments.add(r.getString("department")); counts.add(r.getInt("total")); }
            }
        } catch (SQLException ignored) {}
        repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setColor(AppTheme.TEXT); g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("Certificate Analytics", 12, 22);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11)); g.setColor(AppTheme.MUTED);
        g.drawString("By department", 12, 43);
        int chartLeft = 12, chartTop = 55, chartBottom = h - 32, chartW = Math.max(150, (w * 2 / 3) - 20);
        int max = 1; for (int n : counts) max = Math.max(max, n);
        int barH = Math.max(12, Math.min(22, (chartBottom - chartTop - 8) / Math.max(1, departments.size())));
        for (int i=0; i<departments.size(); i++) {
            int y = chartTop + i * (barH + 8);
            g.setColor(AppTheme.MUTED); g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String name = departments.get(i); if (name.length() > 15) name = name.substring(0, 14) + "…";
            g.drawString(name, chartLeft, y + 11);
            int barX = chartLeft + 105;
            int available = Math.max(30, chartW - 120);
            int barWidth = Math.max(3, (int)(available * (counts.get(i) / (double) max)));
            g.setColor(new Color(226, 234, 248)); g.fillRoundRect(barX, y, available, barH, 8, 8);
            g.setColor(AppTheme.BLUE); g.fillRoundRect(barX, y, barWidth, barH, 8, 8);
            g.setColor(AppTheme.TEXT); g.drawString(String.valueOf(counts.get(i)), barX + barWidth + 5, y + 11);
        }
        int sx = Math.max(w * 2 / 3, 260);
        g.setColor(AppTheme.MUTED); g.setFont(new Font("SansSerif", Font.PLAIN, 11)); g.drawString("Status overview", sx, 43);
        g.setColor(AppTheme.GREEN); g.fillRoundRect(sx, 62, 12, 12, 4, 4);
        g.setColor(AppTheme.TEXT); g.setFont(new Font("SansSerif", Font.PLAIN, 12)); g.drawString("Verified", sx + 20, 72);
        g.setFont(new Font("SansSerif", Font.BOLD, 18)); g.drawString(String.valueOf(verified), sx + 20, 95);
        g.setColor(AppTheme.ORANGE); g.fillRoundRect(sx, 115, 12, 12, 4, 4);
        g.setColor(AppTheme.TEXT); g.setFont(new Font("SansSerif", Font.PLAIN, 12)); g.drawString("Pending", sx + 20, 125);
        g.setFont(new Font("SansSerif", Font.BOLD, 18)); g.drawString(String.valueOf(pending), sx + 20, 148);
        g.dispose();
    }
}
