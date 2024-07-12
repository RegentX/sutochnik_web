package org.example.sutochnikweb.controllers;

import org.apache.poi.ss.usermodel.Workbook;
import org.example.sutochnikweb.models.HeightRange;
import org.example.sutochnikweb.services.ExcelService;
import org.example.sutochnikweb.services.SVGService;
import org.example.sutochnikweb.services.SimpleExcelService;
import org.springframework.core.annotation.AliasFor;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Controller
public class MainController {

    private static final String UPLOAD_DIR = "uploads/";
    //Можно заменить на ExcelService, чтобы был красивый excel файл с подсчётами
    SimpleExcelService excelService;
    SVGService svgService;

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
    public ResponseEntity<InputStreamResource> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InputStreamResource(new ByteArrayInputStream("Пожалуйста, выберите файл для загрузки".getBytes())));
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

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(excelBytes));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "workbook.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelBytes.length)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream("Не удалось загрузить файл".getBytes())));
        }
    }
}