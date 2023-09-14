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

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Getter
class DropSourcesAutolist {
    private static final String dropSourcesRequestBase = "format=json&action=parse&disablelimitreport=true&contentmodel=wikitext";
    private static final String dropSourcesRequestFormat = "&text={{Drop sources|%s|limit=10000|incrdt=y}}";

    public static ArrayList<DropSource> requestWiki(String itemName) throws IOException {
        String wikiApiResponseBody = requestWikiAutolist(itemName);
        String wikiAutolistHtml = extractWikiAutolistHtml(wikiApiResponseBody);
        Element wikiAutolistTable = extractWikiAutolistTable(wikiAutolistHtml);
        //log.info("Autolist table elem: " + wikiAutolistTable);
        if (wikiAutolistTable == null) {
            return null;
        }
        return parseAutolistTable(wikiAutolistTable);
    }

    private static String requestWikiAutolist(String itemName) throws IOException {
        String requestQuery = dropSourcesRequestBase + String.format(dropSourcesRequestFormat, itemName);
        return OsrsWikiScraper.requestOsrsWikiApi(requestQuery);
    }

    private static String extractWikiAutolistHtml(String wikiApiResponseBody) {
        JsonObject responseJson = new JsonParser().parse(wikiApiResponseBody).getAsJsonObject();
        return responseJson.get("parse").getAsJsonObject().get("text").getAsJsonObject().get("*").getAsString();
    }

    private static Element extractWikiAutolistTable(String wikiAutolistHtml) {
        Document doc = Jsoup.parseBodyFragment(wikiAutolistHtml);
        Elements docTables = doc.select("table");
        return docTables.isEmpty() ? null : docTables.first();
    }

    private static ArrayList<DropSource> parseAutolistTable(Element wikiAutolistTable) {
        ArrayList<DropSource> dropSources = new ArrayList<>();

        int sourceColumnIndex = 0;
        int levelColumnIndex = 0;
        int quantityColumnIndex = 0;
        int rarityColumnIndex = 0;

        Elements tableRows = wikiAutolistTable.select("tr");

        // Source|Level|Quantity|Rarity
        Element tableHeaderRow = tableRows.first();
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
            dropSource.setSourceName(Strings.emptyToNull(sourceColumn.text()));
            dropSource.setSourceHref(Strings.emptyToNull(sourceColumn.attr("href")));

            Element levelColumn = rowColumns.get(levelColumnIndex);
            dropSource.setLevel(Strings.emptyToNull(levelColumn.text()));
            dropSource.setLevelType(Strings.emptyToNull(levelColumn.select("span").first().attr("class")));
            dropSource.setLevelDataSortValue(Strings.emptyToNull(levelColumn.attr("data-sort-value")));

            Element quantityColumn = rowColumns.get(quantityColumnIndex);
            dropSource.setQuantity(Strings.emptyToNull(quantityColumn.text()));
            if (!quantityColumn.select("span[class=dropsline-noted]").isEmpty()) {
                dropSource.setIsNoted(true);
            } else {
                dropSource.setIsNoted(false);
            }
            dropSource.setQuantityDataSortValue(Strings.emptyToNull(quantityColumn.attr("data-sort-value")));

            Element rarityColumn = rowColumns.get(rarityColumnIndex).select("span").first();
            dropSource.setRarityString(Strings.emptyToNull(rarityColumn.text()));
            dropSource.setRarityPercent(Strings.emptyToNull(rarityColumn.attr("data-drop-percent")));
            dropSource.setRarityPermil(Strings.emptyToNull(rarityColumn.attr("data-drop-permil")));
            dropSource.setRarityFraction(Strings.emptyToNull(rarityColumn.attr("data-drop-fraction")));
            dropSource.setRarityPermyriad(Strings.emptyToNull(rarityColumn.attr("data-drop-permyriad")));
            dropSource.setRarityOneover(Strings.emptyToNull(rarityColumn.attr("data-drop-oneover")));
            dropSource.setRarityDataSortValue(Strings.emptyToNull(rowColumns.get(rarityColumnIndex).attr("data-sort-value")));
            dropSource.setRarityColor(Strings.emptyToNull(rowColumns.get(rarityColumnIndex).attr("class")));

            //log.info(dropSource.toString());
            dropSources.add(dropSource);
        }

        return dropSources;
    }
}
