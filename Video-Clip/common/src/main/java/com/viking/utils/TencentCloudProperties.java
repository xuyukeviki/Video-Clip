package com.viking.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Data
@PropertySource("classpath:tencentcloud.properties") //绑定对应的properties文件
@ConfigurationProperties(prefix = "tencent.cloud") //绑定对应的yml配置
public class TencentCloudProperties {

    private String secretId;

    private String secretKey;
}
