package com.AnvilShieldGroup.rate_service.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalExchangeRateResponseDto {
    private Map<String,Double>data;
}
