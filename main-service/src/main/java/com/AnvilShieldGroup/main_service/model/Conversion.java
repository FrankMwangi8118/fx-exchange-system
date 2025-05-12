package com.AnvilShieldGroup.main_service.model;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversion {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private Timestamp requestedAt;
}
