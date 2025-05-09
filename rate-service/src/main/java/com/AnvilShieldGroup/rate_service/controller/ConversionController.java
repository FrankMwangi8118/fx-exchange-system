package com.AnvilShieldGroup.rate_service.controller;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.controller.response.ApiResponse;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rateService")
public class ConversionController {
    private final ExchangeRateService currencyConversionService;
    private final ExchangeRateClient exchangeRateClient;

    public ConversionController(ExchangeRateService currencyConversionService, ExchangeRateClient exchangeRateClient) {
        this.currencyConversionService = currencyConversionService;
        this.exchangeRateClient = exchangeRateClient;
    }


    @GetMapping("/rate")
    public ResponseEntity<ApiResponse<ResponseDto>> convert(@RequestParam String to,
                                                            @RequestParam String from) {
        RequestDto requestDto = RequestDto
                .builder()
                .from(from)
                .to(to)
                .build();
        ResponseDto responseDto = currencyConversionService.getCurrencyQuote(requestDto);
        ApiResponse<ResponseDto> apiResponse = ApiResponse.<ResponseDto>builder()
                .data(responseDto)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping
    public void rate(){
        exchangeRateClient.fetchExchangeRate("USD","CAD").subscribe(
                res->{

                    System.out.println(res.getData().get("CAD"));
                }
        );
    }


}
