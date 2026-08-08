package com.sunweisheng.k8sdeployingjava.instance;

import java.time.Instant;

public record InstanceInfoResponse(
        String podName,
        String podIp,
        String nodeName,
        Instant servedAt
) {
}
