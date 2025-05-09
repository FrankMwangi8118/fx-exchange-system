package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateServiceImpl(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    @Override
    public ResponseDto getCurrencyQuote(RequestDto requestDto) {
        return getCurrencyQuoteFromExternal(requestDto);
    }

    @Override
    public ResponseDto getCurrencyQuoteFromExternal(RequestDto requestDto) {
        exchangeRateClient.fetchExchangeRate(requestDto.getTo(), requestDto.getFrom())
                .subscribe(
                        res->{
                            getRate(res.getData().get(requestDto.getTo()));
                        }
                );
       return ResponseDto.builder()
               .from(requestDto.getFrom())
               .to(requestDto.getTo())
               .build();
    }
    private double getRate(double n){
        return n;
    }

}
