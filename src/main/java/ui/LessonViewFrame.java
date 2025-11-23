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

        JPanel header = new JPanel(new BorderLayout());
        JLabel titleLbl = new JLabel(course.getTitle());
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.add(titleLbl, BorderLayout.WEST);

        JButton backBtn = createStyledButton("Close", null);
        backBtn.addActionListener(e -> dispose());
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 2));

        JLabel lessonsLbl = new JLabel("Lessons");
        lessonsLbl.setFont(new Font("Dialog", Font.BOLD, 12));
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

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 5));

        contentArea = new JTextArea();
        contentArea.setFont(REGULAR_FONT);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        rightPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        resourcesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(resourcesPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        takeQuizBtn = createStyledButton("Take Quiz", null);
        takeQuizBtn.addActionListener(e -> takeQuiz());
        takeQuizBtn.setEnabled(false);
        btnPanel.add(takeQuizBtn);

        completeBtn = createStyledButton("Mark Complete", null);
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
                link.setForeground(new Color(70, 130, 180));
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
                String prefix = completed ? "[X] " : "[ ] ";
                setText(prefix + lesson.getTitle());
            }
            return this;
        }
    }
}