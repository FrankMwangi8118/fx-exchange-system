package com.AnvilShieldGroup.main_service.infrastructure.external;

import com.AnvilShieldGroup.main_service.infrastructure.external.dto.FetchRateResponse;
import reactor.core.publisher.Mono;

public interface FetchRateClient {
   Mono<FetchRateResponse> fetchRateFromRateService(String from, String to);
}
