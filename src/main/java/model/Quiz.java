package model;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private String quizId;
    private List<Question> questions;
    private int passingScore;

    public Quiz() {
        this.questions = new ArrayList<>();
        this.passingScore = 50;
    }

    public Quiz(String quizId, List<Question> questions, int passingScore) {
        this.quizId = quizId;
        this.questions = questions;
        this.passingScore = passingScore;
    }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public int getPassingScore() { return passingScore; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void removeQuestion(String questionId) {
        questions.removeIf(q -> q.getQuestionId().equals(questionId));
    }

    public int calculateScore(List<Integer> answers) {
        if (questions.isEmpty()) return 100;

        int correct = 0;
        for (int i = 0; i < Math.min(answers.size(), questions.size()); i++) {
            if (questions.get(i).isCorrect(answers.get(i))) {
                correct++;
            }
        }
        return (int) ((correct * 100.0) / questions.size());
    }

    public boolean isPassing(int score) {
        return score >= passingScore;
    }

    public int getQuestionCount() {
        return questions.size();
    }
}