package ui;

import database.JsonDatabaseManager;
import model.*;
import utils.IdGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuizManagementFrame extends BaseFrame {
    private final String courseId;
    private final String lessonId;
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private Course course;
    private Lesson lesson;
    private JTable questionsTable;
    private DefaultTableModel questionsModel;
    private JSpinner passingScoreSpinner;

    public QuizManagementFrame(String courseId, String lessonId) {
        super("Quiz Management");
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.course = db.findCourseById(courseId);
        this.lesson = course.getLessonById(lessonId);
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        loadQuestions();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        JLabel titleLbl = new JLabel("Quiz: " + lesson.getTitle());
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.add(titleLbl, BorderLayout.WEST);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel scoreLbl = new JLabel("Passing Score (%): ");
        scorePanel.add(scoreLbl);

        int currentScore = lesson.getQuiz() != null ? lesson.getQuiz().getPassingScore() : 50;
        passingScoreSpinner = new JSpinner(new SpinnerNumberModel(currentScore, 0, 100, 5));
        passingScoreSpinner.addChangeListener(e -> updatePassingScore());
        scorePanel.add(passingScoreSpinner);

        JButton backBtn = createStyledButton("Close", null);
        backBtn.addActionListener(e -> dispose());
        scorePanel.add(backBtn);
        header.add(scorePanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        questionsModel = new DefaultTableModel(
                new String[]{"Question ID", "Question", "Options", "Correct"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        questionsTable = new JTable(questionsModel);
        questionsTable.setFont(REGULAR_FONT);

        tablePanel.add(new JScrollPane(questionsTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton addBtn = createStyledButton("Add Question", null);
        addBtn.addActionListener(e -> addQuestion());
        btnPanel.add(addBtn);

        JButton editBtn = createStyledButton("Edit", null);
        editBtn.addActionListener(e -> editQuestion());
        btnPanel.add(editBtn);

        JButton deleteBtn = createStyledButton("Delete", null);
        deleteBtn.addActionListener(e -> deleteQuestion());
        btnPanel.add(deleteBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadQuestions() {
        questionsModel.setRowCount(0);
        course = db.findCourseById(courseId);
        lesson = course.getLessonById(lessonId);

        if (lesson.getQuiz() != null) {
            for (Question q : lesson.getQuiz().getQuestions()) {
                String options = String.join(" | ", q.getOptions());
                String correct = q.getCorrectAnswer();
                questionsModel.addRow(new Object[]{
                        q.getQuestionId(), q.getQuestionText(), options, correct
                });
            }
        }
    }

    private void updatePassingScore() {
        if (lesson.getQuiz() == null) {
            Quiz quiz = new Quiz(IdGenerator.generateQuizId(), new ArrayList<>(),
                    (int) passingScoreSpinner.getValue());
            lesson.setQuiz(quiz);
        } else {
            lesson.getQuiz().setPassingScore((int) passingScoreSpinner.getValue());
        }
        db.updateCourse(course);
    }

    private void addQuestion() {
        QuestionDialog dialog = new QuestionDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            if (lesson.getQuiz() == null) {
                Quiz quiz = new Quiz(IdGenerator.generateQuizId(), new ArrayList<>(),
                        (int) passingScoreSpinner.getValue());
                lesson.setQuiz(quiz);
            }
            lesson.getQuiz().addQuestion(dialog.getQuestion());
            db.updateCourse(course);
            loadQuestions();
        }
    }

    private void editQuestion() {
        int row = questionsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a question to edit");
            return;
        }
        String questionId = (String) questionsModel.getValueAt(row, 0);
        Question question = lesson.getQuiz().getQuestions().stream()
                .filter(q -> q.getQuestionId().equals(questionId))
                .findFirst().orElse(null);

        if (question != null) {
            QuestionDialog dialog = new QuestionDialog(this, question);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Question updated = dialog.getQuestion();
                question.setQuestionText(updated.getQuestionText());
                question.setOptions(updated.getOptions());
                question.setCorrectAnswerIndex(updated.getCorrectAnswerIndex());
                db.updateCourse(course);
                loadQuestions();
            }
        }
    }

    private void deleteQuestion() {
        int row = questionsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a question to delete");
            return;
        }
        String questionId = (String) questionsModel.getValueAt(row, 0);

        if (showConfirm("Delete this question?")) {
            lesson.getQuiz().removeQuestion(questionId);
            db.updateCourse(course);
            loadQuestions();
        }
    }

    // Inner dialog for question editing
    private static class QuestionDialog extends JDialog {
        private final Question existingQuestion;
        private boolean confirmed = false;
        private JTextArea questionText;
        private JTextField[] optionFields;
        private JComboBox<String> correctCombo;

        public QuestionDialog(Frame owner, Question question) {
            super(owner, question == null ? "Add Question" : "Edit Question", true);
            this.existingQuestion = question;
            initDialog();
        }

        private void initDialog() {
            setSize(500, 400);
            setLocationRelativeTo(getOwner());
            setLayout(new BorderLayout(10, 10));
            ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

            formPanel.add(new JLabel("Question:"));
            questionText = new JTextArea(3, 30);
            questionText.setLineWrap(true);
            if (existingQuestion != null) {
                questionText.setText(existingQuestion.getQuestionText());
            }
            formPanel.add(new JScrollPane(questionText));
            formPanel.add(Box.createVerticalStrut(10));

            formPanel.add(new JLabel("Options (4 choices):"));
            optionFields = new JTextField[4];
            List<String> existingOptions = existingQuestion != null ?
                    existingQuestion.getOptions() : null;

            for (int i = 0; i < 4; i++) {
                optionFields[i] = new JTextField();
                if (existingOptions != null && i < existingOptions.size()) {
                    optionFields[i].setText(existingOptions.get(i));
                }
                JPanel optPanel = new JPanel(new BorderLayout(5, 0));
                optPanel.add(new JLabel((char)('A' + i) + ":"), BorderLayout.WEST);
                optPanel.add(optionFields[i], BorderLayout.CENTER);
                formPanel.add(optPanel);
                formPanel.add(Box.createVerticalStrut(5));
            }

            JPanel correctPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            correctPanel.add(new JLabel("Correct Answer:"));
            correctCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
            if (existingQuestion != null) {
                correctCombo.setSelectedIndex(existingQuestion.getCorrectAnswerIndex());
            }
            correctPanel.add(correctCombo);
            formPanel.add(correctPanel);

            add(new JScrollPane(formPanel), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> save());
            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(e -> dispose());
            btnPanel.add(saveBtn);
            btnPanel.add(cancelBtn);
            add(btnPanel, BorderLayout.SOUTH);
        }

        private void save() {
            String qText = questionText.getText().trim();
            if (qText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Question text required");
                return;
            }

            List<String> options = new ArrayList<>();
            for (JTextField field : optionFields) {
                String opt = field.getText().trim();
                if (opt.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All 4 options are required");
                    return;
                }
                options.add(opt);
            }

            confirmed = true;
            dispose();
        }

        public boolean isConfirmed() { return confirmed; }

        public Question getQuestion() {
            List<String> options = new ArrayList<>();
            for (JTextField field : optionFields) {
                options.add(field.getText().trim());
            }
            String id = existingQuestion != null ? existingQuestion.getQuestionId() :
                    IdGenerator.generateQuestionId();
            return new Question(id, questionText.getText().trim(), options,
                    correctCombo.getSelectedIndex());
        }
    }
}