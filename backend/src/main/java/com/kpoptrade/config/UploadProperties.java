package com.kpoptrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private long imageMaxSize = 5242880;
    private String path = "./upload/";
    private String prefix = "/upload/";
}
