package com.pos.client.service.checkout;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private String orderId;          // 訂單 ID
    private Double amount;           // 這筆支付所要抵扣的應付金額
    private String paymentMethodId;  // 付款方式代碼 (例如 "CASH", "CREDIT_CARD")

    // 透過 Jackson 欄位級別的註解，讓解析器在讀取 details 時，去尋找同層同伴屬性 paymentMethodId 作為型別識別
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "paymentMethodId",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CashPaymentDetails.class, name = "CASH"),
        @JsonSubTypes.Type(value = CardPaymentDetails.class, name = "CREDIT_CARD")
    })
    private PaymentDetails details;  // 付款細節 (多型欄位，如刷卡資訊、實收現金)
}
