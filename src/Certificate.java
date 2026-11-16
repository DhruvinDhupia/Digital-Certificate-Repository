import java.time.LocalDate;

/**
 * Simple model class representing a certificate record.
 */
public class Certificate {

    private int id;
    private String certificateNumber;
    private String studentName;
    private String enrollmentNumber;
    private String course;
    private String department;
    private String certificateType;
    private LocalDate issueDate;
    private String issuingAuthority;
    private String status;
    private String filePath;

    public Certificate() {
    }

    public Certificate(int id, String certificateNumber, String studentName, String enrollmentNumber,
                        String course, String department, String certificateType, LocalDate issueDate,
                        String issuingAuthority, String status, String filePath) {
        this.id = id;
        this.certificateNumber = certificateNumber;
        this.studentName = studentName;
        this.enrollmentNumber = enrollmentNumber;
        this.course = course;
        this.department = department;
        this.certificateType = certificateType;
        this.issueDate = issueDate;
        this.issuingAuthority = issuingAuthority;
        this.status = status;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
