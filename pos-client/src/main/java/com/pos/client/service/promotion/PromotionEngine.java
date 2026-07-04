package com.pos.client.service.promotion;

import com.pos.client.domain.entity.LocalPromoRule;
import com.pos.client.domain.entity.LocalPromotion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 促銷規則引擎核心
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionEngine {

    // Spring 自動注入 Map，key 為 Bean 的名稱 (即 rule_type)
    private final Map<String, PromotionStrategy> strategies;

    /**
     * 執行促銷計算
     *
     * @param context 計算上下文
     * @param promotions 依優先度排序的有效促銷方案列表
     */
    public void execute(CartCalculationContext context, List<LocalPromotion> promotions) {
        // 1. 初始化剩餘可用商品數量
        context.initRemainingQuantities();

        // 2. 循序套用每個促銷方案
        for (LocalPromotion promotion : promotions) {
            
            // 檢查會員等級是否相符
            if (promotion.getTargetMemberTier() != null && 
                !promotion.getTargetMemberTier().equalsIgnoreCase(context.getMemberTier())) {
                continue; // 會員等級不符，跳過
            }

            // 執行該方案下的所有規則
            for (LocalPromoRule rule : promotion.getRules()) {
                String ruleType = rule.getRuleType();
                PromotionStrategy strategy = strategies.get(ruleType);

                if (strategy == null) {
                    log.warn("Unknown promotion rule type: {}", ruleType);
                    continue;
                }

                // 執行折扣邏輯
                strategy.apply(context, promotion, rule);
            }
        }
    }
}
