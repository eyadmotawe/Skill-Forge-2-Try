package ui;


import auth.AuthService;
import model.*;

import javax.swing.*;
import java.awt.*;

public class SignupFrame extends BaseFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleCombo;
    private JButton signupBtn, backBtn;

    public SignupFrame() {
        super("SkillForge - Sign Up");
        initComponents();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        JLabel titleLbl = new JLabel("SkillForge - Create Account");
        titleLbl.setFont(TITLE_FONT);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl);
        add(headerPanel, BorderLayout.NORTH);

        // Center form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_COLOR);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        JLabel signupTitle = createTitleLabel("Sign Up");
        signupTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(signupTitle);
        formPanel.add(Box.createVerticalStrut(20));

        usernameField = createStyledTextField();
        formPanel.add(createFormRow("Username:", usernameField));
        formPanel.add(Box.createVerticalStrut(10));

        emailField = createStyledTextField();
        formPanel.add(createFormRow("Email:", emailField));
        formPanel.add(Box.createVerticalStrut(10));

        passwordField = createStyledPasswordField();
        formPanel.add(createFormRow("Password:", passwordField));
        formPanel.add(Box.createVerticalStrut(10));

        confirmPasswordField = createStyledPasswordField();
        formPanel.add(createFormRow("Confirm:", confirmPasswordField));
        formPanel.add(Box.createVerticalStrut(10));

        roleCombo = new JComboBox<>(new String[]{"Student", "Instructor"});
        roleCombo.setFont(REGULAR_FONT);
        roleCombo.setPreferredSize(new Dimension(250, 35));
        formPanel.add(createFormRow("Role:", roleCombo));
        formPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        signupBtn = createStyledButton("Sign Up", SUCCESS_COLOR);
        signupBtn.addActionListener(e -> handleSignup());
        btnPanel.add(signupBtn);

        backBtn = createStyledButton("Back to Login", SECONDARY_COLOR);
        backBtn.addActionListener(e -> backToLogin());
        btnPanel.add(backBtn);

        formPanel.add(btnPanel);
        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);

        getRootPane().setDefaultButton(signupBtn);
    }

    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        Role role = roleCombo.getSelectedIndex() == 0 ? Role.STUDENT : Role.INSTRUCTOR;

        AuthService.AuthResult result = AuthService.getInstance()
                .signup(username, email, password, confirmPassword, role);

        if (result.isSuccess()) {
            showSuccess("Account created successfully!");
            dispose();
            User user = result.getUser();
            if (user instanceof Instructor) {
                new InstructorDashboardFrame().setVisible(true);
            } else {
                new StudentDashboardFrame().setVisible(true);
            }
        } else {
            showError(result.getMessage());
        }
    }

    private void backToLogin() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}