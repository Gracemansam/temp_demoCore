package com.lamicore.coredemo.upload;

import com.lamicore.coredemo.upload.PluginManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/plugins")
public class PluginManagementController {

    @Autowired
    private PluginManagementService pluginManagementService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAndStartPlugin(@RequestParam("file") MultipartFile file) throws IOException {
        String pluginId = pluginManagementService.uploadAndStartPlugin(file);
        if (pluginId != null) {
            return ResponseEntity.ok("Plugin uploaded and started successfully. Plugin ID: " + pluginId);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload and start plugin");
        }
    }

    @PostMapping("/{pluginId}/stop")
    public ResponseEntity<String> stopPlugin(@PathVariable String pluginId) {
        pluginManagementService.stopPlugin(pluginId);
        return ResponseEntity.ok("Plugin stopped successfully");
    }

    @PostMapping("/{pluginId}/unload")
    public ResponseEntity<String> unloadPlugin(@PathVariable String pluginId) {
        pluginManagementService.unloadPlugin(pluginId);
        return ResponseEntity.ok("Plugin unloaded successfully");
    }
}
