package ui;

import javax.swing.*;
import java.awt.*;

public abstract class BaseFrame extends JFrame {
    protected static final Font REGULAR_FONT = new Font("Dialog", Font.PLAIN, 12);

    public BaseFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    protected JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(REGULAR_FONT);
        return btn;
    }

    protected JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(REGULAR_FONT);
        return field;
    }

    protected JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(REGULAR_FONT);
        return field;
    }

    protected JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(REGULAR_FONT);
        return lbl;
    }

    protected JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Dialog", Font.BOLD, 14));
        return lbl;
    }

    protected JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel lbl = createLabel(labelText);
        lbl.setPreferredSize(new Dimension(100, 20));
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