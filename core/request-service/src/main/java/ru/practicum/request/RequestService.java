package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.request",
        "ru.practicum.client"
})
@EnableFeignClients(basePackages = "ru.practicum.request.client")
public class RequestService {
    public static void main(String[] args) {
        SpringApplication.run(RequestService.class, args);
    }
}
