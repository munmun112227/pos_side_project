package com.pos.system.domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing daily revenue report details.
 */
public class DailyRevenueReport {
    private LocalDate reportDate;
    private Long transactionCount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;

    public DailyRevenueReport() {
    }

    public DailyRevenueReport(LocalDate reportDate, Long transactionCount, BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal netAmount) {
        this.reportDate = reportDate;
        this.transactionCount = transactionCount;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
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
}
