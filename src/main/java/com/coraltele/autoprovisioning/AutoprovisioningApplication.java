package com.coraltele.autoprovisioning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoprovisioningApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoprovisioningApplication.class, args);
    }
}
