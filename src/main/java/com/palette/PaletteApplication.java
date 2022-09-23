package com.palette;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PaletteApplication {

    public static void main(String[] args) {
        // Reactor Netty 액세스 로그를 활성화
        System.setProperty("reactor.netty.http.server.accessLogEnabled", "true");
        SpringApplication.run(PaletteApplication.class, args);
    }

}
