package com.group1.shop_runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
public class ShopRunnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopRunnerApplication.class, args);
	}

}
