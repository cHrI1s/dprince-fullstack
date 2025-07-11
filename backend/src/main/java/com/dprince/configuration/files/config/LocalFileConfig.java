package com.dprince.configuration.files.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app-files")
public class LocalFileConfig {
    private String uploadDir;
    private String backupDir;
    private String projectPath;
}
