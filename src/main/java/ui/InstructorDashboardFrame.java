package ui;


import auth.AuthService;
import database.JsonDatabaseManager;
import model.*;
import utils.IdGenerator;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorDashboardFrame extends BaseFrame {
    private final AuthService auth = AuthService.getInstance();
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private JTabbedPane tabbedPane;
    private JTable coursesTable, studentsTable;
    private DefaultTableModel coursesModel, studentsModel;

    public InstructorDashboardFrame() {
        super("SkillForge - Instructor Dashboard");
        initComponents();
        loadData();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(REGULAR_FONT);
        tabbedPane.addTab("My Courses", createCoursesPanel());
        tabbedPane.addTab("Enrolled Students", createStudentsPanel());
        tabbedPane.addTab("Insights", createInsightsPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SECONDARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        User user = auth.getCurrentUser();
        JLabel titleLbl = new JLabel("Instructor: " + user.getUsername());
        titleLbl.setFont(SUBTITLE_FONT);
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        JButton logoutBtn = createStyledButton("Logout", DANGER_COLOR);
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(e -> logout());
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(BG_COLOR);

        coursesModel = new DefaultTableModel(
                new String[]{"Course ID", "Title", "Status", "Students", "Lessons"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        coursesTable = new JTable(coursesModel);
        coursesTable.setFont(REGULAR_FONT);
        coursesTable.setRowHeight(30);
        coursesTable.getTableHeader().setFont(REGULAR_FONT);

        panel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);

        JButton createBtn = createStyledButton("Create Course", SUCCESS_COLOR);
        createBtn.addActionListener(e -> createCourse());
        btnPanel.add(createBtn);

        JButton editBtn = createStyledButton("Edit Course", PRIMARY_COLOR);
        editBtn.addActionListener(e -> editCourse());
        btnPanel.add(editBtn);

        JButton lessonsBtn = createStyledButton("Manage Lessons", WARNING_COLOR);
        lessonsBtn.addActionListener(e -> manageLessons());
        btnPanel.add(lessonsBtn);

        JButton deleteBtn = createStyledButton("Delete", DANGER_COLOR);
        deleteBtn.addActionListener(e -> deleteCourse());
        btnPanel.add(deleteBtn);

        JButton refreshBtn = createStyledButton("Refresh", SECONDARY_COLOR);
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(BG_COLOR);

        studentsModel = new DefaultTableModel(
                new String[]{"Student ID", "Username", "Email", "Enrolled Courses"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        studentsTable = new JTable(studentsModel);
        studentsTable.setFont(REGULAR_FONT);
        studentsTable.setRowHeight(30);
        studentsTable.getTableHeader().setFont(REGULAR_FONT);

        panel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInsightsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(BG_COLOR);

        JButton viewChartBtn = createStyledButton("View Analytics", PRIMARY_COLOR);
        viewChartBtn.addActionListener(e -> {
            int row = coursesTable.getSelectedRow();
            if (row < 0) {
                showError("Select a course from 'My Courses' tab first");
                return;
            }
            String courseId = (String) coursesModel.getValueAt(row, 0);
            new ChartFrame(courseId).setVisible(true);
        });

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel infoLbl = new JLabel("Select a course and click to view analytics");
        infoLbl.setFont(SUBTITLE_FONT);
        infoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(infoLbl);
        infoPanel.add(Box.createVerticalStrut(20));
        viewChartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(viewChartBtn);

        centerPanel.add(infoPanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadData() {
        loadCourses();
        loadStudents();
    }

    private void loadCourses() {
        coursesModel.setRowCount(0);
        Instructor instructor = (Instructor) auth.getCurrentUser();
        List<Course> courses = db.getCoursesByInstructor(instructor.getUserId());

        for (Course c : courses) {
            coursesModel.addRow(new Object[]{
                    c.getCourseId(), c.getTitle(), c.getApprovalStatus(),
                    c.getStudentCount(), c.getLessonCount()
            });
        }
    }

    private void loadStudents() {
        studentsModel.setRowCount(0);
        Instructor instructor = (Instructor) auth.getCurrentUser();
        List<Course> myCourses = db.getCoursesByInstructor(instructor.getUserId());

        for (Student student : db.getAllStudents()) {
            long enrolledCount = myCourses.stream()
                    .filter(c -> c.getStudents().contains(student.getUserId()))
                    .count();
            if (enrolledCount > 0) {
                studentsModel.addRow(new Object[]{
                        student.getUserId(), student.getUsername(),
                        student.getEmail(), enrolledCount
                });
            }
        }
    }

    private void createCourse() {
        JTextField titleField = new JTextField();
        JTextArea descArea = new JTextArea(4, 30);
        descArea.setLineWrap(true);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 10));
        mainPanel.add(panel, BorderLayout.NORTH);
        mainPanel.add(descPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, mainPanel, "Create Course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            String error = ValidationUtils.validateCourse(title, desc);

            if (error != null) {
                showError(error);
                return;
            }

            Instructor instructor = (Instructor) auth.getCurrentUser();
            String courseId = IdGenerator.generateCourseId();
            Course course = new Course(courseId, title, desc, instructor.getUserId());

            db.addCourse(course);
            instructor.addCreatedCourse(courseId);
            db.updateUser(instructor);

            showSuccess("Course created! Status: PENDING (awaiting admin approval)");
            loadData();
        }
    }

    private void editCourse() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to edit");
            return;
        }
        String courseId = (String) coursesModel.getValueAt(row, 0);
        Course course = db.findCourseById(courseId);

        JTextField titleField = new JTextField(course.getTitle());
        JTextArea descArea = new JTextArea(course.getDescription(), 4, 30);
        descArea.setLineWrap(true);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 10));
        mainPanel.add(panel, BorderLayout.NORTH);
        mainPanel.add(descPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, mainPanel, "Edit Course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            course.setTitle(titleField.getText().trim());
            course.setDescription(descArea.getText().trim());
            db.updateCourse(course);
            showSuccess("Course updated!");
            loadData();
        }
    }

    private void manageLessons() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to manage lessons");
            return;
        }
        String courseId = (String) coursesModel.getValueAt(row, 0);
        new LessonManagementFrame(courseId).setVisible(true);
    }

    private void deleteCourse() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to delete");
            return;
        }
        String courseId = (String) coursesModel.getValueAt(row, 0);

        if (showConfirm("Delete this course? This cannot be undone.")) {
            Instructor instructor = (Instructor) auth.getCurrentUser();
            instructor.removeCreatedCourse(courseId);
            db.updateUser(instructor);
            db.deleteCourse(courseId);
            showSuccess("Course deleted");
            loadData();
        }
    }

    private void logout() {
        auth.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
