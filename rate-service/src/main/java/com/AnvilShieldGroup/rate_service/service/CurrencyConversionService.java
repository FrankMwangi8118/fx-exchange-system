package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;

public interface CurrencyConversionService {
    ResponseDto convertCurrency(RequestDto requestDto);

}
