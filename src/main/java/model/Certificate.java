package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Certificate {
    private String certificateId;
    private String studentId;
    private String courseId;
    private String courseTitle;
    private String studentName;
    private LocalDateTime issueDate;

    public Certificate() {
        this.issueDate = LocalDateTime.now();
    }

    public Certificate(String certificateId, String studentId, String courseId,
                       String courseTitle, String studentName) {
        this.certificateId = certificateId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.studentName = studentName;
        this.issueDate = LocalDateTime.now();
    }

    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public String getFormattedIssueDate() {
        return issueDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
    }

    @Override
    public String toString() {
        return "Certificate for " + courseTitle + " - Issued: " + getFormattedIssueDate();
    }
}