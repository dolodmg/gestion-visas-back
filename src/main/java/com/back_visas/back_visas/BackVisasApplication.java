package com.back_visas.back_visas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BackVisasApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackVisasApplication.class, args);
	}

}
