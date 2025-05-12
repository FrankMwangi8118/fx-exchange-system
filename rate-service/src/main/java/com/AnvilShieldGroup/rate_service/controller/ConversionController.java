package com.AnvilShieldGroup.rate_service.controller;

// --- Springdoc OpenAPI Imports ---

import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Swagger Annotation
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// ---------------------------------

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.exception.CustomExceptionDto; // Assuming this exists for error responses

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rateService")
@Tag(name = "Currency Conversion", description = "API for converting currencies and fetching rates") // Added Tag
public class ConversionController {

    private final Validator validator;
    private final ExchangeRateService currencyConversionService;
    private final ExchangeRateClient exchangeRateClient;

    public ConversionController(Validator validator, ExchangeRateService currencyConversionService, ExchangeRateClient exchangeRateClient) {
        this.validator = validator;
        this.currencyConversionService = currencyConversionService;
        this.exchangeRateClient = exchangeRateClient;
    }

    @GetMapping("/rate")
    @Operation(
            summary = "Get exchange rate",
            description = "Fetches the current exchange rate between two currencies using query parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of exchange rate",
                    content = @Content(schema = @Schema(implementation = com.AnvilShieldGroup.rate_service.controller.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid currency codes provided (e.g, validation error)",
                    content = @Content(schema = @Schema(implementation = CustomExceptionDto.class))
            ), @ApiResponse(responseCode = "401", description = "Invalid api credentials  (i.e, security error....wrong api key / api passphrase)",
            content = @Content(schema = @Schema(implementation = CustomExceptionDto.class))
    ),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found for the given currencies",
                    content = @Content(schema = @Schema(implementation = CustomExceptionDto.class))
            ),
            @ApiResponse(responseCode = "503", description = "External exchange rate service unavailable (timeout, connection error)",
                    content = @Content(schema = @Schema(implementation = CustomExceptionDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = CustomExceptionDto.class))
            )
    })
    public Mono<ResponseEntity<com.AnvilShieldGroup.rate_service.controller.response.ApiResponse<ResponseDto>>> convert(
            @RequestParam
            @Parameter(description = "The base currency code (e.g., USD)", required = true, example = "USD", in = ParameterIn.QUERY)
            String from,
            @RequestParam
            @Parameter(description = "The target currency code (e.g., EUR)", required = true, example = "EUR", in = ParameterIn.QUERY)
            String to) {

        RequestDto requestDto = RequestDto.builder()
                .from(from)
                .to(to)
                .build();

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(requestDto);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }

        return currencyConversionService.getCurrencyQuote(requestDto)
                .map(responseDto -> ResponseEntity.ok(
                        com.AnvilShieldGroup.rate_service.controller.response.ApiResponse.<ResponseDto>builder()
                                .responseCode(HttpStatus.OK.value())
                                .data(responseDto)
                                .responseStatus("success")
                                .build()));
    }

    @GetMapping("/status")
    @Operation(
            summary = "gets the status of the service",
            description = "checks the availability of the server"
    )
    public ResponseEntity<?> status() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "up");
        return ResponseEntity.ok().body(status);
    }
}