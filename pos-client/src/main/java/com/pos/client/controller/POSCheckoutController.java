package com.pos.client.controller;

import com.pos.client.service.checkout.CheckoutRequest;
import com.pos.client.service.checkout.OrderPaymentStatus;
import com.pos.client.service.checkout.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.pos.client.service.POSOrderService;
import com.pos.client.dto.cart.CalculationRequest;

/**
 * 複合結帳付款 API
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class POSCheckoutController {

    private final OrderPaymentService orderPaymentService;
    private final POSOrderService posOrderService;

    /**
     * 啟動新交易取得最新的交易編號，並在本地建立草稿訂單。
     *
     * @param orderType 可選參數，訂單類型，預設為 "RETAIL"
     */
    @PostMapping("/new-transaction")
    public ResponseEntity<Map<String, Object>> startNewTransaction(
            @RequestParam(required = false, defaultValue = "RETAIL") String orderType) {
        Map<String, Object> response = new HashMap<>();
        try {
            String orderId = posOrderService.startNewTransaction(orderType);
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("message", "新交易已成功建立，取得交易編號");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "建立新交易失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 確認購物車商品，寫入草稿訂單，準備進入付款階段。
     *
     * @param orderId 訂單編號
     * @param request 當前購物車與促銷計算細節
     */
    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<Map<String, Object>> prepareCheckout(
            @PathVariable String orderId,
            @RequestBody CalculationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            posOrderService.prepareCheckout(orderId, request);
            response.put("success", true);
            response.put("message", "訂單商品明細與優惠計算已確認，準備結帳付款");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "確認結帳明細失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 執行單筆付款 (加入此訂單的付款明細)
     *
     * @param orderId 訂單 ID
     * @param request 支付請求
     */
    @PostMapping("/{orderId}/payment")
    public ResponseEntity<Map<String, Object>> addPayment(
            @PathVariable String orderId,
            @RequestBody CheckoutRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderPaymentStatus status = orderPaymentService.addPayment(orderId, request);
            response.put("success", true);
            response.put("data", status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "付款處理失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 作廢單筆已付的款項項目
     *
     * @param orderId   訂單 ID
     * @param paymentId 付款明細紀錄 ID
     */
    @DeleteMapping("/{orderId}/payment/{paymentId}")
    public ResponseEntity<Map<String, Object>> voidPayment(
            @PathVariable String orderId,
            @PathVariable Long paymentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderPaymentStatus status = orderPaymentService.voidPayment(orderId, paymentId);
            response.put("success", true);
            response.put("data", status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作廢付款項目失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 取消整筆交易並還原/退款
     *
     * @param orderId 訂單 ID
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelTransaction(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            orderPaymentService.cancelTransaction(orderId);
            response.put("success", true);
            response.put("message", "交易已成功取消，相關已支付項目均已作廢退刷");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消交易失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
