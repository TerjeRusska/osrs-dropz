package com.dropz;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

// Yeeted this from worldhopper plugin
class AutolistOrderingPanel extends JPanel {
    private static final ImageIcon ARROW_UP;
    private static final ImageIcon HIGHLIGHT_ARROW_DOWN;
    private static final ImageIcon HIGHLIGHT_ARROW_UP;

    private static final Color ARROW_COLOR = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color HIGHLIGHT_COLOR = ColorScheme.BRAND_ORANGE;

    static {
        final BufferedImage arrowDown = ImageUtil.loadImageResource(DropzPlugin.class, "/arrow_down.png");
        final BufferedImage arrowUp = ImageUtil.rotateImage(arrowDown, Math.PI);
        final BufferedImage arrowUpFaded = ImageUtil.luminanceOffset(arrowUp, -80);
        ARROW_UP = new ImageIcon(arrowUpFaded);

        final BufferedImage highlightArrowDown = ImageUtil.fillImage(arrowDown, HIGHLIGHT_COLOR);
        final BufferedImage highlightArrowUp = ImageUtil.fillImage(arrowUp, HIGHLIGHT_COLOR);
        HIGHLIGHT_ARROW_DOWN = new ImageIcon(highlightArrowDown);
        HIGHLIGHT_ARROW_UP = new ImageIcon(highlightArrowUp);
    }

    private final JLabel textLabel = new JLabel();
    private final JLabel arrowLabel = new JLabel();
    // Determines if this header column is being used to order the list
    private boolean ordering = false;

    AutolistOrderingPanel(String title, boolean ordered, boolean ascending) {
        setLayout(new BorderLayout(3, 0));
        setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, ColorScheme.MEDIUM_GRAY_COLOR),
                new EmptyBorder(0, 5, 0, 2)));
        setBackground(ColorScheme.SCROLL_TRACK_COLOR);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                textLabel.setForeground(HIGHLIGHT_COLOR);
                if (!ordering) {
                    arrowLabel.setIcon(HIGHLIGHT_ARROW_UP);
                }
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                if (!ordering) {
                    textLabel.setForeground(ARROW_COLOR);
                    arrowLabel.setIcon(ARROW_UP);
                }
            }
        });

        textLabel.setText(title);
        textLabel.setFont(FontManager.getRunescapeSmallFont());

        highlight(ordered, ascending);

        add(textLabel, BorderLayout.WEST);
        add(arrowLabel, BorderLayout.EAST);
    }

    /**
     * The labels inherit the parent's mouse listeners.
     */
    @Override
    public void addMouseListener(MouseListener mouseListener) {
        super.addMouseListener(mouseListener);
        textLabel.addMouseListener(mouseListener);
        arrowLabel.addMouseListener(mouseListener);
    }

    /**
     * If this column header is being used to order, then it should be
     * highlighted, changing its font color and icon.
     */
    public void highlight(boolean on, boolean ascending) {
        ordering = on;
        arrowLabel.setIcon(on ? (ascending ? HIGHLIGHT_ARROW_UP : HIGHLIGHT_ARROW_DOWN) : ARROW_UP);
        textLabel.setForeground(on ? HIGHLIGHT_COLOR : ARROW_COLOR);
    }
}
