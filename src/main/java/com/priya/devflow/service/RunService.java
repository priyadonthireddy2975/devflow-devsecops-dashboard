package com.priya.devflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.priya.devflow.dto.Dtos.RunDto;
import com.priya.devflow.dto.Dtos.RunRequest;
import com.priya.devflow.entity.PipelineRun;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.entity.TrackedRepo;
import com.priya.devflow.repository.PipelineRunRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RunService {

    private final PipelineRunRepository runs;
    private final RepoService repoService;

    public RunService(PipelineRunRepository runs, RepoService repoService) {
        this.runs = runs;
        this.repoService = repoService;
    }

    @Transactional(readOnly = true)
    public Page<RunDto> list(RunStatus status, String repository, String branch, Pageable pageable) {
        Specification<PipelineRun> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (repository != null && !repository.isBlank()) {
                predicates.add(cb.equal(root.get("repo").get("fullName"), repository));
            }
            if (branch != null && !branch.isBlank()) {
                predicates.add(cb.equal(root.get("branch"), branch));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return runs.findAll(spec, pageable).map(RunDto::from);
    }

    @Transactional
    public RunDto record(RunRequest request) {
        TrackedRepo repo = repoService.findOrCreate(request.repository());
        PipelineRun run = request.externalId() == null
                ? new PipelineRun(repo, request.name(), request.status())
                : runs.findByRepoAndExternalId(repo, request.externalId())
                .orElseGet(() -> new PipelineRun(repo, request.name(), request.status()));
        run.setExternalId(request.externalId());
        run.setName(request.name());
        run.setStatus(request.status());
        run.setBranch(request.branch());
        run.setCommitSha(request.commitSha());
        run.setActor(request.actor());
        run.setTriggerEvent(request.trigger());
        run.setUrl(request.url());
        run.applyTiming(request.startedAt(), request.finishedAt());
        return RunDto.from(runs.save(run));
    }

    @Transactional
    public void upsertFromGithub(TrackedRepo repo, JsonNode json) {
        long externalId = json.path("id").asLong();
        if (externalId == 0) {
            return;
        }
        PipelineRun run = runs.findByRepoAndExternalId(repo, externalId)
                .orElseGet(() -> new PipelineRun(repo, "workflow", RunStatus.QUEUED));
        run.setExternalId(externalId);
        run.setName(text(json, "name", "workflow"));
        run.setBranch(text(json, "head_branch", null));
        run.setCommitSha(text(json, "head_sha", null));
        run.setActor(json.path("actor").path("login").asText(null));
        run.setTriggerEvent(text(json, "event", null));
        run.setUrl(text(json, "html_url", null));
        RunStatus status = mapStatus(text(json, "status", "queued"), text(json, "conclusion", null));
        run.setStatus(status);
        Instant started = parseInstant(text(json, "run_started_at", text(json, "created_at", null)));
        Instant finished = status == RunStatus.QUEUED || status == RunStatus.RUNNING
                ? null : parseInstant(text(json, "updated_at", null));
        run.applyTiming(started, finished);
        runs.save(run);
    }

    public static RunStatus mapStatus(String status, String conclusion) {
        if ("completed".equalsIgnoreCase(status)) {
            if (conclusion == null) {
                return RunStatus.FAILED;
            }
            return switch (conclusion.toLowerCase()) {
                case "success", "neutral" -> RunStatus.SUCCESS;
                case "cancelled", "skipped" -> RunStatus.CANCELLED;
                default -> RunStatus.FAILED;
            };
        }
        return switch (status == null ? "" : status.toLowerCase()) {
            case "in_progress" -> RunStatus.RUNNING;
            default -> RunStatus.QUEUED;
        };
    }

    private static String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asText();
    }

    private static Instant parseInstant(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
