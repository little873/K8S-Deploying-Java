package com.sunweisheng.k8sdeployingjava.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DemoRecordRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过 100 个字符")
        String title,

        @NotBlank(message = "内容不能为空")
        @Size(max = 1000, message = "内容不能超过 1000 个字符")
        String content
) {
}
