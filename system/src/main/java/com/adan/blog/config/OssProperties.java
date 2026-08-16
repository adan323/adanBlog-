package com.adan.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置（adan.blog.oss.*）
 * - enabled: 是否启用 OSS 存储（false 时回退本地磁盘上传）
 * - endpoint: 如 oss-cn-beijing.aliyuncs.com
 * - public-url: 图片公网访问前缀，如 https://adanc.oss-cn-beijing.aliyuncs.com
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "adan.blog.oss")
public class OssProperties {
    private boolean enabled = false;
    private String endpoint = "";
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String bucket = "";
    private String publicUrl = "";
    /** 存储路径前缀（如 images/），空则存桶根 */
    private String prefix = "images/";
}
