package com.shiyi.shiyicodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeRequest;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootTest
@EnableAspectJAutoProxy
@ComponentScans({
        @ComponentScan("com.shiyi.shiyicodesandbox.config"),
        @ComponentScan("com.shiyi.shiyicodesandbox.aop")
})
class ShiyiCodeSandboxApplicationTests {

    @Test
    void contextLoads() {
        JavaDockerCodeSandBox javaNativeCodeSandBox = new JavaDockerCodeSandBox();
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        request.setInputList(Arrays.asList("1 2", "2 3"));
//        request.setCode(ResourceUtil.readStr("otherSimple/Main.java", StandardCharsets.UTF_8));
        request.setCode(ResourceUtil.readStr("simple/Main.java",StandardCharsets.UTF_8));
        request.setLanguage("java");
        request.setTime(1000);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBox.executeCode(request);
        System.out.println(executeCodeResponse);
    }
    @Test
    void contextLoads1() {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        request.setInputList(Arrays.asList("1 2", "2 3"));
        request.setCode(ResourceUtil.readStr("otherSimple/Main.java", StandardCharsets.UTF_8));
//        request.setCode(ResourceUtil.readStr("simple/Main.java",StandardCharsets.UTF_8));
        request.setLanguage("java");
        request.setTime(1000);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBox.executeCode(request);
        System.out.println(executeCodeResponse);
    }
}
