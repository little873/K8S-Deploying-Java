package com.sunweisheng.k8sdeployingjava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelmValuesPreparationTest {

    private static final Path SCRIPT = Path.of("ci", "prepare-helm-values.sh").toAbsolutePath();

    @TempDir
    Path tempDirectory;

    @Test
    void createsEmptyValuesWhenNoEnvironmentOverrideExists() throws Exception {
        Path target = tempDirectory.resolve("runtime/deploy-overrides-values.yaml");

        CommandResult result = run(tempDirectory.resolve("missing-values.yaml"), target);

        assertEquals(0, result.exitCode(), result.output());
        assertEquals("{}\n", Files.readString(target));
    }

    @Test
    void preservesAnyPartialEnvironmentOverride() throws Exception {
        String[] partialValues = {
                "ingress:\n  host: app.cloud.k8s.lab\n",
                "ingress:\n  tlsSecret: k8s-cloud-lab-tls\n"
        };

        for (int index = 0; index < partialValues.length; index++) {
            Path source = tempDirectory.resolve("source-" + index + ".yaml");
            Path target = tempDirectory.resolve("target-" + index + ".yaml");
            Files.writeString(source, partialValues[index]);

            CommandResult result = run(source, target);

            assertEquals(0, result.exitCode(), result.output());
            assertEquals(partialValues[index], Files.readString(target));
        }
    }

    @Test
    void keepsThePreparationScriptExecutable() {
        assertTrue(Files.isExecutable(SCRIPT));
    }

    private CommandResult run(Path source, Path target) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
                SCRIPT.toString(),
                source.toString(),
                target.toString()
        ).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new CommandResult(process.waitFor(), output);
    }

    private record CommandResult(int exitCode, String output) {
    }
}
