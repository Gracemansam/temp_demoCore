package com.lamicore.coredemo.upload;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public String pluginsFolder() {
        return "path/to/plugins/folder";
    }

    @Bean
    public PluginManager pluginManager() {
        return new DefaultPluginManager();
    }
}

