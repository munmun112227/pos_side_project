package com.pos.client.service.checkout;

import lombok.Getter;
import lombok.Setter;

/**
 * 現金支付特有的詳細參數
 */
@Getter
@Setter
public class CashPaymentDetails implements PaymentDetails {
    private Double payAmount;  // 顧客給的實收金額 (例如應付 $800，顧客給 $1000)
}
