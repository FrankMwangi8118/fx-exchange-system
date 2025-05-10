package com.AnvilShieldGroup.rate_service.controller;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.controller.response.ApiResponse;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.service.ExchangeRateService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rateService")
public class ExchangeRateController {
    private final Validator validator;
    private final ExchangeRateService currencyConversionService;
    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateController(Validator validator, ExchangeRateService currencyConversionService, ExchangeRateClient exchangeRateClient) {
        this.validator = validator;
        this.currencyConversionService = currencyConversionService;
        this.exchangeRateClient = exchangeRateClient;
    }


    @GetMapping("/rate")
    public Mono<ResponseEntity<ApiResponse<ResponseDto>>> convert(@RequestParam String from,
                                                                  @RequestParam String to) {

        RequestDto requestDto = RequestDto.builder()
                .from(from)
                .to(to)
                .build();

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(requestDto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(" | "));
            return Mono.error(new ConstraintViolationException(violations));
        }

        // Proceed with service
        return currencyConversionService.getCurrencyQuote(requestDto)
                .map(responseDto -> ResponseEntity.ok(
                        ApiResponse.<ResponseDto>builder()
                                .data(responseDto)
                                .responseStatus("success")
                                .build()));
    }
}
