package com.priya.devflow.controller;

import com.priya.devflow.dto.Dtos.AddRepoRequest;
import com.priya.devflow.dto.Dtos.RepoDto;
import com.priya.devflow.service.GithubSyncService;
import com.priya.devflow.service.RepoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repositories")
public class RepoController {

    private final RepoService repoService;
    private final GithubSyncService syncService;

    public RepoController(RepoService repoService, GithubSyncService syncService) {
        this.repoService = repoService;
        this.syncService = syncService;
    }

    @GetMapping
    public List<RepoDto> list() {
        return repoService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepoDto add(@Valid @RequestBody AddRepoRequest request) {
        return repoService.add(request.fullName());
    }

    @PostMapping("/{id}/sync")
    public Map<String, Integer> sync(@PathVariable Long id) {
        return Map.of("synced", syncService.syncRepo(repoService.get(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repoService.delete(id);
    }
}
