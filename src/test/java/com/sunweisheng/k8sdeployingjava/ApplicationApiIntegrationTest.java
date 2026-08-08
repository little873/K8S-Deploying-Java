package com.sunweisheng.k8sdeployingjava;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunweisheng.k8sdeployingjava.demo.DemoRecord;
import com.sunweisheng.k8sdeployingjava.demo.DemoRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoRecordRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearRecords() {
        repository.deleteAll();
    }

    @Test
    void returnsConfiguredInstanceInformationWithoutCaching() throws Exception {
        mockMvc.perform(get("/api/instance"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.podName").value("test-pod"))
                .andExpect(jsonPath("$.podIp").value("10.244.1.20"))
                .andExpect(jsonPath("$.nodeName").value("test-node"))
                .andExpect(jsonPath("$.servedAt").exists());
    }

    @Test
    void createsUpdatesListsAndDeletesARecord() throws Exception {
        String location = mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  第一条记录  ","content":"  初始内容  "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("第一条记录"))
                .andExpect(jsonPath("$.content").value("初始内容"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(location).get("id").asLong();

        mockMvc.perform(put("/api/records/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"修改后的标题","content":"修改后的内容"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("修改后的标题"))
                .andExpect(jsonPath("$.content").value("修改后的内容"));

        mockMvc.perform(get("/api/records").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(delete("/api/records/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void validatesRecordContentAndPageBounds() throws Exception {
        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   ","content":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.content").exists());

        mockMvc.perform(get("/api/records").param("page", "-1").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void returnsRecordsInPagesWithNewestIdsFirst() throws Exception {
        IntStream.rangeClosed(1, 12)
                .forEach(number -> repository.save(new DemoRecord("记录 " + number, "内容 " + number)));

        mockMvc.perform(get("/api/records").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].title").value("记录 12"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/records").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void returnsNotFoundWhenUpdatingOrDeletingMissingRecord() throws Exception {
        mockMvc.perform(put("/api/records/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"不存在","content":"不存在"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mockMvc.perform(delete("/api/records/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
