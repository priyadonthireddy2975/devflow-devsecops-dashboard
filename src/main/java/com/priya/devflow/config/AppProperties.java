package com.priya.devflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "devflow")
public record AppProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue Admin admin,
        @DefaultValue("") String apiKey,
        @DefaultValue Github github,
        @DefaultValue("false") boolean demoData) {

    public record Jwt(@DefaultValue("") String secret, @DefaultValue("480") long expiryMinutes) {
    }

    public record Admin(@DefaultValue("admin") String username, @DefaultValue("") String password) {
    }

    public record Github(
            @DefaultValue("") String token,
            @DefaultValue("") String webhookSecret,
            @DefaultValue("300000") long syncIntervalMs) {
    }
}
