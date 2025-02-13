package com.shiyi.shiyicodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目判断信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;
    /**
     * 消耗的内存，单位为KB
     */
    private Long memory;
    /**
     * 消耗时间，单位为KB
     */
    private Long time;
}
