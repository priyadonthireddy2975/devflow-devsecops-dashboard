package com.priya.devflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashboardController {

    @GetMapping("/api/dashboard")
    public Map<String, Object> getDashboardData() {

        Map<String, Object> data = new HashMap<>();

        data.put("totalBuilds", 128);
        data.put("successfulBuilds", 112);
        data.put("failedBuilds", 8);
        data.put("runningPipelines", 3);
        data.put("dockerContainers", 14);
        data.put("repositories", 9);

        return data;
    }

    @GetMapping("/api/health")
    public Map<String, String> healthCheck() {

        Map<String, String> health = new HashMap<>();

        health.put("status", "UP");
        health.put("application", "DevFlow");
        health.put("version", "1.0");

        return health;
    }

    @GetMapping("/api/message")
    public String welcomeMessage() {
        return "Welcome to DevFlow DevSecOps Platform!";
    }
}