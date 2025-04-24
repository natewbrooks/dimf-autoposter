package sections;

import javax.swing.*;
import java.awt.*;

public class SidebarPanel extends JPanel {
    public SidebarPanel(DefaultListModel<String> previousPostsModel, JButton downloadButton) {
        setBackground(new Color(40, 40, 40));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));

        JLabel title = new JLabel("Previous Posts");
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JList<String> postList = new JList<>(previousPostsModel);
        postList.setBackground(new Color(60, 60, 60));
        postList.setForeground(Color.WHITE);
        JScrollPane listScroll = new JScrollPane(postList);

        add(title, BorderLayout.NORTH);
        add(listScroll, BorderLayout.CENTER);
        add(downloadButton, BorderLayout.SOUTH);
    }
}
