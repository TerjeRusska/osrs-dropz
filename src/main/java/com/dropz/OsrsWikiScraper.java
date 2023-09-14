package com.dropz;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
class OsrsWikiScraper {

    private static final OkHttpClient client = new OkHttpClient();
    /**
     * <a href="https://oldschool.runescape.wiki/api.php">MediaWiki API help</a>
     */
    static final String baseWikiApiUrl = "https://oldschool.runescape.wiki/api.php?";

    static String requestOsrsWikiApi(String requestQuery) throws IOException {
        String requestUrl = baseWikiApiUrl + requestQuery;
        log.info("Wiki API request: " + requestUrl);
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String responseBody = response.body().string();
            //log.info("Wiki API response: " + responseBody);
            return responseBody;
        }
    }
}
