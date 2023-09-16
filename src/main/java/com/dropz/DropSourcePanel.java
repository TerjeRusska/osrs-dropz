package com.dropz;

import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class DropSourcePanel extends JPanel {

    private final BufferedImage BANKNOTE_IMG = ImageUtil.loadImageResource(DropzPlugin.class, "/bank_note.png");
    private static final BufferedImage WIKI_LOOKUP_IMG = ImageUtil.loadImageResource(DropzPlugin.class, "/Wiki_lookup.png");
    private static final String baseWikiUrl = "https://oldschool.runescape.wiki";
    private static final Dimension ICON_SIZE = new Dimension(16, 16);
    @Getter
    private final DropSource dropSource;
    private final BufferedImage skillIcon;
    private final GridBagConstraints constraints = new GridBagConstraints();
    private final List<JPanel> colorPanels = new ArrayList<>();

    DropSourcePanel(DropSource dropSource, BufferedImage skillIcon) {
        this.dropSource = dropSource;
        this.skillIcon = skillIcon;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 7, 0));

        JPanel dropSourceTable = new JPanel(new GridBagLayout());
        dropSourceTable.setBorder(new EmptyBorder(4, 0, 4, 0));
        colorPanels.add(dropSourceTable);

        MouseAdapter dropSourceTableMouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                for (JPanel panel : colorPanels) {
                    matchComponentBackground(panel, ColorScheme.DARK_GRAY_HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for (JPanel panel : colorPanels) {
                    matchComponentBackground(panel, ColorScheme.DARKER_GRAY_COLOR);
                }
            }
        };
        dropSourceTable.addMouseListener(dropSourceTableMouseListener);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        JPanel namePanel = buildNamePanel();
        JPanel levelPanel = buildLevelPanel();
        JPanel quantityPanel = buildQuantityPanel();
        JPanel rarityPanel = buildRarityPanel();
        JPanel rarityBar = new JPanel(new BorderLayout());
        rarityBar.setBackground(dropSource.getRarityColorType());
        rarityBar.setPreferredSize(new Dimension(7, 0));

        dropSourceTable.add(namePanel, constraints);
        constraints.gridy++;
        dropSourceTable.add(levelPanel, constraints);
        constraints.gridy++;
        dropSourceTable.add(quantityPanel, constraints);
        constraints.gridy++;
        dropSourceTable.add(rarityPanel, constraints);

        for (JPanel panel : colorPanels) {
            matchComponentBackground(panel, ColorScheme.DARKER_GRAY_COLOR);
        }

        add(rarityBar, BorderLayout.WEST);
        add(dropSourceTable, BorderLayout.CENTER);
    }

    private JPanel buildNamePanel() {
        JPanel container = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(dropSource.getSourceName());
        nameLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setMaximumSize(new Dimension(0, 0));
        nameLabel.setPreferredSize(new Dimension(0, 0));

        JPanel wikiLookupIconWrapper = new JPanel(new BorderLayout());
        if (dropSource.getSourceHref() != null) {
            JLabel wikiLookupIcon = new JLabel(new ImageIcon(WIKI_LOOKUP_IMG));
            wikiLookupIconWrapper.setBorder(new EmptyBorder(0, 5, 0, 4));
            wikiLookupIconWrapper.add(wikiLookupIcon, BorderLayout.CENTER);
            MouseAdapter wikiLookupMouseListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    final String url = baseWikiUrl + dropSource.getSourceHref();
                    LinkBrowser.browse(url);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            };
            wikiLookupIconWrapper.addMouseListener(wikiLookupMouseListener);
        }
        colorPanels.add(wikiLookupIconWrapper);

        container.add(nameLabel, BorderLayout.CENTER);
        container.add(wikiLookupIconWrapper, BorderLayout.EAST);
        return container;
    }

    private JPanel buildQuantityPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel quantityPanel = new JPanel(new BorderLayout());
        quantityPanel.setBorder(new EmptyBorder(0, 4, 0, 4));
        colorPanels.add(quantityPanel);

        JPanel banknoteIconWrapper = new JPanel(new BorderLayout());
        if (dropSource.getIsNoted()) {
            Image banknoteImage = new ImageIcon(BANKNOTE_IMG).getImage();
            Image banknoteImageScaled = banknoteImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            JLabel banknoteIcon = new JLabel(new ImageIcon(banknoteImageScaled));
            banknoteIcon.setPreferredSize(ICON_SIZE);
            banknoteIconWrapper.setBorder(new EmptyBorder(0, 0, 0, 5));
            banknoteIconWrapper.add(banknoteIcon, BorderLayout.CENTER);
        }

        JLabel quantityLabel = new JLabel(dropSource.getParsedQuantity());

        quantityPanel.add(banknoteIconWrapper, BorderLayout.WEST);
        quantityPanel.add(quantityLabel, BorderLayout.CENTER);
        container.add(quantityPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildRarityPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JLabel rarityLabel = new JLabel(dropSource.getRarityFraction() != null ? dropSource.getRarityFraction() : dropSource.getRarityString());
        rarityLabel.setToolTipText(dropSource.getRarityPercent() != null ? dropSource.getRarityPercent() + "%" : null);
        rarityLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
        container.add(rarityLabel, BorderLayout.NORTH);
        return container;
    }

    private JPanel buildLevelPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.setBorder(new EmptyBorder(0, 4, 0, 4));
        colorPanels.add(levelPanel);

        JPanel levelTypeIconWrapper = new JPanel(new BorderLayout());
        JLabel levelTypeIcon = new JLabel(new ImageIcon());
        levelTypeIcon.setPreferredSize(new Dimension(0, 0));

        if (skillIcon != null) {
            Image levelTypeImage = new ImageIcon(skillIcon).getImage();
            Image levelTypeImageScaled = levelTypeImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            levelTypeIcon = new JLabel(new ImageIcon(levelTypeImageScaled));
            levelTypeIcon.setPreferredSize(ICON_SIZE);
            levelTypeIconWrapper.setBorder(new EmptyBorder(0, 0, 0, 5));
        }
        levelTypeIconWrapper.add(levelTypeIcon, BorderLayout.CENTER);

        JLabel levelLabel = new JLabel(dropSource.getLevel());

        levelPanel.add(levelTypeIconWrapper, BorderLayout.WEST);
        levelPanel.add(levelLabel, BorderLayout.CENTER);
        container.add(levelPanel, BorderLayout.NORTH);
        return container;
    }

    private void matchComponentBackground(JPanel panel, Color color) {
        panel.setBackground(color);
        for (Component c : panel.getComponents()) {
            c.setBackground(color);
        }
    }
}
