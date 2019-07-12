package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/1 17:43
 * @Description:
 */
public enum CarTypeEnum {
    TRUCKS(0,"货车"),COACH(1,"客车"),UNKNOWN(-1,"未知");

    public int code;

    public String msg;

    CarTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
