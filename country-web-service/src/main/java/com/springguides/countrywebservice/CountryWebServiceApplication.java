package com.springguides.countrywebservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class CountryWebServiceApplication extends SpringBootServletInitializer {
//https://stackoverflow.com/questions/34353108/soap-without-namespace-in-response-spring-boot
	public static void main(String[] args) {
		SpringApplication.run(CountryWebServiceApplication.class, args);
	}

}
