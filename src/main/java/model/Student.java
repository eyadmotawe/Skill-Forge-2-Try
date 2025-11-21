package model;

public class Student extends User {

    public Student() {
        super();
        this.role = Role.STUDENT;
    }

    public Student(String userId, String username, String email, String passwordHash) {
        super(userId, Role.STUDENT, username, email, passwordHash);
    }

    @Override
    public String getDashboardTitle() {
        return "Student Dashboard - " + username;
    }

    public void markLessonComplete(String courseId, String lessonId) {
        CourseProgress cp = progress.get(courseId);
        if (cp != null) {
            cp.markLessonComplete(lessonId);
        }
    }

    public boolean hasCompletedLesson(String courseId, String lessonId) {
        CourseProgress cp = progress.get(courseId);
        return cp != null && cp.getCompletedLessons().contains(lessonId);
    }

    public boolean hasCertificateForCourse(String courseId) {
        return certificates.stream()
                .anyMatch(c -> c.getCourseId().equals(courseId));
    }
}