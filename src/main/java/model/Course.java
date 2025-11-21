package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private List<String> students;
    private List<Lesson> lessons;

    public Course() {
        this.students = new ArrayList<>();
        this.lessons = new ArrayList<>();
        this.approvalStatus = ApprovalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Course(String courseId, String title, String description, String instructorId) {
        this();
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getStudents() { return students; }
    public void setStudents(List<String> students) { this.students = students; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }

    public void addStudent(String studentId) {
        if (!students.contains(studentId)) {
            students.add(studentId);
        }
    }

    public void removeStudent(String studentId) {
        students.remove(studentId);
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    public void removeLesson(String lessonId) {
        lessons.removeIf(l -> l.getLessonId().equals(lessonId));
    }

    public Lesson getLessonById(String lessonId) {
        return lessons.stream()
                .filter(l -> l.getLessonId().equals(lessonId))
                .findFirst()
                .orElse(null);
    }

    public int getLessonCount() {
        return lessons.size();
    }

    public int getStudentCount() {
        return students.size();
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }

    @Override
    public String toString() {
        return title + " (" + approvalStatus + ")";
    }
}
