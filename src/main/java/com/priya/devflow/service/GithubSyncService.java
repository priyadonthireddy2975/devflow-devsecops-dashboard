package com.priya.devflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.priya.devflow.entity.TrackedRepo;
import com.priya.devflow.repository.TrackedRepoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GithubSyncService {

    private static final Logger log = LoggerFactory.getLogger(GithubSyncService.class);

    private final GithubClient github;
    private final TrackedRepoRepository repos;
    private final RepoService repoService;
    private final RunService runService;

    public GithubSyncService(GithubClient github, TrackedRepoRepository repos, RepoService repoService,
                             RunService runService) {
        this.github = github;
        this.repos = repos;
        this.repoService = repoService;
        this.runService = runService;
    }

    @Scheduled(initialDelayString = "${devflow.github.sync-interval-ms:300000}",
            fixedDelayString = "${devflow.github.sync-interval-ms:300000}")
    public void scheduledSync() {
        if (!github.isConfigured()) {
            return;
        }
        for (TrackedRepo repo : repos.findAll()) {
            syncRepo(repo);
        }
    }

    public int syncRepo(TrackedRepo repo) {
        if (!github.isConfigured()) {
            return 0;
        }
        repoService.applyMetadata(repo);
        JsonNode response = github.workflowRuns(repo.getFullName(), 30).orElse(null);
        if (response == null || !response.has("workflow_runs")) {
            return 0;
        }
        int count = 0;
        for (JsonNode run : response.get("workflow_runs")) {
            runService.upsertFromGithub(repo, run);
            count++;
        }
        log.info("Synced {} runs for {}", count, repo.getFullName());
        return count;
    }
}
