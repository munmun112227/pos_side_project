package com.pos.client.service.checkout;

import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CheckoutResult {
    private boolean success;
    private String errorMessage;
    
    private String transactionId;  // 交易流水號/外部單號
    private Double payAmount;      // 該次交易實際支付/刷卡扣款金額
    private Double changeAmount = 0.0; // 找零金額 (現金專用)

    // 用於存放各個付款方式特有的回傳內容 (例如跳轉 URL 等)
    private Map<String, Object> extraInfo = new HashMap<>();

    public void putExtra(String key, Object value) {
        this.extraInfo.put(key, value);
    }
}
