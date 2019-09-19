package com.drcnet.highway.config;

import com.drcnet.highway.enums.BoundEnum;
import com.drcnet.highway.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/13 10:01
 * @Description:
 */
@Slf4j
public class ExcelImportUtil {

    public static final List<String> OUTBOUND_TEMPLATE_EXCEL_TITLES;
    public static final List<String> INBOUND_TEMPLATE_EXCEL_TITLES;

    static {
        OUTBOUND_TEMPLATE_EXCEL_TITLES = new ArrayList<>(32);
        INBOUND_TEMPLATE_EXCEL_TITLES = new ArrayList<>();
        Resource outboundResource = new ClassPathResource("static/出口数据模板.xls");
        Resource inboundResource = new ClassPathResource("static/入口数据模板.xls");
        try {
            addTitle(outboundResource,OUTBOUND_TEMPLATE_EXCEL_TITLES,3);
            addTitle(inboundResource,INBOUND_TEMPLATE_EXCEL_TITLES,2);
        } catch (IOException e) {
            log.error("{}", e);
            throw new MyException();
        }
    }

    private static void addTitle(Resource resource,List<String> titles,int firstRowNum) throws IOException{
        try (InputStream is = resource.getInputStream()) {
            HSSFWorkbook workbook = new HSSFWorkbook(is);
            HSSFSheet sheet = workbook.getSheetAt(0);
            HSSFRow row = sheet.getRow(firstRowNum);
            short lastCellNum = row.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                HSSFCell cell = row.getCell(i);
                if (cell == null || cell.getCellTypeEnum() == CellType._NONE) {
                    break;
                } else {
                    String cellValue = cell.getStringCellValue();
                    titles.add(cellValue);
                }
            }
        }
    }


    /**
     * 判断excel是不是模板上的excel
     */
    public static boolean isTemplateExcel(Workbook workbook, BoundEnum boundEnum) {
        List<String> title = new ArrayList<>();
        if (boundEnum == BoundEnum.OUTBOUND){
            title = OUTBOUND_TEMPLATE_EXCEL_TITLES;
        }else if (boundEnum == BoundEnum.INBOUND){
            title = INBOUND_TEMPLATE_EXCEL_TITLES;
        }

        Sheet sheet = workbook.getSheetAt(0);
        boolean flag = false;
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Cell firstTitleCell = row.getCell(0);
            if (firstTitleCell.getCellTypeEnum() == CellType.STRING && title.get(0).equals(firstTitleCell.getStringCellValue())) {
                //标题相同，且该列个数相同，则将每个单元格的数据和模板的数据进行比较
                flag = true;
                for (int j = 0; j < title.size(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null || cell.getCellTypeEnum() != CellType.STRING || !title.get(j).equals(cell.getStringCellValue())) {
                        flag = false;
                        break;
                    }
                }
                break;
            }
        }
        return flag;
    }

    /**
     * 获得标题行的index
     */
    public static int getFirstDataRow(Sheet sheet, BoundEnum bound) {
        String firstTitle = "";
        if (bound == BoundEnum.OUTBOUND){
            firstTitle = OUTBOUND_TEMPLATE_EXCEL_TITLES.get(0);
        }else if (bound == BoundEnum.INBOUND){
            firstTitle = INBOUND_TEMPLATE_EXCEL_TITLES.get(0);
        }

        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null){
                continue;
            }
            Cell cell = row.getCell(0);
            if (cell !=null && cell.getCellTypeEnum() == CellType.STRING && firstTitle.equals(cell.getStringCellValue())) {
                return i + 1;
            }
        }
        throw new MyException("excel格式异常");
    }


}
