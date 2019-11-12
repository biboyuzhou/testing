package com.drcnet.highway.service.check.instance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author jack
 * @Date: 2019/10/25 10:04
 * @Desc:
 **/
public class LocalDateTest {

    public static void main(String[] args) {
        /*LocalDate localDate = LocalDate.of(2019, 12, 12);
        localDate = LocalDate.now();

        int year = localDate.getYear();
        int year1 = localDate.get(ChronoField.YEAR);
        Month month = localDate.getMonth();
        int month1 = localDate.get(ChronoField.MONTH_OF_YEAR);
        int day = localDate.getDayOfMonth();
        int day1 = localDate.get(ChronoField.DAY_OF_MONTH);
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        int dayOfWeek1 = localDate.get(ChronoField.DAY_OF_WEEK);
        System.out.println(2);*/

        /*LocalTime localTime = LocalTime.of(13, 51, 10);
        LocalTime localTime1 = LocalTime.now();

        //获取小时
        int hour = localTime.getHour();
        int hour1 = localTime.get(ChronoField.HOUR_OF_DAY);
        //获取分
        int minute = localTime.getMinute();
        int minute1 = localTime.get(ChronoField.MINUTE_OF_HOUR);
        //获取秒
        int second = localTime.getSecond();
        int second1 = localTime.get(ChronoField.SECOND_OF_MINUTE);
        System.out.println(3);*/

       /* LocalDate localDate = LocalDate.of(2019, 9, 10);
        String s1 = localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String s2 = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);*/


        LocalDateTime localDateTime = LocalDateTime.of(2019, 11, 11, 11, 11, 11);
        String s1 = localDateTime.format(DateTimeFormatter.BASIC_ISO_DATE);
        String s2 = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String s3 = localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
