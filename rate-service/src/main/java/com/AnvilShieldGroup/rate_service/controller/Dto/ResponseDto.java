package com.AnvilShieldGroup.rate_service.controller.Dto;

import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto {
    private String to;
    private String from;
    private Double rate;
}
