import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Handles both "Add Certificate" and "Update Certificate".
 * If an existing Certificate is passed to the constructor, the form opens in edit mode.
 */
public class AddCertificateFrame extends JInternalFrame {

    private static final String CERTIFICATES_FOLDER = "certificates";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DashboardFrame dashboardFrame;
    private final CertificateListFrame listFrame; // may be null when opened from the dashboard
    private final Certificate existingCertificate; // null when adding a new certificate

    private final JTextField studentNameField = new JTextField();
    private final JTextField enrollmentField = new JTextField();
    private final JTextField courseField = new JTextField();
    private final JTextField departmentField = new JTextField();
    private final JTextField certTypeField = new JTextField();
    private final JTextField certNumberField = new JTextField();
    private final JTextField issueDateField = new JTextField();
    private final JTextField issuingAuthorityField = new JTextField();
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Verified", "Pending"});
    private final JLabel selectedFileLabel = new JLabel("No file selected");

    private File selectedFile; // newly chosen file, if any

    public AddCertificateFrame(DashboardFrame dashboardFrame, Certificate certificateToEdit) {
        this(dashboardFrame, null, certificateToEdit);
    }

    public AddCertificateFrame(DashboardFrame dashboardFrame, CertificateListFrame listFrame, Certificate certificateToEdit) {
        this.dashboardFrame = dashboardFrame;
        this.listFrame = listFrame;
        this.existingCertificate = certificateToEdit;

        boolean editMode = existingCertificate != null;

        setTitle(editMode ? "Update Certificate" : "Add Certificate");
        setSize(500, 520);
        
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));

        formPanel.add(new JLabel("Student Name:"));
        formPanel.add(studentNameField);

        formPanel.add(new JLabel("Enrollment Number:"));
        formPanel.add(enrollmentField);

        formPanel.add(new JLabel("Course:"));
        formPanel.add(courseField);

        formPanel.add(new JLabel("Department:"));
        formPanel.add(departmentField);

        formPanel.add(new JLabel("Certificate Type:"));
        formPanel.add(certTypeField);

        formPanel.add(new JLabel("Certificate Number:"));
        formPanel.add(certNumberField);

        formPanel.add(new JLabel("Issue Date (yyyy-MM-dd):"));
        formPanel.add(issueDateField);

        formPanel.add(new JLabel("Issuing Authority:"));
        formPanel.add(issuingAuthorityField);

        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusCombo);

        formPanel.add(new JLabel("Certificate File:"));
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        JButton chooseFileBtn = new JButton("Choose File");
        filePanel.add(chooseFileBtn, BorderLayout.WEST);
        filePanel.add(selectedFileLabel, BorderLayout.CENTER);
        formPanel.add(filePanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveBtn = new JButton(editMode ? "Save Changes" : "Save Certificate");
        JButton clearBtn = new JButton("Clear");
        JButton backBtn = new JButton("Back");
        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(backBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        chooseFileBtn.addActionListener(e -> chooseFile());
        saveBtn.addActionListener(e -> {
            if (editMode) {
                updateCertificate();
            } else {
                saveCertificate();
            }
        });
        clearBtn.addActionListener(e -> clearForm());
        backBtn.addActionListener(e -> dispose());

        if (editMode) {
            populateFieldsForEdit();
        }
    }

    private void populateFieldsForEdit() {
        studentNameField.setText(existingCertificate.getStudentName());
        enrollmentField.setText(existingCertificate.getEnrollmentNumber());
        courseField.setText(existingCertificate.getCourse());
        departmentField.setText(existingCertificate.getDepartment());
        certTypeField.setText(existingCertificate.getCertificateType());
        certNumberField.setText(existingCertificate.getCertificateNumber());
        if (existingCertificate.getIssueDate() != null) {
            issueDateField.setText(existingCertificate.getIssueDate().format(DATE_FORMAT));
        }
        issuingAuthorityField.setText(existingCertificate.getIssuingAuthority());
        statusCombo.setSelectedItem(existingCertificate.getStatus());
        if (existingCertificate.getFilePath() != null && !existingCertificate.getFilePath().isEmpty()) {
            selectedFileLabel.setText("Current: " + Paths.get(existingCertificate.getFilePath()).getFileName());
        }
        // Certificate number normally should not change on update; keep it editable but it is
        // still checked for duplicates against every other row.
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Certificate File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Certificate Files (*.pdf, *.jpg, *.jpeg, *.png)", "pdf", "jpg", "jpeg", "png"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String extension = getExtension(file.getName());

            if (!isSupportedExtension(extension)) {
                JOptionPane.showMessageDialog(this,
                        "Unsupported file type. Only PDF, JPG, JPEG and PNG files are allowed.",
                        "Invalid File", JOptionPane.WARNING_MESSAGE);
                return;
            }

            selectedFile = file;
            selectedFileLabel.setText(file.getName());
        }
    }

    private boolean isSupportedExtension(String extension) {
        return extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("jpg")
                || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png");
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0 && dotIndex < fileName.length() - 1) ? fileName.substring(dotIndex + 1) : "";
    }

    private void saveCertificate() {
        String studentName = studentNameField.getText().trim();
        String enrollment = enrollmentField.getText().trim();
        String course = courseField.getText().trim();
        String department = departmentField.getText().trim();
        String certType = certTypeField.getText().trim();
        String certNumber = certNumberField.getText().trim();
        String issueDateText = issueDateField.getText().trim();
        String issuingAuthority = issuingAuthorityField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (studentName.isEmpty() || enrollment.isEmpty() || course.isEmpty() || department.isEmpty()
                || certType.isEmpty() || certNumber.isEmpty() || issueDateText.isEmpty() || issuingAuthority.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!certNumber.matches("[A-Za-z0-9_-]{1,100}")) {
            JOptionPane.showMessageDialog(this, "Certificate number may contain only letters, numbers, hyphens and underscores (max 100 characters).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate issueDate;
        try {
            issueDate = LocalDate.parse(issueDateText, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Issue Date must be in yyyy-MM-dd format.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a certificate file (PDF, JPG, JPEG or PNG).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (isDuplicateCertificateNumber(conn, certNumber, -1)) {
                JOptionPane.showMessageDialog(this, "A certificate with this number already exists.",
                        "Duplicate Certificate Number", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String extension = getExtension(selectedFile.getName());
            String storedFileName = certNumber + "." + extension.toLowerCase();
            String relativePath = CERTIFICATES_FOLDER + "/" + storedFileName;

            copyFileToRepository(selectedFile, storedFileName);

            String sql = "INSERT INTO certificates (certificate_number, student_name, enrollment_number, course, " +
                    "department, certificate_type, issue_date, issuing_authority, status, file_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, certNumber);
                stmt.setString(2, studentName);
                stmt.setString(3, enrollment);
                stmt.setString(4, course);
                stmt.setString(5, department);
                stmt.setString(6, certType);
                stmt.setDate(7, java.sql.Date.valueOf(issueDate));
                stmt.setString(8, issuingAuthority);
                stmt.setString(9, status);
                stmt.setString(10, relativePath);

                stmt.executeUpdate();

                ActivityLogger.log("ADD_CERTIFICATE", certNumber + " / " + studentName);
                JOptionPane.showMessageDialog(this, "Certificate added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                if (dashboardFrame != null) {
                    dashboardFrame.refreshStatistics();
                }
            } catch (SQLException insertEx) {
                // Roll back the file copy if the database insert failed, to avoid leaving orphan files.
                deleteRepositoryFileQuietly(storedFileName);
                JOptionPane.showMessageDialog(this,
                        "Failed to save certificate details. Please try again.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ioEx) {
            JOptionPane.showMessageDialog(this,
                    "Failed to copy the certificate file. Please try again.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database. Check that MySQL is running and DatabaseConnection.java has the right password.\n\nDetails: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCertificate() {
        String studentName = studentNameField.getText().trim();
        String enrollment = enrollmentField.getText().trim();
        String course = courseField.getText().trim();
        String department = departmentField.getText().trim();
        String certType = certTypeField.getText().trim();
        String certNumber = certNumberField.getText().trim();
        String issueDateText = issueDateField.getText().trim();
        String issuingAuthority = issuingAuthorityField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (studentName.isEmpty() || enrollment.isEmpty() || course.isEmpty() || department.isEmpty()
                || certType.isEmpty() || certNumber.isEmpty() || issueDateText.isEmpty() || issuingAuthority.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!certNumber.matches("[A-Za-z0-9_-]{1,100}")) {
            JOptionPane.showMessageDialog(this, "Certificate number may contain only letters, numbers, hyphens and underscores (max 100 characters).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate issueDate;
        try {
            issueDate = LocalDate.parse(issueDateText, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Issue Date must be in yyyy-MM-dd format.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (isDuplicateCertificateNumber(conn, certNumber, existingCertificate.getId())) {
                JOptionPane.showMessageDialog(this, "A certificate with this number already exists.",
                        "Duplicate Certificate Number", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String relativePath = existingCertificate.getFilePath();
            String oldStoredFileName = null;

            if (selectedFile != null) {
                String extension = getExtension(selectedFile.getName());
                String storedFileName = certNumber + "." + extension.toLowerCase();
                copyFileToRepository(selectedFile, storedFileName);

                if (existingCertificate.getFilePath() != null && !existingCertificate.getFilePath().isEmpty()) {
                    oldStoredFileName = Paths.get(existingCertificate.getFilePath()).getFileName().toString();
                }
                relativePath = CERTIFICATES_FOLDER + "/" + storedFileName;
            }

            String sql = "UPDATE certificates SET certificate_number = ?, student_name = ?, enrollment_number = ?, " +
                    "course = ?, department = ?, certificate_type = ?, issue_date = ?, issuing_authority = ?, " +
                    "status = ?, file_path = ? WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, certNumber);
                stmt.setString(2, studentName);
                stmt.setString(3, enrollment);
                stmt.setString(4, course);
                stmt.setString(5, department);
                stmt.setString(6, certType);
                stmt.setDate(7, java.sql.Date.valueOf(issueDate));
                stmt.setString(8, issuingAuthority);
                stmt.setString(9, status);
                stmt.setString(10, relativePath);
                stmt.setInt(11, existingCertificate.getId());

                stmt.executeUpdate();

                // Remove the old file only after the database update succeeded.
                if (oldStoredFileName != null && !oldStoredFileName.equals(Paths.get(relativePath).getFileName().toString())) {
                    deleteRepositoryFileQuietly(oldStoredFileName);
                }

                ActivityLogger.log("UPDATE_CERTIFICATE", certNumber + " / " + studentName);
                JOptionPane.showMessageDialog(this, "Certificate updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                if (dashboardFrame != null) {
                    dashboardFrame.refreshStatistics();
                }
                if (listFrame != null) {
                    listFrame.loadAllCertificates();
                }
                dispose();
            } catch (SQLException updateEx) {
                JOptionPane.showMessageDialog(this,
                        "Failed to update certificate details. Please try again.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ioEx) {
            JOptionPane.showMessageDialog(this,
                    "Failed to copy the new certificate file. Please try again.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database. Check that MySQL is running and DatabaseConnection.java has the right password.\n\nDetails: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isDuplicateCertificateNumber(Connection conn, String certNumber, int excludeId) throws SQLException {
        String sql = "SELECT id FROM certificates WHERE certificate_number = ? AND id <> ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, certNumber);
            stmt.setInt(2, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void copyFileToRepository(File source, String storedFileName) throws IOException {
        Path folder = Paths.get(CERTIFICATES_FOLDER);
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        Path destination = folder.resolve(storedFileName);
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteRepositoryFileQuietly(String storedFileName) {
        try {
            Path path = Paths.get(CERTIFICATES_FOLDER, storedFileName);
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Non-fatal: leftover files can be cleaned up manually if needed.
        }
    }

    private void clearForm() {
        studentNameField.setText("");
        enrollmentField.setText("");
        courseField.setText("");
        departmentField.setText("");
        certTypeField.setText("");
        certNumberField.setText("");
        issueDateField.setText("");
        issuingAuthorityField.setText("");
        statusCombo.setSelectedIndex(0);
        selectedFileLabel.setText("No file selected");
        selectedFile = null;
    }
}
