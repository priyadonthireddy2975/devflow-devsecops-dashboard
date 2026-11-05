package com.priya.devflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "pipeline_run")
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "repo_id", nullable = false)
    private TrackedRepo repo;

    @Column(name = "external_id")
    private Long externalId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String branch;

    @Column(name = "commit_sha", length = 64)
    private String commitSha;

    @Column(length = 100)
    private String actor;

    @Column(name = "trigger_event", length = 60)
    private String triggerEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RunStatus status;

    @Column(length = 500)
    private String url;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    protected PipelineRun() {
    }

    public PipelineRun(TrackedRepo repo, String name, RunStatus status) {
        this.repo = repo;
        this.name = name;
        this.status = status;
    }

    public void applyTiming(Instant started, Instant finished) {
        if (started != null) {
            this.startedAt = started;
        }
        this.finishedAt = finished;
        if (finished != null && this.startedAt != null && !finished.isBefore(this.startedAt)) {
            this.durationSeconds = Duration.between(this.startedAt, finished).getSeconds();
        } else {
            this.durationSeconds = null;
        }
    }

    public Long getId() {
        return id;
    }

    public TrackedRepo getRepo() {
        return repo;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }
}
