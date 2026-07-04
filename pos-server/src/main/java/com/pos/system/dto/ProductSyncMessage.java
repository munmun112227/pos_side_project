package com.pos.system.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductSyncMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sku;
    private String name;
    private BigDecimal originalPrice;
    private String categoryName;
    private Boolean isActive;

}
