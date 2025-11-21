package ui;

import javax.swing.*;
import java.awt.*;

public abstract class BaseFrame extends JFrame {
    protected static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    protected static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    protected static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    protected static final Color DANGER_COLOR = new Color(192, 57, 43);
    protected static final Color WARNING_COLOR = new Color(243, 156, 18);
    protected static final Color BG_COLOR = new Color(236, 240, 241);
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    protected static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    protected static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public BaseFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
    }

    protected JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(REGULAR_FONT);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));
        return btn;
    }

    protected JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(250, 35));
        return field;
    }

    protected JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(250, 35));
        return field;
    }

    protected JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(REGULAR_FONT);
        return lbl;
    }

    protected JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(TITLE_FONT);
        lbl.setForeground(SECONDARY_COLOR);
        return lbl;
    }

    protected JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        JLabel lbl = createLabel(labelText);
        lbl.setPreferredSize(new Dimension(120, 30));
        row.add(lbl);
        row.add(field);
        return row;
    }

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean showConfirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    protected abstract void initComponents();
}
