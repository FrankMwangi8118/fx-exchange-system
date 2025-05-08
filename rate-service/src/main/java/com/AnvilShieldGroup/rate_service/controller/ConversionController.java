package com.AnvilShieldGroup.rate_service.controller;

import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.controller.response.ApiResponse;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateService;
import com.AnvilShieldGroup.rate_service.service.CurrencyConversionService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rateService")
public class ConversionController {
private final CurrencyConversionService currencyConversionService;

    public ConversionController(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("/rate")
    public ResponseEntity<ApiResponse<ResponseDto>> convert(@RequestParam String to,
                                                            @RequestParam String from) {

            currencyConversionService.convertCurrency()
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));

    }

}
