package com.priya.devflow.config;

import com.priya.devflow.dto.Dtos.SecuritySummary;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.repository.PipelineRunRepository;
import com.priya.devflow.service.ScanService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.function.ToDoubleFunction;

@Component
public class BusinessMetrics {

    private final MeterRegistry registry;
    private final PipelineRunRepository runs;
    private final ScanService scans;

    public BusinessMetrics(MeterRegistry registry, PipelineRunRepository runs, ScanService scans) {
        this.registry = registry;
        this.runs = runs;
        this.scans = scans;
    }

    @PostConstruct
    void register() {
        for (RunStatus status : RunStatus.values()) {
            Gauge.builder("devflow.pipeline.runs", runs, r -> r.countByStatus(status))
                    .description("Pipeline runs by status")
                    .tag("status", status.name().toLowerCase())
                    .register(registry);
        }
        severityGauge("critical", SecuritySummary::critical);
        severityGauge("high", SecuritySummary::high);
        severityGauge("medium", SecuritySummary::medium);
        severityGauge("low", SecuritySummary::low);
    }

    private void severityGauge(String severity, ToDoubleFunction<SecuritySummary> picker) {
        Gauge.builder("devflow.vulnerabilities", scans, s -> picker.applyAsDouble(s.summary()))
                .description("Open vulnerabilities from the latest scans")
                .tag("severity", severity)
                .register(registry);
    }
}
