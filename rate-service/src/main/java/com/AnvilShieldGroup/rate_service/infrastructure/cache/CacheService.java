package com.AnvilShieldGroup.rate_service.infrastructure.cache;

import java.util.HashMap;
import java.util.Map;

public interface CacheService {
    Double getRateFromCache(String from, String to);
    void putRate(String from, Map<String, Double> newRates);
}
