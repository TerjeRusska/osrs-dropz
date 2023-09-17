package com.dropz;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

@Slf4j
class OsrsWikiScraper {

    private static final OkHttpClient client = new OkHttpClient();
    static final String baseWikiUrl = "https://oldschool.runescape.wiki";
    /**
     * <a href="https://oldschool.runescape.wiki/api.php">MediaWiki API help</a>
     */
    static final String baseWikiApiUrl = baseWikiUrl + "/api.php?";

    static String requestOsrsWikiApi(String requestQuery) {
        if (requestQuery == null) {
            return null;
        }
        String requestUrl = baseWikiApiUrl + requestQuery;
        log.info("Wiki API request: " + requestUrl);
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        } catch (IOException e) {
            return null;
        }
    }

    static BufferedImage requestOsrsWikiImage(String imageSrc) {
        if (imageSrc == null) {
            return null;
        }
        String requestUrlString = baseWikiUrl + imageSrc;
        log.info("Wiki image request: " + requestUrlString);
        try {
            URL requestUrl = new URL(requestUrlString);
            return ImageIO.read(requestUrl);
        } catch (IOException e) {
            return null;
        }
    }
}
