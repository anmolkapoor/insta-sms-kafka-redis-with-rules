package com.cred.commons;

import lombok.Data;

@Data
public class AppConfiguration {
    private Long highValueTransactionLimit;
    private String customersOriginalCurrency;
    private Long foreignCurrencyHighValueAmount;
    private Integer duplicateTransactionTimeDurationInMinutes;
    private Integer highSpendOnSingleMerchantTimeDurationInHours;
    private Integer highSpendOnSingleMerchantTotalAmount;
}
