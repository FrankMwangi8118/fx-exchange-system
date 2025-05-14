package com.AnvilShieldGroup.main_service.infrastructure.external;

import com.AnvilShieldGroup.main_service.infrastructure.external.dto.FetchRateResponse;
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
    public Mono<FetchRateResponse> fetchRateFromRateService(String from, String to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rate")
                        .queryParam("from",from)
                        .queryParam("to",to)
                        .build())
                .retrieve()
                .bodyToMono(FetchRateResponse.class);
    }



}
