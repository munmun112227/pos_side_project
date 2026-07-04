package com.pos.client.service.checkout;

import com.pos.client.domain.entity.LocalOrderPayment;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 訂單當前的付款狀態 (用於多重付款流程)
 */
@Getter
@Setter
public class OrderPaymentStatus {
    private String orderId;
    private Double receivableAmount;      // 訂單總應收金額 (e.g. $1000)
    private Double totalPaidAmount;       // 目前累計已支付金額 (e.g. $300)
    private Double balanceAmount;         // 剩餘未付金額/餘額 (e.g. $700)
    private boolean fullyPaid;            // 是否已完全付清
    private List<LocalOrderPayment> currentPayments; // 目前已成功的付款明細列表
}
