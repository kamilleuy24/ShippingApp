package com.kamille.gcash.shippingapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.kamille.gcash.shippingapp", "io.swagger"})
@OpenAPIDefinition(
		info = @Info(
				title = "Package API",
				version = "1.0",
				description = "API to retrieve package shipping costs"))
public class ShippingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippingAppApplication.class, args);
	}

}
