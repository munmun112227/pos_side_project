package com.pos.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionUploadMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionNo;
    private String cashierCode;
    private String posCode;
    private String memberCode;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private LocalDateTime createdAt;
    private String status; // 交易狀態 (例如 "PAID", "CANCELLED")
    private String transactionType; // 交易類型 (例如 "RETAIL", "CANCELLED")
    private List<ItemDto> items;
    private List<PaymentDto> payments;
    private List<DiscountDto> discounts;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }



    public TransactionUploadMessage() {
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getCashierCode() {
        return cashierCode;
    }

    public void setCashierCode(String cashierCode) {
        this.cashierCode = cashierCode;
    }

    public String getPosCode() {
        return posCode;
    }

    public void setPosCode(String posCode) {
        this.posCode = posCode;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public List<PaymentDto> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentDto> payments) {
        this.payments = payments;
    }

    public List<DiscountDto> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<DiscountDto> discounts) {
        this.discounts = discounts;
    }

    public static class ItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String sku;
        private Integer quantity;
        private BigDecimal originalUnitPrice;
        private BigDecimal finalUnitPrice;
        private BigDecimal discountAmount;
        private String appliedPromoId;
        private BigDecimal subtotal;

        public ItemDto() {
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getOriginalUnitPrice() {
            return originalUnitPrice;
        }

        public void setOriginalUnitPrice(BigDecimal originalUnitPrice) {
            this.originalUnitPrice = originalUnitPrice;
        }

        public BigDecimal getFinalUnitPrice() {
            return finalUnitPrice;
        }

        public void setFinalUnitPrice(BigDecimal finalUnitPrice) {
            this.finalUnitPrice = finalUnitPrice;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public String getAppliedPromoId() {
            return appliedPromoId;
        }

        public void setAppliedPromoId(String appliedPromoId) {
            this.appliedPromoId = appliedPromoId;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }

    public static class PaymentDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String paymentMethodCode;
        private BigDecimal amount;
        private BigDecimal payAmount;
        private BigDecimal changeAmount;

        public PaymentDto() {
        }

        public String getPaymentMethodCode() {
            return paymentMethodCode;
        }

        public void setPaymentMethodCode(String paymentMethodCode) {
            this.paymentMethodCode = paymentMethodCode;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getPayAmount() {
            return payAmount;
        }

        public void setPayAmount(BigDecimal payAmount) {
            this.payAmount = payAmount;
        }

        public BigDecimal getChangeAmount() {
            return changeAmount;
        }

        public void setChangeAmount(BigDecimal changeAmount) {
            this.changeAmount = changeAmount;
        }
    }

    public static class DiscountDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String discountCode;
        private BigDecimal amount;

        public DiscountDto() {
        }

        public String getDiscountCode() {
            return discountCode;
        }

        public void setDiscountCode(String discountCode) {
            this.discountCode = discountCode;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
