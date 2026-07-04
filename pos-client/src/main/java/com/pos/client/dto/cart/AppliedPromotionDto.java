package com.pos.client.dto.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppliedPromotionDto {
    private String promoId;
    private String name;
    private Double discountAmount;
    private String level; // ITEM or CART
}
