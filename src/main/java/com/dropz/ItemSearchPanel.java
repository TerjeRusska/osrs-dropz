package com.dropz;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemPrice;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Slf4j
class ItemSearchPanel extends JPanel {

    private final ClientThread clientThread;
    private final ItemManager itemManager;
    private final ScheduledExecutorService executor;
    private final DropSourcesAutolistPanel dropSourcesAutolistPanel;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);
    private final JPanel searchPanel = new JPanel(new BorderLayout());
    @Getter(AccessLevel.PACKAGE)
    private final IconTextField searchBar = new IconTextField();
    private final PluginErrorPanel infoPanel = new PluginErrorPanel();
    private final JPanel searchItemsGridPanel = new JPanel(new GridBagLayout());
    private final JPanel searchItemsWrapperPanel = new JPanel(new BorderLayout());
    private final JScrollPane searchItemsScrollPane = new JScrollPane(searchItemsWrapperPanel);
    private final GridBagConstraints constraints = new GridBagConstraints();
    private final List<ItemSearchResult> itemSearchResultList = new ArrayList<>();

    private static final int MAX_SEARCH_ITEMS = 100;
    private static final String[] SEARCH_DEFAULT = {"Item Search", "Examine an item or fill out the search bar."};
    private static final String[] SEARCH_FAIL = {"No results found.", "No items were found with that name."};
    private static final String[] SEARCH_OPTIONS = {"Multiple results found.", "Please pick a specific item from the list."};
    private static final String SEARCH_PANEL = "SEARCH_PANEL";
    private static final String AUTOLIST_RESULTS = "AUTOLIST_RESULTS";
    private static final BufferedImage SEARCH_BACK_ICON = ImageUtil.loadImageResource(DropzPlugin.class, "/back_icon.png");
    private final JPanel searchBackIconWrapperPanel = new JPanel(new BorderLayout());
    private List<ItemPrice> result = new ArrayList<>();

    @Inject
    ItemSearchPanel(ClientThread clientThread,
                    ItemManager itemManager,
                    ScheduledExecutorService executor,
                    DropSourcesAutolistPanel dropSourcesAutolistPanel) {
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.executor = executor;
        // TODO: Autolist panels as MaterialTabs to incorporate Store locations
        this.dropSourcesAutolistPanel = dropSourcesAutolistPanel;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(100, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.addClearListener(this::updateSearch);
        searchBar.addActionListener(e -> executor.execute(this::itemLookup));

        infoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        infoPanel.setContent(SEARCH_DEFAULT[0], SEARCH_DEFAULT[1]);
        searchBackIconWrapperPanel.add(new JLabel(new ImageIcon(SEARCH_BACK_ICON)), BorderLayout.CENTER);
        searchBackIconWrapperPanel.setVisible(false);
        MouseAdapter searchBackIconMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                infoPanel.setContent(SEARCH_OPTIONS[0], SEARCH_OPTIONS[1]);
                searchBackIconWrapperPanel.setVisible(false);
                cardLayout.show(mainContainer, SEARCH_PANEL);
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
        searchBackIconWrapperPanel.addMouseListener(searchBackIconMouseListener);


        searchItemsWrapperPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        searchItemsWrapperPanel.add(searchItemsGridPanel, BorderLayout.NORTH);

        searchItemsScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        searchItemsScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        searchItemsScrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        searchPanel.add(searchBar, BorderLayout.NORTH);
        searchPanel.add(searchBackIconWrapperPanel, BorderLayout.WEST);
        searchPanel.add(infoPanel, BorderLayout.CENTER);

        mainContainer.add(searchItemsScrollPane, SEARCH_PANEL);
        mainContainer.add(dropSourcesAutolistPanel, AUTOLIST_RESULTS);
        cardLayout.show(mainContainer, SEARCH_PANEL);

        add(searchPanel, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
    }

    private boolean updateSearch() {
        searchBackIconWrapperPanel.setVisible(false);
        cardLayout.show(mainContainer, SEARCH_PANEL);
        String lookup = searchBar.getText();
        searchItemsGridPanel.removeAll();

        if (Strings.isNullOrEmpty(lookup)) {
            searchBar.setIcon(IconTextField.Icon.SEARCH);
            infoPanel.setContent(SEARCH_DEFAULT[0], SEARCH_DEFAULT[1]);
            return false;
        }

        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setEditable(false);
        searchBar.setIcon(IconTextField.Icon.LOADING);
        return true;
    }

    private void itemLookup() {
        itemLookup(null);
    }

    void itemLookup(ItemComposition item) {
        if (item != null) {
            searchBar.setText(item.getMembersName());
        }

        if (!updateSearch()) {
            return;
        }

        result = itemManager.search(searchBar.getText());
        if (item != null) {
            // FIXME: Sometimes item can have same name with different ID, check for both (example dragon pickaxe)
            result = result.stream().filter(itemPrice -> itemPrice.getId() == item.getId()).collect(Collectors.toList());
        }
        if (result.isEmpty()) {
            searchBar.setIcon(IconTextField.Icon.ERROR);
            infoPanel.setContent(SEARCH_FAIL[0], SEARCH_FAIL[1]);
            searchBar.setEditable(true);
            return;
        }
        if (result.size() > 1) {
            infoPanel.setContent(SEARCH_OPTIONS[0], SEARCH_OPTIONS[1]);
            List<ItemPrice> finalResult = result;
            clientThread.invokeLater(() -> processResult(finalResult));
            return;
        }

        setItemMatch(result.get(0).getName());
    }

    void setItemMatch(String itemName) {
        cardLayout.show(mainContainer, AUTOLIST_RESULTS);
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setEditable(true);

        // TODO: add item icon and high alch
        infoPanel.setContent(itemName, "");
        if (result.size() > 1) {
            searchBackIconWrapperPanel.setVisible(true);
        }
        dropSourcesAutolistPanel.updateDropSourcesAutolist(itemName);
    }

    private void processResult(List<ItemPrice> result) {
        itemSearchResultList.clear();
        int count = 0;
        for (ItemPrice item : result) {
            if (count++ > MAX_SEARCH_ITEMS) {
                break;
            }
            AsyncBufferedImage itemImage = itemManager.getImage(item.getId());
            itemSearchResultList.add(new ItemSearchResult(item, itemImage));
        }

        SwingUtilities.invokeLater(() -> {
            int index = 0;
            for (ItemSearchResult itemSearchResult : itemSearchResultList) {

                ItemPanel itemPanel = new ItemPanel(this,
                        itemSearchResult.getItemImage(),
                        itemSearchResult.getItemPrice().getName(),
                        itemSearchResult.getItemPrice().getId());

                if (index++ > 0) {
                    JPanel marginWrapper = new JPanel(new BorderLayout());
                    marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
                    marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
                    marginWrapper.add(itemPanel, BorderLayout.NORTH);
                    searchItemsGridPanel.add(marginWrapper, constraints);
                } else {
                    searchItemsGridPanel.add(itemPanel, constraints);

                }
                constraints.gridy++;
            }

            searchBar.setEditable(true);
            if (!itemSearchResultList.isEmpty()) {
                searchBar.setIcon(IconTextField.Icon.SEARCH);
            }
            cardLayout.show(mainContainer, SEARCH_PANEL);
        });
    }
}
