package com.group1.aeropace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ShopRunnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopRunnerApplication.class, args);
	}

}
