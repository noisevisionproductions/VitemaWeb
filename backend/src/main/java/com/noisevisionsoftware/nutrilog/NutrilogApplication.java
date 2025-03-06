package com.noisevisionsoftware.nutrilog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NutrilogApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutrilogApplication.class, args);
    }
}
