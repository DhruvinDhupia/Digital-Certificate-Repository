import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;

public class CertificateListFrame extends JInternalFrame {
    private final DashboardFrame dashboardFrame;
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> departmentFilter = new JComboBox<>(new String[]{"All"});
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Verified", "Pending"});
    private final JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All"});
    private final JComboBox<Integer> pageSizeCombo = new JComboBox<>(new Integer[]{10, 25, 50, 100});
    private final JLabel pageLabel = new JLabel("Page 1");
    private final JLabel resultsLabel = new JLabel("0 records");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Certificate No.", "Student", "Enrollment", "Course", "Department", "Type", "Issue Date", "Status", "File"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private int page = 1;
    private int totalRows = 0;

    public CertificateListFrame(DashboardFrame dashboardFrame) {
        this.dashboardFrame = dashboardFrame;
        setTitle("Certificate Repository"); setSize(1250, 700); setMinimumSize(new Dimension(1000, 580));
         setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(12,12)); root.setBackground(AppTheme.BG); root.setBorder(new EmptyBorder(20,20,18,20));
        JPanel header = new JPanel(new BorderLayout(8,8)); header.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2,1,0,4)); titles.setOpaque(false); titles.add(AppTheme.title("Certificate Repository"));
        JLabel sub = new JLabel("Search, filter, inspect and manage stored certificates"); sub.setForeground(AppTheme.MUTED); titles.add(sub); header.add(titles,BorderLayout.NORTH);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); filters.setOpaque(false);
        searchField.setToolTipText("Search student, enrollment or certificate number");
        JButton searchBtn = AppTheme.primaryButton("Search"); JButton clearBtn = AppTheme.button("Clear");
        filters.add(new JLabel("Search")); filters.add(searchField); filters.add(searchBtn); filters.add(new JLabel("Department")); filters.add(departmentFilter); filters.add(new JLabel("Status")); filters.add(statusFilter); filters.add(new JLabel("Type")); filters.add(typeFilter); filters.add(clearBtn);
        header.add(filters,BorderLayout.SOUTH); root.add(header,BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true); table.setFillsViewportHeight(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setShowVerticalLines(false); table.setIntercellSpacing(new Dimension(0,1));
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0); table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(130); table.getColumnModel().getColumn(2).setPreferredWidth(145); table.getColumnModel().getColumn(3).setPreferredWidth(110); table.getColumnModel().getColumn(9).setPreferredWidth(150);
        JPanel tableCard = AppTheme.card(); tableCard.add(new JScrollPane(table),BorderLayout.CENTER); root.add(tableCard,BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10,10)); footer.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); left.setOpaque(false);
        JButton view = AppTheme.button("Preview / Open"); JButton download = AppTheme.button("Save As"); JButton edit = AppTheme.button("Edit"); JButton delete = AppTheme.button("Delete"); JButton profile = AppTheme.button("Student Profile"); JButton export = AppTheme.button("Export CSV"); JButton refresh = AppTheme.button("Refresh");
        left.add(view); left.add(download); left.add(edit); left.add(delete); left.add(profile); left.add(export); left.add(refresh);
        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); paging.setOpaque(false); JButton prev=AppTheme.button("‹ Previous"); JButton next=AppTheme.button("Next ›"); paging.add(new JLabel("Rows")); paging.add(pageSizeCombo); paging.add(resultsLabel); paging.add(prev); paging.add(pageLabel); paging.add(next);
        footer.add(left,BorderLayout.WEST); footer.add(paging,BorderLayout.EAST); root.add(footer,BorderLayout.SOUTH); setContentPane(root);

        searchBtn.addActionListener(e->{page=1;loadAllCertificates();}); searchField.addActionListener(e->{page=1;loadAllCertificates();});
        clearBtn.addActionListener(e->{searchField.setText("");departmentFilter.setSelectedItem("All");statusFilter.setSelectedItem("All");typeFilter.setSelectedItem("All");page=1;loadAllCertificates();});
        departmentFilter.addActionListener(e->{page=1;loadAllCertificates();}); statusFilter.addActionListener(e->{page=1;loadAllCertificates();}); typeFilter.addActionListener(e->{page=1;loadAllCertificates();});
        pageSizeCombo.addActionListener(e->{page=1;loadAllCertificates();}); prev.addActionListener(e->{if(page>1){page--;loadAllCertificates();}}); next.addActionListener(e->{if(page*pageSize()<totalRows){page++;loadAllCertificates();}});
        view.addActionListener(e->viewSelectedCertificate()); download.addActionListener(e->downloadSelectedCertificate()); edit.addActionListener(e->updateSelectedCertificate()); delete.addActionListener(e->deleteSelectedCertificate());
        profile.addActionListener(e->{int row=selectedModelRow();if(row>=0)AppWindow.showPage(new StudentProfileFrame(String.valueOf(model.getValueAt(row,3))));});
        export.addActionListener(e->CsvExporter.exportCertificates(this,searchField.getText(),selected(departmentFilter),selected(statusFilter),selected(typeFilter)));
        refresh.addActionListener(e->{refreshFilters();page=1;loadAllCertificates();});
        refreshFilters(); loadAllCertificates();
    }

    public void focusSearch() { searchField.requestFocusInWindow(); }
    private int pageSize() { return (Integer)pageSizeCombo.getSelectedItem(); }
    private String selected(JComboBox<String> c) { Object o=c.getSelectedItem();return o==null?"All":o.toString(); }

    private void refreshFilters() {
        String dep = selected(departmentFilter), typ=selected(typeFilter);
        try {
            replaceItems(departmentFilter,"SELECT DISTINCT department FROM certificates WHERE department IS NOT NULL AND department<>'' ORDER BY department",dep);
            replaceItems(typeFilter,"SELECT DISTINCT certificate_type FROM certificates WHERE certificate_type IS NOT NULL AND certificate_type<>'' ORDER BY certificate_type",typ);
        } catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Could not load filters: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE); }
    }
    private void replaceItems(JComboBox<String> combo,String sql,String previous) throws SQLException {
        combo.removeAllItems();combo.addItem("All");
        try(Connection c=DatabaseConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){while(r.next())combo.addItem(r.getString(1));}
        combo.setSelectedItem(previous);
        if(combo.getSelectedItem()==null)combo.setSelectedItem("All");
    }

    private String whereClause() {
        return "(?='' OR student_name LIKE ? OR enrollment_number LIKE ? OR certificate_number LIKE ?) AND (?='All' OR department=?) AND (?='All' OR status=?) AND (?='All' OR certificate_type=?)";
    }
    private void bindFilters(PreparedStatement p) throws SQLException {
        String q=searchField.getText().trim(), like="%"+q+"%"; p.setString(1,q);p.setString(2,like);p.setString(3,like);p.setString(4,like);
        String dep=selected(departmentFilter),status=selected(statusFilter),type=selected(typeFilter);
        p.setString(5,dep);p.setString(6,dep);p.setString(7,status);p.setString(8,status);p.setString(9,type);p.setString(10,type);
    }
    void loadAllCertificates() {
        String where=whereClause();
        try(Connection c=DatabaseConnection.getConnection()) {
            try(PreparedStatement count=c.prepareStatement("SELECT COUNT(*) FROM certificates WHERE "+where)){bindFilters(count);try(ResultSet r=count.executeQuery()){if(r.next())totalRows=r.getInt(1);}}
            int maxPage=Math.max(1,(int)Math.ceil(totalRows/(double)pageSize()));if(page>maxPage)page=maxPage;
            String sql="SELECT id, certificate_number, student_name, enrollment_number, course, department, certificate_type, issue_date, status, file_path FROM certificates WHERE "+where+" ORDER BY created_at DESC LIMIT ? OFFSET ?";
            model.setRowCount(0);
            try(PreparedStatement p=c.prepareStatement(sql)){bindFilters(p);p.setInt(11,pageSize());p.setInt(12,(page-1)*pageSize());try(ResultSet r=p.executeQuery()){while(r.next())model.addRow(new Object[]{r.getInt("id"),r.getString("certificate_number"),r.getString("student_name"),r.getString("enrollment_number"),r.getString("course"),r.getString("department"),r.getString("certificate_type"),r.getDate("issue_date"),r.getString("status"),r.getString("file_path")});}}
            pageLabel.setText("Page "+page+" / "+maxPage);resultsLabel.setText(totalRows+" records");
        } catch(SQLException ex) {JOptionPane.showMessageDialog(this,"Could not load certificate records.\n"+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}
    }
    private int selectedModelRow(){int view=table.getSelectedRow();return view<0?-1:table.convertRowIndexToModel(view);}
    private int getSelectedCertificateId(){int row=selectedModelRow();if(row<0){JOptionPane.showMessageDialog(this,"Please select a certificate row first.","Select a record",JOptionPane.WARNING_MESSAGE);return -1;}return ((Number)model.getValueAt(row,0)).intValue();}
    private String getSelectedFilePath(){int row=selectedModelRow();return row<0?null:String.valueOf(model.getValueAt(row,9));}
    private void viewSelectedCertificate(){int id=getSelectedCertificateId();if(id<0)return;openFile(getSelectedFilePath());}
    private void openFile(String stored){if(stored==null||stored.isBlank()){JOptionPane.showMessageDialog(this,"No certificate file is linked to this record.","File missing",JOptionPane.WARNING_MESSAGE);return;}File f=CertificateFiles.resolve(stored).toFile();if(!f.exists()){JOptionPane.showMessageDialog(this,"Certificate file was not found at:\n"+f.getAbsolutePath(),"File missing",JOptionPane.WARNING_MESSAGE);return;}try{if(Desktop.isDesktopSupported())Desktop.getDesktop().open(f);else JOptionPane.showMessageDialog(this,"Desktop preview is not supported on this system.");ActivityLogger.log("VIEW_CERTIFICATE",f.getName());}catch(IOException ex){JOptionPane.showMessageDialog(this,"Could not open certificate: "+ex.getMessage(),"File error",JOptionPane.ERROR_MESSAGE);}}
    private void downloadSelectedCertificate(){int id=getSelectedCertificateId();if(id<0)return;String stored=getSelectedFilePath();if(stored==null||stored.isBlank()){JOptionPane.showMessageDialog(this,"No certificate file is linked to this record.");return;}File source=CertificateFiles.resolve(stored).toFile();if(!source.exists()){JOptionPane.showMessageDialog(this,"Certificate file not found.");return;}JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Save Certificate As");chooser.setSelectedFile(new File(source.getName()));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;File target=chooser.getSelectedFile();if(target.exists()&&JOptionPane.showConfirmDialog(this,"Overwrite existing file?","Confirm overwrite",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{Files.copy(source.toPath(),target.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);ActivityLogger.log("DOWNLOAD_CERTIFICATE",source.getName()+" -> "+target.getName());JOptionPane.showMessageDialog(this,"Certificate saved successfully.");}catch(IOException ex){JOptionPane.showMessageDialog(this,"Could not save file: "+ex.getMessage(),"File error",JOptionPane.ERROR_MESSAGE);}}
    private Certificate loadCertificateById(int id)throws SQLException{String sql="SELECT * FROM certificates WHERE id=?";try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;return new Certificate(r.getInt("id"),r.getString("certificate_number"),r.getString("student_name"),r.getString("enrollment_number"),r.getString("course"),r.getString("department"),r.getString("certificate_type"),r.getDate("issue_date").toLocalDate(),r.getString("issuing_authority"),r.getString("status"),r.getString("file_path"));}}}
    private void updateSelectedCertificate(){int id=getSelectedCertificateId();if(id<0)return;try{Certificate cert=loadCertificateById(id);if(cert==null){JOptionPane.showMessageDialog(this,"Selected certificate no longer exists.");return;}AppWindow.showPage(new AddCertificateFrame(dashboardFrame,this,cert));}catch(SQLException ex){JOptionPane.showMessageDialog(this,"Could not read certificate: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}}
    private void deleteSelectedCertificate(){int id=getSelectedCertificateId();if(id<0)return;int answer=JOptionPane.showConfirmDialog(this,"Delete this certificate record and its linked local file?","Confirm delete",JOptionPane.YES_NO_OPTION);if(answer!=JOptionPane.YES_OPTION)return;try{Certificate cert=loadCertificateById(id);try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM certificates WHERE id=?")){p.setInt(1,id);p.executeUpdate();}if(cert!=null&&cert.getFilePath()!=null&&!cert.getFilePath().isBlank()){try{Files.deleteIfExists(CertificateFiles.resolve(cert.getFilePath()));}catch(IOException ex){JOptionPane.showMessageDialog(this,"Record deleted, but its file could not be deleted: "+ex.getMessage(),"File warning",JOptionPane.WARNING_MESSAGE);}}ActivityLogger.log("DELETE_CERTIFICATE",cert==null?"ID "+id:cert.getCertificateNumber());refreshFilters();loadAllCertificates();if(dashboardFrame!=null)dashboardFrame.refreshStatistics();}catch(SQLException ex){JOptionPane.showMessageDialog(this,"Could not delete certificate: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}}
}
