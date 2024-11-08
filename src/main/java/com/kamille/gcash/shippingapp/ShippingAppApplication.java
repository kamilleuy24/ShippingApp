package com.kamille.gcash.shippingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.kamille.gcash.shippingapp", "io.swagger"})
public class ShippingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippingAppApplication.class, args);
	}

}
