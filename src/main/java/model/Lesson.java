package model;


import java.util.ArrayList;
import java.util.List;

public class Lesson {
    private String lessonId;
    private String title;
    private String content;
    private List<String> resources;
    private Quiz quiz;

    public Lesson() {
        this.resources = new ArrayList<>();
    }

    public Lesson(String lessonId, String title, String content) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = new ArrayList<>();
    }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getResources() { return resources; }
    public void setResources(List<String> resources) { this.resources = resources; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public void addResource(String resource) {
        resources.add(resource);
    }

    public void removeResource(String resource) {
        resources.remove(resource);
    }

    public boolean hasQuiz() {
        return quiz != null && quiz.getQuestionCount() > 0;
    }

    @Override
    public String toString() {
        return title;
    }
}