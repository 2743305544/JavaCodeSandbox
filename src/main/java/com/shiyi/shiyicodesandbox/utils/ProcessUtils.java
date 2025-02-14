package com.shiyi.shiyicodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeResponse;
import com.shiyi.shiyicodesandbox.model.ExecuteMessage;
import com.shiyi.shiyicodesandbox.model.JudgeInfo;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class ProcessUtils {
    private static final String charset = System.getProperty("os.name").equalsIgnoreCase("linux") ? "UTF-8" : "GBK";

    public static ExecuteMessage runProcessAndGetMessage(Process process) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            int exitCode = process.waitFor();
            executeMessage.setExitValue(exitCode);
            if (exitCode == 0) {
                System.out.println("编译成功");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(charset)));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    System.out.println(compileOutputLine);
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());
                bufferedReader.close();
            } else {
                System.out.println("编译失败 exitCode = " + exitCode);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(charset)));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());
                BufferedReader ErrorbufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName(charset)));
                StringBuilder errorOutputStringBuilder = new StringBuilder();
                String errorOutputLine;
                while ((errorOutputLine = ErrorbufferedReader.readLine()) != null) {
                    errorOutputStringBuilder.append(errorOutputLine);
                }
                ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
                executeCodeResponse.setOutputList(new ArrayList<>());
                executeCodeResponse.setMessage(compileOutputStringBuilder.toString());
                executeCodeResponse.setStatus(2);
                executeCodeResponse.setJudgeInfo(new JudgeInfo());
                executeMessage.setErrorMessage(JSONUtil.toJsonStr(executeCodeResponse));
                bufferedReader.close();
                ErrorbufferedReader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 交互式判题
     * @param process
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process process,String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try(InputStream inputStream = process.getInputStream();
            OutputStream outputStream =process.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, Charset.forName(charset));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(charset)));
            BufferedReader ErrorbufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName(charset)));){
//            String[] s =args.split(" ");
//            String join = StrUtil.join("\n", s) +"\n";
//            outputStreamWriter.write(join);
            long[] memory = {getMemoryByPid(process.pid())};
            outputStreamWriter.write(args + System.lineSeparator());
            // 启动一个线程 每隔20ms获取一次内存使用情况
            Thread thread = getThread(process, memory);
            outputStreamWriter.flush();
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                System.out.println("1111"+compileOutputLine);
                compileOutputStringBuilder.append(compileOutputLine);
            }
//            System.out.println(compileOutputStringBuilder);
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            int i = process.waitFor();
            // 结束线程
            thread.interrupt();
            executeMessage.setExitValue(i);
            StringBuilder errorOutputStringBuilder = new StringBuilder();
            String errorOutputLine;
            while ((errorOutputLine = ErrorbufferedReader.readLine()) != null) {
                errorOutputStringBuilder.append(errorOutputLine);
            }
            executeMessage.setMemory(memory[0]);
            executeMessage.setErrorMessage(errorOutputStringBuilder.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return executeMessage;
    }

    private static Thread getThread(Process process, long[] memory) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20);
                    if (process.isAlive()) {
                        memory[0] = Math.max(memory[0], getMemoryByPid(process.pid()));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }

    private static long getMemoryByPid(long pid) {
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("os = " + os);
        long memoryUsage = 0;
        BufferedReader reader;
        try {
            Process memoryInfoProcess;
            if (os.contains("win")) {
                // Windows系统
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FI", "\"PID eq " + pid + "\"");
                memoryInfoProcess = processBuilder.start();
                reader = new BufferedReader(new InputStreamReader(memoryInfoProcess.getInputStream(), Charset.forName(charset)));
                String line;
                while ((line = reader.readLine()) != null) {
                    // 跳过标题行
                    if (line.startsWith("映像名称") || line.startsWith("PID") || line.startsWith("=")) {
                        continue;
                    }
                    // 解析内存使用情况
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1) {
                        System.out.println(Arrays.toString(parts));
                        memoryUsage = Long.parseLong(parts[4].trim().replace(",", ""));
                    }
                }
            } else if (os.contains("nix") || os.contains("linux") || os.contains("mac")) {
                // Linux或Mac系统
                ProcessBuilder processBuilder = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-o", "rss,%cpu,cmd");
                memoryInfoProcess = processBuilder.start();
                reader = new BufferedReader(new InputStreamReader(memoryInfoProcess.getInputStream(), Charset.forName(charset)));
                String line;
                while ((line = reader.readLine()) != null) {
                    if ( line.startsWith("PID") || line.startsWith("=") || line.contains("RSS")) {
                        continue;
                    }
                    // 解析内存使用情况
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1) {
                        System.out.println(Arrays.toString(parts));
                        memoryUsage = Long.parseLong(parts[0]);
                    }
                }
            } else {
                throw new UnsupportedOperationException("Unsupported operating system: " + os);
            }
            memoryInfoProcess.waitFor();
            reader.close();
            memoryInfoProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memoryUsage;
    }
}
