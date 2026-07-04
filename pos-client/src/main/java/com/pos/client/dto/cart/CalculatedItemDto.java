package com.pos.client.dto.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculatedItemDto {
    private String itemId;
    private String name;
    private Integer quantity;
    private Double originalUnitPrice;
    private Double finalUnitPrice;
    private Double discountAmount;
    private String appliedPromoId;
    private Double subtotal;
}
