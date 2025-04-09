package com.example.dividend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DividendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DividendServiceApplication.class, args);
    }

}
