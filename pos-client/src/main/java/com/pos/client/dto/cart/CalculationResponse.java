package com.pos.client.dto.cart;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CalculationResponse {
    private Double originalTotal;
    private Double cartDiscountAmount;
    private Double finalAmount;
    private List<CalculatedItemDto> items;
    private List<AppliedPromotionDto> appliedPromotions;
}
