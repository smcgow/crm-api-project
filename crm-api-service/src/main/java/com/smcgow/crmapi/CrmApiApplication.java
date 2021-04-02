package com.smcgow.crmapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class CrmApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CrmApiApplication.class, args);
    }

}
