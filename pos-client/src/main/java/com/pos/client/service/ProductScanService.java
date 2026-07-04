package com.pos.client.service;

import com.pos.client.domain.entity.LocalProduct;
import com.pos.client.dto.product.ProductScanResponse;
import com.pos.client.repository.LocalProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;

/**
 * 商品刷讀查詢服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductScanService {

    private final LocalProductRepository productRepository;

    /**
     * 模糊搜尋商品名稱或商品 ID (限制前 10 筆)
     */
    public List<LocalProduct> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.searchProducts(query, PageRequest.of(0, 10));
    }


    /**
     * 刷讀條碼查詢商品
     *
     * @param barcode 前端傳入的條碼
     * @return 商品資訊 DTO
     */
    public Optional<ProductScanResponse> scanProduct(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return Optional.empty();
        }

        // 1. 檢查是否為生鮮秤重條碼 (EAN-13, 以 28 或 29 開頭，長度為 13 碼)
        if (barcode.length() == 13 && (barcode.startsWith("28") || barcode.startsWith("29"))) {
            return parseWeightBarcode(barcode);
        }

        // 2. 一般商品條碼查詢 (此處以 barcode 直接映射為 itemId)
        return productRepository.findByItemId(barcode)
                .filter(LocalProduct::getIsActive)
                .map(this::mapToGeneralResponse);
    }

    /**
     * 解析生鮮秤重條碼
     * 格式：28 + 5碼商品ID + 5碼重量(克) + 1碼校驗碼
     * 例如：2800101002507 -> 商品ID: PROD_00101, 重量: 250克 (0.250kg)
     */
    private Optional<ProductScanResponse> parseWeightBarcode(String barcode) {
        try {
            String productCode = "PROD_" + barcode.substring(2, 7); // 擷取 5 碼商品代碼
            String weightStr = barcode.substring(7, 12);           // 擷取 5 碼重量 (單位：克)
            double weight = Double.parseDouble(weightStr) / 1000.0; // 轉為公斤

            return productRepository.findByItemId(productCode)
                    .filter(LocalProduct::getIsActive)
                    .map(product -> {
                        ProductScanResponse response = new ProductScanResponse();
                        response.setItemId(product.getItemId());
                        response.setName(product.getName());
                        response.setOriginalPrice(product.getOriginalPrice()); // 每公斤單價
                        response.setPromotionalPrice(getEffectivePromoPrice(product));
                        response.setIsWeightItem(true);
                        response.setUnit("kg");
                        response.setQuantity(weight);

                        // 計算小計 (優先使用特價，否則使用原價)
                        double unitPrice = response.getPromotionalPrice() != null ? 
                                           response.getPromotionalPrice() : response.getOriginalPrice();
                        response.setCalculatedPrice(unitPrice * weight);
                        return response;
                    });
        } catch (Exception e) {
            log.error("Failed to parse weight barcode: {}", barcode, e);
            return Optional.empty();
        }
    }

    private ProductScanResponse mapToGeneralResponse(LocalProduct product) {
        ProductScanResponse response = new ProductScanResponse();
        response.setItemId(product.getItemId());
        response.setName(product.getName());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setPromotionalPrice(getEffectivePromoPrice(product));
        response.setIsWeightItem(false);
        response.setUnit("pcs");
        response.setQuantity(1.0); // 預設刷讀數量為 1
        response.setCalculatedPrice(response.getPromotionalPrice() != null ? 
                                    response.getPromotionalPrice() : response.getOriginalPrice());
        return response;
    }

    /**
     * 獲取目前有效特價
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
