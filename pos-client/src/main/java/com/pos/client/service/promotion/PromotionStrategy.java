package com.pos.client.service.promotion;

import com.pos.client.domain.entity.LocalPromoRule;
import com.pos.client.domain.entity.LocalPromotion;

/**
 * 促銷策略介面
 */
public interface PromotionStrategy {

    /**
     * 執行該促銷策略
     *
     * @param context 計算上下文
     * @param promotion 促銷主檔
     * @param rule 促銷規則
     */
    void apply(CartCalculationContext context, LocalPromotion promotion, LocalPromoRule rule);
}
