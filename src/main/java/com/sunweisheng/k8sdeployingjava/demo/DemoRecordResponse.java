package com.sunweisheng.k8sdeployingjava.demo;

import java.time.Instant;

public record DemoRecordResponse(
        Long id,
        String title,
        String content,
        Instant createdAt
) {
    static DemoRecordResponse from(DemoRecord record) {
        return new DemoRecordResponse(
                record.getId(),
                record.getTitle(),
                record.getContent(),
                record.getCreatedAt()
        );
    }
}
