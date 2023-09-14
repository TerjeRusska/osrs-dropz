package com.dropz;

import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private final BufferedImage combatIcon;
    private final BufferedImage rewardIcon;

    @Inject
    DropSourcesAutolistPanel(ClientThread clientThread, SkillIconManager skillIconManager) {
        this.clientThread = clientThread;
        this.skillIconManager = skillIconManager;

        combatIcon = ImageUtil.loadImageResource(getClass(), "/multicombat.png");
        rewardIcon = ImageUtil.loadImageResource(getClass(), "/casket.png");

        setLayout(new BorderLayout());

        dropSourcesAutolistWrapperPanel.add(dropSourcesAutolistGridPanel, BorderLayout.NORTH);

        dropSourcesAutolistScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        dropSourcesAutolistScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        dropSourcesAutolistScrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        add(dropSourcesAutolistScrollPane, BorderLayout.CENTER);
    }

    // TODO: add re-ordering logic

    void updateDropSourcesAutolist (String itemName) {
        dropSourcesAutolistGridPanel.removeAll();

        clientThread.invokeLater(() -> {
            try {
                processDropSources(itemName);
            } catch (IOException ignored) {}
        });
    }

    void processDropSources(String itemName) throws IOException {
        dropSourceList.clear();
        ArrayList<DropSource> wikiResults = DropSourcesAutolist.requestWiki(itemName);
        if (wikiResults == null) {
            return;
        }
        dropSourceList.addAll(wikiResults);

        SwingUtilities.invokeLater(() -> {
            for (DropSource dropSource : dropSourceList) {
                BufferedImage skillTypeIcon = getSkillTypeIcon(dropSource);
                DropSourcePanel dropSourcePanel = new DropSourcePanel(dropSource, skillTypeIcon);
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
}
