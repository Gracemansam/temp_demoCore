package com.lamicore.coredemo.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Component
public class ModuleClassloader {

    @Value("${plugins.folder}")
    private String pluginsFolder;

    private List<JarFile> jars;

    public void init() {
        File[] jarFiles = new File(pluginsFolder).listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            jars = Collections.emptyList();
            return;
        }

        this.jars = new ArrayList<>();

        for (File jarFile : jarFiles) {
            try {
                JarFile jar = new JarFile(jarFile);
                jars.add(jar);

                // Load and run main class from JAR
                String mainClassName = getMainClassName(jar);
                if (mainClassName != null) {
                    runMainClass(jar, mainClassName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMainClassName(JarFile jar) throws IOException {
        return jar.getManifest().getMainAttributes().getValue("Main-Class");
    }

    private void runMainClass(JarFile jar, String mainClassName) {
        try {
            URL[] urls = {new URL("jar:file:" + jar.getName() + "!/")};
            URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
            Class<?> mainClass = Class.forName(mainClassName, true, classLoader);
            mainClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to upload a JAR file to the plugins folder
    public void uploadJar(InputStream jarInputStream, String fileName) throws IOException {
        String targetFilePath = pluginsFolder + File.separator + fileName;
        try (OutputStream outputStream = new FileOutputStream(targetFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        // Initialize after uploading
        init();
    }

    // Connect to the core database
    private Connection connectToCoreDatabase() throws Exception {
        // Replace with your core database connection details
        String url = "jdbc:mysql://localhost:3306/coredemo";
        String username = "postgres";
        String password = "1234";
        return DriverManager.getConnection(url, username, password);
    }
}
