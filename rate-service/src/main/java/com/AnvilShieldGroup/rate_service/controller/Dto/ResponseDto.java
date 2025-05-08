package com.AnvilShieldGroup.rate_service.controller.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto {
    private String to;
    private String from;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal result;

}
