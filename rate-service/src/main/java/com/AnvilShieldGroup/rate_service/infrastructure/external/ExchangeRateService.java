package com.AnvilShieldGroup.rate_service.infrastructure.external;

import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import reactor.core.publisher.Mono;

public interface ExchangeRateService {
    Mono<ExternalExchangeRateResponseDto> getExchangeRate(String to, String from);

}
