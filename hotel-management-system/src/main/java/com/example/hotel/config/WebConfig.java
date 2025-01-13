package com.example.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String projectPath = System.getProperty("user.dir");
        File uploadDirectory = new File(projectPath, uploadDir);
        
        // 确保上传目录存在
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        
        // 配置静态资源映射
        String uploadPath = "file:" + uploadDirectory.getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.replace("\\", "/"));
                
        System.out.println("Upload directory: " + uploadDirectory.getAbsolutePath());
        System.out.println("Resource location: " + uploadPath);
    }
} 