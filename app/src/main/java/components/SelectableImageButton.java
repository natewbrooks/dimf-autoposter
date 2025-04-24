package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SelectableImageButton extends JPanel {
    private boolean selected = false;
    private final ImageIcon icon;
    private final int SIZE = 30;
    private float opacity = 0.3f;
    private final float SELECTED_OPACITY = 1.0f;
    private final float DESELECTED_OPACITY = 0.3f;
    private Timer fadeTimer;

    public SelectableImageButton(ImageIcon originalIcon) {
        // Scale icon
        Image img = originalIcon.getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);
        this.icon = new ImageIcon(img);

        setPreferredSize(new Dimension(SIZE, SIZE));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(!selected);
            }
        });

        // Timer for smooth fading
        fadeTimer = new Timer(15, e -> animateOpacity());
    }

    private void animateOpacity() {
        float target = selected ? SELECTED_OPACITY : DESELECTED_OPACITY;
        float speed = 0.05f;

        if (Math.abs(opacity - target) < speed) {
            opacity = target;
            fadeTimer.stop();
        } else {
            opacity += (target > opacity) ? speed : -speed;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (getWidth() - icon.getIconWidth()) / 2;
        int y = (getHeight() - icon.getIconHeight()) / 2;

        // Fade icon
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        icon.paintIcon(this, g2d, x, y);
        g2d.setComposite(originalComposite);

        g2d.dispose();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        fadeTimer.start();
    }
}
