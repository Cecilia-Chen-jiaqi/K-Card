package com.kpoptrade.controller;

import com.kpoptrade.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${upload.image-max-size}")
    private long maxSize;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.prefix}")
    private String uploadPrefix;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    @PostMapping("/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error("请选择有效图片文件");
        }
        if (file.getSize() > maxSize) {
            return R.error("图片太大，最大支持5MB");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!StringUtils.hasText(originalFilename)) {
            return R.error("请选择有效图片文件");
        }
        String ext = StringUtils.getFilenameExtension(originalFilename);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            return R.error("只支持 jpg、jpeg、png、webp 格式");
        }
        String filename = UUID.randomUUID().toString() + "." + ext.toLowerCase();
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    logger.error("无法创建上传目录：{}", uploadPath);
                    return R.error("服务器图片存储失败，请稍后重试");
                }
            }
            File targetFile = new File(uploadDir, filename);
            file.transferTo(targetFile);
            String imageUrl = uploadPrefix.endsWith("/") ? uploadPrefix + filename : uploadPrefix + "/" + filename;
            return R.ok(imageUrl);
        } catch (IOException e) {
            logger.error("图片上传写入失败", e);
            return R.error("服务器图片存储失败，请稍后重试");
        } catch (Exception e) {
            logger.error("图片上传失败", e);
            return R.error("服务器图片上传失败，请稍后重试");
        }
    }
}
