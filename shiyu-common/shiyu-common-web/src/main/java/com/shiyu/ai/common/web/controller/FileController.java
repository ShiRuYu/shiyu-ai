package com.shiyu.ai.common.web.controller;

import com.shiyu.ai.common.core.api.Result;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Tag(name = "File", description = "File")
@RestController
@RequestMapping("/upload")
public class FileController {

    @Value("${shiyu.upload.path:./uploads}")
    private String uploadPath;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
            log.info("文件上传目录已初始化: {}", uploadDir);
        } catch (IOException e) {
            log.error("初始化文件上传目录失败: {}", uploadDir, e);
        }
    }

    @Operation(summary = "Upload File")
    @PostMapping
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + ext;
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath);

            String url = "/uploads/" + filename;
            log.info("文件上传成功: {} -> {}", originalFilename, url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败");
        }
    }
}
