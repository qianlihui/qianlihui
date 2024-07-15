package com.qlh.base.excel;


import com.qlh.base.QlhJsonUtils;
import com.qlh.base.QlhMap;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
public class QlhWorkbook {

    private Workbook workbook;

    public List<QlhMap> loadData() {
        return loadData(0);
    }

    /**
     * 默认第一行作为header
     *
     * @param sheetIndex
     * @return
     */
    public List<QlhMap> loadData(int sheetIndex) {

        Sheet sheet = workbook.getSheetAt(sheetIndex);
        Row header = sheet.getRow(0);

        List<QlhMap> list = new ArrayList<>(sheet.getLastRowNum());
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // getLastRowNum 0开始的下标
            QlhMap data = new QlhMap();
            Row dataRow = sheet.getRow(i);
            for (int c = 0; c < header.getLastCellNum(); c++) { // getLastCellNum 1开始的下标
                data.put(QlhExcel.getCellFormatValue(header.getCell(c)).trim(), QlhExcel.getCellFormatValue(dataRow.getCell(c)).trim());
            }
            list.add(data);
        }
        return list;
    }


    public static void main(String args[]) throws FileNotFoundException {
        QlhWorkbook workbook = QlhExcel.load(new FileInputStream("d:/调拨申请单导入模板.xlsx"));
        System.out.println(QlhJsonUtils.toFormattedJson(workbook.loadData()));
    }
}
