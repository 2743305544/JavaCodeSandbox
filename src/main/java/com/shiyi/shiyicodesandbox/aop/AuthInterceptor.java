package com.shiyi.shiyicodesandbox.aop;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.shiyi.shiyicodesandbox.constant.SysConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_REQUEST_HEADER = SysConstant.AUTH_REQUEST_HEADER;
    private static final String AUTH_REQUEST_VALUE = SysConstant.AUTH_REQUEST_VALUE;
    private static final byte[] key ="1234567890123456".getBytes();
    AES aes = new AES(key);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);


        String decryptedValue = aes.decryptStr(authHeader);
         System.out.println("解密后的值: " + decryptedValue);

        // 检查Authorization头是否存在并且与预期的值匹配
        if (decryptedValue == null || !decryptedValue.equals(AUTH_REQUEST_VALUE)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 如果验证通过，继续处理请求
        return true;
    }
}
