package com.priya.devflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    String adminToken;

    @BeforeEach
    void login() throws Exception {
        adminToken = loginAs("admin", "admin-pass-123");
    }

    private String loginAs(String user, String pass) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/runs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/runs").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void adminCanTrackAndRemoveRepository() throws Exception {
        String name = "acme/" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult created = mvc.perform(post("/api/repositories").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value(name))
                .andReturn();
        long id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(post("/api/repositories").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"" + name + "\"}"))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/repositories/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/repositories/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidRepositoryNameIsRejected() throws Exception {
        mvc.perform(post("/api/repositories").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"not valid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.fullName").exists());
    }

    @Test
    void viewerCanReadButNotWrite() throws Exception {
        String viewer = "viewer" + UUID.randomUUID().toString().substring(0, 6);
        mvc.perform(post("/api/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + viewer + "\",\"password\":\"viewer-pass-1\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isCreated());
        String token = loginAs(viewer, "viewer-pass-1");

        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/repositories").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"acme/blocked\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void ciCanReportRunsAndScansWithApiKey() throws Exception {
        String repo = "acme/ci-" + UUID.randomUUID().toString().substring(0, 6);
        String run = "{\"repository\":\"" + repo + "\",\"externalId\":4242,\"name\":\"CI\",\"branch\":\"main\","
                + "\"status\":\"FAILED\",\"startedAt\":\"2026-01-01T10:00:00Z\",\"finishedAt\":\"2026-01-01T10:02:00Z\"}";
        mvc.perform(post("/api/runs").header("X-API-Key", "test-ingest-key")
                        .contentType(MediaType.APPLICATION_JSON).content(run))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationSeconds").value(120))
                .andExpect(jsonPath("$.status").value("FAILED"));

        String updated = run.replace("FAILED", "SUCCESS");
        mvc.perform(post("/api/runs").header("X-API-Key", "test-ingest-key")
                        .contentType(MediaType.APPLICATION_JSON).content(updated))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mvc.perform(get("/api/runs").param("repository", repo).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        String scan = "{\"repository\":\"" + repo + "\",\"tool\":\"Trivy\",\"critical\":2,\"high\":3,\"medium\":0,\"low\":1}";
        mvc.perform(post("/api/scans").header("X-API-Key", "test-ingest-key")
                        .contentType(MediaType.APPLICATION_JSON).content(scan))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/scans/summary").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.critical").isNumber());
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRuns").isNumber())
                .andExpect(jsonPath("$.security.tools").isArray());
        mvc.perform(get("/api/dashboard/trend").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(14));
    }

    @Test
    void wrongApiKeyCannotIngest() throws Exception {
        mvc.perform(post("/api/runs").header("X-API-Key", "nope").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repository\":\"a/b\",\"name\":\"CI\",\"status\":\"SUCCESS\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ingestValidatesPayload() throws Exception {
        mvc.perform(post("/api/scans").header("X-API-Key", "test-ingest-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repository\":\"a/b\",\"tool\":\"Trivy\",\"critical\":-1,\"high\":0,\"medium\":0,\"low\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webhookRejectsBadSignatureAndAcceptsValidOne() throws Exception {
        String repo = "acme/hook-" + UUID.randomUUID().toString().substring(0, 6);
        String payload = "{\"repository\":{\"full_name\":\"" + repo + "\",\"html_url\":\"https://github.com/" + repo
                + "\",\"default_branch\":\"main\"},\"workflow_run\":{\"id\":777,\"name\":\"CI\",\"head_branch\":\"main\","
                + "\"head_sha\":\"abc123\",\"event\":\"push\",\"status\":\"completed\",\"conclusion\":\"failure\","
                + "\"run_started_at\":\"2026-01-01T10:00:00Z\",\"updated_at\":\"2026-01-01T10:05:00Z\","
                + "\"actor\":{\"login\":\"priya\"}}}";

        mvc.perform(post("/api/webhooks/github").header("X-GitHub-Event", "workflow_run")
                        .header("X-Hub-Signature-256", "sha256=deadbeef")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/webhooks/github").header("X-GitHub-Event", "workflow_run")
                        .header("X-Hub-Signature-256", sign(payload))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("processed"));

        MvcResult result = mvc.perform(get("/api/runs").param("repository", repo)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode first = mapper.readTree(result.getResponse().getContentAsString()).get("content").get(0);
        assertThat(first.get("status").asText()).isEqualTo("FAILED");
        assertThat(first.get("actor").asText()).isEqualTo("priya");
        assertThat(first.get("durationSeconds").asLong()).isEqualTo(300);
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("whsec_test".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
