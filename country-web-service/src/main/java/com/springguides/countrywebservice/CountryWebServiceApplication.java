package com.springguides.countrywebservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CountryWebServiceApplication {
//https://stackoverflow.com/questions/34353108/soap-without-namespace-in-response-spring-boot
	public static void main(String[] args) {
		SpringApplication.run(CountryWebServiceApplication.class, args);
	}

}
