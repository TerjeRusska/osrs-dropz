package com.dropz;

import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

class DropSourcePanel extends JPanel {

    public static final BufferedImage BANKNOTE_IMG = ImageUtil.loadImageResource(DropzPlugin.class, "/bank_note.png");
    private static final Dimension ICON_SIZE = new Dimension(16, 16);
    private final DropSource dropSource;
    private final BufferedImage skillIcon;
    private final GridBagConstraints constraints = new GridBagConstraints();

    DropSourcePanel(DropSource dropSource, BufferedImage skillIcon) {
        this.dropSource = dropSource;
        this.skillIcon = skillIcon;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0,0,7,0));

        JPanel dropSourceTable = new JPanel(new GridBagLayout());
        dropSourceTable.setBorder(new EmptyBorder(4, 0, 4, 0));

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
        rarityBar.setPreferredSize(new Dimension(7,0));

        dropSourceTable.add(namePanel, constraints);
        constraints.gridy++;
        dropSourceTable.add(levelPanel, constraints);
        constraints.gridy++;
        dropSourceTable.add(quantityPanel, constraints);
        constraints.gridy++;
        dropSourceTable.add(rarityPanel, constraints);

        add(rarityBar, BorderLayout.WEST);
        add(dropSourceTable, BorderLayout.CENTER);
    }

    private JPanel buildNamePanel() {
        JPanel container = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(dropSource.getSourceName());
        nameLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
        nameLabel.setForeground(Color.WHITE);
        container.add(nameLabel, BorderLayout.NORTH);
        return container;
    }

    private JPanel buildQuantityPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel quantityPanel = new JPanel(new BorderLayout());
        quantityPanel.setBorder(new EmptyBorder(0,4,0,4));

        JPanel banknoteIconWrapper = new JPanel(new BorderLayout());
        if (dropSource.getIsNoted()) {
            Image banknoteImage = new ImageIcon(BANKNOTE_IMG).getImage();
            Image banknoteImageScaled = banknoteImage.getScaledInstance(16,16, Image.SCALE_SMOOTH);
            JLabel banknoteIcon = new JLabel(new ImageIcon(banknoteImageScaled));
            banknoteIcon.setPreferredSize(ICON_SIZE);
            banknoteIconWrapper.setBorder(new EmptyBorder(0,0,0,5));
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
        levelPanel.setBorder(new EmptyBorder(0,4,0,4));

        JPanel levelTypeIconWrapper = new JPanel(new BorderLayout());
        JLabel levelTypeIcon = new JLabel(new ImageIcon());
        levelTypeIcon.setPreferredSize(new Dimension(0,0));

        if (skillIcon != null) {
            Image levelTypeImage = new ImageIcon(skillIcon).getImage();
            Image levelTypeImageScaled = levelTypeImage.getScaledInstance(16,16, Image.SCALE_SMOOTH);
            levelTypeIcon = new JLabel(new ImageIcon(levelTypeImageScaled));
            levelTypeIcon.setPreferredSize(ICON_SIZE);
            levelTypeIconWrapper.setBorder(new EmptyBorder(0,0,0,5));
        }
        levelTypeIconWrapper.add(levelTypeIcon, BorderLayout.CENTER);

        JLabel levelLabel = new JLabel(dropSource.getLevel());

        levelPanel.add(levelTypeIconWrapper, BorderLayout.WEST);
        levelPanel.add(levelLabel, BorderLayout.CENTER);
        container.add(levelPanel, BorderLayout.NORTH);
        return container;
    }
}
