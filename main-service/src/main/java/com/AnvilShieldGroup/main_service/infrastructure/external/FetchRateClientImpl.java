package com.AnvilShieldGroup.main_service.infrastructure.external;

import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.ExternalApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class FetchRateClientImpl implements FetchRateClient {
    private final WebClient webClient;

    public FetchRateClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }


    @Override
    public Mono<ExternalApiResponse> fetchRateFromRateService(String from,String to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rate")
                        .queryParam("from",from)
                        .queryParam("to",to)
                        .build())
                .retrieve()
                .bodyToMono(ExternalApiResponse.class);
    }



}
