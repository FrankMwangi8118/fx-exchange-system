package com.AnvilShieldGroup.main_service.infrastructure.external;

import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.ExternalApiResponse;
import reactor.core.publisher.Mono;

public interface FetchRateClient {
   Mono<ExternalApiResponse> fetchRateFromRateService(String from,String to);
}
