import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VerifyCertificateFrame extends JInternalFrame {

    private final JTextField certNumberField = new JTextField(20);
    private final JLabel resultLabel = new JLabel(" ", SwingConstants.CENTER);

    private final JLabel certNumberValue = new JLabel();
    private final JLabel studentNameValue = new JLabel();
    private final JLabel enrollmentValue = new JLabel();
    private final JLabel courseValue = new JLabel();
    private final JLabel departmentValue = new JLabel();
    private final JLabel certTypeValue = new JLabel();
    private final JLabel issueDateValue = new JLabel();
    private final JLabel issuingAuthorityValue = new JLabel();
    private final JLabel statusValue = new JLabel();

    private final JButton viewFileBtn = new JButton("View Certificate");
    private String currentFilePath;

    public VerifyCertificateFrame() {
        setTitle("Verify Certificate");
        setSize(500, 500);
        
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        inputPanel.add(new JLabel("Certificate Number:"));
        inputPanel.add(certNumberField);
        JButton verifyBtn = new JButton("Verify Certificate");
        inputPanel.add(verifyBtn);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Certificate Details"));
        detailsPanel.add(new JLabel("Certificate Number:"));
        detailsPanel.add(certNumberValue);
        detailsPanel.add(new JLabel("Student Name:"));
        detailsPanel.add(studentNameValue);
        detailsPanel.add(new JLabel("Enrollment Number:"));
        detailsPanel.add(enrollmentValue);
        detailsPanel.add(new JLabel("Course:"));
        detailsPanel.add(courseValue);
        detailsPanel.add(new JLabel("Department:"));
        detailsPanel.add(departmentValue);
        detailsPanel.add(new JLabel("Certificate Type:"));
        detailsPanel.add(certTypeValue);
        detailsPanel.add(new JLabel("Issue Date:"));
        detailsPanel.add(issueDateValue);
        detailsPanel.add(new JLabel("Issuing Authority:"));
        detailsPanel.add(issuingAuthorityValue);
        detailsPanel.add(new JLabel("Status:"));
        detailsPanel.add(statusValue);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        centerPanel.add(resultLabel, BorderLayout.NORTH);
        centerPanel.add(detailsPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        viewFileBtn.setEnabled(false);
        JButton closeBtn = new JButton("← Back to Dashboard");
        bottomPanel.add(viewFileBtn);
        bottomPanel.add(closeBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        verifyBtn.addActionListener(e -> verifyCertificate());
        certNumberField.addActionListener(e -> verifyCertificate());
        viewFileBtn.addActionListener(e -> openCertificateFile());
        closeBtn.addActionListener(e -> dispose());

        clearDetails();
    }

    private void verifyCertificate() {
        String certNumber = certNumberField.getText().trim();
        if (certNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a certificate number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM certificates WHERE certificate_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, certNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    resultLabel.setForeground(Color.RED);
                    resultLabel.setText("Certificate Not Found / Invalid Certificate");
                    clearDetails();
                    return;
                }

                String status = rs.getString("status");
                currentFilePath = rs.getString("file_path");
                ActivityLogger.log("VERIFY_CERTIFICATE", certNumber + " / " + status);

                certNumberValue.setText(rs.getString("certificate_number"));
                studentNameValue.setText(rs.getString("student_name"));
                enrollmentValue.setText(rs.getString("enrollment_number"));
                courseValue.setText(rs.getString("course"));
                departmentValue.setText(rs.getString("department"));
                certTypeValue.setText(rs.getString("certificate_type"));
                issueDateValue.setText(String.valueOf(rs.getDate("issue_date")));
                issuingAuthorityValue.setText(rs.getString("issuing_authority"));
                statusValue.setText(status);

                if ("Verified".equalsIgnoreCase(status)) {
                    resultLabel.setForeground(new Color(0, 128, 0));
                    resultLabel.setText("Certificate Verified");
                    viewFileBtn.setEnabled(true);
                } else {
                    resultLabel.setForeground(Color.ORANGE.darker());
                    resultLabel.setText("Certificate exists but is not verified.");
                    viewFileBtn.setEnabled(true);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database. Check that MySQL is running and DatabaseConnection.java has the right password.\n\nDetails: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCertificateFile() {
        if (currentFilePath == null || currentFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Certificate file not found.",
                    "File Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File file = CertificateFiles.resolve(currentFilePath).toFile();
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Certificate file not found.",
                    "File Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open the certificate file.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDetails() {
        certNumberValue.setText("");
        studentNameValue.setText("");
        enrollmentValue.setText("");
        courseValue.setText("");
        departmentValue.setText("");
        certTypeValue.setText("");
        issueDateValue.setText("");
        issuingAuthorityValue.setText("");
        statusValue.setText("");
        viewFileBtn.setEnabled(false);
        currentFilePath = null;
    }
}
