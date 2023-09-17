package com.dropz;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Getter
class DropSourcesAutolist {
    private static final String dropSourcesRequestBase = "format=json&action=parse&disablelimitreport=true&contentmodel=wikitext";
    private static final String dropSourcesRequestFormat = "&text={{Drop sources|%s|limit=10000|incrdt=y}}";

    private HashMap<String, ArrayList<DropSource>> dropSourcesAutolistCache = new HashMap<>();

    public ArrayList<DropSource> requestWiki(String itemName) {
        if (itemName == null) {
            return null;
        }
        if (dropSourcesAutolistCache.containsKey(itemName)) {
            return dropSourcesAutolistCache.get(itemName);
        }

        String wikiApiResponseBody = requestWikiAutolist(itemName);
        String wikiAutolistHtml = extractWikiAutolistHtml(wikiApiResponseBody);
        Element wikiAutolistTable = extractWikiAutolistTable(wikiAutolistHtml);
        ArrayList<DropSource> dropSources = parseAutolistTable(wikiAutolistTable);
        if (dropSources != null) {
            dropSourcesAutolistCache.put(itemName, dropSources);
        }
        return dropSources;
    }

    private String requestWikiAutolist(String itemName) {
        String requestQuery = dropSourcesRequestBase + String.format(dropSourcesRequestFormat, itemName);
        return OsrsWikiScraper.requestOsrsWikiApi(requestQuery);
    }

    private String extractWikiAutolistHtml(String wikiApiResponseBody) {
        JsonObject responseJson = new JsonParser().parse(wikiApiResponseBody).getAsJsonObject();
        return responseJson.get("parse").getAsJsonObject().get("text").getAsJsonObject().get("*").getAsString();
    }

    private Element extractWikiAutolistTable(String wikiAutolistHtml) {
        Document doc = Jsoup.parseBodyFragment(wikiAutolistHtml);
        Elements docTables = doc.select("table");
        return docTables.first();
    }

    private ArrayList<DropSource> parseAutolistTable(Element wikiAutolistTable) {
        if (wikiAutolistTable == null) {
            return null;
        }
        ArrayList<DropSource> dropSources = new ArrayList<>();

        int sourceColumnIndex = 0;
        int levelColumnIndex = 0;
        int quantityColumnIndex = 0;
        int rarityColumnIndex = 0;

        Elements tableRows = wikiAutolistTable.select("tr");

        // Source|Level|Quantity|Rarity
        Element tableHeaderRow = tableRows.first();
        if (tableHeaderRow == null) {
            return null;
        }
        int index = 0;
        for (Element rowColumns : tableHeaderRow.select("th")) {
            switch (rowColumns.text()) {
                case "Source":
                    sourceColumnIndex = index;
                    break;
                case "Level":
                    levelColumnIndex = index;
                    break;
                case "Quantity":
                    quantityColumnIndex = index;
                    break;
                case "Rarity":
                    rarityColumnIndex = index;
                    break;
            }
            index++;
        }

        tableRows.remove(0);
        for (Element tableRow : tableRows) {
            DropSource dropSource = new DropSource();
            Elements rowColumns = tableRow.select("td");

            Element sourceColumn = rowColumns.get(sourceColumnIndex).select("a").first();
            if (sourceColumn != null) {
                dropSource.setSourceName(Strings.emptyToNull(sourceColumn.text()));
                dropSource.setSourceHref(Strings.emptyToNull(sourceColumn.attr("href")));
            }

            Element levelColumn = rowColumns.get(levelColumnIndex);
            dropSource.setLevel(Strings.emptyToNull(levelColumn.text()));
            dropSource.setLevelType(Strings.emptyToNull(levelColumn.select("span").first().attr("class")));
            String levelDataSortValue = Strings.emptyToNull(levelColumn.attr("data-sort-value"));
            dropSource.setLevelDataSortValue(levelDataSortValue != null ? Double.valueOf(levelDataSortValue.replace(",", "")) : null);

            Element quantityColumn = rowColumns.get(quantityColumnIndex);
            dropSource.setQuantity(Strings.emptyToNull(quantityColumn.text()));
            if (!quantityColumn.select("span[class=dropsline-noted]").isEmpty()) {
                dropSource.setIsNoted(true);
            } else {
                dropSource.setIsNoted(false);
            }
            String quantityDataSortValue = Strings.emptyToNull(quantityColumn.attr("data-sort-value"));
            dropSource.setQuantityDataSortValue(quantityDataSortValue != null ? Double.valueOf(quantityDataSortValue.replace(",", "")) : null);

            Element rarityColumn = rowColumns.get(rarityColumnIndex).select("span").first();
            dropSource.setRarityString(Strings.emptyToNull(rarityColumn.text()));
            dropSource.setRarityPercent(Strings.emptyToNull(rarityColumn.attr("data-drop-percent")));
            dropSource.setRarityPermil(Strings.emptyToNull(rarityColumn.attr("data-drop-permil")));
            dropSource.setRarityFraction(Strings.emptyToNull(rarityColumn.attr("data-drop-fraction")));
            dropSource.setRarityPermyriad(Strings.emptyToNull(rarityColumn.attr("data-drop-permyriad")));
            dropSource.setRarityOneover(Strings.emptyToNull(rarityColumn.attr("data-drop-oneover")));
            String rarityDataSortValue = Strings.emptyToNull(rowColumns.get(rarityColumnIndex).attr("data-sort-value"));
            dropSource.setRarityDataSortValue(rarityDataSortValue != null ? Double.valueOf(rarityDataSortValue.replace(",", "")) : null);
            dropSource.setRarityColor(Strings.emptyToNull(rowColumns.get(rarityColumnIndex).attr("class")));

            //log.info(dropSource.toString());
            dropSources.add(dropSource);
        }

        return dropSources;
    }
}
