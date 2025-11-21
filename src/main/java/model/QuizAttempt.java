package model;

import java.time.LocalDateTime;

public class QuizAttempt {
    private String lessonId;
    private String courseId;
    private int score;
    private boolean passed;
    private LocalDateTime attemptDate;

    public QuizAttempt() {
        this.attemptDate = LocalDateTime.now();
    }

    public QuizAttempt(String lessonId, String courseId, int score, boolean passed) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.score = score;
        this.passed = passed;
        this.attemptDate = LocalDateTime.now();
    }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public LocalDateTime getAttemptDate() { return attemptDate; }
    public void setAttemptDate(LocalDateTime attemptDate) { this.attemptDate = attemptDate; }
}