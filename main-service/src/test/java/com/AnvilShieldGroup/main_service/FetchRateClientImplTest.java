package com.AnvilShieldGroup.main_service;

import com.AnvilShieldGroup.main_service.infrastructure.external.FetchRateClientImpl;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.Data;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.FetchRateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FetchRateClientImplTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec mockRequestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec mockRequestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec mockResponseSpec;

    @InjectMocks
    private FetchRateClientImpl fetchRateClientImpl;

    @Test
    @DisplayName("fetchRateFromRateService should return FetchRateResponse on successful call")
    void testFetchRateFromRateService_successful() {
        String from = "USD";
        String to = "KES";
        BigDecimal expectedRate = new BigDecimal("150");

        FetchRateResponse mockResponse = new FetchRateResponse();
        Data rateData = new Data();
        rateData.setRate(expectedRate);
        mockResponse.setData(rateData);

        when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(any(Function.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(FetchRateResponse.class)).thenReturn(Mono.just(mockResponse));

        Mono<FetchRateResponse> result = fetchRateClientImpl.fetchRateFromRateService(from, to);

        StepVerifier.create(result)
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("fetchRateFromRateService should propagate error on WebClient failure")
    void testFetchRateFromRateService_error() {
        String from = "USD";
        String to = "KES";
        RuntimeException clientError = new RuntimeException("Error fetching rate");

        when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(any(Function.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(FetchRateResponse.class)).thenReturn(Mono.error(clientError));

        Mono<FetchRateResponse> result = fetchRateClientImpl.fetchRateFromRateService(from, to);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error fetching rate"))
                .verify();
    }
}
