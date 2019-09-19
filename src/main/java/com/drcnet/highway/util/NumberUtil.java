package com.drcnet.highway.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 17:38
 * @Description:
 */
public class NumberUtil {

    public static Integer nullFormat(Integer num){
        return Optional.ofNullable(num).orElse(0);
    }

    public static Double nullFormat(Double num){
        return Optional.ofNullable(num).orElse(0D);
    }

    public static Double divideThousand(Integer num,int scale){
        if (num == null){
            return null;
        }
        return BigDecimal.valueOf(num).divide(BigDecimal.valueOf(1000),scale, RoundingMode.HALF_UP).doubleValue();
    }

}
