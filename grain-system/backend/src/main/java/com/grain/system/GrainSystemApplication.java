package com.grain.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.grain.system.module.*.mapper")
@EnableAsync
@EnableScheduling
public class GrainSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrainSystemApplication.class, args);
        System.out.println("=======================================================");
        System.out.println("  乡镇粮食代收点进销存系统 启动成功");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("=======================================================");
    }
}
