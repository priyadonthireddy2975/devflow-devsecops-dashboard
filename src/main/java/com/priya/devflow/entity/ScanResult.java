package com.priya.devflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "scan_result")
public class ScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "repo_id", nullable = false)
    private TrackedRepo repo;

    @Column(nullable = false, length = 60)
    private String tool;

    @Column(name = "commit_sha", length = 64)
    private String commitSha;

    @Column(name = "run_external_id")
    private Long runExternalId;

    @Column(nullable = false)
    private int critical;

    @Column(nullable = false)
    private int high;

    @Column(nullable = false)
    private int medium;

    @Column(nullable = false)
    private int low;

    @Column(name = "report_url", length = 500)
    private String reportUrl;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt = Instant.now();

    protected ScanResult() {
    }

    public ScanResult(TrackedRepo repo, String tool, int critical, int high, int medium, int low) {
        this.repo = repo;
        this.tool = tool;
        this.critical = critical;
        this.high = high;
        this.medium = medium;
        this.low = low;
    }

    public Long getId() {
        return id;
    }

    public TrackedRepo getRepo() {
        return repo;
    }

    public String getTool() {
        return tool;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public Long getRunExternalId() {
        return runExternalId;
    }

    public void setRunExternalId(Long runExternalId) {
        this.runExternalId = runExternalId;
    }

    public int getCritical() {
        return critical;
    }

    public int getHigh() {
        return high;
    }

    public int getMedium() {
        return medium;
    }

    public int getLow() {
        return low;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(Instant scannedAt) {
        this.scannedAt = scannedAt;
    }
}
