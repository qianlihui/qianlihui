package com.qlh.base.excel;

import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

@Data
public class QlhExcelStyle {

    private Font font;
    private CellStyle style;

    public QlhExcelStyle(HSSFWorkbook workbook) {
        font = workbook.createFont();
        style = workbook.createCellStyle();
        style.setFont(font);
        setVerticalAlignment(VerticalAlignment.CENTER);
        setAlignment(HorizontalAlignment.CENTER);
    }

    public QlhExcelStyle setFontHeightInPoints(int v) {
        font.setFontHeightInPoints((short) v);
        return this;
    }

    public QlhExcelStyle setFontName(String name) {
        font.setFontName(name);
        return this;
    }

    public QlhExcelStyle setBold(boolean v) {
        font.setBold(v);
        return this;
    }

    public QlhExcelStyle setColor(int v) {
        font.setColor((short) v);
        return this;
    }

    public QlhExcelStyle setAlignment(HorizontalAlignment v) {
        style.setAlignment(v);
        return this;
    }

    public QlhExcelStyle setVerticalAlignment(VerticalAlignment v) {
        style.setVerticalAlignment(v);
        return this;
    }

    public QlhExcelStyle setFillForegroundColor(short v) {
        style.setFillForegroundColor(v);
        return this;
    }

    public QlhExcelStyle setFillPattern(FillPatternType v) {
        style.setFillPattern(v);
        return this;
    }
}
