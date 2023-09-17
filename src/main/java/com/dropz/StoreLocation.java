package com.dropz;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class StoreLocation {
    private String sellerName;
    private String sellerHref;
    private String locationName;
    private String locationHref;
    private String stockAmount;
    private String restockTime;
    private String soldAtPrice;
    private String soldAtImgSrc;
    private String boughtAtPrice;
    private String boughtAtImgSrc;
    private String changePer;
    private Boolean isMembers;
    private Double stockAmountDataSortValue;
    private Double restockDataSortValue;
    private Double soldDataSortValue;
    private Double boughtDataSortValue;
}
