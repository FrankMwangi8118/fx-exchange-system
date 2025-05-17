package com.AnvilShieldGroup.main_service;

import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.controller.dto.ResponseDto;
import com.AnvilShieldGroup.main_service.infrastructure.Repository.ConversionRepositoryService;
import com.AnvilShieldGroup.main_service.infrastructure.external.FetchRateClient;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.Data;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.FetchRateResponse;
import com.AnvilShieldGroup.main_service.model.Conversion;

import com.AnvilShieldGroup.main_service.service.ConversionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {

    @Mock
    private FetchRateClient mockFetchRateClient;

    @Mock
    private ConversionRepositoryService mockConversionRepositoryService;

    @InjectMocks
    private ConversionServiceImpl conversionService;

    @Test
    @DisplayName("getRateFromExternal should fetch rate and return Mono<BigDecimal> on success")
    void testGetRateFromExternal_successful() {
        String from = "EUR";
        String to = "INR";
        BigDecimal expectedRate = new BigDecimal("120.50");

        Data rateData = new Data();
        rateData.setRate(expectedRate);
        FetchRateResponse mockResponse = new FetchRateResponse();
        mockResponse.setData(rateData);

        when(mockFetchRateClient.fetchRateFromRateService(eq(from), eq(to)))
                .thenReturn(Mono.just(mockResponse));

        Mono<BigDecimal> result = conversionService.getRateFromExternal(from, to);

        StepVerifier.create(result)
                .expectNext(expectedRate)
                .verifyComplete();

        verify(mockFetchRateClient, times(1)).fetchRateFromRateService(eq(from), eq(to));
    }

    @Test
    @DisplayName("getRateFromExternal should propagate error from external client")
    void testGetRateFromExternal_clientError_shouldPropagateError() {
        String from = "GBP";
        String to = "JPY";
        RuntimeException clientError = new RuntimeException("External rate service unavailable");

        when(mockFetchRateClient.fetchRateFromRateService(eq(from), eq(to)))
                .thenReturn(Mono.error(clientError));

        Mono<BigDecimal> result = conversionService.getRateFromExternal(from, to);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("External rate service unavailable"))
                .verify();

        verify(mockFetchRateClient, times(1)).fetchRateFromRateService(eq(from), eq(to));
    }

    @Test
    @DisplayName("getRateFromExternal should handle response with null data")
    void testGetRateFromExternal_nullDataInResponse_shouldPropagateError() {
        String from = "AUD";
        String to = "CAD";

        FetchRateResponse mockResponseWithNullData = new FetchRateResponse();
        mockResponseWithNullData.setData(null);

        when(mockFetchRateClient.fetchRateFromRateService(eq(from), eq(to)))
                .thenReturn(Mono.just(mockResponseWithNullData));

        Mono<BigDecimal> result = conversionService.getRateFromExternal(from, to);

        StepVerifier.create(result)
                .expectError(NullPointerException.class)
                .verify();

        verify(mockFetchRateClient, times(1)).fetchRateFromRateService(eq(from), eq(to));
    }

    @Test
    @DisplayName("convertCurrency should fetch rate, calculate, save conversion, and return ResponseDto on success")
    void testConvertCurrency_successful() {
        RequestDto request = new RequestDto();
        request.setFrom("USD");
        request.setTo("KES");
        request.setAmount(BigDecimal.valueOf(100));

        BigDecimal fetchedRate = BigDecimal.valueOf(150);
        BigDecimal expectedConvertedAmount = request.getAmount().multiply(fetchedRate);

        Data rateData = new Data();
        rateData.setRate(fetchedRate);
        FetchRateResponse fetchRateResponse = new FetchRateResponse();
        fetchRateResponse.setData(rateData);

        when(mockFetchRateClient.fetchRateFromRateService(eq("USD"), eq("KES")))
                .thenReturn(Mono.just(fetchRateResponse));

        ArgumentCaptor<Conversion> conversionCaptor = ArgumentCaptor.forClass(Conversion.class);

        Conversion mockSavedConversion = mock(Conversion.class);

        ResponseDto expectedResponseDto = new ResponseDto();
        expectedResponseDto.setFromCurrency(request.getFrom());
        expectedResponseDto.setToCurrency(request.getTo());
        expectedResponseDto.setAmount(request.getAmount());
        expectedResponseDto.setRate(fetchedRate);
        expectedResponseDto.setConvertedAmount(expectedConvertedAmount);

        when(mockConversionRepositoryService.save(any(Conversion.class)))
                .thenReturn(mockSavedConversion);

        when(mockSavedConversion.toResponseDto()).thenReturn(expectedResponseDto);

        Mono<ResponseDto> result = conversionService.convertCurrency(request);

        StepVerifier.create(result)
                .expectNext(expectedResponseDto)
                .verifyComplete();

        verify(mockFetchRateClient, times(1)).fetchRateFromRateService(eq("USD"), eq("KES"));

        verify(mockConversionRepositoryService, times(1)).save(conversionCaptor.capture());

        Conversion savedConversion = conversionCaptor.getValue();
        assertNotNull(savedConversion);
        assertEquals(request.getFrom(), savedConversion.getFromCurrency());
        assertEquals(request.getTo(), savedConversion.getToCurrency());
        assertEquals(request.getAmount(), savedConversion.getAmount());
        assertEquals(fetchedRate, savedConversion.getRate());
        assertEquals(expectedConvertedAmount, savedConversion.getConvertedAmount());

        verify(mockSavedConversion, times(1)).toResponseDto();
    }

    @Test
    @DisplayName("convertCurrency should propagate error if rate fetch fails")
    void testConvertCurrency_rateFetchFails_shouldPropagateErrorAndNotSave() {
        RequestDto request = new RequestDto();
        request.setFrom("USD");
        request.setTo("KES");
        request.setAmount(BigDecimal.valueOf(100));

        RuntimeException fetchError = new RuntimeException("Rate service unavailable");

        when(mockFetchRateClient.fetchRateFromRateService(eq("USD"), eq("KES")))
                .thenReturn(Mono.error(fetchError));

        Mono<ResponseDto> result = conversionService.convertCurrency(request);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Rate service unavailable"))
                .verify();

        verify(mockFetchRateClient, times(1)).fetchRateFromRateService(eq("USD"), eq("KES"));

        verify(mockConversionRepositoryService, times(0)).save(any(Conversion.class));
    }

    @Test
    @DisplayName("convertCurrency should propagate error if repository save fails")
    void testConvertCurrency_saveFails_shouldPropagateError() {
        RequestDto request = new RequestDto();
        request.setFrom("USD");
        request.setTo("KES");
        request.setAmount(BigDecimal.valueOf(100));

        BigDecimal fetchedRate = BigDecimal.valueOf(150);

        Data rateData = new Data();
        rateData.setRate(fetchedRate);
        FetchRateResponse fetchRateResponse = new FetchRateResponse();
        fetchRateResponse.setData(rateData);

        when(mockFetchRateClient.fetchRateFromRateService(eq("USD"), eq("KES")))
                .thenReturn(Mono.just(fetchRateResponse));

        RuntimeException dbError = new RuntimeException("Database connection failed");

        when(mockConversionRepositoryService.save(any(Conversion.class)))
                .thenThrow(dbError);

        Mono<ResponseDto> result = conversionService.convertCurrency(request);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database connection failed"))
                .verify();

        verify(mockFetchRateClient, times(1)).fetchRateFromRateService(eq("USD"), eq("KES"));

        verify(mockConversionRepositoryService, times(1)).save(any(Conversion.class));
    }
}
