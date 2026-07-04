package com.pos.system.domain.report;

import java.math.BigDecimal;

/**
 * DTO representing hot selling products ranking.
 */
public class HotSellingProductReport {
    private Integer productId;
    private String sku;
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalSales;

    public HotSellingProductReport() {
    }

    public HotSellingProductReport(Integer productId, String sku, String productName, Long totalQuantity, BigDecimal totalSales) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalSales = totalSales;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}
