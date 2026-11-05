package com.priya.devflow.service;

import com.priya.devflow.dto.Dtos.DashboardSummary;
import com.priya.devflow.dto.Dtos.TrendPoint;
import com.priya.devflow.entity.PipelineRun;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.repository.PipelineRunRepository;
import com.priya.devflow.repository.TrackedRepoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final PipelineRunRepository runs;
    private final TrackedRepoRepository repos;
    private final ScanService scanService;
    private final DockerStatsService docker;

    public StatsService(PipelineRunRepository runs, TrackedRepoRepository repos, ScanService scanService,
                        DockerStatsService docker) {
        this.runs = runs;
        this.repos = repos;
        this.scanService = scanService;
        this.docker = docker;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        long success = runs.countByStatus(RunStatus.SUCCESS);
        long failed = runs.countByStatus(RunStatus.FAILED);
        long cancelled = runs.countByStatus(RunStatus.CANCELLED);
        long active = runs.countByStatus(RunStatus.RUNNING) + runs.countByStatus(RunStatus.QUEUED);
        long total = success + failed + cancelled + active;
        double rate = success + failed == 0 ? 0 : Math.round(success * 1000.0 / (success + failed)) / 10.0;
        Double avg = runs.averageDuration();
        int containers = docker.runningContainers().orElse(0);
        return new DashboardSummary(total, success, failed, active, rate,
                avg == null ? 0 : Math.round(avg), repos.count(), containers, docker.isAvailable(),
                scanService.summary());
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> trend(int days) {
        int span = Math.max(1, Math.min(days, 90));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = today.minusDays(span - 1L);
        Map<LocalDate, long[]> buckets = new HashMap<>();
        for (PipelineRun run : runs.findByStartedAtAfter(from.atStartOfDay().toInstant(ZoneOffset.UTC))) {
            LocalDate day = run.getStartedAt().atZone(ZoneOffset.UTC).toLocalDate();
            long[] bucket = buckets.computeIfAbsent(day, d -> new long[2]);
            if (run.getStatus() == RunStatus.SUCCESS) {
                bucket[0]++;
            } else if (run.getStatus() == RunStatus.FAILED) {
                bucket[1]++;
            }
        }
        List<TrendPoint> points = new ArrayList<>();
        for (int i = 0; i < span; i++) {
            LocalDate day = from.plusDays(i);
            long[] bucket = buckets.getOrDefault(day, new long[2]);
            points.add(new TrendPoint(day, bucket[0], bucket[1]));
        }
        return points;
    }
}
