package com.AnvilShieldGroup.main_service.controller;

import com.AnvilShieldGroup.main_service.controller.dto.ApiResponse;
import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.service.ConversionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/mainService")
@AllArgsConstructor
public class ConversionController {
    private final ConversionService conversionService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponse>> convert(@RequestBody RequestDto requestDto) {
        return conversionService.convertCurrency(requestDto)
                .map(responseDto -> ResponseEntity.ok(
                        ApiResponse.builder()
                                .responseCode(200)
                                .responseMessage("success")
                                .responseStatus("success")
                                .data(responseDto)
                                .build()
                ));
    }}