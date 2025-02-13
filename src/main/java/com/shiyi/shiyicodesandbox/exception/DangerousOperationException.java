package com.shiyi.shiyicodesandbox.exception;

public class DangerousOperationException extends RuntimeException {
    public DangerousOperationException(String message) {
        super(message);
    }
}
