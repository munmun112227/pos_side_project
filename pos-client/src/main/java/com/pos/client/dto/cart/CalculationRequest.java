package com.pos.client.dto.cart;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CalculationRequest {
    private String memberId;
    private String memberTier;
    private String paymentMethodId;
    private List<CartItemDto> items;

}
