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
        // Arrange: Configure the mock cacheService to return null (simulate cache miss)
        when(cacheService.getRateFromCache(fromCurrency, toCurrency)).thenReturn(null);

        // Arrange: Configure the mock exchangeRateClient to return a successful Mono
        // This simulates the external API returning a response.
        ExternalExchangeRateResponseDto externalResponse = new ExternalExchangeRateResponseDto();
        Map<String, Double> data = new HashMap<>();
        data.put(toCurrency, externalRate);
        externalResponse.setData(data);

        when(exchangeRateClient.fetchExchangeRate(fromCurrency, toCurrency)).thenReturn(Mono.just(externalResponse));

        // Act: Call the method under test
        Mono<ResponseDto> resultMono = exchangeRateService.getCurrencyQuoteFromExternal(requestDto);

        // Assert: Use StepVerifier to test the reactive stream
        StepVerifier.create(resultMono)
                .expectNextMatches(responseDto ->
                        // Verify the emitted ResponseDto contains the expected data from the external call
                        responseDto.getFrom().equals(fromCurrency) &&
                                responseDto.getTo().equals(toCurrency) &&
                                responseDto.getRate().equals(externalRate)
                )
                .verifyComplete(); // Verify the Mono completes successfully after emitting the item

        // Assert: Verify interactions with mocks
        // Verify cacheService.getRateFromCache was called exactly once with correct arguments
        verify(cacheService, times(1)).getRateFromCache(fromCurrency, toCurrency);
        // Verify exchangeRateClient.fetchExchangeRate was called exactly once with correct arguments
        verify(exchangeRateClient, times(1)).fetchExchangeRate(fromCurrency, toCurrency);
        // Verify cacheService.putRate was called exactly once with correct arguments
        // We expect it to be called with the 'from' currency and a map containing the 'to' currency and the fetched rate
        verify(cacheService, times(1)).putRate(eq(fromCurrency), anyMap());
        // Optional: More specific verification of the map content passed to putRate
        // verify(cacheService, times(1)).putRate(eq(fromCurrency), argThat(map -> map.containsKey(toCurrency) && map.get(toCurrency).equals(externalRate)));
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