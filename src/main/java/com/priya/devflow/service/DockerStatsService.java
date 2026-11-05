package com.priya.devflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class DockerStatsService {

    private static final Logger log = LoggerFactory.getLogger(DockerStatsService.class);
    private static final Path SOCKET = Path.of("/var/run/docker.sock");

    private final ObjectMapper mapper;

    public DockerStatsService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public boolean isAvailable() {
        return Files.exists(SOCKET);
    }

    public Optional<Integer> runningContainers() {
        if (!isAvailable()) {
            return Optional.empty();
        }
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(UnixDomainSocketAddress.of(SOCKET));
            String request = "GET /containers/json HTTP/1.0\r\nHost: docker\r\nAccept: application/json\r\n\r\n";
            channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Channels.newInputStream(channel).transferTo(out);
            return parseCount(out.toString(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            log.debug("Docker socket unavailable: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    Optional<Integer> parseCount(String httpResponse) {
        int split = httpResponse.indexOf("\r\n\r\n");
        if (split < 0 || !httpResponse.startsWith("HTTP/1.") || !httpResponse.contains(" 200 ")) {
            return Optional.empty();
        }
        try {
            JsonNode body = mapper.readTree(httpResponse.substring(split + 4));
            return body.isArray() ? Optional.of(body.size()) : Optional.empty();
        } catch (IOException ex) {
            return Optional.empty();
        }
    }
}
