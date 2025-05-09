package com.AnvilShieldGroup.rate_service.infrastructure.external;

import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import reactor.core.publisher.Mono;

public interface ExchangeRateClient {
    Mono<ExternalExchangeRateResponseDto> fetchExchangeRate(String from, String to);


}
