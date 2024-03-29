package com.lamicore.coredemo.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jar")
public class JarController {

    @Value("${upload.directory}")
    private String uploadDirectory;

    public JarController(@Value("${upload.directory}") String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        try {
            Files.createDirectories(Paths.get(uploadDirectory));
        } catch (IOException e) {
            e.printStackTrace();
            // Handle directory creation failure
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJarFile(@RequestParam("file") MultipartFile file) {
        try {
            // Save the uploaded JAR file to the specified directory
            File jarFile = new File(uploadDirectory + File.separator + Objects.requireNonNull(file.getOriginalFilename()));
            FileOutputStream fos = new FileOutputStream(jarFile);
            fos.write(file.getBytes());
            fos.close();

            // Run the JAR file
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath());
            pb.directory(new File(uploadDirectory));
            Process process = pb.start();

            // Wait for the process to finish
            int exitCode = process.waitFor();

            // Check if the process ran successfully
            if (exitCode == 0) {
                return ResponseEntity.ok("JAR file executed successfully");
            } else {
                String errorOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to execute JAR file: " + errorOutput);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        // Load file as Resource
        Resource resource = new org.springframework.core.io.FileSystemResource(uploadDirectory + File.separator + fileName);

        // Try to determine file's content type
        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(uploadDirectory + File.separator + fileName));
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
