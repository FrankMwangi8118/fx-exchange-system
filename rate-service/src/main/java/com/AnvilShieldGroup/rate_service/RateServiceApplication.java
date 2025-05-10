package com.AnvilShieldGroup.rate_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RateServiceApplication {


	public static void main(String[] args) {

		SpringApplication.run(RateServiceApplication.class, args);
	}

}
