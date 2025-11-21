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

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        JLabel titleLbl = new JLabel("SkillForge Learning Platform");
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

        JLabel loginTitle = createTitleLabel("Login");
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(loginTitle);
        formPanel.add(Box.createVerticalStrut(20));

        emailField = createStyledTextField();
        formPanel.add(createFormRow("Email:", emailField));
        formPanel.add(Box.createVerticalStrut(10));

        passwordField = createStyledPasswordField();
        formPanel.add(createFormRow("Password:", passwordField));
        formPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        loginBtn = createStyledButton("Login", PRIMARY_COLOR);
        loginBtn.addActionListener(e -> handleLogin());
        btnPanel.add(loginBtn);

        signupBtn = createStyledButton("Sign Up", SECONDARY_COLOR);
        signupBtn.addActionListener(e -> openSignup());
        btnPanel.add(signupBtn);

        formPanel.add(btnPanel);
        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(SECONDARY_COLOR);
        footerPanel.setPreferredSize(new Dimension(getWidth(), 40));
        JLabel footerLbl = new JLabel("© 2025 SkillForge - CC272 Programming II");
        footerLbl.setForeground(Color.WHITE);
        footerPanel.add(footerLbl);
        add(footerPanel, BorderLayout.SOUTH);

        // Enter key triggers login
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