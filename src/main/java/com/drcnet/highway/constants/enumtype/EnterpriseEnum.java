package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/8/6 16:49
 * @Desc:
 **/
public enum EnterpriseEnum {
    SECOND_ROUND("二绕西", 1),
    YILU("宜泸", 2),
    XUGU("叙古", 3),
    NANDALIANG("南大梁", 4),
    YIXU("宜叙", 5),
    BAGUANGYU("巴广渝", 6),
    MIANNAN("绵南", 7),
    CHENGMIANFUXIAN("成绵复线", 8),
    ZILONG("自隆", 9),
    CHENGZILU("成自泸", 10),
    NEIWEIRONG("内威荣", 11),
    JIANGXIGU("江习古", 12);

    private String name;
    private Integer code;

    EnterpriseEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public static EnterpriseEnum getEnumByCode(Integer code) {
        for(EnterpriseEnum enterpriseEnum : EnterpriseEnum.values()) {
            if (enterpriseEnum.getCode().equals(code)) {
                return enterpriseEnum;
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
