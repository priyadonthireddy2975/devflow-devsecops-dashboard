package com.priya.devflow.controller;

import com.priya.devflow.dto.Dtos.ScanDto;
import com.priya.devflow.dto.Dtos.ScanRequest;
import com.priya.devflow.dto.Dtos.SecuritySummary;
import com.priya.devflow.service.ScanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping
    public Page<ScanDto> list(@RequestParam(required = false) String repository,
                              @RequestParam(required = false) String tool,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "15") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "scannedAt"));
        return scanService.list(repository, tool, pageable);
    }

    @GetMapping("/summary")
    public SecuritySummary summary() {
        return scanService.summary();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScanDto record(@Valid @RequestBody ScanRequest request) {
        return scanService.record(request);
    }
}
