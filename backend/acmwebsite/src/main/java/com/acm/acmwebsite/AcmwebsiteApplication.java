package com.acm.acmwebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcmwebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcmwebsiteApplication.class, args);
	}

}
