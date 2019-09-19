package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/9/3 10:24
 * @Desc:
 **/
public enum  YesNoEnum {

    YES("是", 1),
    NO("否", 0);

    private String name;
    private Integer code;

    YesNoEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
