package com.AnvilShieldGroup.main_service.controller;

import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.infrastructure.external.FetchRateClient;
import com.AnvilShieldGroup.main_service.infrastructure.external.FetchRateClientImpl;
import com.AnvilShieldGroup.main_service.infrastructure.external.dto.ExternalApiResponse;
import com.AnvilShieldGroup.main_service.service.ConversionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/mainService")
@AllArgsConstructor
public class ConversionController {
    private final ConversionService conversionService;
    @GetMapping
    public Mono<ResponseEntity<?>> convert(@RequestBody RequestDto requestDto){
        return conversionService.convertCurrency(requestDto)
                .map(saved -> ResponseEntity.ok(saved)); // this triggers the reactive pipeline
    }


}
