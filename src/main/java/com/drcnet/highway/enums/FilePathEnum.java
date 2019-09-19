package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/13 19:45
 * @Description:
 */
public enum FilePathEnum {

    INBOUND("C:/highway/inbound/","/var/highway/inbound/")
    ,OUTBOUND("C:/highway/outbound/","/var/highway/outbound/")
    ;

    public final String windows;

    public final String linux;

    FilePathEnum(String windows, String linux) {
        this.windows = windows;
        this.linux = linux;
    }
    public static FilePathEnum getBound(int type){
        if (type == 0){
            return INBOUND;
        }else if (type == 1){
            return OUTBOUND;
        }
        throw new IllegalArgumentException();
    }

}
