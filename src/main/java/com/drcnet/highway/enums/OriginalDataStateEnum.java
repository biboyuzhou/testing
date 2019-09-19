package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/13 20:05
 * @Description:
 */
public enum OriginalDataStateEnum {

    UPLOADED(1,"已上传")
    ,IMPORTING(2,"数据导入中")
    ,IMPORTED(3,"导入成功")
    ,IMPORTED_DEFEAT(4,"导入失败")
    ;
    public final int code;

    public final String msg;

    OriginalDataStateEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
