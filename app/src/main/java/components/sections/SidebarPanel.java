package components.sections;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {
    private JList<String> previousPosts;
    private FormPanel formPanel;

    public SidebarPanel(DefaultListModel<String> postsModel, JButton exportExcelButton) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(225, 0));
        setBackground(new Color(45, 45, 60));
        setBorder(null);

        // Header
        JLabel headerLabel = new JLabel("Previous Posts", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Post list
        previousPosts = new JList<>(postsModel);
        previousPosts.setBackground(new Color(60, 60, 75));
        previousPosts.setForeground(Color.WHITE);
        previousPosts.setSelectionBackground(new Color(80, 80, 100));
        previousPosts.setSelectionForeground(Color.WHITE);
        previousPosts.setBorder(null);

        // Custom renderer with left margin
        previousPosts.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
                return label;
            }
        });

        previousPosts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        previousPosts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = previousPosts.locationToIndex(e.getPoint());
                    if (index >= 0 && formPanel != null) {
                        String selectedName = postsModel.getElementAt(index);
                        formPanel.loadPostByName(selectedName);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(previousPosts);
        scrollPane.setBorder(null);

        // Bottom buttons (full-width)
        JButton newPostBtn = new JButton("New Post");
        newPostBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        newPostBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newPostBtn.addActionListener(e -> {
            if (formPanel != null) {
                formPanel.resetForm();
            }
        });

        exportExcelButton.setText("Export Excel");
        exportExcelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        exportExcelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonPanel.add(newPostBtn);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(exportExcelButton);

        // Add to layout
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setFormPanel(FormPanel panel) {
        this.formPanel = panel;
    }
}
