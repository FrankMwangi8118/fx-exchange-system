package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.infrastructure.cache.CacheServiceImpl;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final ExchangeRateClient exchangeRateClient;

    private final CacheServiceImpl cacheService;

    public ExchangeRateServiceImpl(ExchangeRateClient exchangeRateClient, CacheServiceImpl cacheService) {
        this.exchangeRateClient = exchangeRateClient;
        this.cacheService = cacheService;
    }

    @Override
    public Mono<ResponseDto> getCurrencyQuote(RequestDto requestDto) {
        return getCurrencyQuoteFromExternal(requestDto);
    }

    // Retrieves the exchange rate from cache or external API
    @Override
    public Mono<ResponseDto> getCurrencyQuoteFromExternal(RequestDto requestDto) {
        String from = requestDto.getFrom();
        String to = requestDto.getTo();

        // First, check if the exchange rate is in the cache
        Double cachedRate = cacheService.getRateFromCache(from, to);
        if (cachedRate != null) {
            // If found in cache, return it as a response
            return Mono.just(ResponseDto.builder()
                    .from(from)
                    .to(to)
                    .rate(cachedRate)
                    .build());
        }

        // If not found in cache, fetch from external API
        return exchangeRateClient.fetchExchangeRate(from, to)
                .map(res -> {
                    Double rate = res.getData().get(to);
                    // Cache the newly fetched rate for future use
                    passToCacheService(from, to, rate);
                    // Build and return the response
                    return ResponseDto.builder()
                            .from(from)
                            .to(to)
                            .rate(rate)
                            .build();
                });
    }

    // Helper method to pass the rate to the cache service
    private void passToCacheService(String from, String to, Double rate) {
        HashMap<String, Double> rateMap = new HashMap<>();
        rateMap.put(to, rate);
        cacheService.putRate(from, rateMap);
    }
}
