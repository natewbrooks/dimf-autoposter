package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class SelectableImageButton extends JLabel {
    private static final int BOX_SIZE = 40;
    private boolean selected = false;
    private final Image colorImage;
    private final float deselectedAlpha = 0.3f;
    private float currentAlpha = deselectedAlpha;
    private float animationProgress = 1f;

    private final Timer animationTimer;

    public SelectableImageButton(ImageIcon icon) {
        Image img = icon.getImage();
        BufferedImage bufferedCopy = new BufferedImage(
                Math.max(1, img.getWidth(null)),
                Math.max(1, img.getHeight(null)),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedCopy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        this.colorImage = bufferedCopy.getScaledInstance(BOX_SIZE, BOX_SIZE, Image.SCALE_SMOOTH);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(BOX_SIZE + 6, BOX_SIZE + 6));

        selected = false;
        currentAlpha = deselectedAlpha;

        animationTimer = new Timer(15, e -> {
            animationProgress += 0.1f;
            if (animationProgress >= 1f) {
                animationProgress = 1f;
                ((Timer) e.getSource()).stop();
            }

            if (selected) {
                currentAlpha = deselectedAlpha + (1.0f - deselectedAlpha) * animationProgress;
            } else {
                currentAlpha = 1.0f - (1.0f - deselectedAlpha) * animationProgress;
            }

            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                animationProgress = 0f;
                animationTimer.start();
            }
        });
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.SrcOver.derive(currentAlpha));
        g2d.drawImage(colorImage, 3, 3, this);
        g2d.setComposite(originalComposite);
        g2d.dispose();
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            animationProgress = 0f;
            animationTimer.start();
        }
    }
}