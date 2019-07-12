package com.drcnet.highway.constants;

import com.drcnet.highway.dto.RiskMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 15:51
 * @Description:
 */
public class RiskConsts {

    public static final Map<String, RiskMap> RISK_MAP;

    static {
        RISK_MAP = new HashMap<>();
        RISK_MAP.put("diffFlagstationInfo",new RiskMap(1,"行驶路径不一致"));
        RISK_MAP.put("shortDisOverweight",new RiskMap(2,"短途重载风险"));
        RISK_MAP.put("longDisLightweight",new RiskMap(3,"长途轻载风险"));
        RISK_MAP.put("speed",new RiskMap(4,"行驶速度异常风险"));
        RISK_MAP.put("sameStation",new RiskMap(5,"同站进出风险"));
        RISK_MAP.put("sameCarType",new RiskMap(6,"进出车型不一致风险"));
        RISK_MAP.put("sameCarSituation",new RiskMap(7,"进出车情不一致风险"));
        RISK_MAP.put("differentZhou",new RiskMap(8,"轴数异常风险"));
        RISK_MAP.put("sameCarNumber",new RiskMap(9,"进出车牌不一致"));
        RISK_MAP.put("minOutIn",new RiskMap(10,"高频进出风险"));
        RISK_MAP.put("sameTimeRangeAgain",new RiskMap(11,"重复驶入风险"));
        RISK_MAP.put("flagstationLost",new RiskMap(12,"行驶路径值缺失"));
        RISK_MAP.put("highSpeed",new RiskMap(13,"高速异常"));
        RISK_MAP.put("lowSpeed",new RiskMap(14,"低速异常"));

    }

}
