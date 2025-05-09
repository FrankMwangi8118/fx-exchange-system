package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface ExchangeRateService {
   Mono <ResponseDto> getCurrencyQuote(RequestDto requestDto);    // main public-facing method
    Mono<ResponseDto> getCurrencyQuoteFromExternal(RequestDto requestDto);    //external call helper


}
