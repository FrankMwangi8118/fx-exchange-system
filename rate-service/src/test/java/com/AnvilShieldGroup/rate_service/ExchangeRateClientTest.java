package com.AnvilShieldGroup.rate_service;

import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClientImpl;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // Import for setting private fields
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations for JUnit 5
class ExchangeRateClientTest {

    @Mock // Mock the WebClient dependency
    private WebClient mockWebClient;

    // Mock the intermediate steps of the WebClient fluent API chain
    @Mock
    private RequestHeadersUriSpec mockRequestHeadersUriSpec;
    @Mock
    private RequestHeadersSpec mockRequestHeadersSpec;
    @Mock
    private ResponseSpec mockResponseSpec;

    @InjectMocks // Inject mocks into the class under test
    private ExchangeRateClientImpl exchangeRateClient;

    private String testApiKey = "dummy-api-key-for-test";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRateClient, "apiKey", testApiKey);
    }

    @Test
    void fetchExchangeRate_shouldReturnExchangeRateDto_onSuccess() {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        Double expectedRate = 0.85;
        ExternalExchangeRateResponseDto expectedResponseDto = new ExternalExchangeRateResponseDto();
        expectedResponseDto.setData(Collections.singletonMap(toCurrency, expectedRate));
        when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(any(Function.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(eq(ExternalExchangeRateResponseDto.class)))
                .thenReturn(Mono.just(expectedResponseDto));
        Mono<ExternalExchangeRateResponseDto> resultMono = exchangeRateClient.fetchExchangeRate(fromCurrency, toCurrency);
        StepVerifier.create(resultMono)
                .expectNext(expectedResponseDto)
                .verifyComplete();
        verify(mockWebClient).get();
        verify(mockRequestHeadersUriSpec).uri(any(Function.class));
        verify(mockRequestHeadersSpec).retrieve();
        verify(mockResponseSpec).bodyToMono(eq(ExternalExchangeRateResponseDto.class));
    }

}