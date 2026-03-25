package com.scnu.springbootjdk17demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.scnu.springbootjdk17demo.mapper")
public class SpringBootJdk17DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootJdk17DemoApplication.class, args);
    }

}
