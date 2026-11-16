import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;

public final class CsvExporter {
    private CsvExporter() {}
    public static void exportCertificates(java.awt.Component parent, String query, String department, String status, String type) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export certificates to CSV");
        chooser.setSelectedFile(new File("certificates-export.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
        String sql = "SELECT certificate_number, student_name, enrollment_number, course, department, certificate_type, issue_date, issuing_authority, status, file_path FROM certificates WHERE " +
                "(? = '' OR student_name LIKE ? OR enrollment_number LIKE ? OR certificate_number LIKE ?) AND " +
                "(? = 'All' OR department = ?) AND (? = 'All' OR status = ?) AND (? = 'All' OR certificate_type = ?) ORDER BY created_at DESC";
        String like = "%" + query.trim() + "%";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, query.trim()); p.setString(2, like); p.setString(3, like); p.setString(4, like);
            p.setString(5, department); p.setString(6, department); p.setString(7, status); p.setString(8, status); p.setString(9, type); p.setString(10, type);
            try (ResultSet rs = p.executeQuery(); BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i=1; i<=md.getColumnCount(); i++) { if (i>1) w.write(','); w.write(csv(md.getColumnLabel(i))); }
                w.newLine();
                while (rs.next()) { for (int i=1; i<=md.getColumnCount(); i++) { if (i>1) w.write(','); Object v=rs.getObject(i); w.write(csv(v == null ? "" : v.toString())); } w.newLine(); }
            }
            ActivityLogger.log("EXPORT_CSV", "Exported certificate records to " + file.getName());
            JOptionPane.showMessageDialog(parent, "CSV exported successfully:\n" + file.getAbsolutePath(), "Export complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(parent, "Could not export CSV: " + ex.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private static String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
}
