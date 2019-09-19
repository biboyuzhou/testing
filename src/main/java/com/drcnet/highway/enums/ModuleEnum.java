package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/7 15:41
 * @Description:
 */
public enum ModuleEnum {

    HOME_PAGE("homePage","首页+车辆画像+风险详情")
    ,RISK_CAR("riskCar","风险车辆查询")
    ,EXCHANGE_CAR("exchangeCar","换卡风险查询")
    ,ACCESS_RECORDS("accessRecords","通行记录查询")
    ,UPLOAD_DATA("uploadData","上传数据")
    ,DATA_SURVEY("dataSurvey","数据概况查询")
    ,ACCOUNT_MANAGEMENT("accountManagement","账号管理")
    ;

    public final String code;

    public final String des;

    ModuleEnum(String code, String des) {
        this.code = code;
        this.des = des;
    }
}
