package com.priya.devflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priya.devflow.config.AppProperties;
import com.priya.devflow.entity.TrackedRepo;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class WebhookService {

    private final String secret;
    private final ObjectMapper mapper;
    private final RepoService repoService;
    private final RunService runService;

    public WebhookService(AppProperties props, ObjectMapper mapper, RepoService repoService, RunService runService) {
        this.secret = props.github().webhookSecret();
        this.mapper = mapper;
        this.repoService = repoService;
        this.runService = runService;
    }

    public boolean isConfigured() {
        return secret != null && !secret.isBlank();
    }

    public boolean verify(byte[] body, String signatureHeader) {
        if (!isConfigured() || signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            return false;
        }
    }

    public String handle(String event, byte[] body) throws IOException {
        if ("ping".equals(event)) {
            return "pong";
        }
        if (!"workflow_run".equals(event)) {
            return "ignored";
        }
        JsonNode payload = mapper.readTree(body);
        JsonNode repository = payload.path("repository");
        JsonNode run = payload.path("workflow_run");
        String fullName = repository.path("full_name").asText(null);
        if (fullName == null || run.isMissingNode()) {
            return "ignored";
        }
        TrackedRepo repo = repoService.findOrCreate(fullName);
        repoService.applyMetadata(repo, repository);
        runService.upsertFromGithub(repo, run);
        return "processed";
    }
}
