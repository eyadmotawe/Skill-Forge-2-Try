package model;

import java.util.ArrayList;
import java.util.List;

public class CourseProgress {
    private List<String> completedLessons;
    private String currentLessonId;

    public CourseProgress() {
        this.completedLessons = new ArrayList<>();
    }

    public List<String> getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(List<String> completedLessons) {
        this.completedLessons = completedLessons;
    }

    public String getCurrentLessonId() { return currentLessonId; }
    public void setCurrentLessonId(String currentLessonId) {
        this.currentLessonId = currentLessonId;
    }

    public void markLessonComplete(String lessonId) {
        if (!completedLessons.contains(lessonId)) {
            completedLessons.add(lessonId);
        }
    }

    public boolean isLessonCompleted(String lessonId) {
        return completedLessons.contains(lessonId);
    }

    public int getCompletionCount() {
        return completedLessons.size();
    }
}