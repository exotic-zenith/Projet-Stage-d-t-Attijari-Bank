package com.example.accountservice.controller;

import com.example.accountservice.repository.AccountRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void crudFlowWorks() throws Exception {
        HttpResponse<String> created = send("POST", "/api/accounts", """
                {"accountHolderName":"Ada Lovelace","accountNumber":"ACC-001","balance":1250.50,"currency":"USD"}
                """);
        assertThat(created.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(created.body()).contains("Ada Lovelace", "ACC-001", "USD");

        Long id = repository.findAll().get(0).getId();

        HttpResponse<String> listed = send("GET", "/api/accounts", null);
        assertThat(listed.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(listed.body()).contains("ACC-001");

        HttpResponse<String> updated = send("PUT", "/api/accounts/" + id, """
                {"accountHolderName":"Ada Byron","accountNumber":"ACC-001","balance":1500.00,"currency":"EUR"}
                """);
        assertThat(updated.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(updated.body()).contains("Ada Byron", "EUR");
        assertThat(repository.findById(id).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("1500.00"));

        HttpResponse<String> deleted = send("DELETE", "/api/accounts/" + id, null);
        assertThat(deleted.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        HttpResponse<String> missing = send("GET", "/api/accounts/" + id, null);
        assertThat(missing.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private HttpResponse<String> send(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
