package com.drcnet.highway.util;

import com.drcnet.highway.constants.ConfigConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.exception.InternalServerErrorException;
import com.drcnet.highway.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/28 13:37
 * @Description: bean的工具
 */
@Slf4j
public class EntityUtil {

    private EntityUtil() {
    }

    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    private static final String SET = "set";
    private static final String GET = "get";
    private static final String GET_CLASS = "getClass";
    private static final Pattern yearMonth = Pattern.compile("^20\\d{2}((0[1-9])|1[012])");
    private static final Pattern date = Pattern.compile("^20\\d{2}((0[1-9])|(1[012]))[0123]\\d");
    private static final MyException myException = new MyException("日期格式异常");
    private static final MyException DateException = new MyException("日期格式异常");
    private static final MyException tableNotFoundException = new MyException("没有该月份的数据");
    private static Pattern likeWordPattern = Pattern.compile(".*[.*%].*");
    /**
     * 将t对象的同名属性复制到M对象
     *
     * @param from
     * @param to
     * @return
     */
    public static <T, M> M copyNotNullFields(T from, M to) {
        return copyNotNullFields(from, to, null);
    }

    public static <T, M> M copyNotNullFields(T from, M to, List<String> ignoreFields) {
        if (from == null || to == null)
            return null;
        Field[] tFields = FieldUtils.getAllFields(from.getClass());
        Field[] mFields = FieldUtils.getAllFields(to.getClass());
        for (Field tField : tFields) {
            String tName = tField.getName();
            Class<?> tType = tField.getType();
            if (tName.equals(SERIAL_VERSION_UID))
                continue;
            for (Field mField : mFields) {
                if (mField.getName().equals(tName) && mField.getType() == tType) {
                    if (ignoreFields != null && ignoreFields.contains(tName))
                        continue;
                    try {
                        tField.setAccessible(true);
                        if (tField.get(from) == null)
                            continue;
                        mField.setAccessible(true);
                        mField.set(to, tField.get(from));
                    } catch (IllegalAccessException e) {
                        log.error("获取对象属性失败!{}", e);
                        throw new InternalServerErrorException(TipsConsts.SERVER_ERROR);
                    }
                }
            }
        }
        return to;
    }

    public static <T, M> M copyNotNullFieldsByGetSet(T from, M to) {
        return copyNotNullFieldsByGetSet(from, to, null);
    }

    public static <T, M> M copyNotNullFieldsByGetSet(T from, M to, List<String> ignoreFields) {
        if (from == null || to == null)
            return null;
        Method[] tMethods = from.getClass().getMethods();
        Method[] mMethods = to.getClass().getMethods();
        for (Method tMethod : tMethods) {
            String tName = tMethod.getName();
            Class<?> tReturnType = tMethod.getReturnType();
            if (!tName.startsWith(GET) || tName.equals(GET_CLASS))
                continue;
            for (Method mMethod : mMethods) {
                if (tName.equals(mMethod.getName()) && mMethod.getReturnType() == tReturnType) {
                    if (ignoreFields != null && !ignoreFields.contains(tName.substring(3).toLowerCase()))
                        continue;
                    String setMethodName = tName.replaceFirst(GET, SET);
                    try {
                        Object fromValue = tMethod.invoke(from);
                        if (fromValue == null)
                            continue;
                        Method toMethod = to.getClass().getMethod(setMethodName, tReturnType);
                        toMethod.invoke(to, fromValue);
                    } catch (ReflectiveOperationException e) {
                        log.error("获取对象属性失败!{}", e);
                        throw new InternalServerErrorException(TipsConsts.SERVER_ERROR);
                    }

                }
            }
        }
        return to;
    }

    /**
     * 判断时间格式是否为yyyyMM格式，若不是则抛异常
     *
     * @param pattern 200001 - 209912
     */
    public static void dateMonthChecked(String pattern) {
        if (!yearMonth.matcher(pattern).matches())
            throw myException;
        if (!ConfigConsts.monthTimes.contains(pattern))
            throw tableNotFoundException;
    }

    public static byte[] input2byte(InputStream inStream)
            throws IOException {
        try (ByteArrayOutputStream swapStream = new ByteArrayOutputStream()) {
            byte[] buff = new byte[4096];
            int rc = 0;
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toByteArray();
        }
    }

    /**
     * 判断时间格式是否为yyyyMMdd格式，若不是则抛异常
     *
     * @param pattern 200001 - 209912
     */
    public static void dateChecked(String pattern) {
        if (!date.matcher(pattern).matches()) {
            throw DateException;
        }

    }

    public static boolean isNormalCarNo(String carNo) {

        return false;
    }
    public static String formatKeyWord(String word) {
        if (StringUtils.isBlank(word)) {
            return null;
        }
        if (likeWordPattern.matcher(word).matches()) {
            return word.replaceAll("\\%", "\\\\%");
        }
        return word;
    }

    public static String formatKeyWordWithPrev(String word) {
        String keyWord = formatKeyWord(word);
        if (keyWord == null) {
            return null;
        }
        return "%" + word + "%";
    }
}
