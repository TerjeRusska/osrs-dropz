package com.dropz;

import lombok.*;
import net.runelite.api.Skill;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.lang.reflect.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class DropSource {
    public final static String LEVEL_TYPE_COMBAT = "COMBAT";
    public final static String LEVEL_TYPE_REWARD = "REWARD";

    private String sourceName;
    private String sourceHref;
    private String level;
    private String levelType;
    private String quantity;
    private Boolean isNoted;
    private String rarityString;
    private String rarityPercent;
    private String rarityPermil;
    private String rarityPermyriad;
    private String rarityFraction;
    private String rarityOneover;
    private Double levelDataSortValue;
    private Double quantityDataSortValue;
    private Double rarityDataSortValue;
    private String rarityColor;

    public String getParsedLevelType() {
        if (levelType != null) {
            return StringUtils.upperCase(levelType.replace("drops-", ""));
        }
        return null;
    }

    public Skill getSkillLevelType() {
        try {
            return Skill.valueOf(getParsedLevelType());
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    public String getParsedQuantity() {
        return quantity.replace("(noted)", "");
    }

    public String getParsedRarityColor() {
        if (rarityColor != null) {
            return rarityColor.replace("table-bg-", "");
        }
        return null;
    }

    public Color getRarityColorType() {
        try {
            Field field = Class.forName("java.awt.Color").getField(getParsedRarityColor());
            return (Color) field.get(null);
        } catch (Exception ignored) {}
        return null;
    }
}
