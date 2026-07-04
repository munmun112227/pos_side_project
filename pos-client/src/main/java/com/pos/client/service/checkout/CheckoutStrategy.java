package com.pos.client.service.checkout;

/**
 * 付款用介面
 */
public interface CheckoutStrategy {

    /**
     * 判斷此策略是否支援該付款方式代碼
     */
    boolean supports(String paymentMethodId);

    /**
     * 執行付款邏輯
     */
    CheckoutResult checkout(CheckoutRequest request);
}

