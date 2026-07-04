package com.pos.client.service.promotion.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.client.domain.entity.LocalPromoRule;
import com.pos.client.domain.entity.LocalPromotion;
import com.pos.client.dto.cart.CalculatedItemDto;
import com.pos.client.service.promotion.CartCalculationContext;
import com.pos.client.service.promotion.PromotionStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CART_THRESHOLD")
public class CartThresholdPromotionStrategy implements PromotionStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    public static class Condition {
        private Double thresholdAmount;
    }

    @Getter
    @Setter
    public static class Action {
        private String discountType; // PERCENT, FLAT
        private Double discountValue;
    }

    @Override
    public void apply(CartCalculationContext context, LocalPromotion promotion, LocalPromoRule rule) {
        try {
            Condition cond = objectMapper.readValue(rule.getConditionPayload(), Condition.class);
            Action act = objectMapper.readValue(rule.getActionPayload(), Action.class);

            // 計算目前購物車的折後總額 (即所有 item 累計的 subtotal)
            double currentTotal = 0.0;
            for (CalculatedItemDto item : context.getItems()) {
                currentTotal += item.getSubtotal();
            }

            // 檢查是否符合門檻金額
            if (currentTotal < cond.getThresholdAmount()) {
                return; // 未達門檻
            }

            double discount = 0.0;
            if ("FLAT".equalsIgnoreCase(act.getDiscountType())) {
                discount = act.getDiscountValue();
            } else if ("PERCENT".equalsIgnoreCase(act.getDiscountType())) {
                discount = currentTotal * (1.0 - act.getDiscountValue());
            }

            if (discount > 0) {
                // 整單促銷折扣不分攤到單品 finalUnitPrice 上，
                // 而是直接在 context 記錄 applied_promotion，並由最後的計算服務計入整單折扣 (cartDiscountAmount)。
                // 避免干擾單品折扣的追蹤。
                context.addAppliedPromotion(promotion.getPromoId(), promotion.getName(), discount, "CART");
            }

        } catch (Exception e) {
            log.error("Failed to apply CartThresholdPromotionStrategy", e);
        }
    }
}
