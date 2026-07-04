package com.pos.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application configuration class for the POS local cash register (Client).
 */
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class PosClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PosClientApplication.class, args);
    }
}
