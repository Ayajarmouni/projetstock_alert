package com.stockalert.approvisionnement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync // V5 : la publication RabbitMQ se fait dans un thread séparé, hors de la requête HTTP
public class ServiceApprovisionnementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApprovisionnementApplication.class, args);
    }
}
