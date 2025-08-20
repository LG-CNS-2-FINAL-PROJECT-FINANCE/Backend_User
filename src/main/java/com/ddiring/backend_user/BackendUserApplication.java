package com.ddiring.backend_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class BackendUserApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(BackendUserApplication.class, args);
    }
}
