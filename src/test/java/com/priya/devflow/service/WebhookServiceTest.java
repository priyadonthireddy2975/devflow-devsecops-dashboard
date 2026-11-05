package com.priya.devflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.priya.devflow.config.AppProperties;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookServiceTest {

    private static AppProperties props(String secret) {
        return new AppProperties(new AppProperties.Jwt("x", 60), new AppProperties.Admin("admin", "pw"), "",
                new AppProperties.Github("", secret, 1000), false);
    }

    @Test
    void verifiesCorrectSignatureOnly() throws Exception {
        WebhookService service = new WebhookService(props("secret"), new ObjectMapper(), null, null);
        byte[] body = "{\"a\":1}".getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String good = "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));

        assertThat(service.verify(body, good)).isTrue();
        assertThat(service.verify(body, "sha256=00")).isFalse();
        assertThat(service.verify(body, null)).isFalse();
        assertThat(service.verify("{\"a\":2}".getBytes(StandardCharsets.UTF_8), good)).isFalse();
    }

    @Test
    void refusesEverythingWhenSecretMissing() {
        WebhookService service = new WebhookService(props(""), new ObjectMapper(), null, null);
        assertThat(service.isConfigured()).isFalse();
        assertThat(service.verify(new byte[0], "sha256=abc")).isFalse();
    }
}
