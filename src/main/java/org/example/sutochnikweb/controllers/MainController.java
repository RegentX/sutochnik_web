package org.example.sutochnikweb.controllers;

import org.apache.poi.ss.usermodel.Workbook;
import org.example.sutochnikweb.models.HeightRange;
import org.example.sutochnikweb.services.ExcelService;
import org.example.sutochnikweb.services.SVGService;
import org.springframework.core.annotation.AliasFor;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Controller
public class MainController {

    private static final String UPLOAD_DIR = "uploads/";

    ExcelService excelService;
    SVGService svgService;

    public MainController(ExcelService excelService, SVGService svgService) {
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
    public ResponseEntity<byte[]> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Please select a file to upload".getBytes());
        }

        try {
            // Ensure the upload directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Save the file locally
            Path tempFilePath = Files.createTempFile("temp", file.getOriginalFilename());
            Files.write(tempFilePath, file.getBytes());

            // Create a File object from the temporary file path
            File tempFile = tempFilePath.toFile();

            // Parse SVG and convert to Excel
            Map<String, HeightRange> map = svgService.parseSvg(tempFile);
            Workbook excelFile = excelService.convertToExcel(map);

            // Create a temporary file for Excel
            Path excelPath = Files.createTempFile("temp", ".xlsx");
            try (OutputStream os = Files.newOutputStream(excelPath)) {
                excelFile.write(os); // Write Excel content to temporary file
            }

            // Load Excel content as byte array
            byte[] excelContent = Files.readAllBytes(excelPath);

            // Set headers for the file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", file.getOriginalFilename());

            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to upload " + file.getOriginalFilename()).getBytes());
        }
    }
}