package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;

public interface ExchangeRateService {
    ResponseDto getCurrencyQuote(RequestDto requestDto);    // main public-facing method
    ResponseDto getCurrencyQuoteFromExternal(RequestDto requestDto);    //external call helper


}
