package com.pos.client.service.checkout;

import lombok.Getter;
import lombok.Setter;

/**
 * 信用卡支付特有的詳細參數
 */
@Getter
@Setter
public class CardPaymentDetails implements PaymentDetails {
    private String cardNo;     // 卡號 (或遮蔽後的卡號末四碼)
    private String authCode;   // 刷卡機回傳的授權碼
}
