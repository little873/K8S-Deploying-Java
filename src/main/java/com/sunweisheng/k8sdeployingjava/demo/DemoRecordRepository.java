package com.sunweisheng.k8sdeployingjava.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoRecordRepository extends JpaRepository<DemoRecord, Long> {
}
