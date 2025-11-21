package ui;


import auth.AuthService;
import database.JsonDatabaseManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuizFrame extends BaseFrame {
    private final Course course;
    private final Lesson lesson;
    private final Quiz quiz;
    private final AuthService auth = AuthService.getInstance();
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private List<ButtonGroup> answerGroups;
    private JPanel questionsPanel;

    public QuizFrame(Course course, Lesson lesson) {
        super("Quiz - " + lesson.getTitle());
        this.course = course;
        this.lesson = lesson;
        this.quiz = lesson.getQuiz();
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setBackground(WARNING_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLbl = new JLabel("Quiz: " + lesson.getTitle());
        titleLbl.setFont(SUBTITLE_FONT);
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl);
        add(header, BorderLayout.NORTH);

        // Questions
        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        questionsPanel.setBackground(Color.WHITE);

        answerGroups = new ArrayList<>();
        List<Question> questions = quiz.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            JPanel qPanel = createQuestionPanel(i + 1, q);
            questionsPanel.add(qPanel);
            questionsPanel.add(Box.createVerticalStrut(20));
        }

        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(BG_COLOR);

        JButton submitBtn = createStyledButton("Submit Quiz", SUCCESS_COLOR);
        submitBtn.addActionListener(e -> submitQuiz());
        footer.add(submitBtn);

        JButton cancelBtn = createStyledButton("Cancel", SECONDARY_COLOR);
        cancelBtn.addActionListener(e -> dispose());
        footer.add(cancelBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createQuestionPanel(int num, Question question) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel qLabel = new JLabel("<html><b>Q" + num + ":</b> " + question.getQuestionText() + "</html>");
        qLabel.setFont(REGULAR_FONT);
        qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(qLabel);
        panel.add(Box.createVerticalStrut(10));

        ButtonGroup group = new ButtonGroup();
        answerGroups.add(group);

        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            JRadioButton rb = new JRadioButton(options.get(i));
            rb.setFont(REGULAR_FONT);
            rb.setOpaque(false);
            rb.setActionCommand(String.valueOf(i));
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(rb);
            panel.add(rb);
        }

        return panel;
    }

    private void submitQuiz() {
        List<Integer> answers = new ArrayList<>();
        for (ButtonGroup group : answerGroups) {
            ButtonModel selection = group.getSelection();
            if (selection == null) {
                showError("Please answer all questions");
                return;
            }
            answers.add(Integer.parseInt(selection.getActionCommand()));
        }

        int score = quiz.calculateScore(answers);
        boolean passed = quiz.isPassing(score);

        Student student = (Student) auth.getCurrentUser();
        QuizAttempt attempt = new QuizAttempt(
                lesson.getLessonId(), course.getCourseId(), score, passed
        );
        student.addQuizAttempt(attempt);

        if (passed) {
            student.markLessonComplete(course.getCourseId(), lesson.getLessonId());
            showSuccess("Quiz Passed!\nScore: " + score + "% (Passing: " + quiz.getPassingScore() + "%)");
        } else {
            showError("Quiz Failed\nScore: " + score + "% (Passing: " + quiz.getPassingScore() + "%)\nTry again!");
        }

        db.updateUser(student);
        showResults(answers, score, passed);
    }

    private void showResults(List<Integer> answers, int score, boolean passed) {
        questionsPanel.removeAll();

        JLabel resultLbl = new JLabel(passed ? "PASSED - Score: " + score + "%" : "FAILED - Score: " + score + "%");
        resultLbl.setFont(TITLE_FONT);
        resultLbl.setForeground(passed ? SUCCESS_COLOR : DANGER_COLOR);
        resultLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionsPanel.add(resultLbl);
        questionsPanel.add(Box.createVerticalStrut(20));

        List<Question> questions = quiz.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            int userAnswer = answers.get(i);
            boolean correct = q.isCorrect(userAnswer);

            JPanel qPanel = new JPanel();
            qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
            qPanel.setBackground(correct ? new Color(212, 239, 223) : new Color(250, 219, 216));
            qPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            qPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel qLbl = new JLabel("<html><b>Q" + (i+1) + ":</b> " + q.getQuestionText() + "</html>");
            qLbl.setFont(REGULAR_FONT);
            qPanel.add(qLbl);

            JLabel yourAns = new JLabel("Your answer: " + q.getOptions().get(userAnswer));
            yourAns.setFont(REGULAR_FONT);
            qPanel.add(yourAns);

            if (!correct) {
                JLabel correctAns = new JLabel("Correct answer: " + q.getCorrectAnswer());
                correctAns.setFont(REGULAR_FONT);
                correctAns.setForeground(SUCCESS_COLOR);
                qPanel.add(correctAns);
            }

            questionsPanel.add(qPanel);
            questionsPanel.add(Box.createVerticalStrut(10));
        }

        questionsPanel.revalidate();
        questionsPanel.repaint();
    }
}
