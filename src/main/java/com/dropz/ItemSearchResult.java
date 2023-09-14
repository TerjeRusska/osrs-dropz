package com.dropz;

import lombok.Value;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.http.api.item.ItemPrice;

@Value
class ItemSearchResult {
    ItemPrice itemPrice;
    AsyncBufferedImage itemImage;
}
