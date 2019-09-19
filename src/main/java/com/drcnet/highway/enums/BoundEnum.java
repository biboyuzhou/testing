package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/14 10:14
 * @Description:
 */
public enum  BoundEnum {

    INBOUND(0,"进站"),OUTBOUND(1,"出站")
    ;
    public final int code;
    public final String msg;

    BoundEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public static BoundEnum getBound(int code){
        if (code == INBOUND.code){
            return INBOUND;
        }else if (code == OUTBOUND.code){
            return OUTBOUND;
        }
        return null;
    }
}
