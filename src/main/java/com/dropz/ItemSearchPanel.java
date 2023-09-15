package com.dropz;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Slf4j
class ItemSearchPanel extends JPanel {

    private final Client client;
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
    private static final BufferedImage WIKI_LOOKUP_IMG = ImageUtil.loadImageResource(DropzPlugin.class, "/Wiki_lookup.png");
    private static final String baseWikiUrl = "https://oldschool.runescape.wiki";
    private String selectedItemHref = "";
    private final JPanel searchBackIconWrapperPanel = new JPanel(new BorderLayout());
    private final JPanel wikiLookupIconWrapper = new JPanel(new BorderLayout());
    private List<ItemComposition> allItemCompositions = new ArrayList<>();
    private List<ItemComposition> resultItemCompositionList = new ArrayList<>();

    @Inject
    ItemSearchPanel(Client client, ClientThread clientThread,
                    ItemManager itemManager,
                    ScheduledExecutorService executor,
                    DropSourcesAutolistPanel dropSourcesAutolistPanel) {
        this.client = client;
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.executor = executor;
        // TODO: Autolist panels as MaterialTabs to incorporate Store locations
        this.dropSourcesAutolistPanel = dropSourcesAutolistPanel;

        clientThread.invokeLater(() -> allItemCompositions = IntStream.range(0, client.getItemCount())
                .mapToObj(itemManager::getItemComposition)
                .filter(itemComposition -> itemComposition.getNote() == -1)
                .collect(Collectors.toList()));

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
                wikiLookupIconWrapper.setVisible(false);
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

        JLabel wikiLookupIcon = new JLabel(new ImageIcon(WIKI_LOOKUP_IMG));
        wikiLookupIconWrapper.setBorder(new EmptyBorder(0, 5, 0, 17));
        wikiLookupIconWrapper.add(wikiLookupIcon, BorderLayout.CENTER);
        wikiLookupIconWrapper.setVisible(false);
        MouseAdapter wikiLookupMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String url = baseWikiUrl + selectedItemHref;
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
        searchPanel.add(wikiLookupIconWrapper, BorderLayout.EAST);

        mainContainer.add(searchItemsScrollPane, SEARCH_PANEL);
        mainContainer.add(dropSourcesAutolistPanel, AUTOLIST_RESULTS);
        cardLayout.show(mainContainer, SEARCH_PANEL);

        add(searchPanel, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
    }

    private boolean updateSearch() {
        searchBackIconWrapperPanel.setVisible(false);
        wikiLookupIconWrapper.setVisible(false);
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

        resultItemCompositionList = allItemCompositions.stream()
                .filter(itemComposition -> itemComposition.getMembersName().toLowerCase().contains(searchBar.getText().toLowerCase()))
                .collect(Collectors.toList());
        if (item != null) {
            resultItemCompositionList = resultItemCompositionList.stream().filter(itemComposition -> itemComposition.getId() == item.getId()).collect(Collectors.toList());
        }
        resultItemCompositionList = resultItemCompositionList.stream().collect(
                collectingAndThen(
                        toCollection(() ->
                                new TreeSet<>(Comparator.comparing(ItemComposition::getMembersName))),
                        ArrayList::new));
        if (resultItemCompositionList.isEmpty()) {
            searchBar.setIcon(IconTextField.Icon.ERROR);
            infoPanel.setContent(SEARCH_FAIL[0], SEARCH_FAIL[1]);
            searchBar.setEditable(true);
            return;
        }
        if (resultItemCompositionList.size() > 1) {
            infoPanel.setContent(SEARCH_OPTIONS[0], SEARCH_OPTIONS[1]);
            List<ItemComposition> finalResult = resultItemCompositionList;
            clientThread.invokeLater(() -> processResult(finalResult));
            return;
        }

        setItemMatch(resultItemCompositionList.get(0).getMembersName());
    }

    void setItemMatch(String itemName) {
        cardLayout.show(mainContainer, AUTOLIST_RESULTS);
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setEditable(true);

        // TODO: add item icon? maybe pretty maybe not
        selectedItemHref = "/w/" + itemName.replace(" ", "_");
        infoPanel.setContent(itemName, "");
        wikiLookupIconWrapper.setVisible(true);
        if (resultItemCompositionList.size() > 1) {
            searchBackIconWrapperPanel.setVisible(true);
        }
        dropSourcesAutolistPanel.updateDropSourcesAutolist(itemName);
    }

    private void processResult(List<ItemComposition> result) {
        itemSearchResultList.clear();
        int count = 0;
        for (ItemComposition item : result) {
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
                        itemSearchResult.getItemComposition().getMembersName(),
                        itemSearchResult.getItemComposition().getId());

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
