package com.qlh.base.excel;

import org.apache.poi.ss.usermodel.Cell;

public class QlhExcelCell {

    private Cell cell;

    public QlhExcelCell(Cell cell) {
        this.cell = cell;
    }

    public QlhExcelCell setValue(String v) {
        cell.setCellValue(v);
        return this;
    }

    public QlhExcelCell setValue(long v) {
        cell.setCellValue(v);
        return this;
    }

    public QlhExcelCell setValue(double v) {
        cell.setCellValue(v);
        return this;
    }

    public QlhExcelCell setStyle(QlhExcelStyle style) {
        cell.setCellStyle(style.getStyle());
        return this;
    }
}
