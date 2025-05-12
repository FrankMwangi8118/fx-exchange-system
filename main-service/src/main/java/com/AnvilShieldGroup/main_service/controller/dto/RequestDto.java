package com.AnvilShieldGroup.main_service.controller.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    private String from;
    private String to;
    private BigDecimal amount;
}
