package com.shiyi.shiyicodesandbox;

import com.shiyi.shiyicodesandbox.model.ExecuteCodeRequest;
import com.shiyi.shiyicodesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    /**
     * 执行代码
     * @param request
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest request);
}
