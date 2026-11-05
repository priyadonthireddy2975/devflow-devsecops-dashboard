package com.priya.devflow.controller;

import com.priya.devflow.dto.Dtos.RunDto;
import com.priya.devflow.dto.Dtos.RunRequest;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.service.RunService;
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
@RequestMapping("/api/runs")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping
    public Page<RunDto> list(@RequestParam(required = false) RunStatus status,
                             @RequestParam(required = false) String repository,
                             @RequestParam(required = false) String branch,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "15") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "startedAt"));
        return runService.list(status, repository, branch, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunDto record(@Valid @RequestBody RunRequest request) {
        return runService.record(request);
    }
}
