package com.drcnet.highway.constants;

import com.drcnet.highway.dto.RiskMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author jack
 * @Date: 2019/7/22 17:42
 * @Desc:
 **/
public class CarSituationConsts {

    public static final Map<String, Integer> SITUATION_MAP;
    public static final Map<String, Integer> CAR_TYPE_MAP;

    static {
        SITUATION_MAP = new HashMap<>();
        SITUATION_MAP.put("未知车种", 0);
        SITUATION_MAP.put("普通车", 1);
        SITUATION_MAP.put("军车", 2);
        SITUATION_MAP.put("车队", 4);
        SITUATION_MAP.put("全免鲜活车", 7);
        SITUATION_MAP.put("集装箱货车", 10);
        SITUATION_MAP.put("临时免1", 12);
        SITUATION_MAP.put("临时免2", 13);
        SITUATION_MAP.put("临时免3", 14);
        SITUATION_MAP.put("大件货车", 15);
        SITUATION_MAP.put("无证大件货车", 16);
        SITUATION_MAP.put("20/40英尺集装箱", 51);
        SITUATION_MAP.put("非20/40英尺集装箱", 52);
        SITUATION_MAP.put("省内免费鲜活车", 53);

        CAR_TYPE_MAP = new HashMap<>();
        SITUATION_MAP.put("客一", 1);
        SITUATION_MAP.put("客二", 2);
        SITUATION_MAP.put("客三", 3);
        SITUATION_MAP.put("客四", 4);
        SITUATION_MAP.put("客五", 5);
        SITUATION_MAP.put("货一", 11);
        SITUATION_MAP.put("货二", 12);
        SITUATION_MAP.put("货三", 13);
        SITUATION_MAP.put("货四", 14);
        SITUATION_MAP.put("货五", 15);
    }
}
