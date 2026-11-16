import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class DashboardFrame extends JInternalFrame {
    private final JLabel totalCertificatesLabel = statValue();
    private final JLabel totalStudentsLabel = statValue();
    private final JLabel verifiedLabel = statValue();
    private final JLabel pendingLabel = statValue();
    private final AnalyticsPanel analyticsPanel = new AnalyticsPanel();

    public DashboardFrame() {
        setTitle("Digital Certificate Repository | Dashboard");
        setSize(1100, 740); setMinimumSize(new Dimension(900, 620));
         setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        try { DatabaseSetup.ensureSchema(); } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Could not initialize database tables: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }

        JPanel root = new JPanel(new BorderLayout()); root.setBackground(AppTheme.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        JPanel content = new JPanel(new BorderLayout(18, 18)); content.setBackground(AppTheme.BG);
        content.setBorder(new EmptyBorder(24, 26, 24, 26));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel headings = new JPanel(new GridLayout(2, 1, 0, 4)); headings.setOpaque(false);
        headings.add(AppTheme.title("Dashboard"));
        JLabel subtitle = new JLabel("Overview of your certificate repository"); subtitle.setForeground(AppTheme.MUTED); headings.add(subtitle);
        header.add(headings, BorderLayout.WEST);
        JLabel user = new JLabel("Signed in as  " + Session.getUsername() + "   "); user.setForeground(AppTheme.MUTED); header.add(user, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(16, 16)); middle.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 14)); stats.setOpaque(false);
        stats.add(statCard("TOTAL CERTIFICATES", totalCertificatesLabel, new Color(55,112,235)));
        stats.add(statCard("UNIQUE STUDENTS", totalStudentsLabel, new Color(116,86,220)));
        stats.add(statCard("VERIFIED", verifiedLabel, AppTheme.GREEN));
        stats.add(statCard("PENDING", pendingLabel, AppTheme.ORANGE));
        middle.add(stats, BorderLayout.NORTH);
        JPanel chartCard = AppTheme.card(); chartCard.add(analyticsPanel, BorderLayout.CENTER);
        middle.add(chartCard, BorderLayout.CENTER);
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); quick.setOpaque(false);
        JButton add = AppTheme.primaryButton("+ Add Certificate"); JButton repo = AppTheme.button("Open Repository"); JButton verify = AppTheme.button("Verify Certificate"); JButton refresh = AppTheme.button("Refresh Analytics");
        quick.add(add); quick.add(repo); quick.add(verify); quick.add(refresh); middle.add(quick, BorderLayout.SOUTH);
        content.add(middle, BorderLayout.CENTER); root.add(content, BorderLayout.CENTER); setContentPane(root);

        add.addActionListener(e -> AppWindow.showPage(new AddCertificateFrame(this, null)));
        repo.addActionListener(e -> AppWindow.showPage(new CertificateListFrame(this)));
        verify.addActionListener(e -> AppWindow.showPage(new VerifyCertificateFrame()));
        refresh.addActionListener(e -> { refreshStatistics(); analyticsPanel.refreshData(); });
        refreshStatistics();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(); side.setBackground(AppTheme.NAVY); side.setPreferredSize(new Dimension(220, 700)); side.setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.setBorder(new EmptyBorder(24, 18, 24, 18));
        JLabel logo = new JLabel("DCR  /  ADMIN"); logo.setForeground(Color.WHITE); logo.setFont(new Font("SansSerif", Font.BOLD, 17)); top.add(logo, BorderLayout.NORTH);
        JLabel small = new JLabel("Certificate workspace"); small.setForeground(new Color(180, 193, 216)); small.setBorder(new EmptyBorder(8,0,0,0)); top.add(small, BorderLayout.SOUTH); side.add(top, BorderLayout.NORTH);
        JPanel nav = new JPanel(new GridLayout(0,1,0,8)); nav.setOpaque(false); nav.setBorder(new EmptyBorder(5, 12, 5, 12));
        addNav(nav, "Dashboard", () -> { refreshStatistics(); analyticsPanel.refreshData(); });
        addNav(nav, "Certificate Repository", () -> AppWindow.showPage(new CertificateListFrame(this)));
        addNav(nav, "Add Certificate", () -> AppWindow.showPage(new AddCertificateFrame(this, null)));
        addNav(nav, "Verify Certificate", () -> AppWindow.showPage(new VerifyCertificateFrame()));
        addNav(nav, "Student Profiles", () -> AppWindow.showPage(new StudentProfileFrame()));
        addNav(nav, "Activity Log", () -> AppWindow.showPage(new ActivityLogFrame()));
        addNav(nav, "Admin Settings", () -> AppWindow.showPage(new AdminSettingsFrame()));
        addNav(nav, "Backup / Restore", () -> AppWindow.showPage(new BackupRestoreFrame()));
        side.add(nav, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new GridLayout(1,1)); bottom.setOpaque(false); bottom.setBorder(new EmptyBorder(12,12,20,12));
        JButton logout = new JButton("Log out"); logout.setForeground(Color.WHITE); logout.setBackground(AppTheme.NAVY_LIGHT); logout.setFocusPainted(false); logout.setBorderPainted(false); bottom.add(logout); side.add(bottom, BorderLayout.SOUTH);
        logout.addActionListener(e -> { int answer=JOptionPane.showConfirmDialog(this,"Log out of the admin session?","Confirm logout",JOptionPane.YES_NO_OPTION); if(answer==JOptionPane.YES_OPTION){ ActivityLogger.log("LOGOUT","Administrator logged out"); AppWindow.logout(); }});
        return side;
    }

    private void addNav(JPanel nav, String text, Runnable action) {
        JButton b = new JButton(text); b.setHorizontalAlignment(SwingConstants.LEFT); b.setForeground(new Color(232,237,247)); b.setBackground(AppTheme.NAVY); b.setFocusPainted(false); b.setBorderPainted(false); b.setBorder(new EmptyBorder(12,12,12,8)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addActionListener(e -> action.run()); nav.add(b);
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel card = AppTheme.card(); JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel t = new JLabel(title); t.setForeground(AppTheme.MUTED); t.setFont(new Font("SansSerif", Font.BOLD, 10)); top.add(t, BorderLayout.WEST);
        JPanel bar = new JPanel(); bar.setBackground(accent); bar.setPreferredSize(new Dimension(4,28)); top.add(bar, BorderLayout.EAST); card.add(top, BorderLayout.NORTH);
        value.setForeground(AppTheme.TEXT); card.add(value, BorderLayout.CENTER); return card;
    }
    private static JLabel statValue() { JLabel l = new JLabel("0"); l.setFont(new Font("SansSerif", Font.BOLD, 30)); return l; }

    public void refreshStatistics() {
        String sql = "SELECT (SELECT COUNT(*) FROM certificates) total_certificates, (SELECT COUNT(DISTINCT enrollment_number) FROM certificates) total_students, (SELECT COUNT(*) FROM certificates WHERE status='Verified') verified_count, (SELECT COUNT(*) FROM certificates WHERE status='Pending') pending_count";
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (r.next()) { totalCertificatesLabel.setText(r.getString("total_certificates")); totalStudentsLabel.setText(r.getString("total_students")); verifiedLabel.setText(r.getString("verified_count")); pendingLabel.setText(r.getString("pending_count")); }
            analyticsPanel.refreshData();
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Could not load dashboard statistics. Check MySQL connection.\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
    }
}
