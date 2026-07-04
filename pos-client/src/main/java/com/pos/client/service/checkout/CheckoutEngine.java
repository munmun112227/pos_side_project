package com.pos.client.service.checkout;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CheckoutEngine {

    private final List<CheckoutStrategy> strategies;

    public CheckoutEngine(List<CheckoutStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 根據請求中的 paymentMethodId 動態路由並執行對應的結帳策略
     */
    public CheckoutResult process(CheckoutRequest request) {
        CheckoutStrategy strategy = strategies.stream()
                .filter(s -> s.supports(request.getPaymentMethodId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未支援的付款方式: " + request.getPaymentMethodId()));

        return strategy.checkout(request);
    }
}

