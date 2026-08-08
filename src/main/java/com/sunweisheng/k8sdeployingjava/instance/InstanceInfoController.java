package com.sunweisheng.k8sdeployingjava.instance;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instance")
public class InstanceInfoController {

    private final InstanceInfoService instanceInfoService;

    public InstanceInfoController(InstanceInfoService instanceInfoService) {
        this.instanceInfoService = instanceInfoService;
    }

    @GetMapping
    public ResponseEntity<InstanceInfoResponse> current() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(instanceInfoService.current());
    }
}
