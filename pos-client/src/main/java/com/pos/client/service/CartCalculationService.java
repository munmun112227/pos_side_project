package com.pos.client.service;

import com.pos.client.domain.entity.LocalProduct;
import com.pos.client.domain.entity.LocalPromotion;
import com.pos.client.dto.cart.*;
import com.pos.client.repository.LocalProductRepository;
import com.pos.client.repository.LocalPromotionRepository;
import com.pos.client.service.promotion.CartCalculationContext;
import com.pos.client.service.promotion.PromotionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 購物車計算服務 (整合商品與促銷引擎)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartCalculationService {

    private final LocalProductRepository productRepository;
    private final LocalPromotionRepository promotionRepository;
    private final PromotionEngine promotionEngine;

    /**
     * 執行購物車明細與促銷計算
     *
     * @param request 前端購物車狀態
     * @return 計算結果 DTO
     */
    public CalculationResponse calculateCart(CalculationRequest request) {
        CalculationResponse response = new CalculationResponse();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            response.setOriginalTotal(0.0);
            response.setCartDiscountAmount(0.0);
            response.setFinalAmount(0.0);
            response.setItems(new ArrayList<>());
            response.setAppliedPromotions(new ArrayList<>());
            return response;
        }

        // 1. 查詢所有購物車商品的資料庫主檔
        List<String> itemIds = request.getItems().stream()
                .map(CartItemDto::getItemId)
                .collect(Collectors.toList());
        List<LocalProduct> products = productRepository.findAllById(itemIds);
        Map<String, LocalProduct> productMap = products.stream()
                .collect(Collectors.toMap(LocalProduct::getItemId, p -> p));

        // 2. 初始化 Context 並建立初始明細項目 (CalculatedItemDto)
        CartCalculationContext context = new CartCalculationContext(
                request.getMemberTier(), request.getPaymentMethodId());

        double originalTotal = 0.0;
        List<CalculatedItemDto> calculatedItems = new ArrayList<>();

        for (CartItemDto reqItem : request.getItems()) {
            LocalProduct product = productMap.get(reqItem.getItemId());
            if (product == null || !product.getIsActive()) {
                continue; // 商品不存在或停售，跳過
            }

            CalculatedItemDto itemDto = new CalculatedItemDto();
            itemDto.setItemId(product.getItemId());
            itemDto.setName(product.getName());
            itemDto.setQuantity(reqItem.getQuantity());
            itemDto.setOriginalUnitPrice(product.getOriginalPrice());

            // 判斷是否有商品特價 (基礎特價優先於促銷活動)
            Double basePrice = getEffectivePromoPrice(product);
            if (basePrice != null) {
                itemDto.setFinalUnitPrice(basePrice);
                itemDto.setDiscountAmount((product.getOriginalPrice() - basePrice) * reqItem.getQuantity());
            } else {
                itemDto.setFinalUnitPrice(product.getOriginalPrice());
                itemDto.setDiscountAmount(0.0);
            }

            itemDto.setSubtotal(itemDto.getFinalUnitPrice() * reqItem.getQuantity());
            calculatedItems.add(itemDto);

            // 累計原始總額
            originalTotal += itemDto.getOriginalUnitPrice() * reqItem.getQuantity();
        }

        context.setItems(calculatedItems);

        // 3. 載入目前有效的促銷方案 (按優先級 Priority 遞增排序)
        List<LocalPromotion> activePromotions = getActivePromotions();

        // 4. 送入促銷引擎執行計算
        promotionEngine.execute(context, activePromotions);

        // 5. 彙總整單金額
        double finalItemTotal = 0.0; // 商品層級折扣後的小計之和
        double itemDiscountSum = 0.0; // 所有商品折扣總和

        for (CalculatedItemDto item : context.getItems()) {
            finalItemTotal += item.getSubtotal();
            itemDiscountSum += item.getDiscountAmount();
        }

        // 統計整單 (CART) 層級的促銷折扣
        double cartDiscountSum = 0.0;
        for (AppliedPromotionDto promo : context.getAppliedPromotions()) {
            if ("CART".equalsIgnoreCase(promo.getLevel())) {
                cartDiscountSum += promo.getDiscountAmount();
            }
        }

        // 最終實際應付金額 = 商品級折後總額 - 整單級折扣
        double finalAmount = Math.max(0.0, finalItemTotal - cartDiscountSum);
        // 全館滿減與單品折扣總額
        double totalDiscountAmount = itemDiscountSum + cartDiscountSum;

        // 6. 包裝 Response
        response.setOriginalTotal(originalTotal);
        response.setCartDiscountAmount(totalDiscountAmount);
        response.setFinalAmount(finalAmount);
        response.setItems(context.getItems());
        response.setAppliedPromotions(context.getAppliedPromotions());

        return response;
    }

    /**
     * 獲取目前有效促銷列表
     */
    private List<LocalPromotion> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findByOrderByPriorityAsc().stream()
                .filter(p -> {
                    boolean startOk = p.getStartTime() == null || now.isAfter(p.getStartTime());
                    boolean endOk = p.getEndTime() == null || now.isBefore(p.getEndTime());
                    return startOk && endOk;
                })
                .collect(Collectors.toList());
    }

    /**
     * 獲取商品有效特價
     */
    private Double getEffectivePromoPrice(LocalProduct product) {
        if (product.getPromotionalPrice() == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean startOk = product.getPromoStart() == null || now.isAfter(product.getPromoStart());
        boolean endOk = product.getPromoEnd() == null || now.isBefore(product.getPromoEnd());
        return (startOk && endOk) ? product.getPromotionalPrice() : null;
    }
}
