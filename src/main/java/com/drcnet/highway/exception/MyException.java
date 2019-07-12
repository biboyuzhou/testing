package com.drcnet.highway.exception;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/9 10:42
 * @Description:
 */
public class MyException extends RuntimeException {

    public MyException() {
    }

    public MyException(String message) {
        super(message);
    }
}
