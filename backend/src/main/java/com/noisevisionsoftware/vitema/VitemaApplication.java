package com.noisevisionsoftware.vitema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VitemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitemaApplication.class, args);
    }
}
