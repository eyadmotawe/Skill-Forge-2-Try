package ui;

import auth.AuthService;
import database.JsonDatabaseManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboardFrame extends BaseFrame {
    private final AuthService auth = AuthService.getInstance();
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private JTabbedPane tabbedPane;
    private JTable availableCoursesTable, enrolledCoursesTable, certificatesTable;
    private DefaultTableModel availableModel, enrolledModel, certificatesModel;

    public StudentDashboardFrame() {
        super("SkillForge - Student Dashboard");
        initComponents();
        loadData();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(REGULAR_FONT);
        tabbedPane.addTab("Available Courses", createAvailableCoursesPanel());
        tabbedPane.addTab("My Courses", createEnrolledCoursesPanel());
        tabbedPane.addTab("Certificates", createCertificatesPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());

        User user = auth.getCurrentUser();
        JLabel titleLbl = new JLabel("Student Dashboard - " + user.getUsername());
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.add(titleLbl, BorderLayout.WEST);

        JButton logoutBtn = createStyledButton("Logout", null);
        logoutBtn.addActionListener(e -> logout());
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createAvailableCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        availableModel = new DefaultTableModel(
                new String[]{"Course ID", "Title", "Description", "Instructor", "Lessons"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        availableCoursesTable = new JTable(availableModel);
        availableCoursesTable.setFont(REGULAR_FONT);

        JScrollPane scrollPane = new JScrollPane(availableCoursesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton enrollBtn = createStyledButton("Enroll", null);
        enrollBtn.addActionListener(e -> enrollInCourse());
        JButton refreshBtn = createStyledButton("Refresh", null);
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(enrollBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEnrolledCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        enrolledModel = new DefaultTableModel(
                new String[]{"Course ID", "Title", "Progress", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        enrolledCoursesTable = new JTable(enrolledModel);
        enrolledCoursesTable.setFont(REGULAR_FONT);

        JScrollPane scrollPane = new JScrollPane(enrolledCoursesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton viewBtn = createStyledButton("View Lessons", null);
        viewBtn.addActionListener(e -> viewLessons());
        btnPanel.add(viewBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCertificatesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        certificatesModel = new DefaultTableModel(
                new String[]{"Certificate ID", "Course", "Issue Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        certificatesTable = new JTable(certificatesModel);
        certificatesTable.setFont(REGULAR_FONT);

        JScrollPane scrollPane = new JScrollPane(certificatesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton viewCertBtn = createStyledButton("View Certificate", null);
        viewCertBtn.addActionListener(e -> viewCertificate());
        btnPanel.add(viewCertBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadData() {
        auth.refreshCurrentUser();
        loadAvailableCourses();
        loadEnrolledCourses();
        loadCertificates();
    }

    private void loadAvailableCourses() {
        availableModel.setRowCount(0);
        Student student = (Student) auth.getCurrentUser();
        List<Course> courses = db.getApprovedCourses();

        for (Course c : courses) {
            if (!student.getEnrolledCourses().contains(c.getCourseId())) {
                User instructor = db.findUserById(c.getInstructorId());
                String instructorName = instructor != null ? instructor.getUsername() : "Unknown";
                availableModel.addRow(new Object[]{
                        c.getCourseId(), c.getTitle(), c.getDescription(),
                        instructorName, c.getLessonCount()
                });
            }
        }
    }

    private void loadEnrolledCourses() {
        enrolledModel.setRowCount(0);
        Student student = (Student) auth.getCurrentUser();

        for (String courseId : student.getEnrolledCourses()) {
            Course c = db.findCourseById(courseId);
            if (c != null) {
                CourseProgress progress = student.getProgress().get(courseId);
                int completed = progress != null ? progress.getCompletionCount() : 0;
                int total = c.getLessonCount();
                String progressStr = completed + "/" + total + " lessons";
                String status = completed == total && total > 0 ? "Completed" : "In Progress";
                enrolledModel.addRow(new Object[]{c.getCourseId(), c.getTitle(), progressStr, status});
            }
        }
    }

    private void loadCertificates() {
        certificatesModel.setRowCount(0);
        User user = auth.getCurrentUser();
        for (Certificate cert : user.getCertificates()) {
            certificatesModel.addRow(new Object[]{
                    cert.getCertificateId(), cert.getCourseTitle(), cert.getFormattedIssueDate()
            });
        }
    }

    private void enrollInCourse() {
        int row = availableCoursesTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a course to enroll");
            return;
        }
        String courseId = (String) availableModel.getValueAt(row, 0);
        Course course = db.findCourseById(courseId);
        Student student = (Student) auth.getCurrentUser();

        if (showConfirm("Enroll in '" + course.getTitle() + "'?")) {
            student.enrollInCourse(courseId);
            course.addStudent(student.getUserId());
            db.updateUser(student);
            db.updateCourse(course);
            showSuccess("Successfully enrolled!");
            loadData();
        }
    }

    private void viewLessons() {
        int row = enrolledCoursesTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a course to view lessons");
            return;
        }
        String courseId = (String) enrolledModel.getValueAt(row, 0);
        new LessonViewFrame(courseId).setVisible(true);
    }

    private void viewCertificate() {
        int row = certificatesTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a certificate to view");
            return;
        }
        String certId = (String) certificatesModel.getValueAt(row, 0);
        User user = auth.getCurrentUser();
        Certificate cert = user.getCertificates().stream()
                .filter(c -> c.getCertificateId().equals(certId))
                .findFirst().orElse(null);
        if (cert != null) {
            new CertificateViewFrame(cert).setVisible(true);
        }
    }

    private void logout() {
        auth.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}