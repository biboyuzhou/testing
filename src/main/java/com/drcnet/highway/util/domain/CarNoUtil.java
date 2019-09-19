package com.drcnet.highway.util.domain;

import com.drcnet.highway.constants.ConfigConsts;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.TietouOutbound;
import com.drcnet.highway.util.EntityUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/13 11:10
 * @Description:
 */
public class CarNoUtil {

    private static Pattern CHINESE_CAR = Pattern.compile("^[\\u4e00-\\u9fa5][a-zA-Z][0-9a-zA-Z]{5,6}$");

    /**
     * 判断车牌号是否是合法车牌号
     * 传入的车牌号必须是全大写
     *
     * @return
     */
    public static boolean generateCarUseFlag(String carNo) {
        if (StringUtils.isBlank(carNo)){
            return false;
        }
        if (carNo.endsWith(ConfigConsts.HUO_SUFFIX)){
            carNo = carNo.replaceAll(ConfigConsts.HUO_SUFFIX,"");
        }
        if (!CHINESE_CAR.matcher(carNo).matches()) {
            return false;
        }
        int length = carNo.length();
        //判断8位数的车牌第3位是否是D(纯电动)或F(插电式混动)开头
        if (length == 8) {
            char c = carNo.charAt(2);
            if (c != 'F' && c != 'D') {
                return false;
            }
        }
        //截取车牌后5位，看有无超过3位字母
        String carNoMain = carNo.substring(length - 5);
        int letterNum = 0;
        for (int i = 0; i < carNoMain.length(); i++) {
            int ascii = (int) carNoMain.charAt(i);
            if (ascii >= 65 && ascii <= 90 || ascii >= 97 && ascii <= 122) {
                letterNum++;
            }
        }
        return letterNum <= 2;
    }

    /**
     * 将TietouOrigin对象的entime,extime,rkId,ckId,envlpId,vlpId组合成一个字符串
     *
     * @return
     */
    public static String formatOrigin(TietouOrigin tietouOrigin) {
        String s = "_";
        LocalDateTime entime = tietouOrigin.getEntime();
        LocalDateTime extime = tietouOrigin.getExtime();
        Integer envlpId = tietouOrigin.getEnvlpId();
        Integer vlpId = tietouOrigin.getVlpId();
        Integer rkId = tietouOrigin.getRkId();
        Integer ckId = tietouOrigin.getCkId();
        return entime + s + extime + s + envlpId + s + vlpId + s + rkId + s + ckId;
    }

    public static String formatOrigin(TietouOutbound tietouOutbound) {
        TietouOrigin tietouOrigin = EntityUtil.copyNotNullFields(tietouOutbound, new TietouOrigin());
        return formatOrigin(tietouOrigin);
    }


}
