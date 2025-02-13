package com.shiyi.shiyicodesandbox.aop;

import cn.hutool.json.JSONUtil;
import com.shiyi.shiyicodesandbox.model.LogModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * 请求响应日志 AOP
 *
 * @author <a href="https://github.com/lishiyi">程序员鱼皮</a>
 * @from <a href="https://shiyi.icu">编程导航知识星球</a>
 **/
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogInterceptor {

    /**
     * 执行拦截
     */
//    @Around("execution(* com.shiyi.shiyioj.controller.*.*(..))")
//    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
//        // 计时
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        // 获取请求路径
//        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
//        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
//        // 生成请求唯一 id
//        String requestId = UUID.randomUUID().toString();
//        String url = httpServletRequest.getRequestURI();
//        // 获取请求参数
//        Object[] args = point.getArgs();
//        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
//        // 输出请求日志
//        log.info("request start，id: {}, path: {}, ip: {}, params: {}", requestId, url,
//                httpServletRequest.getRemoteHost(), reqParam);
//        // 执行原方法
//        Object result = point.proceed();
//        // 输出响应日志
//        stopWatch.stop();
//        long totalTimeMillis = stopWatch.getTotalTimeMillis();
//        log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
//        return result;
//    }
    private final HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);
    /**
     * 监听使用了RestController注解的类
     *
     * @param point 切点
     * @return 接口返回结果
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object apiLog(ProceedingJoinPoint point) throws Throwable {
        String requestUri = request.getRequestURI();
        String requestIp = request.getRemoteAddr();
        LocalDateTime requestTime = LocalDateTime.now();
        Throwable exceptionInfo = null;
        Object proceed = null;
        try {
            proceed = point.proceed();
            return proceed;
        } catch (Throwable e) {
            exceptionInfo = e;
            throw e;
        } finally {
            //打印日志
            new LogModel(requestUri,
                    point.getSignature().toString(),
                    Arrays.toString(point.getArgs()),
                    Objects.isNull(proceed) ? null : JSONUtil.toJsonStr(proceed),
                    Objects.isNull(exceptionInfo) ? null : exceptionInfo.toString(),
                    requestTime,
                    LocalDateTime.now(),
                    requestIp).
                    log();
        }
    }
    @After("@annotation(com.shiyi.shiyicodesandbox.annotation.JvmInfoLog)")
    public void logJvmInfo(){
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        log.info("总内存：{}", totalMemory);
        log.info("已使用内存：{}", usedMemory);
        log.info("空闲内存：{}", freeMemory);
    }

}

