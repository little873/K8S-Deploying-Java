package com.sunweisheng.k8sdeployingjava.instance;

import com.sunweisheng.k8sdeployingjava.config.InstanceProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@Service
public class InstanceInfoService {

    private final InstanceProperties properties;

    public InstanceInfoService(InstanceProperties properties) {
        this.properties = properties;
    }

    public InstanceInfoResponse current() {
        LocalAddress localAddress = resolveLocalAddress();
        return new InstanceInfoResponse(
                valueOrFallback(properties.podName(), localAddress.hostName()),
                valueOrFallback(properties.podIp(), localAddress.hostAddress()),
                valueOrFallback(properties.nodeName(), localAddress.hostName()),
                Instant.now()
        );
    }

    private LocalAddress resolveLocalAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return new LocalAddress(address.getHostName(), address.getHostAddress());
        } catch (UnknownHostException exception) {
            return new LocalAddress("local", "127.0.0.1");
        }
    }

    private String valueOrFallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private record LocalAddress(String hostName, String hostAddress) {
    }
}
