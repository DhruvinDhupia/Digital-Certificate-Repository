import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ActivityLogFrame extends JInternalFrame {
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Admin User","Action","Details","Time"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=new JTable(model);
    public ActivityLogFrame(){setTitle("Admin Activity Log");setSize(950,600);setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout(12,12));root.setBackground(AppTheme.BG);root.setBorder(new EmptyBorder(20,20,20,20));JPanel head=new JPanel(new BorderLayout());head.setOpaque(false);head.add(AppTheme.title("Activity Log"),BorderLayout.NORTH);JLabel sub=new JLabel("Recent administrative actions recorded by the application");sub.setForeground(AppTheme.MUTED);head.add(sub,BorderLayout.SOUTH);root.add(head,BorderLayout.NORTH);
        JPanel card=AppTheme.card();table.setAutoCreateRowSorter(true);table.setFillsViewportHeight(true);table.getColumnModel().getColumn(0).setPreferredWidth(45);table.getColumnModel().getColumn(1).setPreferredWidth(100);table.getColumnModel().getColumn(2).setPreferredWidth(150);table.getColumnModel().getColumn(3).setPreferredWidth(350);card.add(new JScrollPane(table),BorderLayout.CENTER);root.add(card,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.setOpaque(false);JButton refresh=AppTheme.button("Refresh"),close=AppTheme.button("← Back to Dashboard");bottom.add(refresh);bottom.add(close);root.add(bottom,BorderLayout.SOUTH);setContentPane(root);refresh.addActionListener(e->loadLogs());close.addActionListener(e->dispose());loadLogs();}
    private void loadLogs(){model.setRowCount(0);try{DatabaseSetup.ensureSchema();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,username,action,details,created_at FROM activity_logs ORDER BY created_at DESC LIMIT 500");ResultSet r=p.executeQuery()){while(r.next())model.addRow(new Object[]{r.getInt("id"),r.getString("username"),r.getString("action"),r.getString("details"),r.getTimestamp("created_at")});}}catch(SQLException ex){JOptionPane.showMessageDialog(this,"Could not load activity log: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}}
}
