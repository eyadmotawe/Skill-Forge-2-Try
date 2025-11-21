package ui;


import model.Certificate;

import javax.swing.*;
import java.awt.*;

public class CertificateViewFrame extends BaseFrame {
    private final Certificate certificate;

    public CertificateViewFrame(Certificate certificate) {
        super("Certificate of Completion");
        this.certificate = certificate;
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        JPanel certPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185),
                        getWidth(), getHeight(), new Color(52, 73, 94));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Certificate frame
                int margin = 40;
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(margin, margin, getWidth() - 2*margin, getHeight() - 2*margin, 20, 20);

                // Border
                g2d.setColor(new Color(212, 175, 55));
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(margin + 10, margin + 10,
                        getWidth() - 2*margin - 20, getHeight() - 2*margin - 20, 15, 15);
            }
        };
        certPanel.setLayout(new GridBagLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLbl = new JLabel("Certificate of Completion");
        titleLbl.setFont(new Font("Serif", Font.BOLD, 32));
        titleLbl.setForeground(new Color(41, 128, 185));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLbl);
        contentPanel.add(Box.createVerticalStrut(30));

        // This certifies
        JLabel certifyLbl = new JLabel("This is to certify that");
        certifyLbl.setFont(new Font("Serif", Font.ITALIC, 16));
        certifyLbl.setForeground(Color.DARK_GRAY);
        certifyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(certifyLbl);
        contentPanel.add(Box.createVerticalStrut(10));

        // Student name
        JLabel nameLbl = new JLabel(certificate.getStudentName());
        nameLbl.setFont(new Font("Serif", Font.BOLD, 28));
        nameLbl.setForeground(new Color(52, 73, 94));
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLbl);
        contentPanel.add(Box.createVerticalStrut(20));

        // Has completed
        JLabel completedLbl = new JLabel("has successfully completed the course");
        completedLbl.setFont(new Font("Serif", Font.ITALIC, 16));
        completedLbl.setForeground(Color.DARK_GRAY);
        completedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(completedLbl);
        contentPanel.add(Box.createVerticalStrut(10));

        // Course name
        JLabel courseLbl = new JLabel(certificate.getCourseTitle());
        courseLbl.setFont(new Font("Serif", Font.BOLD, 24));
        courseLbl.setForeground(new Color(41, 128, 185));
        courseLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(courseLbl);
        contentPanel.add(Box.createVerticalStrut(30));

        // Date
        JLabel dateLbl = new JLabel("Issued on: " + certificate.getFormattedIssueDate());
        dateLbl.setFont(new Font("Serif", Font.PLAIN, 14));
        dateLbl.setForeground(Color.DARK_GRAY);
        dateLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(dateLbl);
        contentPanel.add(Box.createVerticalStrut(5));

        // Certificate ID
        JLabel idLbl = new JLabel("Certificate ID: " + certificate.getCertificateId());
        idLbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        idLbl.setForeground(Color.GRAY);
        idLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(idLbl);

        certPanel.add(contentPanel);
        add(certPanel, BorderLayout.CENTER);

        // Close button
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(SECONDARY_COLOR);
        JButton closeBtn = createStyledButton("Close", PRIMARY_COLOR);
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
