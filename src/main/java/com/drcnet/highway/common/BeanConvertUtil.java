package com.drcnet.highway.common;

import com.drcnet.highway.entity.TietouFeatureStatistic;
import com.drcnet.highway.entity.TietouFeatureStatisticMonth;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.TietouOriginal2019;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author jack
 * @Date: 2019/6/10 11:20
 * @Desc:
 **/
public class BeanConvertUtil {

    public static TietouOrigin convertOriginal2Tietou(TietouOriginal2019 record){
        TietouOrigin tietou = new TietouOrigin();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        tietou.setEntime(LocalDateTime.parse(replace(record.getEntime()), df));
        tietou.setRk(record.getRk());
        tietou.setEnvlp(record.getEnvlp());
        tietou.setEnvc(Integer.parseInt(record.getEnvc()));
        tietou.setEnvt(Integer.parseInt(record.getEnvt()));
        tietou.setExtime(LocalDateTime.parse(replace(record.getExtime()), df));
        tietou.setCk(record.getCk());
        tietou.setVlp(record.getVlp());
        tietou.setVc(Integer.parseInt(record.getVc()));
        tietou.setVt(Integer.parseInt(record.getVt()));
        tietou.setExlane(record.getExlane());
        tietou.setOper(record.getOper());
        tietou.setLastmoney(convert2BigDecimal(record.getLastmoney()));
        tietou.setFreemoney(convert2BigDecimal(record.getFreemoney()));
        tietou.setTolldistance(Integer.parseInt(record.getTolldistance()));
        tietou.setTotalweight(Integer.parseInt(record.getTotalweight()));
        tietou.setAxlenum(Integer.parseInt(record.getAxlenum()));
        tietou.setCard(record.getCard());
        tietou.setFlagstationinfo(record.getFlagstationinfo());
        tietou.setRealflagstationinfo(record.getRealflagstationinfo());
        tietou.setInv(record.getInv());
        DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyyMM");
        String monthTime = df1.format(tietou.getExtime());
        tietou.setMonthTime(Integer.parseInt(monthTime));
        if (tietou.getAxlenum() == 2) {
            tietou.setWeightLimitation(18000);
        } else if (tietou.getAxlenum() == 3) {
            tietou.setWeightLimitation(27000);
        } else if (tietou.getAxlenum() == 4) {
            tietou.setWeightLimitation(36000);
        } else if (tietou.getAxlenum() == 5) {
            tietou.setWeightLimitation(43000);
        } else if (tietou.getAxlenum() == 6) {
            tietou.setWeightLimitation(49000);
        } else {
            tietou.setWeightLimitation(0);
        }
        return tietou;
    }

    public static TietouOrigin convertOriginal2Tietou(String[] source) {
        TietouOrigin tietou = new TietouOrigin();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        tietou.setEntime(LocalDateTime.parse(replace(source[0]), df));
        tietou.setRk(source[1]);
        tietou.setEnvlp(source[2]);
        tietou.setEnvc(source[3] == null ? null : Integer.parseInt(source[3]));
        tietou.setEnvt(source[4] == null ? null : Integer.parseInt(source[4]));
        tietou.setExtime(source[5] == null ? null : LocalDateTime.parse(replace(source[5]), df));
        tietou.setCk(source[6]);
        tietou.setVlp(source[7]);
        tietou.setVc(source[8] == null ? null : Integer.parseInt(source[8]));
        tietou.setVt(source[9] == null ? null : Integer.parseInt(source[9]));
        tietou.setExlane(source[10]);
        tietou.setOper(source[11]);
        tietou.setLastmoney(source[12] == null ? null : convert2BigDecimal(source[12]));
        tietou.setFreemoney(source[13] == null ? null : convert2BigDecimal(source[13]));
        tietou.setTolldistance(source[14] == null ? null : Integer.parseInt(source[14]));
        tietou.setAxlenum(StringUtils.isEmpty(source[15]) ? null : Integer.parseInt(source[15]));
        tietou.setTotalweight(source[16] == null ? null : Integer.parseInt(source[16]));
        tietou.setCard(source[17]);
        tietou.setFlagstationinfo(source[18]);
        tietou.setRealflagstationinfo(source[19]);
        tietou.setInv(source[20]);
        DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyyMM");
        String monthTime = df1.format(tietou.getExtime());
        tietou.setMonthTime(Integer.parseInt(monthTime));
        if (tietou.getAxlenum() == 2) {
            tietou.setWeightLimitation(18000);
        } else if (tietou.getAxlenum() == 3) {
            tietou.setWeightLimitation(27000);
        } else if (tietou.getAxlenum() == 4) {
            tietou.setWeightLimitation(36000);
        } else if (tietou.getAxlenum() == 5) {
            tietou.setWeightLimitation(43000);
        } else if (tietou.getAxlenum() == 6) {
            tietou.setWeightLimitation(49000);
        } else {
            tietou.setWeightLimitation(0);
        }
        return tietou;
    }

    public static String replace(String text) {
        if (text.startsWith("\uFEFF")) {
            return text.replace("\uFEFF", "");
        }
        return text;
    }

    public static BigDecimal convert2BigDecimal(String data) {
        StringBuilder sb = new StringBuilder("0");
        if (data.startsWith(".")) {
            data = sb.append(data).toString();
        }
        return new BigDecimal(Double.valueOf(data)).setScale(2, BigDecimal.ROUND_HALF_UP);

    }

    public static void main(String[] args) {
        String d = "13.00000";
        String t = ".000000";

        System.out.println(convert2BigDecimal(d));
        System.out.println(convert2BigDecimal(t));

    }

    public static TietouFeatureStatistic convert2Statistic(TietouFeatureStatisticMonth month) {
        TietouFeatureStatistic statistic = new TietouFeatureStatistic();
        statistic.setVlp(month.getVlp());
        statistic.setVlpId((month.getVlpId()));
        statistic.setSameCarNumber(month.getSameCarNumber());
        statistic.setSpeed(month.getSpeed());
        statistic.setSameCarType(month.getSameCarType());
        statistic.setSameCarSituation(month.getSameCarSituation());
        statistic.setShortDisOverweight(month.getShortDisOverweight());
        statistic.setLongDisLightweight(month.getLongDisLightweight());
        statistic.setDiffFlagstationInfo(month.getDiffFlagstationInfo());
        statistic.setSameStation(month.getSameStation());
        statistic.setSameTimeRangeAgain(month.getSameTimeRangeAgain());
        statistic.setMinOutIn(month.getMinOutIn());
        statistic.setFlagstationLost(month.getFlagstationLost());
        statistic.setDifferentZhou(month.getDifferentZhou());
        statistic.setTotal(month.getTotal());
        statistic.setCarType(month.getCarType());
        statistic.setLabel(month.getLabel());
        statistic.setScore(month.getScore());
        statistic.setUseFlag(month.getUseFlag());
        return statistic;
    }
}
