package com.AnvilShieldGroup.main_service.util;

import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class IdGeneratorUtil {
    public  String idGenerator(){
        return UUID.randomUUID().toString();
    }
}
