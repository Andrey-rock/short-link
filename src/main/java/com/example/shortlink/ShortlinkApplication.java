package com.example.shortlink;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class ShortlinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortlinkApplication.class, args);
	}

}
