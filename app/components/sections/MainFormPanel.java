package components.sections;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MainFormPanel extends JPanel {
    public JTextField nameField;
    public JSpinner dodSpinner;
    public JTextArea descriptionField;
    public JTextArea aiContentArea;

    public MainFormPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(300, 50));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        nameField.setBorder(BorderFactory.createTitledBorder("Name of Deceased"));

        SpinnerDateModel dateModel = new SpinnerDateModel();
        dodSpinner = new JSpinner(dateModel);
        dodSpinner.setEditor(new JSpinner.DateEditor(dodSpinner, "yyyy-MM-dd"));
        dodSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        dodSpinner.setBorder(BorderFactory.createTitledBorder("Date of Death"));

        descriptionField = new JTextArea(3, 20);
        descriptionField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));
        descriptionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        descriptionField.setBorder(BorderFactory.createTitledBorder("Description"));
        descriptionField.setLineWrap(true);

        aiContentArea = new JTextArea(1, 20);
        aiContentArea.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));
        aiContentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        aiContentArea.setBorder(BorderFactory.createTitledBorder("AI Content"));
        aiContentArea.setLineWrap(true);

        add(nameField);
        add(dodSpinner);
        add(new JScrollPane(descriptionField));
        add(new JScrollPane(aiContentArea));
    }
}