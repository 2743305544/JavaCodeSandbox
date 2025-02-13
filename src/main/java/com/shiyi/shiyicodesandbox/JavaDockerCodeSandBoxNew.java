//package com.shiyi.shiyicodesandbox;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.dfa.WordTree;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.command.PullImageCmd;
//import com.github.dockerjava.api.command.PullImageResultCallback;
//import com.github.dockerjava.api.model.PullResponseItem;
//import com.github.dockerjava.core.DockerClientBuilder;
//import com.shiyi.shiyicodesandbox.constant.SysConstant;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;

//@Service("JavaDockerCodeSandBoxNew")
//public class JavaDockerCodeSandBoxNew extends JavaCodeSandboxTemplate {
//    private String dockerId;
//    private final DockerClient dockerClient = DockerClientBuilder.getInstance().build();
//    private static boolean DOCKER_INIT = false;
//    @PostConstruct
//    public void init() {
//        if(!DOCKER_INIT) {
//            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(SysConstant.DOCKER_JAVA_IMAGE);
//            try {
//                pullImageCmd.exec(new PullImageResultCallback() {
//                    @Override
//                    public void onNext(PullResponseItem item) {
//                        System.out.println("Docker(java) is in progress..." + item.getStatus());
//                        super.onNext(item);
//                    }
//                }).awaitCompletion();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            DOCKER_INIT = true;
//        }
//    }
//    // 懒得重构了
//}
