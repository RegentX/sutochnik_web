package org.example.sutochnikweb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class MainController {

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "upload";
        }

        try {
            // Ensure the upload directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Save the file locally
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, file.getBytes());

            // Process the SVG file (you can add your custom processing logic here)
            // Example: Print file size
            model.addAttribute("message", "Successfully uploaded " + file.getOriginalFilename());
            model.addAttribute("fileSize", file.getSize());
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to upload " + file.getOriginalFilename());
            System.out.print(e.toString());
        }

        return "upload";
    }
}
