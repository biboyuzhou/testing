package com.drcnet.highway.exception;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/16 16:05
 * @Description:
 */
public class TimeOutException extends RuntimeException {
    public TimeOutException() {
    }

    public TimeOutException(String message) {
        super(message);
    }
}
