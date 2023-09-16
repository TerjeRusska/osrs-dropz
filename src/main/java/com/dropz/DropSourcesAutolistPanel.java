package com.dropz;

import com.google.common.collect.Ordering;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.dropz.DropSource.LEVEL_TYPE_COMBAT;
import static com.dropz.DropSource.LEVEL_TYPE_REWARD;


class DropSourcesAutolistPanel extends JPanel {
    private final ClientThread clientThread;
    private final SkillIconManager skillIconManager;
    private final JPanel dropSourcesAutolistGridPanel = new JPanel(new GridBagLayout());
    private final JPanel dropSourcesAutolistWrapperPanel = new JPanel(new BorderLayout());
    private final JScrollPane dropSourcesAutolistScrollPane = new JScrollPane(
            dropSourcesAutolistWrapperPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final GridBagConstraints constraints = new GridBagConstraints();
    private final List<DropSource> dropSourceList = new ArrayList<>();
    private final List<DropSourcePanel> dropSourcePanelList = new ArrayList<>();

    private final BufferedImage combatIcon;
    private final BufferedImage rewardIcon;

    private AutolistOrderingPanel nameOrderingPanel;
    private AutolistOrderingPanel levelOrderingPanel;
    private AutolistOrderingPanel quantityOrderingPanel;
    private AutolistOrderingPanel rarityOrderingPanel;

    private AutolistOrder orderIndex = AutolistOrder.NAME;
    private boolean ascendingOrder = true;

    @Inject
    DropSourcesAutolistPanel(ClientThread clientThread, SkillIconManager skillIconManager) {
        this.clientThread = clientThread;
        this.skillIconManager = skillIconManager;

        combatIcon = ImageUtil.loadImageResource(getClass(), "/multicombat.png");
        rewardIcon = ImageUtil.loadImageResource(getClass(), "/casket.png");

        setLayout(new BorderLayout());

        JPanel dropSourcesAutolistOrderingHeader = buildOrderingPanel();

        dropSourcesAutolistWrapperPanel.add(dropSourcesAutolistGridPanel, BorderLayout.NORTH);

        dropSourcesAutolistScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        dropSourcesAutolistScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        dropSourcesAutolistScrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        add(dropSourcesAutolistOrderingHeader, BorderLayout.NORTH);
        add(dropSourcesAutolistScrollPane, BorderLayout.CENTER);
    }

    void updateDropSourcesAutolist(String itemName) {
        dropSourcesAutolistGridPanel.removeAll();

        clientThread.invokeLater(() -> {
            try {
                processDropSources(itemName);
            } catch (IOException ignored) {
            }
        });
    }

    void processDropSources(String itemName) throws IOException {
        dropSourceList.clear();
        dropSourcePanelList.clear();

        ascendingOrder = true;
        nameOrderingPanel.highlight(true, ascendingOrder);
        levelOrderingPanel.highlight(false, ascendingOrder);
        quantityOrderingPanel.highlight(false, ascendingOrder);
        rarityOrderingPanel.highlight(false, ascendingOrder);

        ArrayList<DropSource> wikiResults = DropSourcesAutolist.requestWiki(itemName);
        if (wikiResults == null) {
            return;
        }
        dropSourceList.addAll(wikiResults);

        SwingUtilities.invokeLater(() -> {
            for (DropSource dropSource : dropSourceList) {
                BufferedImage skillTypeIcon = getSkillTypeIcon(dropSource);
                DropSourcePanel dropSourcePanel = new DropSourcePanel(dropSource, skillTypeIcon);
                dropSourcePanelList.add(dropSourcePanel);
                dropSourcesAutolistGridPanel.add(dropSourcePanel, constraints);
                constraints.gridy++;
            }
            this.updateUI();
        });
    }

    private BufferedImage getSkillTypeIcon(DropSource dropSource) {
        Skill skill = dropSource.getSkillLevelType();
        if (skill != null) {
            return skillIconManager.getSkillImage(skill, true);
        } else if (dropSource.getParsedLevelType().equals(LEVEL_TYPE_COMBAT)) {
            return combatIcon;
        } else if (dropSource.getParsedLevelType().equals(LEVEL_TYPE_REWARD)) {
            return rewardIcon;
        }
        return null;
    }

    private JPanel buildOrderingPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 0, 3, 0));
        JPanel leftSide = new JPanel(new BorderLayout());
        JPanel rightSide = new JPanel(new BorderLayout());

        nameOrderingPanel = new AutolistOrderingPanel("Name", orderIndex == AutolistOrder.NAME, ascendingOrder);
        nameOrderingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ascendingOrder = orderIndex != AutolistOrder.NAME || !ascendingOrder;
                orderBy(AutolistOrder.NAME);
            }
        });

        levelOrderingPanel = new AutolistOrderingPanel("Lvl", orderIndex == AutolistOrder.LEVEL, ascendingOrder);
        levelOrderingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ascendingOrder = orderIndex != AutolistOrder.LEVEL || !ascendingOrder;
                orderBy(AutolistOrder.LEVEL);
            }
        });

        quantityOrderingPanel = new AutolistOrderingPanel("Qty", orderIndex == AutolistOrder.QUANTITY, ascendingOrder);
        quantityOrderingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ascendingOrder = orderIndex != AutolistOrder.QUANTITY || !ascendingOrder;
                orderBy(AutolistOrder.QUANTITY);
            }
        });

        rarityOrderingPanel = new AutolistOrderingPanel("Rarity", orderIndex == AutolistOrder.RARITY, ascendingOrder);
        rarityOrderingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ascendingOrder = orderIndex != AutolistOrder.RARITY || !ascendingOrder;
                orderBy(AutolistOrder.RARITY);
            }
        });

        leftSide.add(nameOrderingPanel, BorderLayout.WEST);
        leftSide.add(levelOrderingPanel, BorderLayout.CENTER);
        rightSide.add(quantityOrderingPanel, BorderLayout.CENTER);
        rightSide.add(rarityOrderingPanel, BorderLayout.EAST);

        header.add(leftSide, BorderLayout.WEST);
        header.add(rightSide, BorderLayout.CENTER);
        return header;
    }

    private void orderBy(AutolistOrder order) {
        nameOrderingPanel.highlight(false, ascendingOrder);
        levelOrderingPanel.highlight(false, ascendingOrder);
        quantityOrderingPanel.highlight(false, ascendingOrder);
        rarityOrderingPanel.highlight(false, ascendingOrder);

        switch (order) {
            case NAME:
                nameOrderingPanel.highlight(true, ascendingOrder);
                break;
            case LEVEL:
                levelOrderingPanel.highlight(true, ascendingOrder);
                break;
            case QUANTITY:
                quantityOrderingPanel.highlight(true, ascendingOrder);
                break;
            case RARITY:
                rarityOrderingPanel.highlight(true, ascendingOrder);
                break;
        }

        orderIndex = order;
        updateAutolistOrder(order);
    }

    private void updateAutolistOrder(AutolistOrder order) {
        dropSourcePanelList.sort((panel1, panel2) -> {
            switch (order) {
                case NAME:
                    return getCompareValue(panel1, panel2, panel -> panel.getDropSource().getSourceName());
                case LEVEL:
                    return getCompareValue(panel1, panel2, panel -> panel.getDropSource().getLevelDataSortValue());
                case QUANTITY:
                    return getCompareValue(panel1, panel2, panel -> panel.getDropSource().getQuantityDataSortValue());
                case RARITY:
                    return getCompareValue(panel1, panel2, panel -> panel.getDropSource().getRarityDataSortValue());
                default:
                    return 0;
            }
        });

        constraints.gridy = 0;
        SwingUtilities.invokeLater(() -> {
            dropSourcesAutolistGridPanel.removeAll();
            for (DropSourcePanel dropSourcePanel : dropSourcePanelList) {
                dropSourcesAutolistGridPanel.add(dropSourcePanel, constraints);
                constraints.gridy++;
            }
            this.updateUI();
        });
    }

    private int getCompareValue(DropSourcePanel panel1, DropSourcePanel panel2, Function<DropSourcePanel, Comparable> compareByFn) {
        Ordering<Comparable> ordering = Ordering.natural();
        if (!ascendingOrder) {
            ordering = ordering.reverse();
        }
        ordering = ordering.nullsLast();
        return ordering.compare(compareByFn.apply(panel1), compareByFn.apply(panel2));
    }

    private enum AutolistOrder {
        NAME,
        LEVEL,
        QUANTITY,
        RARITY
    }
}
