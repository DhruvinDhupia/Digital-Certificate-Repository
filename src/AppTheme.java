import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class AppTheme {
    public static final Color NAVY = new Color(19, 29, 51);
    public static final Color NAVY_LIGHT = new Color(31, 45, 72);
    public static final Color BLUE = new Color(55, 112, 235);
    public static final Color BG = new Color(245, 247, 251);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(32, 42, 58);
    public static final Color MUTED = new Color(105, 117, 135);
    public static final Color GREEN = new Color(27, 146, 99);
    public static final Color ORANGE = new Color(211, 133, 36);

    private AppTheme() {}

    public static void apply() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("Button.background", CARD);
        UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("ComboBox.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Table.font", new Font("SansSerif", Font.PLAIN, 12));
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.selectionBackground", new Color(220, 232, 255));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.gridColor", new Color(231, 235, 242));
        UIManager.put("TableHeader.background", new Color(236, 240, 247));
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 23));
        label.setForeground(TEXT);
        return label;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 13, 9, 13));
        return b;
    }

    public static JButton primaryButton(String text) {
        JButton b = button(text);
        b.setBackground(BLUE);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    public static JPanel card() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 234, 242)),
                new EmptyBorder(15, 16, 15, 16)));
        return p;
    }
}
