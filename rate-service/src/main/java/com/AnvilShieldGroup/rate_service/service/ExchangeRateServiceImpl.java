package com.AnvilShieldGroup.rate_service.service;

import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.infrastructure.cache.CacheServiceImpl;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final ExchangeRateClient exchangeRateClient;
    private final CacheServiceImpl cacheService;

    public ExchangeRateServiceImpl(ExchangeRateClient exchangeRateClient, CacheServiceImpl cacheService) {
        this.exchangeRateClient = exchangeRateClient;
        this.cacheService = cacheService;
    }

    @Override
    public Mono<ResponseDto> getCurrencyQuote(RequestDto requestDto) {

        return getCurrencyQuoteFromExternal(requestDto);
    }

    @Override
    public Mono<ResponseDto> getCurrencyQuoteFromExternal(RequestDto requestDto) {
        String from = requestDto.getFrom();
        String to = requestDto.getTo();

        Double cachedRate = cacheService.getRateFromCache(from, to);
        if (cachedRate != null){
            return Mono.just(ResponseDto.builder()
                            .from(from)
                            .to(to)
                            .rate(cachedRate)
                    .build());
        }
        return exchangeRateClient.fetchExchangeRate(from,to)
                .map(res->{
                Double rate=res.getData().get(to);
                    passToCacheService(from,to,rate);
                    return ResponseDto.builder()
                            .from(from)
                            .to(to)
                            .rate(rate)
                            .build();
                });
    }
    private void passToCacheService(String from ,String to,Double rate){
        HashMap<String, Double>rateMap=new HashMap<>();
        rateMap.put(to,rate);
        cacheService.putRate(from,rateMap);

    }


    }
