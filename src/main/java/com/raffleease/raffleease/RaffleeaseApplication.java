package com.raffleease.raffleease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
public class RaffleeaseApplication {
	public static void main(String[] args) {
		SpringApplication.run(RaffleeaseApplication.class, args);
	}

}
