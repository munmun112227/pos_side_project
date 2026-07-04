package com.pos.client.service.checkout.impl;

import com.pos.client.service.checkout.CardPaymentDetails;
import com.pos.client.service.checkout.CheckoutRequest;
import com.pos.client.service.checkout.CheckoutResult;
import com.pos.client.service.checkout.CheckoutStrategy;
import org.springframework.stereotype.Component;

/**
 * 信用卡支付策略 (模擬串接)
 */
@Component
public class CreditCardCheckoutStrategy implements CheckoutStrategy {

    @Override
    public boolean supports(String paymentMethodId) {
        return "CREDIT_CARD".equalsIgnoreCase(paymentMethodId);
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) {
        if (!(request.getDetails() instanceof CardPaymentDetails)) {
            throw new IllegalArgumentException("細節參數必須為 CardPaymentDetails");
        }

        CardPaymentDetails details = (CardPaymentDetails) request.getDetails();

        // 在真實場景中，此處會呼叫外部刷卡機的 SDK 或 API 進行扣款
        // 這裡做模擬邏輯
        CheckoutResult result = new CheckoutResult();
        result.setSuccess(true);
        result.setPayAmount(request.getAmount());
        result.setChangeAmount(0.0);
        result.setTransactionId("CC-" + System.currentTimeMillis());
        result.putExtra("authCode", details.getAuthCode() != null ? details.getAuthCode() : "MOCK-AUTH-12345");
        result.putExtra("cardNo", details.getCardNo() != null ? details.getCardNo() : "xxxx-xxxx-xxxx-8888");
        
        return result;
    }
}
