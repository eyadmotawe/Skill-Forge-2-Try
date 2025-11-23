package ui;

import auth.AuthService;
import model.*;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends BaseFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginBtn, signupBtn;

    public LoginFrame() {
        super("SkillForge - Login");
        initComponents();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new GridBagLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel loginTitle = createTitleLabel("Login");
        formPanel.add(loginTitle);
        formPanel.add(Box.createVerticalStrut(15));

        emailField = createStyledTextField();
        formPanel.add(createFormRow("Email:", emailField));
        formPanel.add(Box.createVerticalStrut(5));

        passwordField = createStyledPasswordField();
        formPanel.add(createFormRow("Password:", passwordField));
        formPanel.add(Box.createVerticalStrut(15));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        loginBtn = createStyledButton("Login", null);
        loginBtn.addActionListener(e -> handleLogin());
        btnPanel.add(loginBtn);

        signupBtn = createStyledButton("Sign Up", null);
        signupBtn.addActionListener(e -> openSignup());
        btnPanel.add(signupBtn);

        formPanel.add(btnPanel);
        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginBtn);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        AuthService.AuthResult result = AuthService.getInstance().login(email, password);

        if (result.isSuccess()) {
            User user = result.getUser();
            dispose();

            if (user instanceof Admin) {
                new AdminDashboardFrame().setVisible(true);
            } else if (user instanceof Instructor) {
                new InstructorDashboardFrame().setVisible(true);
            } else {
                new StudentDashboardFrame().setVisible(true);
            }
        } else {
            showError(result.getMessage());
        }
    }

    private void openSignup() {
        dispose();
        new SignupFrame().setVisible(true);
    }
}