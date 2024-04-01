package com.lamicore.coredemo.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.loader.launch.JarLauncher;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.jar.JarEntry;
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
        //  init();
    }

    // Connect to the core database
    private Connection connectToCoreDatabase() throws Exception {
        // Replace with your core database connection details
        String url = "jdbc:mysql://localhost:3306/coredemo";
        String username = "postgres";
        String password = "1234";
        return DriverManager.getConnection(url, username, password);
    }

    public static void launchSpringBootApplication(String pathToJar) throws Exception {
        Map<String, Class<?>> result = loadClassesFromJar(pathToJar);

        // Logging to check the content of the result map
        System.out.println("Result map: " + result);

        // Create JarFileArchive(File) object, needed for JarLauncher.
        final Class<?> jarFileArchiveClass = result.get("org.springframework.boot.loader.archive.JarFileArchive");
        if (jarFileArchiveClass == null) {
            throw new ClassNotFoundException("JarFileArchive class not found in the result map.");
        }

        final Constructor<?> jarFileArchiveConstructor = jarFileArchiveClass.getConstructor(File.class);
        final Object jarFileArchive = jarFileArchiveConstructor.newInstance(new File(pathToJar));

        // Create JarLauncher object using JarLauncher(Archive) constructor.
        final Class<?> archiveClass = result.get("org.springframework.boot.loader.archive.Archive");
        if (archiveClass == null) {
            throw new ClassNotFoundException("Archive class not found in the result map.");
        }

        final Constructor<?> jarLauncherConstructor = archiveClass.getDeclaredConstructor(archiveClass);
        jarLauncherConstructor.setAccessible(true);
        final Object jarLauncher = jarLauncherConstructor.newInstance(jarFileArchive);

        // Invoke JarLauncher#launch(String[]) method.
        final Class<?> launcherClass = result.get("org.springframework.boot.loader.Launcher");
        if (launcherClass == null) {
            throw new ClassNotFoundException("Launcher class not found in the result map.");
        }

        final Method launchMethod = launcherClass.getDeclaredMethod("launch", String[].class);
        launchMethod.setAccessible(true);
        launchMethod.invoke(jarLauncher, new Object[]{new String[0]});
    }

    //    public static Map<String, Class<?>> loadClassesFromJar(final String pathToJar) throws IOException, ClassNotFoundException {
//        // Class name to Class object mapping.
//        final Map<String, Class<?>> result = new HashMap<>();
//
//        try (JarFile jarFile = new JarFile(pathToJar)) {
//            Enumeration<JarEntry> jarEntryEnum = jarFile.entries();
//
//            while (jarEntryEnum.hasMoreElements()) {
//                JarEntry jarEntry = jarEntryEnum.nextElement();
//                System.out.println(jarEntry.getName()); // Print out the entry name
//                // Load the class if it matches the desired package and ends with ".class"
//                if (jarEntry.getName().startsWith("org/springframework/boot") && jarEntry.getName().endsWith(".class")) {
//                    String className = jarEntry.getName().replace('/', '.').substring(0, jarEntry.getName().length() - ".class".length());
//                    try {
//                        final Class<?> loadedClass = Class.forName(className);
//                        result.put(loadedClass.getName(), loadedClass);
//                    } catch (final ClassNotFoundException ex) {
//                        // Handle class not found exception
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//    public static Map<String, Class<?>> loadClassesFromJar(final String pathToJar) throws IOException, ClassNotFoundException {
//        // Class name to Class object mapping.
//        final Map<String, Class<?>> result = new HashMap<>();
//
//        try (JarFile jarFile = new JarFile(pathToJar)) {
//            Enumeration<JarEntry> jarEntryEnum = jarFile.entries();
//
//            while (jarEntryEnum.hasMoreElements()) {
//                JarEntry jarEntry = jarEntryEnum.nextElement();
//                // Load the class if it matches the desired package and ends with ".class"
//                if (jarEntry.getName().startsWith("org/springframework/boot") && jarEntry.getName().endsWith(".class")) {
//                    String className = jarEntry.getName().replace('/', '.').substring(0, jarEntry.getName().length() - ".class".length());
//                    try {
//                        final Class<?> loadedClass = Class.forName(className);
//                        result.put(loadedClass.getName(), loadedClass);
//                    } catch (final ClassNotFoundException ex) {
//                        ex.printStackTrace();
//                        // Handle class not found exception
//                    }
//                }
//            }
//        }
//
//        return result;
//    }


    public static Map<String, Class<?>> loadClassesFromJar(final String pathToJar) throws IOException, ClassNotFoundException {
        // Class name to Class object mapping.
        final Map<String, Class<?>> result = new HashMap<>();

        try (JarFile jarFile = new JarFile(pathToJar)) {
            Enumeration<JarEntry> jarEntryEnum = jarFile.entries();

            while (jarEntryEnum.hasMoreElements()) {
                JarEntry jarEntry = jarEntryEnum.nextElement();
                // Load the class if it matches the desired package and ends with ".class"
                if (jarEntry.getName().startsWith("org/springframework/boot") && jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName().replace('/', '.').substring(0, jarEntry.getName().length() - ".class".length());
                    try {
                        // Load the class using a class loader
                        URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + pathToJar + "!/")});
                        final Class<?> loadedClass = classLoader.loadClass(className);
                        result.put(loadedClass.getName(), loadedClass);
                    } catch (final ClassNotFoundException ex) {
                        ex.printStackTrace();
                        // Handle class not found exception
                    }
                }
            }
        }

        return result;
    }
    public static void loadJar(final String pathToJar) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Class name to Class object mapping.
        final Map<String, Class<?>> result = new HashMap<>();

        final JarFile jarFile = new JarFile(pathToJar);
        final Enumeration<JarEntry> jarEntryEnum = jarFile.entries();

        final URL[] urls = {new URL("jar:file:" + pathToJar + "!/")};
        final URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls);
        while (jarEntryEnum.hasMoreElements()) {
            final JarEntry jarEntry = jarEntryEnum.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                String className = jarEntry.getName().replace('/', '.').replaceAll(".class$", "");
                try {
                    // Load the class
                    final Class<?> loadedClass = urlClassLoader.loadClass(className);
                    // Add the loaded class to the result map
                    result.put(loadedClass.getName(), loadedClass);
                    // Debug: Print out the loaded class name
                    System.out.println("Loaded class: " + loadedClass.getName());
                } catch (final ClassNotFoundException ex) {
                    // Error handling: Print out any class loading errors
                    System.err.println("Error loading class: " + className);
                    ex.printStackTrace();
                }
            }
        }

        jarFile.close();

        // Debug: Print out the result map
        System.out.println("Result map: " + result);

        // Check if the expected class is loaded
        final Class<?> launcherClass = result.get("org.springframework.boot.loader.Launcher");
        if (launcherClass == null) {
            // Handle the case where the class is not found
            System.err.println("Error: Launcher class not found in the result map.");
            return;
        }

        // Invoke the launch method
        try {
            final Method launchMethod = launcherClass.getDeclaredMethod("launch", String[].class);
            launchMethod.setAccessible(true);
            launchMethod.invoke(null, new Object[]{new String[0]});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}

