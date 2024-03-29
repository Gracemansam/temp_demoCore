package com.lamicore.coredemo.upload;

import jakarta.servlet.ServletContext;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PluginManagementService {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ServletContext servletContext;

    private static final String PLUGIN_FOLDER = "/path/to/plugin/folder/";

    public String uploadAndStartPlugin(MultipartFile file) throws IOException {
        // Get the absolute path to the plugin folder using ServletContext
        String pluginFolderPath = servletContext.getRealPath("/WEB-INF/plugins/");

        // Construct the full path to save the uploaded file
        String fileName = file.getOriginalFilename();
        Path pluginPath = Paths.get(pluginFolderPath, fileName);

        // Save the uploaded file to the plugin folder
        try (OutputStream os = Files.newOutputStream(pluginPath)) {
            os.write(file.getBytes());
        }

        // Load and start the plugin
        String pluginId = pluginManager.loadPlugin(pluginPath);
        pluginManager.startPlugin(pluginId);

        return pluginId;
    }



    public void stopPlugin(String pluginId) {
        pluginManager.stopPlugin(pluginId);
    }

    public void unloadPlugin(String pluginId) {
        pluginManager.unloadPlugin(pluginId);
    }
}
