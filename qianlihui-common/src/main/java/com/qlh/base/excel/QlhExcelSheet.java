package com.qlh.base.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

public class QlhExcelSheet {
    private Sheet sheet;

    public QlhExcelSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public QlhExcelRow createRow(int i) {
        return new QlhExcelRow(sheet.createRow(i));
    }

    public QlhExcelRow getRow(int i) {
        return new QlhExcelRow(sheet.getRow(i));
    }

    public QlhExcelSheet autoSizeColumn(int i) {
        sheet.autoSizeColumn(i);
        return this;
    }

    public QlhExcelSheet setWidth(int column, int width) {
        sheet.setColumnWidth(column, width);
        return this;
    }

    public QlhExcelSheet createFreezePane(int a, int b, int c, int d) {
        sheet.createFreezePane(a, b, c, d);
        return this;
    }

    public QlhExcelSheet merge(int sRowIdx, int eRowIdx, int sColIdx, int eColIdx) {
        sheet.addMergedRegion(new CellRangeAddress(sRowIdx, eRowIdx, sColIdx, eColIdx));
        return this;
    }

}
