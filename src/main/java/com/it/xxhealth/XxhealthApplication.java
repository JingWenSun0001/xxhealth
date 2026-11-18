package com.it.xxhealth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 熙心健康体检系统的 Spring Boot 启动入口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@SpringBootApplication
@MapperScan("com.it.xxhealth.mapper")
public class XxhealthApplication {

    /**
     * 启动熙心健康体检系统。
     * @param args 应用启动参数或 SQL 绑定参数
     */
    public static void main(String[] args) {
        SpringApplication.run(XxhealthApplication.class, args);
    }

}
