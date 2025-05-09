package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateServiceImpl(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    @Override
    public Mono<ResponseDto> getCurrencyQuote(RequestDto requestDto) {
        return getCurrencyQuoteFromExternal(requestDto);
    }

    @Override
    public Mono<ResponseDto> getCurrencyQuoteFromExternal(RequestDto requestDto) {
        return exchangeRateClient.fetchExchangeRate(requestDto.getFrom(), requestDto.getTo())
                .map(res -> ResponseDto.builder()
                        .from(requestDto.getFrom())
                        .to(requestDto.getTo())
                        .rate(res.getData().get(requestDto.getTo()))
                        .build());
    }



}
