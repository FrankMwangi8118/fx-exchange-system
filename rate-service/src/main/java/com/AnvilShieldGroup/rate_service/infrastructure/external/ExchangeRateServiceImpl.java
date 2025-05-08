package com.AnvilShieldGroup.rate_service.infrastructure.external;

import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    @Value("${EXCHANGE_RATE_IO_API_KEY}")
    private String apiKey;
    private final WebClient webClient;

    public ExchangeRateServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ExternalExchangeRateResponseDto> getExchangeRate(String from, String to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("apikey", apiKey)
                        .queryParam("base_currency", from)
                        .queryParam("currencies", to)
                        .build())
                .retrieve()
                .bodyToMono(ExternalExchangeRateResponseDto.class);
    }


}
