import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class StudentProfileFrame extends JInternalFrame {
    private final JTextField enrollmentField = new JTextField(18);
    private final JLabel nameValue = new JLabel("—"), courseValue = new JLabel("—"), departmentValue = new JLabel("—"), countValue = new JLabel("0");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Certificate No.","Type","Course","Department","Issue Date","Status","File"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table = new JTable(model);
    public StudentProfileFrame(){this("");}
    public StudentProfileFrame(String enrollment){
        setTitle("Student Profile");setSize(900,570);setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout(12,12));root.setBackground(AppTheme.BG);root.setBorder(new EmptyBorder(20,20,20,20));
        JPanel head=new JPanel(new BorderLayout(8,8));head.setOpaque(false);JPanel titles=new JPanel(new GridLayout(2,1,0,4));titles.setOpaque(false);titles.add(AppTheme.title("Student Profile"));JLabel sub=new JLabel("Look up a student by enrollment number and review their certificates");sub.setForeground(AppTheme.MUTED);titles.add(sub);head.add(titles,BorderLayout.NORTH);
        JPanel search=new JPanel(new FlowLayout(FlowLayout.LEFT,8,6));search.setOpaque(false);search.add(new JLabel("Enrollment Number"));search.add(enrollmentField);JButton find=AppTheme.primaryButton("Find Student");search.add(find);head.add(search,BorderLayout.SOUTH);root.add(head,BorderLayout.NORTH);
        JPanel profile=AppTheme.card();profile.setLayout(new GridLayout(1,4,10,5));profile.add(infoBlock("Student Name",nameValue));profile.add(infoBlock("Course",courseValue));profile.add(infoBlock("Department",departmentValue));profile.add(infoBlock("Certificates",countValue));
        JPanel center=new JPanel(new BorderLayout(10,10));center.setOpaque(false);center.add(profile,BorderLayout.NORTH);JPanel tableCard=AppTheme.card();table.setAutoCreateRowSorter(true);tableCard.add(new JScrollPane(table),BorderLayout.CENTER);center.add(tableCard,BorderLayout.CENTER);root.add(center,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.setOpaque(false);JButton view=AppTheme.button("Preview Selected Certificate");JButton close=AppTheme.button("← Back to Dashboard");bottom.add(view);bottom.add(close);root.add(bottom,BorderLayout.SOUTH);setContentPane(root);
        find.addActionListener(e->loadProfile());enrollmentField.addActionListener(e->loadProfile());view.addActionListener(e->viewSelected());close.addActionListener(e->dispose());
        enrollmentField.setText(enrollment);if(!enrollment.isBlank())loadProfile();
    }
    private JPanel infoBlock(String title,JLabel value){JPanel p=new JPanel(new GridLayout(2,1,0,5));p.setOpaque(false);JLabel t=new JLabel(title);t.setForeground(AppTheme.MUTED);t.setFont(new Font("SansSerif",Font.BOLD,11));value.setFont(new Font("SansSerif",Font.BOLD,15));p.add(t);p.add(value);return p;}
    private void loadProfile(){String enrollment=enrollmentField.getText().trim();if(enrollment.isEmpty()){JOptionPane.showMessageDialog(this,"Enter an enrollment number.");return;}model.setRowCount(0);nameValue.setText("—");courseValue.setText("—");departmentValue.setText("—");countValue.setText("0");
        String sql="SELECT certificate_number,student_name,enrollment_number,course,department,certificate_type,issue_date,status,file_path FROM certificates WHERE enrollment_number=? ORDER BY issue_date DESC";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,enrollment);try(ResultSet r=p.executeQuery()){while(r.next()){if(model.getRowCount()==0){nameValue.setText(r.getString("student_name"));courseValue.setText(r.getString("course"));departmentValue.setText(r.getString("department"));}model.addRow(new Object[]{r.getString("certificate_number"),r.getString("certificate_type"),r.getString("course"),r.getString("department"),r.getDate("issue_date"),r.getString("status"),r.getString("file_path")});}}countValue.setText(String.valueOf(model.getRowCount()));if(model.getRowCount()==0)JOptionPane.showMessageDialog(this,"No certificates found for enrollment "+enrollment,"Student not found",JOptionPane.INFORMATION_MESSAGE);else ActivityLogger.log("VIEW_STUDENT_PROFILE",enrollment);}
        catch(SQLException ex){JOptionPane.showMessageDialog(this,"Could not load student profile: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}}
    private void viewSelected(){int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Select a certificate first.");return;}row=table.convertRowIndexToModel(row);String path=String.valueOf(model.getValueAt(row,6));if(path.isBlank()){JOptionPane.showMessageDialog(this,"No file is linked to this certificate.");return;}File f=CertificateFiles.resolve(path).toFile();if(!f.exists()){JOptionPane.showMessageDialog(this,"Certificate file not found: "+f.getAbsolutePath());return;}try{Desktop.getDesktop().open(f);ActivityLogger.log("VIEW_CERTIFICATE",String.valueOf(model.getValueAt(row,0)));}catch(Exception ex){JOptionPane.showMessageDialog(this,"Could not open file: "+ex.getMessage());}}
}
