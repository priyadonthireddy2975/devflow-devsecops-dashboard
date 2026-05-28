package com.priya.devflow.controller;

import com.priya.devflow.entity.Build;
import com.priya.devflow.repository.BuildRepository;

import com.priya.devflow.service.BuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.priya.devflow.service.BuildService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DashboardController {

    @Autowired
    private BuildService buildService;

    // Dashboard API
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

    // Health API
    @GetMapping("/api/health")
    public Map<String, String> healthCheck() {

        Map<String, String> health = new HashMap<>();

        health.put("status", "UP");
        health.put("application", "DevFlow");
        health.put("version", "1.0");

        return health;
    }

    // SAVE build into PostgreSQL
    @PostMapping("/api/build")
    public Build createBuild(@RequestBody Build build) {
        return buildService.createBuild(build);
    }

    // FETCH all builds from PostgreSQL
    @GetMapping("/api/builds")
    public List<Build> getAllBuilds() {
        return buildService.getAllBuilds();
    }


    @PutMapping("/api/build/{id}")
    public Build updateBuild(@PathVariable Long id,
                             @RequestBody Build updatedBuild) {

        return buildService.updateBuild(id, updatedBuild);
    }

    @DeleteMapping("/api/build/{id}")
    public String deleteBuild(@PathVariable Long id) {

        buildService.deleteBuild(id);

        return "Build deleted successfully";
    }
}