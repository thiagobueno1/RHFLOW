package com.seuprojeto.rhapi; 

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.seuprojeto.rhapi")
public class RhApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RhApiApplication.class, args);
    }
}
