package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/8/14 9:38
 * @Desc: 数据库task表的类型枚举，主要有1：导入入口数据，2：导入出库数据，3：数据计算
 **/
public enum TaskTypeEnum {
    IMPORT_INBOUND_DATA("", 1),
    IMPORT_OUTBOUND_DATA("", 2),
    CALCULATE_DATA("", 3);


    private String name;
    private Integer code;

    TaskTypeEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public static TaskTypeEnum getEnumByCode(Integer code) {
        for(TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (taskTypeEnum.getCode().equals(code)) {
                return taskTypeEnum;
            }
        }
        return null;
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
