package com.adan.blog.service;

import com.adan.blog.config.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 图片存储服务：优先 OSS，未启用时由调用方回退本地磁盘。
 * 上传成功后返回公网可访问 URL（桶为公共读）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssStorageService {

    private final OssProperties props;

    private OSS client() {
        return new OSSClientBuilder().build(props.getEndpoint(), props.getAccessKeyId(), props.getAccessKeySecret());
    }

    /** 是否启用 OSS */
    public boolean enabled() {
        return props.isEnabled();
    }

    /**
     * 上传图片到 OSS，返回公网 URL。
     * key 规则：{prefix}{yyyy-MM-dd}/{uuid}.{ext}
     */
    public String upload(InputStream in, String contentType, String ext) {
        String dateDir = LocalDate.now().toString();
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        String key = props.getPrefix() + dateDir + "/" + filename;

        OSS oss = client();
        try {
            com.aliyun.oss.model.ObjectMetadata meta = new com.aliyun.oss.model.ObjectMetadata();
            meta.setContentType(contentType);
            meta.setContentLength(-1);
            oss.putObject(props.getBucket(), key, in, meta);
            String base = props.getPublicUrl();
            if (base == null || base.isBlank()) {
                base = "https://" + props.getBucket() + "." + props.getEndpoint();
            }
            return base + "/" + key;
        } finally {
            oss.shutdown();
        }
    }
}
