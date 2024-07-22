package org.example.sutochnikweb.controllers;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.example.sutochnikweb.models.HeightRange;
import org.example.sutochnikweb.services.SimpleExcelService;
import org.example.sutochnikweb.services.SVGService;
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
    private byte[] excelBytes;

    public MainController(SimpleExcelService excelService, SVGService svgService) {
        this.excelService = excelService;
        this.svgService = svgService;
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

            // Extract headers and rows from the workbook
            Sheet sheet = excelFile.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            headerRow.forEach(cell -> headers.add(cell.getStringCellValue()));

            sheet.forEach(row -> {
                if (row.getRowNum() > 0) { // Skip the header row
                    List<String> rowData = new ArrayList<>();
                    row.forEach(cell -> rowData.add(cell.toString()));
                    rows.add(rowData);
                }
            });

            model.addAttribute("excelPreview", new ExcelPreview(headers, rows));
            return "preview";
        } catch (IOException e) {
            model.addAttribute("message", "Не удалось загрузить файл");
            return "upload";
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

        public ExcelPreview(List<String> headers, List<List<String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }
    }
}