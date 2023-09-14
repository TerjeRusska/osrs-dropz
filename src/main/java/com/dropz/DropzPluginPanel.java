package com.dropz;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
class DropzPluginPanel extends PluginPanel {

    @Getter(AccessLevel.PACKAGE)
    private final ItemSearchPanel itemSearchPanel;

    @Inject
    private DropzPluginPanel(ItemSearchPanel itemSearchPanel) {
        super(false);
        this.itemSearchPanel = itemSearchPanel;

        setLayout(new BorderLayout());
        // TODO: move ItemSearchPanel as the main DropzPluginPanel
        add(itemSearchPanel, BorderLayout.CENTER);
    }
}
