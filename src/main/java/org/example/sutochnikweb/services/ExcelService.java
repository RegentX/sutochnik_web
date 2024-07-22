package org.example.sutochnikweb.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.sutochnikweb.models.Action;
import org.example.sutochnikweb.models.HeightRange;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
@Service
public class ExcelService {
    private TimeService timeService;

    public ExcelService(TimeService timeService) {
        this.timeService = timeService;
    }

    public Workbook convertToExcel(Map<String, HeightRange> map){
        SVGService svgParsing = new SVGService();
        // Создание нового Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        String[] fields = {"Номер п/п", "Операция", "Время начала", "Время конца", "Продолжительность", "Дополнительная информация"};

        Row headerRow = sheet.createRow(0);

        // Заполнение первой строки данными из массива fields, начиная со второй ячейки
        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(fields[i]);
        }

        // Создание стиля для выравнивания по верхнему краю
        CellStyle topAlignedStyle = workbook.createCellStyle();
        topAlignedStyle.setVerticalAlignment(VerticalAlignment.TOP);

        // Переменная для отслеживания текущей строки в Excel
        int rowNum = 1;

        // Проходим по карте и заполняем строки данными
        for (Map.Entry<String, HeightRange> entry : map.entrySet()) {
            String key = entry.getKey();
            HeightRange heightRange = entry.getValue();
            List<Action> actions = heightRange.getActions();

            int startRow = rowNum;  // Начальная строка для объединения ячеек
            int nonEmptyRows = 0;  // Количество непустых строк
            long totalDuration = 0;  // Суммарная продолжительность

            if (actions.isEmpty()) {
                // Создаем строку с ключом и пустыми ячейками, если нет действий
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(key);
                cell.setCellStyle(topAlignedStyle);
                for (int i = 1; i <= fields.length; i++) {
                    row.createCell(i).setCellValue("");
                }
            } else {
                for (Action action : actions) {
                    Row row = sheet.createRow(rowNum++);

                    Cell cell = row.createCell(0);
                    cell.setCellValue(key);
                    cell.setCellStyle(topAlignedStyle);
                    row.createCell(1).setCellValue(action.getOperationNumber());
                    row.createCell(2).setCellValue(action.getType().name());
                    row.createCell(3).setCellValue(timeService.convertMillisToTime(action.getStart()));
                    row.createCell(4).setCellValue(timeService.convertMillisToTime(action.getEnd()));
                    row.createCell(5).setCellValue(timeService.convertMillisToTime(action.getDuration()));
                    if (action.getOtherNumInfo() != 0) {
                        row.createCell(6).setCellValue(action.getOtherNumInfo());
                    } else
                        row.createCell(6).setCellValue(action.getOtherInfo());

                    nonEmptyRows++;
                    totalDuration += action.getDuration();  // Подсчитываем общую продолжительность
                }
            }
            int endRow = rowNum - 1;  // Конечная строка для объединения ячеек
            if (startRow < endRow) {
                // Объединяем ячейки с одинаковыми ключами
                sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0));
            }

            // Добавляем строку с итоговыми значениями
            Row summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(7).setCellValue("Итого количество:");
            summaryRow.createCell(8).setCellValue(nonEmptyRows);  // Количество непустых строк
            summaryRow.createCell(10).setCellValue("Итого время:");
            summaryRow.createCell(11).setCellValue(totalDuration);  // Суммарная продолжительность
        }

        // Автоматическая настройка ширины столбцов
        for (int i = 0; i < fields.length + 6; i++) {  // fields.length + 6 включает все дополнительные столбцы до 11-го
            sheet.autoSizeColumn(i);
        }

        // Сохранение workbook в файл
        try (FileOutputStream fileOut = new FileOutputStream("workbook.xlsx")) {
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
