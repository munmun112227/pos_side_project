package com.pos.system.repository.projection;

import java.math.BigDecimal;

public interface ProductSyncProjection {
    String getSku();
    String getName();
    BigDecimal getPrice();
    String getCategoryName();
}
