package com.pos.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductSyncMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sku;
    private String name;
    private BigDecimal originalPrice;
    private String categoryName;
    private Boolean isActive;

    public ProductSyncMessage() {
    }

    public ProductSyncMessage(String sku, String name, BigDecimal originalPrice, String categoryName, Boolean isActive) {
        this.sku = sku;
        this.name = name;
        this.originalPrice = originalPrice;
        this.categoryName = categoryName;
        this.isActive = isActive;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
