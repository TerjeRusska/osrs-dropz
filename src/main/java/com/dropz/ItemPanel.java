package com.dropz;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class ItemPanel extends JPanel {

    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    ItemPanel(ItemSearchPanel itemSearchPanel, AsyncBufferedImage itemImage, String itemName, int itemId) {
        setLayout(new BorderLayout(10, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        Color background = getBackground();
        List<JPanel> panels = new ArrayList<>();
        panels.add(this);

        MouseAdapter itemPanelMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                itemSearchPanel.setItemMatch(itemName);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                for (JPanel panel : panels) {
                    matchComponentBackground(panel, ColorScheme.DARK_GRAY_HOVER_COLOR);
                }
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for (JPanel panel : panels) {
                    matchComponentBackground(panel, background);
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };

        addMouseListener(itemPanelMouseListener);

        setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel itemIcon = new JLabel();
        itemIcon.setPreferredSize(ICON_SIZE);
        if (itemImage != null) {
            itemImage.addTo(itemIcon);
        }
        add(itemIcon, BorderLayout.LINE_START);

        JPanel rightPanel = new JPanel(new GridLayout(1, 1));
        panels.add(rightPanel);
        rightPanel.setBackground(background);

        JLabel itemNameLabel = new JLabel();
        itemNameLabel.setForeground(Color.WHITE);
        itemNameLabel.setMaximumSize(new Dimension(0, 0));
        itemNameLabel.setPreferredSize(new Dimension(0, 0));
        itemNameLabel.setText(itemName);
        rightPanel.add(itemNameLabel);

        add(rightPanel, BorderLayout.CENTER);
    }

    private void matchComponentBackground(JPanel panel, Color color) {
        panel.setBackground(color);
        for (Component c : panel.getComponents()) {
            c.setBackground(color);
        }
    }
}
