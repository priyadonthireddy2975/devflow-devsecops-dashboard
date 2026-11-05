package com.priya.devflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.priya.devflow.dto.Dtos.RepoDto;
import com.priya.devflow.entity.PipelineRun;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.entity.TrackedRepo;
import com.priya.devflow.exception.ConflictException;
import com.priya.devflow.exception.NotFoundException;
import com.priya.devflow.repository.PipelineRunRepository;
import com.priya.devflow.repository.TrackedRepoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepoService {

    private final TrackedRepoRepository repos;
    private final PipelineRunRepository runs;
    private final GithubClient github;

    public RepoService(TrackedRepoRepository repos, PipelineRunRepository runs, GithubClient github) {
        this.repos = repos;
        this.runs = runs;
        this.github = github;
    }

    @Transactional(readOnly = true)
    public List<RepoDto> list() {
        Map<Long, Map<RunStatus, Long>> counts = new HashMap<>();
        for (Object[] row : runs.countByRepoAndStatus()) {
            counts.computeIfAbsent((Long) row[0], k -> new HashMap<>()).put((RunStatus) row[1], (Long) row[2]);
        }
        return repos.findAllByOrderByFullNameAsc().stream().map(r -> toDto(r, counts.get(r.getId()))).toList();
    }

    @Transactional
    public RepoDto add(String fullName) {
        if (repos.findByFullName(fullName).isPresent()) {
            throw new ConflictException("Repository is already tracked");
        }
        TrackedRepo repo = repos.save(new TrackedRepo(fullName));
        applyMetadata(repo);
        return toDto(repo, Map.of());
    }

    @Transactional
    public TrackedRepo findOrCreate(String fullName) {
        return repos.findByFullName(fullName).orElseGet(() -> repos.save(new TrackedRepo(fullName)));
    }

    @Transactional
    public void delete(Long id) {
        if (!repos.existsById(id)) {
            throw new NotFoundException("Repository not found");
        }
        repos.deleteById(id);
    }

    @Transactional
    public void applyMetadata(TrackedRepo repo) {
        github.repository(repo.getFullName()).ifPresent(json -> applyMetadata(repo, json));
    }

    @Transactional
    public void applyMetadata(TrackedRepo repo, JsonNode json) {
        repo.setHtmlUrl(text(json, "html_url", repo.getHtmlUrl()));
        repo.setDefaultBranch(text(json, "default_branch", repo.getDefaultBranch()));
        repo.setLanguage(text(json, "language", repo.getLanguage()));
        repo.setDescription(text(json, "description", repo.getDescription()));
        repo.setLastSyncedAt(Instant.now());
        repos.save(repo);
    }

    @Transactional(readOnly = true)
    public TrackedRepo get(Long id) {
        return repos.findById(id).orElseThrow(() -> new NotFoundException("Repository not found"));
    }

    private RepoDto toDto(TrackedRepo r, Map<RunStatus, Long> counts) {
        Map<RunStatus, Long> c = counts == null ? Map.of() : counts;
        long success = c.getOrDefault(RunStatus.SUCCESS, 0L);
        long failed = c.getOrDefault(RunStatus.FAILED, 0L);
        long total = c.values().stream().mapToLong(Long::longValue).sum();
        double rate = success + failed == 0 ? 0 : Math.round(success * 1000.0 / (success + failed)) / 10.0;
        PipelineRun last = runs.findFirstByRepoOrderByStartedAtDesc(r).orElse(null);
        return new RepoDto(r.getId(), r.getFullName(), r.getHtmlUrl(), r.getDefaultBranch(), r.getLanguage(),
                r.getDescription(), r.getLastSyncedAt(), total, rate,
                last == null ? null : last.getStatus(), last == null ? null : last.getStartedAt());
    }

    private static String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asText();
    }
}
