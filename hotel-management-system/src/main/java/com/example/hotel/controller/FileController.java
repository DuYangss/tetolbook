package com.example.hotel.controller;

import com.example.hotel.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @PostMapping("/image")
    public ApiResponse<?> uploadImage(@RequestParam("file") MultipartFile file) {
        logger.info("开始处理文件上传请求");
        logger.info("文件名: {}", file.getOriginalFilename());
        logger.info("文件大小: {} bytes", file.getSize());
        logger.info("文件类型: {}", file.getContentType());
        
        if (file.isEmpty()) {
            logger.error("上传的文件为空");
            return ApiResponse.error(400, "请选择要上传的文件");
        }
        
        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.error("文件类型不正确: {}", contentType);
                return ApiResponse.error(400, "只能上传图片文件");
            }
            
            // 生成日期文件夹路径
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            
            // 获取项目根目录
            String projectPath = System.getProperty("user.dir");
            logger.info("项目根目录: {}", projectPath);
            
            // 构建完整的上传路径
            Path uploadPath = Paths.get(projectPath, uploadDir, datePath);
            logger.info("上传目录路径: {}", uploadPath);
            
            // 确保目录存在
            Files.createDirectories(uploadPath);
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            logger.info("文件将被保存到: {}", filePath);
            
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件访问URL（相对路径）
            String fileUrl = "/uploads/" + datePath + "/" + filename;
            logger.info("文件上传成功，访问URL: {}", fileUrl);
            
            return ApiResponse.success(fileUrl);
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return ApiResponse.error(500, "文件上传失败：" + e.getMessage());
        }
    }
} 