package com.drcnet.highway.util;

import com.drcnet.highway.constants.TimeConsts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class DateUtils {

    /**
     * 获取两个月份之间的月份集合
     * @param minDate 开始月份
     * @param maxDate  结束月份
     * @return
     * @throws ParseException
     */
    public static List<String> getMonthBetween(String minDate, String maxDate) throws ParseException {
        ArrayList<String> result = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");//格式化为年月

        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();

        min.setTime(sdf.parse(minDate));
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

        max.setTime(sdf.parse(maxDate));
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

        Calendar curr = min;
        while (curr.before(max)) {
            result.add(sdf.format(curr.getTime()));
            curr.add(Calendar.MONTH, 1);
        }

        return result;
    }

    public static String convertDatePattern(String source) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date d = sdf.parse(source);
        SimpleDateFormat target = new SimpleDateFormat("yyyy-MM-dd");
        return target.format(d);
    }

    /**
     * 获取当前月的第一天
     * @return
     */
    public static String getFirstDayOfCurrentMonth() {
        LocalDateTime localDateTime = LocalDateTime.now();
        StringBuilder sb = new StringBuilder(localDateTime.getYear());
        sb.append("-").append(localDateTime.getMonthValue()).append("-").append("01");
        return sb.toString();
    }

    /**
     * 获取当前月的第一天
     * @return
     */
    public static String getFirstDayOfCurrentYear() {
        LocalDateTime localDateTime = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(localDateTime.getYear()).append("-").append("01").append("-").append("01");
        return sb.toString();
    }

    /**
     * 获取今天的日期字符串
     * @return
     */
    public static String getCurrentDay() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return df.format(localDateTime);
    }

    /**
     * 获取当前月的字符串
     * @return
     */
    public static String getCurrentMonth() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMM");
        return df.format(localDateTime);
    }

    public static String formatTime(LocalDateTime localDateTime){
        if (localDateTime == null){
            return null;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(TimeConsts.TIME_FORMAT));
    }

    public static LocalDateTime parseTime(String time,DateTimeFormatter formatter){
        try {
            return LocalDateTime.parse(time, formatter);
        }catch (DateTimeParseException e){
            return null;
        }
    }

    public static LocalDateTime date2LocalDateTime(Date date){
        if (date == null){
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

}
