package org.example.sutochnikweb.controllers;

import org.apache.poi.ss.usermodel.*;
import org.example.sutochnikweb.models.ExcelPreview;
import org.example.sutochnikweb.models.HeightRange;
import org.example.sutochnikweb.models.OperationStats;
import org.example.sutochnikweb.services.SimpleExcelService;
import org.example.sutochnikweb.services.SVGService;
import org.example.sutochnikweb.services.TimeService;
import org.example.sutochnikweb.services.UtilService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class MainController {

    private static final String UPLOAD_DIR = "uploads/";
    private SimpleExcelService excelService;
    private SVGService svgService;
    private TimeService timeService;
    private UtilService utilService;
    private byte[] excelBytes;

    public MainController(SimpleExcelService excelService, SVGService svgService, TimeService timeService, UtilService utilService) {
        this.excelService = excelService;
        this.svgService = svgService;
        this.timeService = timeService;
        this.utilService = utilService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Пожалуйста, выберите файл для загрузки");
            return "upload";
        }

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            Path tempFilePath = Files.createTempFile("temp", file.getOriginalFilename());
            Files.write(tempFilePath, file.getBytes());

            File tempFile = tempFilePath.toFile();

            Map<String, HeightRange> map = svgService.parseSvg(tempFile);
            Workbook excelFile = excelService.convertToExcel(map);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            excelFile.write(bos);
            excelBytes = bos.toByteArray();

            List<String> headers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();
            List<List<String>> analyticRows = new ArrayList<>();  // New list for analytic rows

            // Extract headers and rows from the workbook
            Sheet sheet = excelFile.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            headerRow.forEach(cell -> headers.add(cell.getStringCellValue()));

            Map<String, Map<String, OperationStats>> operationStatsMap = new HashMap<>();  // For calculating analytic rows

            sheet.forEach(row -> {
                if (row.getRowNum() > 0) { // Skip the header row
                    List<String> rowData = new ArrayList<>();
                    String rowKey = utilService.getStringCellValue(row.getCell(0));
                    String operationType = utilService.getStringCellValue(row.getCell(2));

                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        if (i == 1) { // Second column
                            // Parse as integer
                            try {
                                rowData.add(String.valueOf(utilService.getNumericCellValue(row.getCell(i))));
                            } catch (Exception e) {
                                rowData.add("Invalid number");
                            }
                        } else if (i == 5) { // Duration column
                            // Parse the time string
                            rowData.add(utilService.getTimeCellValue(row.getCell(i)));
                        } else {
                            rowData.add(utilService.getStringCellValue(row.getCell(i)));
                        }
                    }
                    rows.add(rowData);

                    // Calculate operation stats for analytic rows
                    operationStatsMap.putIfAbsent(rowKey, new HashMap<>());
                    operationStatsMap.get(rowKey).putIfAbsent(operationType, new OperationStats());
                    operationStatsMap.get(rowKey).get(operationType).incrementCount();
                    operationStatsMap.get(rowKey).get(operationType).addDuration(utilService.parseDuration(utilService.getTimeCellValue(row.getCell(5))));
                }
            });



            for (Map.Entry<String, Map<String, OperationStats>> entry : operationStatsMap.entrySet()) {
                String rowKey = entry.getKey();
                int count = 0;
                for (Map.Entry<String, OperationStats> operationEntry : entry.getValue().entrySet()) {
                    List<String> analyticRow = new ArrayList<>();
                    OperationStats stats = operationEntry.getValue();

                    analyticRow.add(rowKey);
                    analyticRow.add(String.valueOf(count++));
                    analyticRow.add(operationEntry.getKey());
                    analyticRow.add(String.valueOf(stats.getCount()));
                    analyticRow.add(utilService.formatDuration(stats.getTotalDuration()));

                    analyticRows.add(analyticRow);
                }
            }

            List<String> analyticHeaders = Arrays.asList(
                    "Строка", "Номер п/п", "Операция", "Количество операций", "Общая продолжительность"
            );
            model.addAttribute("excelPreview", new ExcelPreview(headers, rows, analyticRows, analyticHeaders));
            return "preview";
        } catch (IOException e) {
            model.addAttribute("message", "Не удалось загрузить файл");
            return "upload";
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile() {
        if (excelBytes == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=generated_excel.xlsx");

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }


}