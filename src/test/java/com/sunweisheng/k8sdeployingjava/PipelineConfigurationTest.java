package com.sunweisheng.k8sdeployingjava;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineConfigurationTest {

    private final JsonNode configuration = readConfiguration();

    @Test
    void restrictsImagePublishingAndDeploymentToMain() {
        assertMainBranchCondition(stage("image"));
        assertMainBranchCondition(stage("deploy"));
    }

    @Test
    void deploysOnlyToThePrecreatedNamespace() {
        JsonNode steps = stage("deploy").path("steps");
        List<String> actions = new ArrayList<>();
        steps.forEach(step -> actions.add(step.path("action").asText()));
        assertEquals(List.of("lint", "template", "upgrade", "status"), actions);

        assertImageCoordinates(findStep(steps, "lint"));
        assertImageCoordinates(findStep(steps, "template"));
        JsonNode upgrade = findStep(steps, "upgrade");
        assertTrue(upgrade.has("createNamespace"));
        assertFalse(upgrade.path("createNamespace").asBoolean());
        assertTrue(upgrade.path("rollbackOnFailure").asBoolean());
        assertImageCoordinates(upgrade);
    }

    private void assertMainBranchCondition(JsonNode stage) {
        JsonNode condition = stage.path("condition");
        assertEquals("BRANCH_NAME", condition.path("variable").asText());
        assertEquals("equals", condition.path("operator").asText());
        assertEquals("main", condition.path("value").asText());
    }

    private JsonNode stage(String id) {
        for (JsonNode stage : configuration.path("stages")) {
            if (id.equals(stage.path("id").asText())) {
                return stage;
            }
        }
        throw new IllegalStateException("Missing pipeline stage: " + id);
    }

    private JsonNode findStep(JsonNode steps, String action) {
        for (JsonNode step : steps) {
            if (action.equals(step.path("action").asText())) {
                return step;
            }
        }
        throw new IllegalStateException("Missing Helm action: " + action);
    }

    private void assertImageCoordinates(JsonNode step) {
        assertEquals("${IMAGE_REPOSITORY}", step.path("setValues").path("image.repository").asText());
        assertEquals("${IMAGE_DIGEST}", step.path("setValues").path("image.digest").asText());
    }

    private JsonNode readConfiguration() {
        Path path = Path.of("ci", "jenkins-project.json");
        try {
            return new ObjectMapper().readTree(Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read pipeline configuration: " + path, exception);
        }
    }
}
