package com.hmall.cart;

import com.hmall.api.client.ItemClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.cart.mapper")
@SpringBootApplication
@EnableFeignClients(clients = ItemClient.class)
//@EnableFeignClients(basePackages = "com.hmall.api.client")
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }

}