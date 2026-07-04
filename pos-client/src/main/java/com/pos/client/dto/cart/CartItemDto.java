package com.pos.client.dto.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDto {
    private String itemId;
    private Integer quantity;
}
