package com.dropz;

import lombok.Value;
import net.runelite.api.ItemComposition;
import net.runelite.client.util.AsyncBufferedImage;

@Value
class ItemSearchResult {
    ItemComposition itemComposition;
    AsyncBufferedImage itemImage;
}
