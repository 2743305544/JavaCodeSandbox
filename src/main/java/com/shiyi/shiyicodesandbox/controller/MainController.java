package com.shiyi.shiyicodesandbox.controller;

import com.shiyi.shiyicodesandbox.CodeSandbox;
import com.shiyi.shiyicodesandbox.JavaDockerCodeSandBox;
import com.shiyi.shiyicodesandbox.JavaNativeCodeSandBox;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeRequest;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {


    @Value("${docker.enable}")
    private boolean dockerEnable;

    @Autowired
    private Map<String, CodeSandbox> CodeSandBoxMapList;

    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

    @PostConstruct
    public void init() {
        if (dockerEnable) {
            javaDockerCodeSandBox = (JavaDockerCodeSandBox) CodeSandBoxMapList.get("JavaDockerCodeSandBox");
        }
    }

    private JavaDockerCodeSandBox javaDockerCodeSandBox;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping("/native/executeCode")
    public ExecuteCodeResponse nativeExecuteCode(@RequestBody @NonNull ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBox.executeCode(executeCodeRequest);
        System.out.println("executeCodeResponse: " + executeCodeResponse.toString());
        return executeCodeResponse;
    }

    @PostMapping("/docker/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody @NonNull ExecuteCodeRequest executeCodeRequest) {
        if (dockerEnable) {
            return javaDockerCodeSandBox.executeCode(executeCodeRequest);
        }else {
            return new ExecuteCodeResponse("Docker is not enabled");
        }
    }

}
