package com.priya.devflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DockerStatsServiceTest {

    private final DockerStatsService service = new DockerStatsService(new ObjectMapper());

    @Test
    void countsContainersFromSuccessfulResponse() {
        String response = "HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n[{\"Id\":\"a\"},{\"Id\":\"b\"},{\"Id\":\"c\"}]";
        assertThat(service.parseCount(response)).contains(3);
    }

    @Test
    void returnsEmptyForErrorsAndGarbage() {
        assertThat(service.parseCount("HTTP/1.0 500 Internal Server Error\r\n\r\n{}")).isEmpty();
        assertThat(service.parseCount("HTTP/1.0 200 OK\r\n\r\nnot json")).isEmpty();
        assertThat(service.parseCount("nonsense")).isEmpty();
    }
}
