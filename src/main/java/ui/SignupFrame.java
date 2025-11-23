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

        JPanel centerPanel = new JPanel(new GridBagLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel signupTitle = createTitleLabel("Sign Up");
        formPanel.add(signupTitle);
        formPanel.add(Box.createVerticalStrut(15));

        usernameField = createStyledTextField();
        formPanel.add(createFormRow("Username:", usernameField));
        formPanel.add(Box.createVerticalStrut(5));

        emailField = createStyledTextField();
        formPanel.add(createFormRow("Email:", emailField));
        formPanel.add(Box.createVerticalStrut(5));

        passwordField = createStyledPasswordField();
        formPanel.add(createFormRow("Password:", passwordField));
        formPanel.add(Box.createVerticalStrut(5));

        confirmPasswordField = createStyledPasswordField();
        formPanel.add(createFormRow("Confirm:", confirmPasswordField));
        formPanel.add(Box.createVerticalStrut(5));

        roleCombo = new JComboBox<>(new String[]{"Student", "Instructor"});
        formPanel.add(createFormRow("Role:", roleCombo));
        formPanel.add(Box.createVerticalStrut(15));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        signupBtn = createStyledButton("Sign Up", null);
        signupBtn.addActionListener(e -> handleSignup());
        btnPanel.add(signupBtn);

        backBtn = createStyledButton("Cancel", null);
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