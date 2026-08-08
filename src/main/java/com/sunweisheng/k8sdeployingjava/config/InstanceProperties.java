package com.sunweisheng.k8sdeployingjava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.instance")
public record InstanceProperties(
        String podName,
        String podIp,
        String nodeName
) {
}
