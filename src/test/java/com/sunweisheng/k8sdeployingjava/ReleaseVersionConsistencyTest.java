package com.sunweisheng.k8sdeployingjava;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReleaseVersionConsistencyTest {

    @Test
    void keepsMavenAndHelmReleaseVersionsAligned() throws Exception {
        String projectVersion = readProjectVersion();
        Map<String, Object> chart = readChart();

        assertEquals(projectVersion, String.valueOf(chart.get("version")));
        assertEquals(projectVersion, String.valueOf(chart.get("appVersion")));
    }

    private String readProjectVersion() throws Exception {
        Element project = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(Path.of("pom.xml").toFile())
                .getDocumentElement();
        for (Node child = project.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && "version".equals(element.getTagName())) {
                return element.getTextContent().trim();
            }
        }
        throw new IllegalStateException("Missing project version in pom.xml");
    }

    private Map<String, Object> readChart() throws IOException {
        Path path = Path.of("deploy", "charts", "spring-app", "Chart.yaml");
        try (InputStream input = Files.newInputStream(path)) {
            return new Yaml().load(input);
        }
    }
}
