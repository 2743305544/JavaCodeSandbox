package com.shiyi.shiyicodesandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ShiyiCodeSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiyiCodeSandboxApplication.class, args);
    }

}
