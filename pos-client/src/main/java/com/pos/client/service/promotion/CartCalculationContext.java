package com.pos.client.service.promotion;

import com.pos.client.dto.cart.AppliedPromotionDto;
import com.pos.client.dto.cart.CalculatedItemDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 購物車計算上下文，維護促銷套用過程中的狀態
 */
@Getter
@Setter
public class CartCalculationContext {

    private String memberTier;
    private String paymentMethodId;

    // 計算中的商品明細列表
    private List<CalculatedItemDto> items = new ArrayList<>();

    // 儲存各商品剩餘「未促銷打折」的數量，用於處理非疊加(互斥)促銷
    private Map<String, Integer> remainingQuantities = new HashMap<>();

    // 已套用的促銷清單
    private List<AppliedPromotionDto> appliedPromotions = new ArrayList<>();

    public CartCalculationContext(String memberTier, String paymentMethodId) {
        this.memberTier = memberTier;
        this.paymentMethodId = paymentMethodId;
    }

    /**
     * 初始化剩餘可促銷數量
     */
    public void initRemainingQuantities() {
        remainingQuantities.clear();
        for (CalculatedItemDto item : items) {
            remainingQuantities.put(item.getItemId(), item.getQuantity());
        }
    }

    /**
     * 獲取商品剩餘可用數量
     */
    public int getRemainingQuantity(String itemId) {
        return remainingQuantities.getOrDefault(itemId, 0);
    }

    /**
     * 扣減商品可用數量
     */
    public void consumeQuantity(String itemId, int quantity) {
        int current = getRemainingQuantity(itemId);
        if (current >= quantity) {
            remainingQuantities.put(itemId, current - quantity);
        } else {
            remainingQuantities.put(itemId, 0);
        }
    }

    /**
     * 記錄套用的促銷
     */
    public void addAppliedPromotion(String promoId, String name, Double discountAmount, String level) {
        AppliedPromotionDto dto = new AppliedPromotionDto();
        dto.setPromoId(promoId);
        dto.setName(name);
        dto.setDiscountAmount(discountAmount);
        dto.setLevel(level);
        appliedPromotions.add(dto);
    }
}
