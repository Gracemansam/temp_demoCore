package com.lamicore.coredemo.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@RestController

public class UploadController {



    @Autowired
    private ModuleClassloader moduleClassloader;

    private static final String MODULES_DIR = "modules/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please select a file to upload", HttpStatus.BAD_REQUEST);
        }

        try {
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            moduleClassloader.uploadJar(inputStream, fileName);
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/api/modules/install")
    public String installModule(@RequestParam("file") MultipartFile moduleFile) throws IOException {
        if (moduleFile == null || moduleFile.isEmpty()) {
            return "No file uploaded";
        }

        // Ensure that the directory for saving modules exists
        File modulesDir = new File(MODULES_DIR);
        if (!modulesDir.exists()) {
            modulesDir.mkdirs(); // Create the directory and any necessary parent directories
        }

        // Construct the file path for saving the uploaded JAR file
        String originalFilename = StringUtils.cleanPath(moduleFile.getOriginalFilename());
        String moduleFileName = MODULES_DIR + originalFilename;
        System.out.println("Module file name: " + moduleFileName);

        // Save the uploaded JAR file to the specified directory
        try (FileOutputStream fos = new FileOutputStream(moduleFileName)) {
            fos.write(moduleFile.getBytes());
            System.out.println("Module file saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to save module file";
        }

        // Load and run the module
        try {
            File file = new File(moduleFileName);
            System.out.println("Module file path: " + file.getAbsolutePath());
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()});
            System.out.println("Class loader: " + classLoader);
            if(classLoader == null){
                return "Failed to load module";
            }


            File packageDirectory = new File(classLoader.getResource("com/moduledemo/user/ModuleController").getFile());
            System.out.println("Package directory: " + packageDirectory.getAbsolutePath());

            if (!packageDirectory.exists()) {
                return "No package found in module";
            }
            String[] classNames = packageDirectory.list();
            System.out.println("Class names: " + Arrays.toString(classNames));

            if (classNames == null) {
                return "No classes found in package";
            }

            for (String className : classNames) {
                if (className.endsWith(".class")) {
                    String fullClassName = "com.moduledemo.user.ModuleController." + className.substring(0, className.lastIndexOf('.'));
                    Class<?> clazz = classLoader.loadClass(fullClassName);
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    // Invoke methods of the class
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        method.invoke(instance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to load module";
        }

        return "Module installed and executed successfully!";
    }

}