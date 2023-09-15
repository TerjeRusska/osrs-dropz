package com.dropz;

import com.google.inject.Provides;

import javax.inject.Inject;
import javax.swing.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.List;

import static net.runelite.api.MenuAction.*;

@Slf4j
@PluginDescriptor(
        name = "Dropz",
        description = "TODO",
        tags = {"item", "items", "drop", "drops", "dropz", "find", "finder", "loot"},
        loadWhenOutdated = true
)
public class DropzPlugin extends Plugin {

    private static final List<MenuAction> excludedExamineMenuActions = List.of(EXAMINE_NPC, EXAMINE_OBJECT);

    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;

    @Inject
    private DropzPluginConfig config;

    @Getter(AccessLevel.PACKAGE)
    private DropzPluginPanel panel;

    @Inject
    private ClientToolbar clientToolbar;
    @Getter(AccessLevel.PACKAGE)
    private NavigationButton navButton;

    @Override
    protected void startUp() {
        panel = injector.getInstance(DropzPluginPanel.class);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/Potato_with_cheese.png");
        navButton = NavigationButton.builder()
                .tooltip("Dropz")
                .icon(icon)
                .priority(8)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
        if (!navButton.isSelected()) {
            return;
        }

        MenuAction menuAction = menuOptionClicked.getMenuAction();
        if (menuOptionClicked.getMenuOption().equals("Examine") && !excludedExamineMenuActions.contains(menuAction)) {
            int itemId;
            if (menuAction == EXAMINE_ITEM_GROUND) {
                itemId = menuOptionClicked.getId();
            } else {
                // FIXME: itemID is -1 and ID is 10 if item is equipped action CC_OP_LOW_PRIORITY
                itemId = menuOptionClicked.getItemId();
            }
            ItemComposition item = itemManager.getItemComposition(itemId);
            log.info(String.format("%s, %s", item.getId(), item.getMembersName()));
            if (item.getMembersName() == null || item.getMembersName().equals("null")) {
                return;
            }

            SwingUtilities.invokeLater(() -> {
                this.getPanel().getItemSearchPanel().itemLookup(item);
            });
        }
    }

    @Provides
    DropzPluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DropzPluginConfig.class);
    }
}
