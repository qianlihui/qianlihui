package com.qlh.base.excel;

import org.apache.poi.ss.usermodel.Row;

public class QlhExcelRow {

    private Row row;

    public QlhExcelRow(Row row) {
        this.row = row;
    }

    public QlhExcelCell getCell(int i) {
        return new QlhExcelCell(row.getCell(i));
    }

    public QlhExcelCell createCell(int i) {
        return new QlhExcelCell(row.createCell(i));
    }
}
