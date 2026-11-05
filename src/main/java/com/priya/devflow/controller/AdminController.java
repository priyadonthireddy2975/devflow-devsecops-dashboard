package com.priya.devflow.controller;

import com.priya.devflow.config.AppProperties;
import com.priya.devflow.dto.Dtos.CreateUserRequest;
import com.priya.devflow.dto.Dtos.SettingsInfo;
import com.priya.devflow.dto.Dtos.UserDto;
import com.priya.devflow.service.DockerStatsService;
import com.priya.devflow.service.GithubClient;
import com.priya.devflow.service.UserService;
import com.priya.devflow.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdminController {

    private final UserService userService;
    private final AppProperties props;
    private final GithubClient github;
    private final WebhookService webhook;
    private final DockerStatsService docker;

    public AdminController(UserService userService, AppProperties props, GithubClient github,
                           WebhookService webhook, DockerStatsService docker) {
        this.userService = userService;
        this.props = props;
        this.github = github;
        this.webhook = webhook;
        this.docker = docker;
    }

    @GetMapping("/settings/info")
    public SettingsInfo info() {
        return new SettingsInfo("/api/webhooks/github", webhook.isConfigured(), github.isConfigured(),
                props.apiKey() != null && !props.apiKey().isBlank(), docker.isAvailable(), props.demoData());
    }

    @GetMapping("/users")
    public List<UserDto> users() {
        return userService.list();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }
}
