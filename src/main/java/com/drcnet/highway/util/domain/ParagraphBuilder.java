package com.drcnet.highway.util.domain;

import lombok.Data;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/9 16:03
 * @Description:
 */
@Data
public class ParagraphBuilder {


    public static Builder createParagraph(XWPFParagraph paragraph) {
        return new Builder(paragraph);
    }

    public static class Builder {

        private XWPFParagraph paragraph;
        private XWPFRun run;

        private ParagraphAlignment alignment;
        private String txt;
        private String fontFamily;
        private int fontSize;
        private String fontColor;
        private String fillColor;
        private boolean bold = false;
        private boolean addTab = false;

        public void build() {
            if (alignment != null)
                paragraph.setAlignment(alignment);
            if (addTab)
                run.addTab();
            if (txt != null)
                run.setText(txt);
            if (fontFamily != null)
                run.setFontFamily(fontFamily);
            if (fontColor != null)
                run.setColor(fontColor);
            if (fontSize != 0)
                run.setFontSize(fontSize);
            if (bold)
                run.setBold(true);

            if (fillColor != null) {
                CTRPr rPr = run.getCTR().getRPr();
                CTRPr ctrPr = rPr == null ? run.getCTR().addNewRPr() : rPr;
                CTShd ctShd = ctrPr.getShd() == null ? ctrPr.addNewShd() : ctrPr.getShd();
                ctShd.setFill(fillColor);
            }

        }

        public Builder(XWPFParagraph paragraph) {
            this.paragraph = paragraph;
            List<XWPFRun> runs = paragraph.getRuns();
            run = CollectionUtils.isEmpty(runs) ? paragraph.createRun() : runs.get(0);
        }

        //文本位置
        public Builder setAlignment(ParagraphAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        //设置文本
        public Builder setTxt(String txt) {
            this.txt = txt;
            return this;
        }

        //字体
        public Builder setFontFamily(String fontFamily) {
            this.fontFamily = fontFamily;
            return this;
        }

        //字体大小
        public Builder setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        //字体颜色
        public Builder setFontColor(String rgbStr) {
            this.fontColor = rgbStr;
            return this;
        }

        //底纹颜色
        public Builder setFillColor(String rgbStr) {
            this.fillColor = rgbStr;
            return this;
        }

        //加粗
        public Builder setBold() {
            this.bold = true;
            return this;
        }

        //加tab
        public Builder addTab() {
            this.addTab = true;
            return this;
        }
    }

}
