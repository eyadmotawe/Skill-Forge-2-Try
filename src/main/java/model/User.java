package model;


import java.time.LocalDateTime;
import java.util.*;

public abstract class User {
    protected String userId;
    protected Role role;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected LocalDateTime createdAt;
    protected List<String> enrolledCourses;
    protected Map<String, CourseProgress> progress;
    protected List<QuizAttempt> quizAttempts;
    protected List<Certificate> certificates;

    public User() {
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
        this.quizAttempts = new ArrayList<>();
        this.certificates = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public User(String userId, Role role, String username, String email, String passwordHash) {
        this();
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<String> enrolledCourses) { this.enrolledCourses = enrolledCourses; }

    public Map<String, CourseProgress> getProgress() { return progress; }
    public void setProgress(Map<String, CourseProgress> progress) { this.progress = progress; }

    public List<QuizAttempt> getQuizAttempts() { return quizAttempts; }
    public void setQuizAttempts(List<QuizAttempt> quizAttempts) { this.quizAttempts = quizAttempts; }

    public List<Certificate> getCertificates() { return certificates; }
    public void setCertificates(List<Certificate> certificates) { this.certificates = certificates; }

    public void enrollInCourse(String courseId) {
        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
            progress.put(courseId, new CourseProgress());
        }
    }

    public void addQuizAttempt(QuizAttempt attempt) {
        quizAttempts.add(attempt);
    }

    public void addCertificate(Certificate certificate) {
        certificates.add(certificate);
    }

    public abstract String getDashboardTitle();
}