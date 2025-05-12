package com.AnvilShieldGroup.main_service.infrastructure.external.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class Data {
    private String from;
    private String to;
    private BigDecimal rate;
}
