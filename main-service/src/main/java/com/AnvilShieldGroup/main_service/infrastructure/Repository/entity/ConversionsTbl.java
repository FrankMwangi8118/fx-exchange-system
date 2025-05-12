package com.AnvilShieldGroup.main_service.infrastructure.Repository.entity;

import com.AnvilShieldGroup.main_service.model.Conversion;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionsTbl {
    private String conversionId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private Timestamp requestedAt;

   public Conversion toConversion() {
        return Conversion.builder()
                .fromCurrency(this.fromCurrency)
                .toCurrency(this.toCurrency)
                .amount(this.amount)
                .rate(this.rate)
                .convertedAmount(this.convertedAmount)
                .requestedAt(this.requestedAt)
                .build();
    }
}
