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
@Component("NTH_ITEM")
public class NthItemPromotionStrategy implements PromotionStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    public static class Condition {
        private String targetItemId;
        private Integer triggerQuantity;
    }

    @Getter
    @Setter
    public static class Action {
        private String discountType; // PERCENT, FLAT
        private Double discountValue; // 折扣值，例如 0.5 代表五折，20.0 代表減 20 元
        private Integer applyToNth;  // 套用到第幾件 (通常是 2, 代表第二件打折；若是買一送一，則是 2)
    }

    @Override
    public void apply(CartCalculationContext context, LocalPromotion promotion, LocalPromoRule rule) {
        try {
            Condition cond = objectMapper.readValue(rule.getConditionPayload(), Condition.class);
            Action act = objectMapper.readValue(rule.getActionPayload(), Action.class);

            // 尋找購物車中符合該商品的項目
            CalculatedItemDto targetItem = null;
            for (CalculatedItemDto item : context.getItems()) {
                if (item.getItemId().equals(cond.getTargetItemId())) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem == null) {
                return; // 購物車內無此商品
            }

            // 檢查該商品可用於計算促銷的剩餘數量
            int remainingQty = context.getRemainingQuantity(targetItem.getItemId());
            int triggerQty = cond.getTriggerQuantity();

            if (remainingQty < triggerQty) {
                return; // 數量不足，無法觸發促銷
            }

            // 計算可以套用多少組促銷
            // 例如：買二送一，triggerQuantity = 3，若有 7 件，可套用 7/3 = 2 組
            int applyGroups = remainingQty / triggerQty;
            double totalDiscount = 0.0;

            // 計算折扣
            for (int i = 0; i < applyGroups; i++) {
                double discount = 0.0;
                if ("PERCENT".equalsIgnoreCase(act.getDiscountType())) {
                    // 折扣 = 原單價 * (1.0 - 折扣率)
                    discount = targetItem.getOriginalUnitPrice() * (1.0 - act.getDiscountValue());
                } else if ("FLAT".equalsIgnoreCase(act.getDiscountType())) {
                    discount = act.getDiscountValue();
                }
                totalDiscount += discount;
            }

            if (totalDiscount > 0) {
                // 套用折扣
                // 扣減 context 中此商品的剩餘可用數量 (一組促銷消耗 triggerQuantity 個商品)
                context.consumeQuantity(targetItem.getItemId(), applyGroups * triggerQty);

                // 更新商品明細的折扣資訊
                // 對於商品明細，我們把總折扣分攤到所有此商品上，或是單獨記錄 (本設計將折扣累加至該 item 的 discountAmount)
                targetItem.setDiscountAmount(targetItem.getDiscountAmount() + totalDiscount);
                
                // 重新計算最終單價與小計
                double finalSubtotal = (targetItem.getOriginalUnitPrice() * targetItem.getQuantity()) - targetItem.getDiscountAmount();
                targetItem.setSubtotal(finalSubtotal);
                targetItem.setFinalUnitPrice(finalSubtotal / targetItem.getQuantity());
                targetItem.setAppliedPromoId(promotion.getPromoId());

                // 記錄套用的促銷
                context.addAppliedPromotion(promotion.getPromoId(), promotion.getName(), totalDiscount, "ITEM");
            }

        } catch (Exception e) {
            log.error("Failed to apply NthItemPromotionStrategy", e);
        }
    }
}
