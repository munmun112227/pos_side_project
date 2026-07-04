package com.pos.system.domain.enums;

/**
 * Type of Transaction.
 */
public enum TransactionType {
  SALES,
  SALES_CANCELLATION,
  DELETE_ORDER,
  RETURN,
  RETURN_CANCELLATION,
  OPEN_CASH_DRAWER,
  REPRINT_RECEIPT
}
