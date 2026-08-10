package com.likelion.olion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OlionApplication {

	public static void main(String[] args) {
		SpringApplication.run(OlionApplication.class, args);
	}

}
