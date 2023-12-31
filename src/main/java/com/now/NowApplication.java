package com.now;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.now.core", annotationClass = Mapper.class)
public class NowApplication {

    public static void main(String[] args) {
        SpringApplication.run(NowApplication.class, args);
    }

}
