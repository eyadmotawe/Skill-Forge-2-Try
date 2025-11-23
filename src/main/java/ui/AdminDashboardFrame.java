package ui;

import auth.AuthService;
import database.JsonDatabaseManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardFrame extends BaseFrame {
    private final AuthService auth = AuthService.getInstance();
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private JTabbedPane tabbedPane;
    private JTable pendingTable, allCoursesTable, usersTable;
    private DefaultTableModel pendingModel, allCoursesModel, usersModel;

    public AdminDashboardFrame() {
        super("SkillForge - Admin Dashboard");
        initComponents();
        loadData();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(REGULAR_FONT);
        tabbedPane.addTab("Pending Courses", createPendingCoursesPanel());
        tabbedPane.addTab("All Courses", createAllCoursesPanel());
        tabbedPane.addTab("Users", createUsersPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());

        User user = auth.getCurrentUser();
        JLabel titleLbl = new JLabel("Admin Panel - " + user.getUsername());
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.add(titleLbl, BorderLayout.WEST);

        JButton logoutBtn = createStyledButton("Logout", null);
        logoutBtn.addActionListener(e -> logout());
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createPendingCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pendingModel = new DefaultTableModel(
                new String[]{"Course ID", "Title", "Description", "Instructor", "Created"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingTable = new JTable(pendingModel);
        pendingTable.setFont(REGULAR_FONT);

        panel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton approveBtn = createStyledButton("Approve", null);
        approveBtn.addActionListener(e -> approveCourse());
        btnPanel.add(approveBtn);

        JButton rejectBtn = createStyledButton("Reject", null);
        rejectBtn.addActionListener(e -> rejectCourse());
        btnPanel.add(rejectBtn);

        JButton viewBtn = createStyledButton("View Details", null);
        viewBtn.addActionListener(e -> viewCourseDetails());
        btnPanel.add(viewBtn);

        JButton refreshBtn = createStyledButton("Refresh", null);
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAllCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        allCoursesModel = new DefaultTableModel(
                new String[]{"Course ID", "Title", "Status", "Instructor", "Students", "Lessons"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        allCoursesTable = new JTable(allCoursesModel);
        allCoursesTable.setFont(REGULAR_FONT);

        panel.add(new JScrollPane(allCoursesTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton deleteBtn = createStyledButton("Delete Course", null);
        deleteBtn.addActionListener(e -> deleteAnyCourse());
        btnPanel.add(deleteBtn);

        JButton changeStatusBtn = createStyledButton("Change Status", null);
        changeStatusBtn.addActionListener(e -> changeStatus());
        btnPanel.add(changeStatusBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usersModel = new DefaultTableModel(
                new String[]{"User ID", "Username", "Email", "Role", "Enrolled/Created"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        usersTable.setFont(REGULAR_FONT);

        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        loadPendingCourses();
        loadAllCourses();
        loadUsers();
    }

    private void loadPendingCourses() {
        pendingModel.setRowCount(0);
        List<Course> pending = db.getPendingCourses();

        for (Course c : pending) {
            User instructor = db.findUserById(c.getInstructorId());
            String instructorName = instructor != null ? instructor.getUsername() : "Unknown";
            String created = c.getCreatedAt().toLocalDate().toString();
            pendingModel.addRow(new Object[]{
                    c.getCourseId(), c.getTitle(), c.getDescription(), instructorName, created
            });
        }
    }

    private void loadAllCourses() {
        allCoursesModel.setRowCount(0);
        List<Course> courses = db.getAllCourses();

        for (Course c : courses) {
            User instructor = db.findUserById(c.getInstructorId());
            String instructorName = instructor != null ? instructor.getUsername() : "Unknown";
            allCoursesModel.addRow(new Object[]{
                    c.getCourseId(), c.getTitle(), c.getApprovalStatus(),
                    instructorName, c.getStudentCount(), c.getLessonCount()
            });
        }
    }

    private void loadUsers() {
        usersModel.setRowCount(0);
        List<User> users = db.getAllUsers();

        for (User u : users) {
            String extra;
            if (u instanceof Instructor) {
                extra = ((Instructor) u).getCreatedCourses().size() + " courses created";
            } else if (u instanceof Student) {
                extra = u.getEnrolledCourses().size() + " courses enrolled";
            } else {
                extra = "Admin";
            }
            usersModel.addRow(new Object[]{
                    u.getUserId(), u.getUsername(), u.getEmail(), u.getRole(), extra
            });
        }
    }

    private void approveCourse() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to approve");
            return;
        }
        String courseId = (String) pendingModel.getValueAt(row, 0);
        Course course = db.findCourseById(courseId);

        if (showConfirm("Approve course '" + course.getTitle() + "'?")) {
            course.setApprovalStatus(ApprovalStatus.APPROVED);
            db.updateCourse(course);
            showSuccess("Course approved! It's now visible to students.");
            loadData();
        }
    }

    private void rejectCourse() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to reject");
            return;
        }
        String courseId = (String) pendingModel.getValueAt(row, 0);
        Course course = db.findCourseById(courseId);

        String reason = JOptionPane.showInputDialog(this,
                "Reason for rejection (optional):", "Reject Course", JOptionPane.PLAIN_MESSAGE);

        if (reason != null) {
            course.setApprovalStatus(ApprovalStatus.REJECTED);
            db.updateCourse(course);
            showSuccess("Course rejected.");
            loadData();
        }
    }

    private void viewCourseDetails() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to view");
            return;
        }
        String courseId = (String) pendingModel.getValueAt(row, 0);
        Course course = db.findCourseById(courseId);
        User instructor = db.findUserById(course.getInstructorId());

        StringBuilder details = new StringBuilder();
        details.append("Course ID: ").append(course.getCourseId()).append("\n");
        details.append("Title: ").append(course.getTitle()).append("\n");
        details.append("Description: ").append(course.getDescription()).append("\n");
        details.append("Instructor: ").append(instructor != null ? instructor.getUsername() : "Unknown").append("\n");
        details.append("Lessons: ").append(course.getLessonCount()).append("\n\n");

        if (!course.getLessons().isEmpty()) {
            details.append("Lesson List:\n");
            for (int i = 0; i < course.getLessons().size(); i++) {
                Lesson l = course.getLessons().get(i);
                details.append("  ").append(i + 1).append(". ").append(l.getTitle());
                details.append(l.hasQuiz() ? " [Quiz: " + l.getQuiz().getQuestionCount() + " questions]" : " [No Quiz]");
                details.append("\n");
            }
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(REGULAR_FONT);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Course Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteAnyCourse() {
        int row = allCoursesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to delete");
            return;
        }
        String courseId = (String) allCoursesModel.getValueAt(row, 0);

        if (showConfirm("Delete this course permanently? This cannot be undone.")) {
            Course course = db.findCourseById(courseId);
            User instructor = db.findUserById(course.getInstructorId());
            if (instructor instanceof Instructor) {
                ((Instructor) instructor).removeCreatedCourse(courseId);
                db.updateUser(instructor);
            }
            db.deleteCourse(courseId);
            showSuccess("Course deleted");
            loadData();
        }
    }

    private void changeStatus() {
        int row = allCoursesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a course to change status");
            return;
        }
        String courseId = (String) allCoursesModel.getValueAt(row, 0);
        Course course = db.findCourseById(courseId);

        String[] options = {"PENDING", "APPROVED", "REJECTED"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select new status:", "Change Status",
                JOptionPane.PLAIN_MESSAGE, null, options, course.getApprovalStatus().toString());

        if (selected != null) {
            course.setApprovalStatus(ApprovalStatus.valueOf(selected));
            db.updateCourse(course);
            showSuccess("Status updated to " + selected);
            loadData();
        }
    }

    private void logout() {
        auth.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}