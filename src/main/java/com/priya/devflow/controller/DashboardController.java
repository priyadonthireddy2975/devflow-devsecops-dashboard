package com.priya.devflow.controller;

import com.priya.devflow.dto.Dtos.DashboardSummary;
import com.priya.devflow.dto.Dtos.TrendPoint;
import com.priya.devflow.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final StatsService stats;

    public DashboardController(StatsService stats) {
        this.stats = stats;
    }

    @GetMapping("/dashboard")
    public DashboardSummary summary() {
        return stats.summary();
    }

    @GetMapping("/dashboard/trend")
    public List<TrendPoint> trend(@RequestParam(defaultValue = "14") int days) {
        return stats.trend(days);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "DevFlow");
    }
}
