package com.shiyi.shiyicodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteMessage {
    private Integer exitValue;
    private String message;
    private String errorMessage;
    private long memory;
    private long time;
}
