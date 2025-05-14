package com.AnvilShieldGroup.main_service.model;

import com.AnvilShieldGroup.main_service.controller.dto.ResponseDto;
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

   public ResponseDto toResponseDto(){
       return ResponseDto.builder()
               .fromCurrency(this.fromCurrency)
               .toCurrency(this.toCurrency)
               .amount(this.amount)
               .rate(this.rate)
               .convertedAmount(this.convertedAmount)
               .requestedAt(this.requestedAt)
               .build();
    }
}
