//package com.shiyi.shiyicodesandbox;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.lang.UUID;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.dfa.FoundWord;
//import cn.hutool.dfa.WordTree;
//import cn.hutool.json.JSONUtil;
//import com.shiyi.shiyicodesandbox.constant.SysConstant;
//import com.shiyi.shiyicodesandbox.exception.DangerousOperationException;
//import com.shiyi.shiyicodesandbox.model.ExecuteCodeRequest;
//import com.shiyi.shiyicodesandbox.model.ExecuteCodeResponse;
//import com.shiyi.shiyicodesandbox.model.ExecuteMessage;
//import com.shiyi.shiyicodesandbox.model.JudgeInfo;
//import com.shiyi.shiyicodesandbox.utils.ProcessUtils;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StopWatch;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Service("JavaCodeSandboxTemplate")
//public abstract class JavaCodeSandboxTemplate implements CodeSandbox {
//    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
//
//
////    @Resource
////    private ScheduledExecutorService executorService;
//
//    private final String field = System.getProperty("user.dir");
//    private final String globalCodePathName = field + File.separator + SysConstant.TMP_CODE;
//    private static WordTree wordTree;
//
//    @PostConstruct
//    public void init() {
//        if (!FileUtil.exist(globalCodePathName)) {
//            FileUtil.mkdir(globalCodePathName);
//        }
//        if(wordTree == null){
//            wordTree = new WordTree();
//            wordTree.addWords(SysConstant.BLACK_CODE);
//        }
//    }
//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
//        List<String> inputs = request.getInputList();
//        String code = request.getCode();
//
//        // TODO 使用布隆过滤器检测code里面是否存在黑名单代码 后续优化
//
//        FoundWord foundWord = wordTree.matchWord(code);
//        if (foundWord != null) {
//            return getErrorResponse(new DangerousOperationException("代码中存在危险代码"));
//        }
//
//        File userCodeFile = saveCodeToFile(code);
//        String compileCmd = String.format(SysConstant.COMPILE_CMD, userCodeFile.getAbsolutePath());
//        ExecuteMessage executeMessage = runProcess(userCodeFile.getParent(), compileCmd);
//        if(executeMessage.getExitValue() == -1){
//            return JSONUtil.toBean(executeMessage.getErrorMessage(), ExecuteCodeResponse.class);
//        }
//        List<ExecuteMessage> executeMessages = runCode(userCodeFile.getParent(), inputs, request);
//        return getExecuteCodeResponse(executeMessages, userCodeFile, userCodeFile.getParent());
//    }
//
//    public ExecuteCodeResponse getExecuteCodeResponse(List<ExecuteMessage> executeCodeResponses, File userCodeFile, String userCodePathName) {
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        List<String> outputs = new ArrayList<>();
//        long maxTime = 0;
//        long maxMemory = 0;
//        for (ExecuteMessage executeCodeResponse1 : executeCodeResponses) {
//            // 懒得重构了，飞线了
//            if (executeCodeResponse1.getExitValue() == -1) {
//                return JSONUtil.toBean(executeCodeResponse1.getErrorMessage(), ExecuteCodeResponse.class);
//            }
//            String errorMessage = executeCodeResponse1.getErrorMessage();
//            maxTime = Math.max(maxTime, executeCodeResponse1.getTime());
//            maxMemory = Math.max(maxMemory, executeCodeResponse1.getMemory());
//            if (StrUtil.isNotBlank(errorMessage)) {
//                executeCodeResponse.setMessage(errorMessage);
//                executeCodeResponse.setStatus(3);
//                break;
//            }
//            outputs.add(executeCodeResponse1.getMessage());
//        }
//        executeCodeResponse.setOutputList(outputs);
//        if (executeCodeResponse.getStatus() == null && outputs.size() == executeCodeResponses.size()) {
//            executeCodeResponse.setStatus(1);
//        }
//        JudgeInfo judgeInfo = JudgeInfo.builder()
//                .time(maxTime)
//                .memory(maxMemory)
//                .build();
//        executeCodeResponse.setJudgeInfo(judgeInfo);
//        if (userCodeFile.getParentFile() != null) {
//            boolean del = FileUtil.del(userCodePathName);
//            if (del) {
//                System.out.println("删除成功" + userCodePathName);
//            }else {
//                System.out.println("删除失败" + userCodePathName);
//            }
//        }
//        return executeCodeResponse;
//    }
//
//    public File saveCodeToFile(String code) {
//        String userCodePathName = globalCodePathName + File.separator + UUID.randomUUID();
//        String userCodeFileName = userCodePathName + File.separator + SysConstant.TMP_CODE_FILE_NAME;
//        return FileUtil.writeString(code, userCodeFileName, StandardCharsets.UTF_8);
//    }
//
//    public List<ExecuteMessage> runCode(String userCodePathName, List<String> inputs, ExecuteCodeRequest request) {
//        List<ExecuteMessage> executeCodeResponses = new ArrayList<>();
//        for (String input : inputs) {
//            String runCmd = String.format(SysConstant.RUN_CMD, userCodePathName, input);
////            runProcess(userCodePathName, runCmd);
//            ExecuteMessage executeMessage = runInterProcess(userCodePathName, runCmd, input,request.getTime());
//            executeCodeResponses.add(executeMessage);
//        }
//        return executeCodeResponses;
//    }
//
//    private ExecuteMessage runInterProcess (String userCodePathName, String runCmd, String args, int time){
//        ExecuteMessage executeMessage =new ExecuteMessage();
//        try {
//            //JDK 8 之后的写法
//            ProcessBuilder processBuilder = new ProcessBuilder(runCmd.split(" "));
//            processBuilder.directory(new File(userCodePathName));
//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            Process runProcess = processBuilder.start();
//            executorService.schedule(()->{
//                if (runProcess.isAlive()) {
//                    runProcess.destroyForcibly();
//                }
//            }, time, TimeUnit.MILLISECONDS);
//            //JDK 8写法
////                Process runProcess = Runtime.getRuntime().exec(runCmd);
//            executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, args);
//            runProcess.destroy();
//            stopWatch.stop();
//            executeMessage.setMemory(executeMessage.getMemory());
//            executeMessage.setTime(Math.toIntExact(stopWatch.getLastTaskTimeMillis()));
//        } catch (IOException e) {
//            executeMessage.setExitValue(-1);
//            executeMessage.setErrorMessage(JSONUtil.toJsonStr(getErrorResponse(e)));
//        }
//        return executeMessage;
//    }
//
//
//    public ExecuteMessage runProcess(String userCodePathName, String runCmd) {
//        ExecuteMessage executeMessage = new ExecuteMessage();
//        try {
//            //JDK 8 之后的写法
//            ProcessBuilder processBuilder = new ProcessBuilder(runCmd.split(" "));
//            processBuilder.directory(new File(userCodePathName));
//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            Process runProcess = processBuilder.start();
//            //JDK 8写法
////                Process runProcess = Runtime.getRuntime().exec(runCmd);
//            executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess);
//            runProcess.destroy();
//            stopWatch.stop();
//            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
//        } catch (IOException e) {
//            executeMessage.setExitValue(-1);
//            executeMessage.setErrorMessage(JSONUtil.toJsonStr(getErrorResponse(e)));
//        }
//        return executeMessage;
//    }
//
//
//    private ExecuteCodeResponse getErrorResponse(Throwable e) {
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        executeCodeResponse.setOutputList(new ArrayList<>());
//        executeCodeResponse.setMessage(e.getMessage());
//        executeCodeResponse.setStatus(2);
//        executeCodeResponse.setJudgeInfo(new JudgeInfo());
//        return executeCodeResponse;
//    }
//}
