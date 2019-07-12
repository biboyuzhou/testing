package com.drcnet.highway.util;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/17 17:57
 * @Description:
 */


import com.drcnet.highway.annotation.ColumnName;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dto.ExcelSheetDto;
import com.drcnet.highway.exception.InternalServerErrorException;
import com.drcnet.highway.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * powered by IntelliJ IDEA
 *
 * @Description:
 **/
@Slf4j
public class ExcelUtil {

    private ExcelUtil() {
    }


    /**
     * 导出Excel
     *
     * @param sheetName sheet名称
     * @param title     标题
     * @param values    内容
     * @param wb        HSSFWorkbook对象
     * @return
     */
    public static XSSFWorkbook getHSSFWorkbook(String sheetName, String[] title, String[][] values, XSSFWorkbook wb) {

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        if (wb == null) {
            wb = new XSSFWorkbook();
        }

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        XSSFSheet sheet = wb.createSheet(sheetName);

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        XSSFRow row = sheet.createRow(0);

        // 第四步，创建单元格，并设置值表头 设置表头居中
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment((short) 0); // 创建一个居中格式

        //声明列对象
        XSSFCell cell = null;

        //创建标题
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }
        //创建内容
        for (int i = 0; i < values.length; i++) {
            row = sheet.createRow(i + 1);
            for (int j = 0; j < values[i].length; j++) {
                //将内容按顺序赋给对应的列对象
                row.createCell(j).setCellValue(values[i][j]);
            }
        }
        return wb;
    }

    public static XSSFWorkbook getHSSFWorkbook(String sheetName, String[] title, String[][] values) {
        return getHSSFWorkbook(sheetName, title, values, null);
    }

    /**
     * 单一sheet导出Excel文件
     *
     * @param data     数据列表
     * @param fileName 文件名称
     * @param response 响应
     * @param <C>      必须是泛型列表
     */
    public static <C> void export(List<C> data, String fileName, HttpServletResponse response) {
        if (data == null || data.isEmpty()) {
            try {
                response.getWriter().println(TipsConsts.SERVER_ERROR);
            } catch (IOException e) {
                log.error("print message error,{},{}", e, e.getLocalizedMessage());
            }
            return;
        }
        String[] titles = generateTitle(data.get(0).getClass());
        String[][] values = generateValue(data);

        if (fileName == null)
            fileName = "数据" + LocalDateTime.now().getSecond();
        XSSFWorkbook workbook = getHSSFWorkbook(fileName, titles, values);
        exportAction(workbook, response, fileName);
    }

    public static <C> byte[] getExportBytes(List<C> data, String fileName) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        String[] titles = generateTitle(data.get(0).getClass());
        String[][] values = generateValue(data);
        if (fileName == null)
            fileName = "数据" + LocalDateTime.now().getSecond();
        XSSFWorkbook workbook = getHSSFWorkbook(fileName, titles, values);
        byte[] bytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            workbook.write(outputStream);
            bytes = outputStream.toByteArray();
        } catch (IOException e) {
            log.error("{}",e);
            throw new MyException(TipsConsts.SERVER_ERROR);
        }
        return bytes;
    }

    /**
     * 导出多个sheet的excel
     */
    public static <C> void exportMany(List<ExcelSheetDto<C>> datas, String fileName, HttpServletResponse response) {
        XSSFWorkbook wb = new XSSFWorkbook();
        for (ExcelSheetDto<C> sheet : datas) {
            String[] title;
            List<C> data;
            if (sheet == null)
                data = null;
            else
                data = sheet.getData();

            if (data == null || data.isEmpty() || data.get(0) == null)
                title = new String[0];
            else
                title = generateTitle(data.get(0).getClass());
            String[][] values = generateValue(data);
            getHSSFWorkbook(Optional.ofNullable(sheet).map(ExcelSheetDto::getSheetName).orElse(""), title, values, wb);
        }
        exportAction(wb, response, fileName);
    }

    /**
     * 计算内容
     *
     * @param data 查询条件参数
     */
    private static <C> String[][] generateValue(List<C> data) {
        if (data == null || data.isEmpty())
            return new String[0][];
        Class<?> aClass = data.get(0).getClass();
        int size = data.size();
        String[][] values = new String[size][];

        for (int i = 0; i < size; i++) {
            Field[] fields = aClass.getDeclaredFields();
            List<String> list = new ArrayList<>();
            for (Field field : fields) {
                if (field.getAnnotation(ColumnName.class) != null) {
                    field.setAccessible(true);
                    try {
                        list.add(String.valueOf(Optional.ofNullable(field.get(data.get(i))).orElse("")));
                    } catch (IllegalAccessException e) {
                        log.error("{},{}", e, e.getMessage());
                        throw new InternalServerErrorException(TipsConsts.SERVER_ERROR);
                    }
                }
            }
            values[i] = list.toArray(new String[0]);
        }
        return values;
    }

    /**
     * 获得Excel标题
     *
     * @param aClass
     * @return
     */
    private static String[] generateTitle(Class aClass) {
        List<String> columns = new ArrayList<>();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            ColumnName annotation = field.getAnnotation(ColumnName.class);
            if (annotation != null)
                columns.add(annotation.value());
        }
        columns.toArray(new String[0]);
        return columns.toArray(new String[0]);
    }

    //设置响应头
    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            throw new InternalServerErrorException(TipsConsts.SERVER_ERROR);
        }
    }

    /**
     * 响应导出Excel操作
     *
     * @param fileName 要保存的文件名称
     * @param response
     */
    public static void exportAction(XSSFWorkbook workbook, HttpServletResponse response, String fileName) {

        try {
            setResponseHeader(response, fileName + ".xlsx");
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("{},{}", e, e.getMessage());
            throw new InternalServerErrorException("服务器异常");
        }
    }


}