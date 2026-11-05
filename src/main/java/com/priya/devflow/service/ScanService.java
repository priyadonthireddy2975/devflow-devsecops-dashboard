package com.priya.devflow.service;

import com.priya.devflow.dto.Dtos.ScanDto;
import com.priya.devflow.dto.Dtos.ScanRequest;
import com.priya.devflow.dto.Dtos.SecuritySummary;
import com.priya.devflow.dto.Dtos.ToolSummary;
import com.priya.devflow.entity.ScanResult;
import com.priya.devflow.entity.TrackedRepo;
import com.priya.devflow.repository.ScanResultRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
public class ScanService {

    private final ScanResultRepository scans;
    private final RepoService repoService;

    public ScanService(ScanResultRepository scans, RepoService repoService) {
        this.scans = scans;
        this.repoService = repoService;
    }

    @Transactional
    public ScanDto record(ScanRequest request) {
        TrackedRepo repo = repoService.findOrCreate(request.repository());
        ScanResult scan = new ScanResult(repo, request.tool().trim(), request.critical(), request.high(),
                request.medium(), request.low());
        scan.setCommitSha(request.commitSha());
        scan.setRunExternalId(request.runExternalId());
        scan.setReportUrl(request.reportUrl());
        return ScanDto.from(scans.save(scan));
    }

    @Transactional(readOnly = true)
    public Page<ScanDto> list(String repository, String tool, Pageable pageable) {
        Specification<ScanResult> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (repository != null && !repository.isBlank()) {
                predicates.add(cb.equal(root.get("repo").get("fullName"), repository));
            }
            if (tool != null && !tool.isBlank()) {
                predicates.add(cb.equal(root.get("tool"), tool));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return scans.findAll(spec, pageable).map(ScanDto::from);
    }

    @Transactional(readOnly = true)
    public SecuritySummary summary() {
        Map<String, ScanResult> latest = new LinkedHashMap<>();
        for (ScanResult scan : scans.findAllByOrderByScannedAtDesc()) {
            latest.putIfAbsent(scan.getRepo().getId() + "|" + scan.getTool(), scan);
        }
        Map<String, int[]> byTool = new TreeMap<>();
        Map<String, Set<Long>> reposByTool = new TreeMap<>();
        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;
        for (ScanResult s : latest.values()) {
            critical += s.getCritical();
            high += s.getHigh();
            medium += s.getMedium();
            low += s.getLow();
            int[] t = byTool.computeIfAbsent(s.getTool(), k -> new int[4]);
            t[0] += s.getCritical();
            t[1] += s.getHigh();
            t[2] += s.getMedium();
            t[3] += s.getLow();
            reposByTool.computeIfAbsent(s.getTool(), k -> new HashSet<>()).add(s.getRepo().getId());
        }
        List<ToolSummary> tools = byTool.entrySet().stream()
                .map(e -> new ToolSummary(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2],
                        e.getValue()[3], reposByTool.get(e.getKey()).size()))
                .toList();
        return new SecuritySummary(critical, high, medium, low, tools);
    }
}
