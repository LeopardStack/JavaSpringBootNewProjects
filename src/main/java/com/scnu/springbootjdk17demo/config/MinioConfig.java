package com.scnu.springbootjdk17demo.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置
 *
 * 用法：注入 MinioClient 直接使用 io.minio 提供的 API
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.minio")
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
    private String publicEndpoint;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }
}