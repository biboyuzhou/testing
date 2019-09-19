package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/9/4 15:34
 * @Desc:
 **/
public enum ScheduleTaskEnum {
    CALCULATE("数据打标计算分数",1),
    PULL("从总表拉取当前路段的数据",2);

    private String name;
    private Integer code;

    ScheduleTaskEnum(String name, Integer code) {
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
    }}
