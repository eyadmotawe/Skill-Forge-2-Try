package ui;

import database.JsonDatabaseManager;
import model.*;
import utils.IdGenerator;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LessonManagementFrame extends BaseFrame {
    private final String courseId;
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private Course course;
    private JTable lessonsTable;
    private DefaultTableModel lessonsModel;

    public LessonManagementFrame(String courseId) {
        super("Manage Lessons");
        this.courseId = courseId;
        this.course = db.findCourseById(courseId);
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        loadLessons();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        JLabel titleLbl = new JLabel("Lessons: " + course.getTitle());
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.add(titleLbl, BorderLayout.WEST);

        JButton backBtn = createStyledButton("Close", null);
        backBtn.addActionListener(e -> dispose());
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        lessonsModel = new DefaultTableModel(
                new String[]{"Lesson ID", "Title", "Has Quiz", "Questions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        lessonsTable = new JTable(lessonsModel);
        lessonsTable.setFont(REGULAR_FONT);

        tablePanel.add(new JScrollPane(lessonsTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton addBtn = createStyledButton("Add Lesson", null);
        addBtn.addActionListener(e -> addLesson());
        btnPanel.add(addBtn);

        JButton editBtn = createStyledButton("Edit", null);
        editBtn.addActionListener(e -> editLesson());
        btnPanel.add(editBtn);

        JButton quizBtn = createStyledButton("Quiz", null);
        quizBtn.addActionListener(e -> manageQuiz());
        btnPanel.add(quizBtn);

        JButton deleteBtn = createStyledButton("Delete", null);
        deleteBtn.addActionListener(e -> deleteLesson());
        btnPanel.add(deleteBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadLessons() {
        lessonsModel.setRowCount(0);
        course = db.findCourseById(courseId);
        for (Lesson l : course.getLessons()) {
            boolean hasQuiz = l.hasQuiz();
            int qCount = hasQuiz ? l.getQuiz().getQuestionCount() : 0;
            lessonsModel.addRow(new Object[]{
                    l.getLessonId(), l.getTitle(), hasQuiz ? "Yes" : "No", qCount
            });
        }
    }

    private void addLesson() {
        JTextField titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(400, 28));

        JTextArea contentArea = new JTextArea(6, 35);
        contentArea.setLineWrap(true);
        JTextField resourcesField = new JTextField();
        resourcesField.setPreferredSize(new Dimension(400, 28));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createFormRow("Title:", titleField));
        panel.add(Box.createVerticalStrut(5));
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        panel.add(contentPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(createFormRow("Resources:", resourcesField));

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Lesson",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String error = ValidationUtils.validateLesson(title, content);

            if (error != null) {
                showError(error);
                return;
            }

            Lesson lesson = new Lesson(IdGenerator.generateLessonId(), title, content);
            String resources = resourcesField.getText().trim();
            if (!resources.isEmpty()) {
                Arrays.stream(resources.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(lesson::addResource);
            }

            course.addLesson(lesson);
            db.updateCourse(course);
            showSuccess("Lesson added!");
            loadLessons();
        }
    }

    private void editLesson() {
        int row = lessonsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a lesson to edit");
            return;
        }
        String lessonId = (String) lessonsModel.getValueAt(row, 0);
        Lesson lesson = course.getLessonById(lessonId);

        JTextField titleField = new JTextField(lesson.getTitle());
        titleField.setPreferredSize(new Dimension(400, 28));

        JTextArea contentArea = new JTextArea(lesson.getContent(), 6, 35);
        contentArea.setLineWrap(true);
        JTextField resourcesField = new JTextField(String.join(", ", lesson.getResources()));
        resourcesField.setPreferredSize(new Dimension(400, 28));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createFormRow("Title:", titleField));
        panel.add(Box.createVerticalStrut(5));
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        panel.add(contentPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(createFormRow("Resources:", resourcesField));

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Lesson",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            lesson.setTitle(titleField.getText().trim());
            lesson.setContent(contentArea.getText().trim());
            lesson.setResources(new ArrayList<>());
            String resources = resourcesField.getText().trim();
            if (!resources.isEmpty()) {
                Arrays.stream(resources.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(lesson::addResource);
            }
            db.updateCourse(course);
            showSuccess("Lesson updated!");
            loadLessons();
        }
    }

    private void manageQuiz() {
        int row = lessonsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a lesson to manage its quiz");
            return;
        }
        String lessonId = (String) lessonsModel.getValueAt(row, 0);
        new QuizManagementFrame(courseId, lessonId).setVisible(true);

        // Refresh when quiz management closes
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                loadLessons();
            }
        });
    }

    private void deleteLesson() {
        int row = lessonsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a lesson to delete");
            return;
        }
        String lessonId = (String) lessonsModel.getValueAt(row, 0);

        if (showConfirm("Delete this lesson?")) {
            course.removeLesson(lessonId);
            db.updateCourse(course);
            showSuccess("Lesson deleted");
            loadLessons();
        }
    }
}