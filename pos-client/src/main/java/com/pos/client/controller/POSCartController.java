package com.pos.client.controller;

import com.pos.client.dto.cart.CalculationRequest;
import com.pos.client.dto.cart.CalculationResponse;
import com.pos.client.service.CartCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 購物車計算與促銷觸發 API
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class POSCartController {

    private final CartCalculationService cartCalculationService;
    private final com.pos.client.service.POSOrderService posOrderService;

    /**
     * 計算購物車明細與促銷
     *
     * @param request 包含購物車內商品與會員等級等 JSON 資料
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateCart(@RequestBody CalculationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            CalculationResponse result = cartCalculationService.calculateCart(request);
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "購物車計算失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/abandon")
    public ResponseEntity<Map<String, Object>> abandonCart(
            @RequestParam(required = false) String orderId,
            @RequestBody CalculationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String finalOrderId = posOrderService.abandonCart(orderId, request);
            response.put("success", true);
            response.put("message", "交易已中途取消，並已記錄該筆作廢交易");
            response.put("orderId", finalOrderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "中途取消交易處理失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

