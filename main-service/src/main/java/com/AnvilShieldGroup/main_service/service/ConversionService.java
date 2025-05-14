package com.AnvilShieldGroup.main_service.service;

import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.controller.dto.ResponseDto;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ConversionService {
    Mono<ResponseDto> convertCurrency(RequestDto requestDto);


    Mono<BigDecimal> getRateFromExternal(String from, String to);

}
