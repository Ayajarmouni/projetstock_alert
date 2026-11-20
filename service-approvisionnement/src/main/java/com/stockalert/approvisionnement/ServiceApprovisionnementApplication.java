package com.stockalert.approvisionnement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceApprovisionnementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApprovisionnementApplication.class, args);
    }
}
