package com.pos.client.service.checkout.impl;

import com.pos.client.service.checkout.CashPaymentDetails;
import com.pos.client.service.checkout.CheckoutRequest;
import com.pos.client.service.checkout.CheckoutResult;
import com.pos.client.service.checkout.CheckoutStrategy;
import org.springframework.stereotype.Component;

/**
 * 現金支付策略
 */
@Component
public class CashCheckoutStrategy implements CheckoutStrategy {

    @Override
    public boolean supports(String paymentMethodId) {
        return "CASH".equalsIgnoreCase(paymentMethodId);
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) {
        if (!(request.getDetails() instanceof CashPaymentDetails)) {
            throw new IllegalArgumentException("細節參數必須為 CashPaymentDetails");
        }

        CashPaymentDetails details = (CashPaymentDetails) request.getDetails();
        double payAmount = details.getPayAmount() != null ? details.getPayAmount() : 0.0;
        double change = payAmount - request.getAmount();

        if (change < 0) {
            throw new IllegalArgumentException("實收現金金額不足 (應付: " + request.getAmount() + ", 實收: " + payAmount + ")");
        }

        CheckoutResult result = new CheckoutResult();
        result.setSuccess(true);
        result.setPayAmount(payAmount);
        result.setChangeAmount(change);
        result.setTransactionId("CASH-" + System.currentTimeMillis());
        return result;
    }
}
