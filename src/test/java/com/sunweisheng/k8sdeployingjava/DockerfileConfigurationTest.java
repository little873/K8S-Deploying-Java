package com.sunweisheng.k8sdeployingjava;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerfileConfigurationTest {

    @Test
    void declaresTheGitHubRepositoryAsTheOciImageSource() throws IOException {
        String dockerfile = Files.readString(Path.of("Dockerfile"));
        assertTrue(dockerfile.contains(
                "LABEL org.opencontainers.image.source=\"https://github.com/sunweisheng/K8S-Deploying-Java\""
        ));
    }
}
