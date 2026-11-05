package com.priya.devflow.dto;

import com.priya.devflow.entity.PipelineRun;
import com.priya.devflow.entity.Role;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.entity.ScanResult;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class Dtos {

    private Dtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, String username, Role role, Instant expiresAt) {
    }

    public record UserDto(Long id, String username, Role role, Instant createdAt) {
    }

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "^[A-Za-z0-9._-]+$") String username,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotNull Role role) {
    }

    public record AddRepoRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$",
                    message = "must look like owner/repository") String fullName) {
    }

    public record RepoDto(
            Long id,
            String fullName,
            String htmlUrl,
            String defaultBranch,
            String language,
            String description,
            Instant lastSyncedAt,
            long totalRuns,
            double successRate,
            RunStatus lastStatus,
            Instant lastRunAt) {
    }

    public record RunRequest(
            @NotBlank String repository,
            Long externalId,
            @NotBlank @Size(max = 200) String name,
            @Size(max = 200) String branch,
            @Size(max = 64) String commitSha,
            @Size(max = 100) String actor,
            @Size(max = 60) String trigger,
            @NotNull RunStatus status,
            @Size(max = 500) String url,
            Instant startedAt,
            Instant finishedAt) {
    }

    public record RunDto(
            Long id,
            String repository,
            String name,
            String branch,
            String commitSha,
            String actor,
            String trigger,
            RunStatus status,
            String url,
            Instant startedAt,
            Instant finishedAt,
            Long durationSeconds) {

        public static RunDto from(PipelineRun r) {
            return new RunDto(r.getId(), r.getRepo().getFullName(), r.getName(), r.getBranch(),
                    r.getCommitSha(), r.getActor(), r.getTriggerEvent(), r.getStatus(), r.getUrl(),
                    r.getStartedAt(), r.getFinishedAt(), r.getDurationSeconds());
        }
    }

    public record ScanRequest(
            @NotBlank String repository,
            @NotBlank @Size(max = 60) String tool,
            @Size(max = 64) String commitSha,
            Long runExternalId,
            @Min(0) int critical,
            @Min(0) int high,
            @Min(0) int medium,
            @Min(0) int low,
            @Size(max = 500) String reportUrl) {
    }

    public record ScanDto(
            Long id,
            String repository,
            String tool,
            String commitSha,
            int critical,
            int high,
            int medium,
            int low,
            String reportUrl,
            Instant scannedAt) {

        public static ScanDto from(ScanResult s) {
            return new ScanDto(s.getId(), s.getRepo().getFullName(), s.getTool(), s.getCommitSha(),
                    s.getCritical(), s.getHigh(), s.getMedium(), s.getLow(), s.getReportUrl(), s.getScannedAt());
        }
    }

    public record ToolSummary(String tool, int critical, int high, int medium, int low, int repositories) {
    }

    public record SecuritySummary(int critical, int high, int medium, int low, List<ToolSummary> tools) {
    }

    public record DashboardSummary(
            long totalRuns,
            long successfulRuns,
            long failedRuns,
            long activeRuns,
            double successRate,
            long averageDurationSeconds,
            long repositories,
            int containersRunning,
            boolean dockerAvailable,
            SecuritySummary security) {
    }

    public record TrendPoint(LocalDate date, long success, long failed) {
    }

    public record SettingsInfo(
            String webhookPath,
            boolean webhookSecretConfigured,
            boolean githubTokenConfigured,
            boolean apiKeyConfigured,
            boolean dockerAvailable,
            boolean demoData) {
    }

    public record ApiError(Instant timestamp, int status, String error, String message, java.util.Map<String, String> details) {
    }
}
