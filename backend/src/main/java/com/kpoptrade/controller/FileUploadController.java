package com.kpoptrade.controller;

import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    @Value("${upload.image-max-size}")
    private long maxSize;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.prefix}")
    private String uploadPrefix;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    @PostMapping("/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return R.error("请选择要上传的图片");
        }
        if (file.getSize() > maxSize) {
            return R.error("图片太大，最大支持5MB");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(originalFilename);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            return R.error("只支持 jpg、jpeg、png、webp 格式");
        }
        String filename = UUID.randomUUID().toString() + "." + ext;
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path targetPath = uploadDir.resolve(filename);
        file.transferTo(targetPath.toFile());
        String imageUrl = uploadPrefix.endsWith("/") ? uploadPrefix + filename : uploadPrefix + "/" + filename;
        return R.ok(imageUrl);
    }
}
