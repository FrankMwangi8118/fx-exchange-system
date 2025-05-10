package com.AnvilShieldGroup.rate_service;

import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClientImpl;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateClientTest {

    @Mock
    private WebClient mockWebClient;

    // Mock the intermediate steps of the WebClient fluent API chain
    @Mock
    private RequestHeadersUriSpec mockRequestHeadersUriSpec;
    @Mock
    private RequestHeadersSpec mockRequestHeadersSpec;
    @Mock
    private ResponseSpec mockResponseSpec;

    @InjectMocks
    private ExchangeRateClientImpl exchangeRateClient;
    @Value("${externalApi.key}")
    private String testApiKey;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRateClient, "apiKey", testApiKey);
    }

    @Test
    void fetchExchangeRate_shouldReturnExchangeRateDto_onSuccess() {
        // Arrange
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

    @Test
    void fetchExchangeRate_shouldReturnErrorMono_onErrorResponse() {
        // Arrange
        String fromCurrency = "USD";
        String toCurrency = "GBP";
        HttpStatus errorStatus = HttpStatus.BAD_REQUEST; // Simulate a 400 error
        String errorBody = "{\"error\": \"Invalid currency code\"}"; // Example error body

        // Create the exception that WebClient would throw for this error status
        WebClientResponseException expectedException = WebClientResponseException.create(
                errorStatus.value(), // Status code
                errorStatus.getReasonPhrase(), // Status text
                null, // Headers (can be null for simplicity)
                errorBody.getBytes(), // Response body as bytes
                null // Charset (can be null)
        );

        // --- Mock the WebClient call chain to return an error ---
        when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(any(Function.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);

        // Mock bodyToMono to return a Mono that immediately signals the error
        when(mockResponseSpec.bodyToMono(eq(ExternalExchangeRateResponseDto.class)))
                .thenReturn(Mono.error(expectedException));
        // -------------------------------------------------------

        // Act
        // Call the method under test
        Mono<ExternalExchangeRateResponseDto> resultMono = exchangeRateClient.fetchExchangeRate(fromCurrency, toCurrency);

        // Assert
        // Use StepVerifier to test the reactive Mono stream for an error
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode() == errorStatus)
                .verify(); // Verify that the Mono signals an error and completes

        // Optional: Verify that the WebClient methods were called up to the point of error
        verify(mockWebClient).get();
        verify(mockRequestHeadersUriSpec).uri(any(Function.class));
        verify(mockRequestHeadersSpec).retrieve();
        verify(mockResponseSpec).bodyToMono(eq(ExternalExchangeRateResponseDto.class)); // Verify bodyToMono was attempted
    }
}