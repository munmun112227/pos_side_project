package com.pos.client.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductScanResponse {
    private String itemId;
    private String name;
    private Double originalPrice;
    private Double promotionalPrice;
    private Boolean isWeightItem;
    private String unit;
    private Double quantity;         // 用於生鮮秤重條碼攜帶的數量
    private Double calculatedPrice; // 用於生鮮秤重商品的小計 (單價 * 數量)
}
