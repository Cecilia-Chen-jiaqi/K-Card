package com.kpoptrade.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadDirectoryInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(UploadDirectoryInitializer.class);
    private final UploadProperties uploadProperties;

    public UploadDirectoryInitializer(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Path dir = Paths.get(uploadProperties.getPath()).toAbsolutePath().normalize();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                logger.info("Created upload directory: {}", dir);
            } else {
                logger.info("Upload directory ready: {}", dir);
            }
            uploadProperties.setPath(dir.toString() + File.separator);
        } catch (Exception ex) {
            logger.error("Failed to initialize upload directory: {}", uploadProperties.getPath(), ex);
        }
    }
}
