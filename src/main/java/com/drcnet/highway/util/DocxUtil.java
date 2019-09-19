package com.drcnet.highway.util;

import com.drcnet.highway.constants.RgbConsts;
import com.drcnet.highway.enums.FeatureEnum;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/2 10:36
 * @Description:
 */
public class DocxUtil {

    public static final String FANG_SONG = "仿宋";
    public static final String HEI_TI = "黑体";
    public static final String HUA_WEN_XI_HEI = "华文细黑";
    public static final String CALIBRI = "Calibri";

    /**
     * 将一段文本中的占位符替换成具体的参数
     * 占位符样例:{0}、{1}
     *
     * @param featureEnum 异常特征枚举
     * @param params      参数
     */
    public static String getTxt(FeatureEnum featureEnum, Object... params) {
        List<Object> paramList = Arrays.asList(params);
        String txt = featureEnum.detail;
        if (StringUtils.isEmpty(txt)) {
            return "";
        }
        for (int i = 0; i < paramList.size(); i++) {
            String path = "{" + i + "}";
            if (txt.contains(path)) {
                txt = txt.replace(path, String.valueOf(paramList.get(i)));
            }
        }
        return txt;
    }

    /**
     * 设置单元格宽度
     *
     * @param table 表
     * @param width 宽度
     */
    public static void setTableWidth(XWPFTable table, long width) {
        CTTblWidth tblW = table.getCTTbl().getTblPr().getTblW();
        tblW.setType(STTblWidth.DXA);
        tblW.setW(BigInteger.valueOf(width));

        for (XWPFTableCell cell : table.getRow(0).getTableCells()) {
            CTTc cttc = cell.getCTTc();
            CTTcPr ctTcPr = cttc.addNewTcPr();
//            ctTcPr.addNewVAlign().setVal(STVerticalJc.CENTER);
//            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
            ctTcPr.addNewShd().setFill(RgbConsts.BLUE);
        }
    }

    /**
     * 计算百分比
     *
     * @param amount 当前数量
     * @param total  总数
     */
    public static double getPercent(int amount, int total, int scale) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf((double) amount / total * 100).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static double getDivide(double amount, double total, int scale) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf(amount / total).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 创建paragraph并生成run
     */
    public static XWPFRun createParagraphAndRun(XWPFParagraph paragraph, String txt, boolean tab, ParagraphAlignment alignment, String fontFamily, String color
            , Integer fontSize, boolean bold) {
        if (alignment != null) {
            paragraph.setAlignment(alignment);
        }
        XWPFRun run = paragraph.createRun();
        if (tab) {
            run.addTab();
        }
        if (fontFamily != null) {
            run.setFontFamily(fontFamily);
        }
        if (fontSize != null) {
            run.setFontSize(fontSize);
        }
        if (txt != null) {
            run.setText(txt);
        }
        if (color != null) {
            run.setColor(color);
        }
        run.setBold(bold);
        return run;
    }

    public static XWPFRun createParagraphAndRun(XWPFDocument document, String txt, boolean tab, ParagraphAlignment alignment, String fontFamily
            , Integer fontSize, boolean bold) {
        XWPFParagraph paragraph = document.createParagraph();
        return createParagraphAndRun(paragraph, txt, tab, alignment, fontFamily,null, fontSize, bold);
    }

    public static XWPFRun createParagraphAndRun(XWPFDocument document, String txt, boolean tab, ParagraphAlignment alignment, String fontFamily
            ,String color, Integer fontSize, boolean bold) {
        XWPFParagraph paragraph = document.createParagraph();
        return createParagraphAndRun(paragraph, txt, tab, alignment, fontFamily,color, fontSize, bold);
    }

    public static XWPFRun createParagraphAndRun(XWPFDocument document, ParagraphAlignment alignment, String fontFamily, Integer fontSize, boolean bold) {
        return createParagraphAndRun(document, null, false, alignment, fontFamily, fontSize, bold);
    }

    public static XWPFRun createParagraphAndRun(XWPFDocument document, String txt, ParagraphAlignment alignment, String fontFamily, Integer fontSize, boolean bold) {
        return createParagraphAndRun(document, txt, false, alignment, fontFamily, fontSize, bold);
    }


    /**
     * 设置行距
     *
     * @param para 段落
     * @param line 行距，240为1倍行距
     */
    public static void setSingleLineSpacing(XWPFParagraph para, long line) {
        CTPPr ppr = para.getCTP().getPPr();
        if (ppr == null) ppr = para.getCTP().addNewPPr();
        CTSpacing spacing = ppr.isSetSpacing() ? ppr.getSpacing() : ppr.addNewSpacing();
        spacing.setAfter(BigInteger.valueOf(0));
        spacing.setBefore(BigInteger.valueOf(0));
        spacing.setLineRule(STLineSpacingRule.AUTO);
        spacing.setLine(BigInteger.valueOf(line));
    }

}
