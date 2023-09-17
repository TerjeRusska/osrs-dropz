package com.dropz;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
class StoreLocationsAutolist {
    private static final String storeLocationsRequestBase = "format=json&action=parse&disablelimitreport=true&contentmodel=wikitext&text=";
    private static final String storeLocationsRequestFormat = "{{Store locations list|%s|limit=10000}}";

    private HashMap<String, BufferedImage> storeLocationsAutolistImageCache = new HashMap<>();
    private HashMap<String, ArrayList<StoreLocation>> storeLocationsAutolistCache = new HashMap<>();

    public ArrayList<StoreLocation> requestWiki(String itemName) {
        if (itemName == null) {
            return null;
        }
        if (storeLocationsAutolistCache.containsKey(itemName)) {
            return storeLocationsAutolistCache.get(itemName);
        }

        String wikiApiResponseBody = requestWikiAutolist(itemName);
        String wikiAutolistHtml = extractWikiAutolistHtml(wikiApiResponseBody);
        Element wikiAutolistTable = extractWikiAutolistTable(wikiAutolistHtml);
        ArrayList<StoreLocation> storeLocations = parseAutolistTable(wikiAutolistTable);
        if (storeLocations != null) {
            storeLocationsAutolistCache.put(itemName, storeLocations);
        }
        return storeLocations;
    }

    private String requestWikiAutolist(String itemName) {
        String requestQuery = storeLocationsRequestBase + String.format(storeLocationsRequestFormat, itemName);
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

    private ArrayList<StoreLocation> parseAutolistTable(Element wikiAutolistTable) {
        if (wikiAutolistTable == null) {
            return null;
        }
        ArrayList<StoreLocation> storeLocations = new ArrayList<>();

        int sellerColumnIndex = 0;
        int locationColumnIndex = 0;
        int stockColumnIndex = 0;
        int restockColumnIndex = 0;
        int soldColumnIndex = 0;
        int boughtColumnIndex = 0;
        int changeColumnIndex = 0;
        int membersColumnIndex = 0;

        Elements tableRows = wikiAutolistTable.select("tr");

        // Seller|Location|Number in stock|Restock time|Price sold at|Price bought at|Change Per|Members
        Element tableHeaderRow = tableRows.first();
        if (tableHeaderRow == null) {
            return null;
        }
        int index = 0;
        for (Element rowColumns : tableHeaderRow.select("th")) {
            switch (rowColumns.text()) {
                case "Seller":
                    sellerColumnIndex = index;
                    break;
                case "Location":
                    locationColumnIndex = index;
                    break;
                case "Number in stock":
                    stockColumnIndex = index;
                    break;
                case "Restock time":
                    restockColumnIndex = index;
                    break;
                case "Price sold at":
                    soldColumnIndex = index;
                    break;
                case "Price bought at":
                    boughtColumnIndex = index;
                    break;
                case "Change Per":
                    changeColumnIndex = index;
                    break;
                case "Members":
                    membersColumnIndex = index;
                    break;
            }
            index++;
        }

        tableRows.remove(0);
        for (Element tableRow : tableRows) {
            StoreLocation storeLocation = new StoreLocation();
            Elements rowColumns = tableRow.select("td");

            Element sellerColumn = rowColumns.get(sellerColumnIndex).select("a").first();
            if (sellerColumn != null) {
                storeLocation.setSellerName(Strings.emptyToNull(sellerColumn.text()));
                storeLocation.setSellerHref(Strings.emptyToNull(sellerColumn.attr("href")));
            }

            Element locationColumn = rowColumns.get(locationColumnIndex).select("a").first();
            if (locationColumn != null) {
                storeLocation.setLocationName(Strings.emptyToNull(locationColumn.text()));
                storeLocation.setLocationHref(Strings.emptyToNull(locationColumn.attr("href")));
            }

            Element stockColumn = rowColumns.get(stockColumnIndex);
            storeLocation.setStockAmount(Strings.emptyToNull(stockColumn.text()));
            //TODO: data sort value 10e99

            Element restockColumns = rowColumns.get(restockColumnIndex);
            storeLocation.setRestockTime(Strings.emptyToNull(restockColumns.text()));
            //TODO: data sort value 10e99

            Element soldColumn = rowColumns.get(soldColumnIndex);
            storeLocation.setSoldAtPrice(Strings.emptyToNull(soldColumn.text()));
            //TODO: data sort value
            Element soldColumnImage = soldColumn.select("a").first();
            if (soldColumnImage != null) {
                Element soldImage = soldColumnImage.select("img").first();
                if (soldImage != null) {
                    String soldImageSrc = Strings.emptyToNull(soldImage.attr("src"));
                    storeLocation.setSoldAtImgSrc(soldImageSrc);
                    cacheImage(soldImageSrc);
                }
            }

            Element boughtColumn = rowColumns.get(boughtColumnIndex);
            storeLocation.setBoughtAtPrice(Strings.emptyToNull(boughtColumn.text()));
            //TODO: data sort value
            Element boughtColumnImage = boughtColumn.select("a").first();
            if (boughtColumnImage != null) {
                Element boughtImage = boughtColumnImage.select("img").first();
                if (boughtImage != null) {
                    String boughtImageSrc = Strings.emptyToNull(boughtImage.attr("src"));
                    storeLocation.setBoughtAtImgSrc(boughtImageSrc);
                    cacheImage(boughtImageSrc);
                }
            }

            Element changeColumn = rowColumns.get(changeColumnIndex);
            storeLocation.setChangePer(Strings.emptyToNull(changeColumn.text()));

            Element membersColumn = rowColumns.get(membersColumnIndex).select("a").first();
            if (membersColumn != null) {
                storeLocation.setIsMembers(membersColumn.attr("title").equals("Pay-to-play"));
            }

            log.info(storeLocation.toString());
            storeLocations.add(storeLocation);
        }

        return storeLocations;
    }

    private void cacheImage(String imageSrc) {
        if (storeLocationsAutolistImageCache.containsKey(imageSrc)) {
            return;
        }
        BufferedImage image = OsrsWikiScraper.requestOsrsWikiImage(imageSrc);
        if (imageSrc == null || image == null) {
            return;
        }
        storeLocationsAutolistImageCache.put(imageSrc, image);
    }
}
