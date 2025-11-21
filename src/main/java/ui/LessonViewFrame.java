package ui;


import auth.AuthService;
import database.JsonDatabaseManager;
import model.*;
import utils.IdGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LessonViewFrame extends BaseFrame {
    private final String courseId;
    private final AuthService auth = AuthService.getInstance();
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private Course course;
    private JList<Lesson> lessonList;
    private DefaultListModel<Lesson> lessonModel;
    private JTextArea contentArea;
    private JPanel resourcesPanel;
    private JButton takeQuizBtn, completeBtn;

    public LessonViewFrame(String courseId) {
        super("Course Lessons");
        this.courseId = courseId;
        this.course = db.findCourseById(courseId);
        setSize(900, 650);
        initComponents();
        loadLessons();
    }

    @Override
    protected void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLbl = new JLabel(course.getTitle());
        titleLbl.setFont(SUBTITLE_FONT);
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        JButton backBtn = createStyledButton("Back", SECONDARY_COLOR);
        backBtn.addActionListener(e -> dispose());
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);

        // Left - Lesson list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        leftPanel.setBackground(BG_COLOR);

        JLabel lessonsLbl = createLabel("Lessons");
        lessonsLbl.setFont(SUBTITLE_FONT);
        leftPanel.add(lessonsLbl, BorderLayout.NORTH);

        lessonModel = new DefaultListModel<>();
        lessonList = new JList<>(lessonModel);
        lessonList.setFont(REGULAR_FONT);
        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonList.setCellRenderer(new LessonListRenderer());
        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) displayLesson();
        });
        leftPanel.add(new JScrollPane(lessonList), BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // Right - Content
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        rightPanel.setBackground(BG_COLOR);

        contentArea = new JTextArea();
        contentArea.setFont(REGULAR_FONT);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        resourcesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resourcesPanel.setOpaque(false);
        bottomPanel.add(resourcesPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);

        takeQuizBtn = createStyledButton("Take Quiz", WARNING_COLOR);
        takeQuizBtn.addActionListener(e -> takeQuiz());
        takeQuizBtn.setEnabled(false);
        btnPanel.add(takeQuizBtn);

        completeBtn = createStyledButton("Mark Complete", SUCCESS_COLOR);
        completeBtn.addActionListener(e -> markComplete());
        completeBtn.setEnabled(false);
        btnPanel.add(completeBtn);

        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadLessons() {
        lessonModel.clear();
        for (Lesson lesson : course.getLessons()) {
            lessonModel.addElement(lesson);
        }
        if (!lessonModel.isEmpty()) {
            lessonList.setSelectedIndex(0);
        }
    }

    private void displayLesson() {
        Lesson lesson = lessonList.getSelectedValue();
        if (lesson == null) return;

        contentArea.setText("Lesson: " + lesson.getTitle() + "\n\n" + lesson.getContent());

        resourcesPanel.removeAll();
        if (!lesson.getResources().isEmpty()) {
            resourcesPanel.add(createLabel("Resources: "));
            for (String res : lesson.getResources()) {
                JLabel link = new JLabel("<html><u>" + res + "</u></html>");
                link.setForeground(PRIMARY_COLOR);
                link.setCursor(new Cursor(Cursor.HAND_CURSOR));
                resourcesPanel.add(link);
            }
        }
        resourcesPanel.revalidate();
        resourcesPanel.repaint();

        Student student = (Student) auth.getCurrentUser();
        boolean completed = student.hasCompletedLesson(courseId, lesson.getLessonId());

        takeQuizBtn.setEnabled(lesson.hasQuiz() && !completed);
        completeBtn.setEnabled(!completed && !lesson.hasQuiz());
    }

    private void takeQuiz() {
        Lesson lesson = lessonList.getSelectedValue();
        if (lesson == null || !lesson.hasQuiz()) return;

        QuizFrame quizFrame = new QuizFrame(course, lesson);
        quizFrame.setVisible(true);
        quizFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                auth.refreshCurrentUser();
                displayLesson();
                checkCourseCompletion();
            }
        });
    }

    private void markComplete() {
        Lesson lesson = lessonList.getSelectedValue();
        if (lesson == null) return;

        Student student = (Student) auth.getCurrentUser();
        student.markLessonComplete(courseId, lesson.getLessonId());
        db.updateUser(student);
        showSuccess("Lesson marked as complete!");
        displayLesson();
        lessonList.repaint();
        checkCourseCompletion();
    }

    private void checkCourseCompletion() {
        Student student = (Student) auth.getCurrentUser();
        CourseProgress progress = student.getProgress().get(courseId);
        if (progress == null) return;

        int completed = progress.getCompletionCount();
        int total = course.getLessonCount();

        if (completed == total && total > 0 && !student.hasCertificateForCourse(courseId)) {
            Certificate cert = new Certificate(
                    IdGenerator.generateCertificateId(),
                    student.getUserId(),
                    courseId,
                    course.getTitle(),
                    student.getUsername()
            );
            student.addCertificate(cert);
            db.updateUser(student);
            showSuccess("Congratulations! You've completed the course and earned a certificate!");
        }
    }

    private class LessonListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Lesson lesson) {
                Student student = (Student) auth.getCurrentUser();
                boolean completed = student.hasCompletedLesson(courseId, lesson.getLessonId());
                String prefix = completed ? "✓ " : "○ ";
                setText(prefix + lesson.getTitle());
                if (completed) setForeground(SUCCESS_COLOR);
            }
            return this;
        }
    }
}