package com.shiyi.shiyicodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import cn.hutool.json.JSONUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.shiyi.shiyicodesandbox.annotation.JvmInfoLog;
import com.shiyi.shiyicodesandbox.constant.SysConstant;
import com.shiyi.shiyicodesandbox.exception.DangerousOperationException;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeRequest;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeResponse;
import com.shiyi.shiyicodesandbox.model.ExecuteMessage;
import com.shiyi.shiyicodesandbox.model.JudgeInfo;
import com.shiyi.shiyicodesandbox.utils.ProcessUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service("JavaDockerCodeSandBox")
@ConditionalOnProperty(name = "docker.enable", havingValue = "true")
public class JavaDockerCodeSandBox implements CodeSandbox {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private static boolean DOCKER_INIT = false;

//    @Resource
//    private ScheduledExecutorService executorService;

    private final String field = System.getProperty("user.dir");
    private final String globalCodePathName = field + File.separator + SysConstant.TMP_CODE;
    private static WordTree wordTree;
    private String dockerId;
    private final DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    @PostConstruct
    public void init() {
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        if (wordTree == null) {
            wordTree = new WordTree();
            wordTree.addWords(SysConstant.BLACK_CODE);
        }
        if(!DOCKER_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(SysConstant.DOCKER_JAVA_IMAGE);
            try {
                pullImageCmd.exec(new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        System.out.println("Docker(java) is in progress..." + item.getStatus());
                        super.onNext(item);
                    }
                }).awaitCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            DOCKER_INIT = true;
        }
    }

    public static void main(String[] args) {
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

    @JvmInfoLog
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        List<String> inputs = request.getInputList();
        String code = request.getCode();

        // TODO 使用布隆过滤器检测code里面是否存在黑名单代码 后续优化

        FoundWord foundWord = wordTree.matchWord(code);
        if (foundWord != null) {
            return getErrorResponse(new DangerousOperationException("代码中存在危险代码"));
        }

        String language = request.getLanguage();
        UUID uuid = UUID.randomUUID();
        String userCodePathName = globalCodePathName + File.separator + uuid;
        String userCodeFileName = userCodePathName + File.separator + SysConstant.TMP_CODE_FILE_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodeFileName, StandardCharsets.UTF_8);
        String compileCmd = String.format(SysConstant.COMPILE_CMD, userCodeFile.getAbsolutePath());
        runProcess(globalCodePathName, compileCmd);
        long maxTime = 0;
        long[] maxMemory = {0};
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        try {
            if (dockerId == null) {
                HostConfig hostConfig = new HostConfig();
                hostConfig.withMemory(SysConstant.DOCKER_CONTAINER_MEMORY);
                hostConfig.withBinds(new Bind(globalCodePathName, new Volume("/app/code")));
                hostConfig.withCpuCount(SysConstant.DOCKER_CPU_COUNT);
                CreateContainerResponse exec = dockerClient.createContainerCmd(SysConstant.DOCKER_JAVA_IMAGE)
                        .withHostConfig(hostConfig)
                        .withNetworkDisabled(true)
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .withTty(true)
                        .exec();
                dockerId = exec.getId();
            }
            final String[] message = {null};
            final String[] errorMessage = {null};
            dockerClient.startContainerCmd(dockerId).exec();
            for (String input : inputs){
                ExecuteMessage executeMessage = new ExecuteMessage();
                StopWatch stopWatch = new StopWatch();
                String[] s = input.split(" ");
                String[] cmdArray = new String[]{"java","-cp","/app/code/" + uuid ,"Main"};
                String[] append = ArrayUtil.append(cmdArray, s);
                System.out.println(Arrays.toString(append));
                ExecCreateCmdResponse exec = dockerClient.execCreateCmd(dockerId)
                        .withCmd(append)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .withAttachStdin(true)
                        .exec();
                stopWatch.start();
                StatsCmd statsCmd = dockerClient.statsCmd(dockerId);
                statsCmd.exec(new ResultCallback<Statistics>() {
                    @Override
                    public void onStart(Closeable closeable) {

                    }

                    @Override
                    public void onNext(Statistics object) {
                        maxMemory[0] = Math.max(maxMemory[0], object.getMemoryStats().getUsage());
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void close() throws IOException {

                    }
                });
                final boolean[] timedOut = {true};
                dockerClient.execStartCmd(exec.getId()).exec(new ExecStartResultCallback(){
                    @Override
                    public void onComplete() {
                        timedOut[0] = false;
                        super.onComplete();
                    }

                    @Override
                    public void onNext(Frame frame) {
                        if(frame.getStreamType() == StreamType.STDOUT){
                            message[0] = new String(frame.getPayload());
                        }
                        if(frame.getStreamType() == StreamType.STDERR){
                            errorMessage[0] = new String(frame.getPayload());
                        }
                        super.onNext(frame);
                    }
                }).awaitCompletion(request.getTime(),TimeUnit.MILLISECONDS);
                stopWatch.stop();
                statsCmd.close();
                if(timedOut[0]){
                    maxTime = request.getTime();
                }
                maxTime = Math.max(maxTime, stopWatch.getTotalTimeMillis());
                message[0] = StrUtil.trim(message[0]);
                executeMessage.setMessage(message[0]);
                executeMessage.setErrorMessage(errorMessage[0]);
                executeMessageList.add(executeMessage);
            }
        } catch (Exception e) {
            System.out.println("docker start error..." + e.getMessage());
            return getErrorResponse(e);
        }
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(executeMessageList.stream().map(ExecuteMessage::getMessage).toList());
        executeCodeResponse.setStatus(1);
        executeCodeResponse.setJudgeInfo(JudgeInfo.builder()
                        .memory(maxMemory[0]/1000) // 单位KB
                        .time(maxTime)
                        .message(executeMessageList.stream().map(ExecuteMessage::getErrorMessage).toList().toString())
                        .build());
            if (userCodeFile.getParentFile() != null) {
                FileUtil.del(userCodePathName);
                System.out.println("删除成功");
            }
        return executeCodeResponse;
    }

    private ExecuteMessage runProcess (String userCodePathName, String runCmd){
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            //JDK 8 之后的写法
            ProcessBuilder processBuilder = new ProcessBuilder(runCmd.split(" "));
            processBuilder.directory(new File(userCodePathName));
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Process runProcess = processBuilder.start();
            //JDK 8写法
//                Process runProcess = Runtime.getRuntime().exec(runCmd);
            executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess);
            runProcess.destroy();
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (IOException e) {
            executeMessage.setExitValue(-1);
            executeMessage.setErrorMessage(JSONUtil.toJsonStr(getErrorResponse(e)));
        }
        return executeMessage;
    }

    /**
     * 返回代码沙箱的问题而不是用户程序的问题
     *
     * @param e
     * @return ExecuteCodeResponse
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
