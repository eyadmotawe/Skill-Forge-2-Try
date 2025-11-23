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

        JPanel certPanel = new JPanel();
        certPanel.setLayout(new GridBagLayout());
        certPanel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        contentPanel.setBackground(Color.WHITE);

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        innerPanel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel("Certificate of Completion");
        titleLbl.setFont(new Font("Serif", Font.BOLD, 22));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(titleLbl);
        innerPanel.add(Box.createVerticalStrut(20));

        JLabel certifyLbl = new JLabel("This certifies that");
        certifyLbl.setFont(new Font("Serif", Font.PLAIN, 13));
        certifyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(certifyLbl);
        innerPanel.add(Box.createVerticalStrut(8));

        JLabel nameLbl = new JLabel(certificate.getStudentName());
        nameLbl.setFont(new Font("Serif", Font.BOLD, 18));
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(nameLbl);
        innerPanel.add(Box.createVerticalStrut(15));

        JLabel completedLbl = new JLabel("has successfully completed");
        completedLbl.setFont(new Font("Serif", Font.PLAIN, 13));
        completedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(completedLbl);
        innerPanel.add(Box.createVerticalStrut(8));

        JLabel courseLbl = new JLabel(certificate.getCourseTitle());
        courseLbl.setFont(new Font("Serif", Font.BOLD, 16));
        courseLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(courseLbl);
        innerPanel.add(Box.createVerticalStrut(20));

        JLabel dateLbl = new JLabel("Date: " + certificate.getFormattedIssueDate());
        dateLbl.setFont(new Font("Dialog", Font.PLAIN, 11));
        dateLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(dateLbl);
        innerPanel.add(Box.createVerticalStrut(3));

        JLabel idLbl = new JLabel("Certificate ID: " + certificate.getCertificateId());
        idLbl.setFont(new Font("Dialog", Font.PLAIN, 10));
        idLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(idLbl);

        contentPanel.add(innerPanel);
        certPanel.add(contentPanel);
        add(certPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton closeBtn = createStyledButton("Close", null);
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}