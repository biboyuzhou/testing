package com.drcnet.highway.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/18 9:54
 * @Description: 特征标准化参数
 */
public class StandardParam {


    public static final Map<String, FeatureStandard> featureStandardMap;

    static {
        featureStandardMap = new HashMap<>();
        featureStandardMap.put("sameCarNumber", new FeatureStandard(179D, 49.11, 24.0, 287.724));
        featureStandardMap.put("speed", new FeatureStandard(332D, 22D, 17D, 278.54));
        featureStandardMap.put("sameCarType", new FeatureStandard(311D, 12D, 6D, 18.5));
        featureStandardMap.put("sameCarSituation", new FeatureStandard(1199D, 275.78, 76D, 202D));
        featureStandardMap.put("shortDisOverweight", new FeatureStandard(239D, 38.78, 16D, 32D));
        featureStandardMap.put("longDisLightweight", new FeatureStandard(522D, 238.67, 182D, 43.15));
        featureStandardMap.put("diffFlagstationInfo", new FeatureStandard(1940D, 654.67, 440.44, 584.92));
        featureStandardMap.put("sameStation", new FeatureStandard(203D, 33.67, 17D, 55D));
        featureStandardMap.put("sameTimeRangeAgain", new FeatureStandard(93D, 12D, 2.89, 491D));
        featureStandardMap.put("minOutIn", new FeatureStandard(1271D, 291.78, 151.89, 1002D));
        featureStandardMap.put("flagstationLost", new FeatureStandard(101D, 12D, 8.11, 2537.26));
        featureStandardMap.put("differentZhou", new FeatureStandard(85D, 28.22, 20.11, 12D));
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureStandard {

        private Double highest;

        private Double high;

        private Double middle;

        private Double low;

        public Integer getScore(Integer origin) {
            if (origin == null)
                throw new IllegalArgumentException();
            origin = Math.abs(origin);
            if (origin > highest) {
                return 98;
            } else if (origin > high) {
                return (int)((origin-high)/(highest-high) * (93-75) + 75);
            }else if (origin >= middle){
                return (int)((origin-middle)/(high-middle) * (75-50) + 50);
            }
            return 0;
        }
    }

}
