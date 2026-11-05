package com.priya.devflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.priya.devflow.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class GithubClient {

    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);

    private final RestClient client;
    private final boolean configured;

    public GithubClient(AppProperties props) {
        String token = props.github().token();
        this.configured = token != null && !token.isBlank();
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (configured) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        this.client = builder.build();
    }

    public boolean isConfigured() {
        return configured;
    }

    public Optional<JsonNode> repository(String fullName) {
        return get("/repos/" + fullName);
    }

    public Optional<JsonNode> workflowRuns(String fullName, int perPage) {
        return get("/repos/" + fullName + "/actions/runs?per_page=" + perPage);
    }

    private Optional<JsonNode> get(String path) {
        try {
            return Optional.ofNullable(client.get().uri(path).retrieve().body(JsonNode.class));
        } catch (RestClientException ex) {
            log.warn("GitHub request failed for {}: {}", path, ex.getMessage());
            return Optional.empty();
        }
    }
}
