package com.dropz;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("dropz")
public interface DropzPluginConfig extends Config {
    enum RarityFormat {
        /**
         * Default string value when other format types are not available
         * Example: "Rare"; "Varies"; "Always"
         */
        DEFAULT,
        PERCENT,
        PERMIL,
        PERMYRIAD,
        FRACTION,
        ONEOVER
    }

    @ConfigItem(
            //position = 5,
            keyName = "rarityFormatConfig",
            name = "Rarity format",
            description = "TODO"
    )
    default RarityFormat rarityFormatConfig() {
        return RarityFormat.DEFAULT;
    }
}
