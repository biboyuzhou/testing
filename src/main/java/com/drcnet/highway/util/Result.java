package com.drcnet.highway.util;

import lombok.Getter;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/28 14:45
 * @Description:
 */
@Getter
public class Result<T> {

    private int code;

    private String message;

    private T data;

    public static Result ok(){
        return new Result(200);
    }

    public static <T> Result ok(T data){
        Result result = new Result(200);
        result.data = data;
        return result;
    }

    public static Result success(String message){
        Result result = new Result(200);
        result.message = message;
        return result;
    }

    public static Result error(int code, String message) {
        Result result = new Result(code);
        result.message = message;
        return result;
    }

    public static Result error(String message) {
        return error(500, message);
    }

    private Result(int code) {
        this.code = code;
    }
}
