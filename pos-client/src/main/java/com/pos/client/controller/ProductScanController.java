package com.pos.client.controller;

import com.pos.client.dto.product.ProductScanResponse;
import com.pos.client.service.ProductScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


/**
 * 商品刷讀 API
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductScanController {

    private final ProductScanService productScanService;

    /**
     * 模糊搜尋上架中的商品 (支援名稱或條碼模糊查詢)
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String query) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<com.pos.client.domain.entity.LocalProduct> results = productScanService.searchProducts(query);
            response.put("success", true);
            response.put("data", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜尋商品失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 刷讀條碼
     *
     * @param barcode 條碼 (一般條碼或生鮮秤重條碼)
     */
    @GetMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanProduct(@RequestParam String barcode) {
        Map<String, Object> response = new HashMap<>();
        
        return productScanService.scanProduct(barcode)
                .map(product -> {
                    response.put("success", true);
                    response.put("data", product);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "查無商品或商品已停售");
                    return ResponseEntity.status(404).body(response);
                });
    }
}
