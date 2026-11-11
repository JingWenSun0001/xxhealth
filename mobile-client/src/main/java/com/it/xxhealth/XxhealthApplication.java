package com.it.xxhealth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.it.xxhealth.mapper")
public class XxhealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxhealthApplication.class, args);
    }

}
