package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/8/5 15:22
 * @Desc:
 **/
public enum EscapeBehaviorEnum {

    CHANGE_CARD("换卡", 1),
    CHANGE_GOODS("换货", 2),
    THROW_HANG("甩挂", 3),
    CHANGE_ZHOU("改轴", 4),
    OTHER("其他", 5);


    /**
     * 名称
     */
    private String name;
    /**
     * 编码
     */
    private Integer code;

    EscapeBehaviorEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public static EscapeBehaviorEnum getEnumByCode(Integer code) {
        for(EscapeBehaviorEnum escapeBehaviorEnum : EscapeBehaviorEnum.values()) {
            if (escapeBehaviorEnum.getCode().equals(code)) {
                return escapeBehaviorEnum;
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
    }}
