package com.qlh.base.excel;


import com.qlh.base.QlhException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class QlhExcel {

    private HSSFWorkbook workbook;

    public QlhExcel() {
        workbook = new HSSFWorkbook();
    }

    public QlhExcel(String filePath) {
        this((InputStream) QlhException.runtime(() -> new FileInputStream(filePath)));
    }

    public QlhExcel(InputStream inputStream) {
        workbook = QlhException.runtime(() -> WorkbookFactory.create(inputStream));
    }

    public QlhExcelSheet createSheet(String name) {
        return new QlhExcelSheet(workbook.createSheet(name));
    }

    public QlhExcelStyle createStyle() {
        return new QlhExcelStyle(workbook);
    }

    public void write(String path) {
        write(new File(path));
    }

    public void write(File file) {
        QlhException.runtime(() -> workbook.write(file));
    }

    /* ************************ 静态方法 即将废弃 ************************ */
    public static QlhWorkbook load(InputStream inputStream) {
        return new QlhWorkbook().setWorkbook(QlhException.runtime(() -> WorkbookFactory.create(inputStream)));
    }

    public static String getCellFormatValue(Cell cell) {
        if (Objects.nonNull(cell)) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case BLANK:
                    break;
                default:
                    throw new RuntimeException("不支持的单元格类型:" + cell.getCellType());
            }
        }
        return "";
    }

    public static void createCell(HSSFRow row, int idx, Object value, CellStyle cellStyle) {
        final HSSFCell cell = row.createCell(idx);
        cell.setCellValue(value.toString());
        cell.setCellStyle(cellStyle);
    }

}


