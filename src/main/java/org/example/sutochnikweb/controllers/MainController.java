package org.example.sutochnikweb.controllers;

import org.apache.poi.ss.usermodel.*;
import org.example.sutochnikweb.models.Action;
import org.example.sutochnikweb.models.HeightRange;
import org.example.sutochnikweb.services.SimpleExcelService;
import org.example.sutochnikweb.services.SVGService;
import org.example.sutochnikweb.services.TimeService;
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
    private byte[] excelBytes;

    public MainController(SimpleExcelService excelService, SVGService svgService, TimeService timeService) {
        this.excelService = excelService;
        this.svgService = svgService;
        this.timeService = timeService;
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
            byte[] excelBytes = bos.toByteArray();

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
                    String rowKey = getStringCellValue(row.getCell(0));
                    String operationType = getStringCellValue(row.getCell(2));

                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        if (i == 1) { // Second column
                            // Parse as integer
                            try {
                                rowData.add(String.valueOf(getNumericCellValue(row.getCell(i))));
                            } catch (Exception e) {
                                rowData.add("Invalid number");
                            }
                        } else if (i == 5) { // Duration column
                            // Parse the time string
                            rowData.add(getTimeCellValue(row.getCell(i)));
                        } else {
                            rowData.add(getStringCellValue(row.getCell(i)));
                        }
                    }
                    rows.add(rowData);

                    // Calculate operation stats for analytic rows
                    operationStatsMap.putIfAbsent(rowKey, new HashMap<>());
                    operationStatsMap.get(rowKey).putIfAbsent(operationType, new OperationStats());
                    operationStatsMap.get(rowKey).get(operationType).incrementCount();
                    operationStatsMap.get(rowKey).get(operationType).addDuration(parseDuration(getTimeCellValue(row.getCell(5))));
                }
            });

            // Create analytic rows
            int count = 0;
            for (Map.Entry<String, Map<String, OperationStats>> entry : operationStatsMap.entrySet()) {
                String rowKey = entry.getKey();
                for (Map.Entry<String, OperationStats> operationEntry : entry.getValue().entrySet()) {
                    List<String> analyticRow = new ArrayList<>();
                    OperationStats stats = operationEntry.getValue();

                    analyticRow.add(rowKey);
                    analyticRow.add(String.valueOf(count++));
                    analyticRow.add(operationEntry.getKey());
                    analyticRow.add(String.valueOf(stats.getCount()));
                    analyticRow.add(formatDuration(stats.getTotalDuration()));

                    analyticRows.add(analyticRow);
                }
            }
//            int operationNumber = 1;
//            for (Map.Entry<String, HeightRange> entry : map.entrySet()) {
//                String key = entry.getKey();
//                HeightRange heightRange = entry.getValue();
//                Map<String, Integer> operationCounts = new HashMap<>();
//                Map<String, Long> operationDurations = new HashMap<>();
//
//                for (Action action : heightRange.getActions()) {
//                    String operation = action.getType().getDescription();
//                    operationCounts.put(operation, operationCounts.getOrDefault(operation, 0) + 1);
//                    operationDurations.put(operation, operationDurations.getOrDefault(operation, 0L) + action.getDuration());
//                }
//
//                for (String operation : operationCounts.keySet()) {
//                    List<String> rowData = new ArrayList<>();
//                    rowData.add(key);
//                    rowData.add(String.valueOf(operationNumber++));
//                    rowData.add(operation);
//                    rowData.add(String.valueOf(operationCounts.get(operation)));
//                    rowData.add(timeService.convertMillisToTime(Math.toIntExact(operationDurations.get(operation))));
//                    analyticRows.add(rowData);
//                }
//            }

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

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Cannot get a NUMERIC value from a STRING cell");
                }
            default:
                throw new IllegalStateException("Cannot get a NUMERIC value from this cell type");
        }
    }

    private String getTimeCellValue(Cell cell) {
        if (cell == null) return "00:00:00";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return formatDuration(cell.getNumericCellValue() * 86400);  // Convert Excel time to seconds
            default:
                return "00:00:00";
        }
    }

    private double parseDuration(String time) {
        String[] parts = time.split(":");
        if (parts.length != 3) return 0;
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    private String formatDuration(double durationInSeconds) {
        int hours = (int) (durationInSeconds / 3600);
        int minutes = (int) ((durationInSeconds % 3600) / 60);
        int seconds = (int) (durationInSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static class OperationStats {
        private int count;
        private double totalDuration;

        public OperationStats() {
            this.count = 0;
            this.totalDuration = 0;
        }

        public void incrementCount() {
            this.count++;
        }

        public void addDuration(double duration) {
            this.totalDuration += duration;
        }

        public int getCount() {
            return count;
        }

        public double getTotalDuration() {
            return totalDuration;
        }
    }


    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile() {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(excelBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "workbook.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelBytes.length)
                .body(resource);
    }

    public static class ExcelPreview {
        private List<String> headers;
        private List<List<String>> rows;
        private List<List<String>> analyticRows;
        private List<String> analyticHeaders;// New field

        public ExcelPreview(List<String> headers, List<List<String>> rows, List<List<String>> analyticRows, List<String> analyticHeaders) {
            this.headers = headers;
            this.rows = rows;
            this.analyticRows = analyticRows;
            this.analyticHeaders = analyticHeaders;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        public List<List<String>> getAnalyticRows() {
            return analyticRows;
        }

        public List<String> getAnalyticHeaders() {
            return analyticHeaders;
        }
    }

}