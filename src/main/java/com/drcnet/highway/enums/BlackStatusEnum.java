package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 9:50
 * @Description:
 */
public enum BlackStatusEnum {
    NONE(0),WHITE(1),BLACK(2);
    public int code;

    BlackStatusEnum(int code) {
        this.code = code;
    }
}
