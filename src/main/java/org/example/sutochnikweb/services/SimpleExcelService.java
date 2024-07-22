package org.example.sutochnikweb.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.sutochnikweb.models.Action;
import org.example.sutochnikweb.models.HeightRange;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
@Service
public class SimpleExcelService {
    private TimeService timeService;

    public SimpleExcelService(TimeService timeService) {
        this.timeService = timeService;
    }
    public Workbook convertToExcel(Map<String, HeightRange> map){
        SVGService svgParsing = new SVGService();
        // Создание нового Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        String[] fields = {"Строка", "Номер п/п", "Операция", "Время начала", "Время конца", "Продолжительность", "Дополнительная информация"};

        Row headerRow = sheet.createRow(0);

        // Заполнение первой строки данными из массива fields, начиная со второй ячейки
        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields[i]);
        }
        // Переменная для отслеживания текущей строки в Excel
        int rowNum = 1;

        // Проходим по карте и заполняем строки данными
        for (Map.Entry<String, HeightRange> entry : map.entrySet()) {
            String key = entry.getKey();
            HeightRange heightRange = entry.getValue();
            List<Action> actions = heightRange.getActions();

                for (Action action : actions) {
                    Row row = sheet.createRow(rowNum++);

                    Cell cell = row.createCell(0);
                    cell.setCellValue(key);
                    row.createCell(1).setCellValue(action.getOperationNumber());
                    row.createCell(2).setCellValue(action.getType().name());
                    row.createCell(3).setCellValue(timeService.convertMillisToTime(action.getStart()));
                    row.createCell(4).setCellValue(timeService.convertMillisToTime(action.getEnd()));
                    row.createCell(5).setCellValue(timeService.convertMillisToTime(action.getDuration()));
                    if (action.getOtherNumInfo() != 0) {
                        row.createCell(6).setCellValue(action.getOtherNumInfo());
                    } else
                        row.createCell(6).setCellValue(action.getOtherInfo());

                }
        }

        // Сохранение workbook в файл
        try (FileOutputStream fileOut = new FileOutputStream("simpleworkbook.xlsx")) {
            workbook.write(fileOut);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Закрытие workbook
        /*try {
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return workbook;
    }
}
