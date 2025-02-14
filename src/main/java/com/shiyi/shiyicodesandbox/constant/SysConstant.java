package com.shiyi.shiyicodesandbox.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SysConstant {
    //临时代码目录
    public static final String TMP_CODE = "tmpCode";

    //临时代码文件名
    public static final String TMP_CODE_FILE_NAME = "Main.java";

    //编译命令
    public static final String COMPILE_CMD = "javac -encoding UTF-8 %s";

    //运行命令
    public static final String RUN_CMD = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s";

    //代码黑名单
    public static final List<String> BLACK_CODE = List.of("File","exec","ProcessBuilder","Process","Runtime","java.lang.Runtime","java.lang.ProcessBuilder","java.lang.Process","java.net","java.lang.Class.forName","java.lang.ClassLoader","java.lang.reflect.Method.invoke","java.lang.reflect.Field.setAccessible");

    //Docker java 镜像
    public static final String DOCKER_JAVA_IMAGE = "azul/zulu-openjdk:21";

    //Docker 容器内存
    public static final long DOCKER_CONTAINER_MEMORY = 100*1024*1024L;

    //Docker 容器 CPU 数量
    public static final long DOCKER_CPU_COUNT = 1L;

    // auth request header
    public static final String AUTH_REQUEST_HEADER = "Authorization";

    // auth request algorithm
    private static final String ALGORITHM = "AES";

    // auth request value
    public static final String AUTH_REQUEST_VALUE = "Basic YWRtaW46YWRtaW4=";
}
