package com.AnvilShieldGroup.rate_service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.infrastructure.cache.CacheServiceImpl;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import com.AnvilShieldGroup.rate_service.service.ExchangeRateServiceImpl; // Import the service implementation
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private CacheServiceImpl cacheService;

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;
    private RequestDto requestDto;
    private String fromCurrency = "USD";
    private String toCurrency = "EUR";
    private Double externalRate = 0.85;
    private Double cachedRate = 0.86;

    @BeforeEach
    void setUp() {
        // Set up a standard RequestDto for tests
        requestDto = RequestDto.builder()
                .from(fromCurrency)
                .to(toCurrency)
                .build();
    }


    @Test
    void onCacheHit() {
        when(cacheService.getRateFromCache(fromCurrency, toCurrency)).thenReturn(cachedRate);
        Mono<ResponseDto> resultMono = exchangeRateService.getCurrencyQuoteFromExternal(requestDto);
        StepVerifier.create(resultMono)
                .expectNextMatches(responseDto ->
                        responseDto.getFrom().equals(fromCurrency) &&
                                responseDto.getTo().equals(toCurrency) &&
                                responseDto.getRate().equals(cachedRate)
                )
                .verifyComplete();
        verify(cacheService, times(1)).getRateFromCache(fromCurrency, toCurrency);
        verify(exchangeRateClient, never()).fetchExchangeRate(anyString(), anyString());
        verify(cacheService, never()).putRate(anyString(), anyMap());
    }

    @Test
    void onCacheMissFetchFromExternal() {
        when(cacheService.getRateFromCache(fromCurrency, toCurrency)).thenReturn(null);

        ExternalExchangeRateResponseDto externalResponse = new ExternalExchangeRateResponseDto();
        Map<String, Double> data = new HashMap<>();
        data.put(toCurrency, externalRate);
        externalResponse.setData(data);

        when(exchangeRateClient.fetchExchangeRate(fromCurrency, toCurrency)).thenReturn(Mono.just(externalResponse));

        Mono<ResponseDto> resultMono = exchangeRateService.getCurrencyQuoteFromExternal(requestDto);

        StepVerifier.create(resultMono)
                .expectNextMatches(responseDto ->
                        responseDto.getFrom().equals(fromCurrency) &&
                                responseDto.getTo().equals(toCurrency) &&
                                responseDto.getRate().equals(externalRate)
                )
                .verifyComplete();
        verify(cacheService, times(1)).getRateFromCache(fromCurrency, toCurrency);
        verify(exchangeRateClient, times(1)).fetchExchangeRate(fromCurrency, toCurrency);
        verify(cacheService, times(1)).putRate(eq(fromCurrency), anyMap());

    }

    @Test
    void getCurrencyQuoteGetQuoteFromGetQuoteFromExternal() {
        when(cacheService.getRateFromCache(fromCurrency, toCurrency)).thenReturn(cachedRate);
        Mono<ResponseDto> resultMono = exchangeRateService.getCurrencyQuote(requestDto);
        StepVerifier.create(resultMono)
                .expectNextMatches(responseDto ->
                        responseDto.getFrom().equals(fromCurrency) &&
                                responseDto.getTo().equals(toCurrency) &&
                                responseDto.getRate().equals(cachedRate)
                )
                .verifyComplete();
        verify(cacheService, times(1)).getRateFromCache(fromCurrency, toCurrency);
        verify(exchangeRateClient, never()).fetchExchangeRate(anyString(), anyString()); // This assertion is correct
        verify(cacheService, never()).putRate(anyString(), anyMap());
    }
}