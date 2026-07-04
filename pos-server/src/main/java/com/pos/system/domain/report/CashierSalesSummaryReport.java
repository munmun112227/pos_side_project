package com.pos.system.domain.report;

import java.math.BigDecimal;

/**
 * DTO representing cashier sales performance summary.
 */
public class CashierSalesSummaryReport {
    private Integer cashierId;
    private String cashierCode;
    private String cashierName;
    private Long transactionCount;
    private BigDecimal totalSales;

    public CashierSalesSummaryReport() {
    }

    public CashierSalesSummaryReport(Integer cashierId, String cashierCode, String cashierName, Long transactionCount, BigDecimal totalSales) {
        this.cashierId = cashierId;
        this.cashierCode = cashierCode;
        this.cashierName = cashierName;
        this.transactionCount = transactionCount;
        this.totalSales = totalSales;
    }

    public Integer getCashierId() {
        return cashierId;
    }

    public void setCashierId(Integer cashierId) {
        this.cashierId = cashierId;
    }

    public String getCashierCode() {
        return cashierCode;
    }

    public void setCashierCode(String cashierCode) {
        this.cashierCode = cashierCode;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}
