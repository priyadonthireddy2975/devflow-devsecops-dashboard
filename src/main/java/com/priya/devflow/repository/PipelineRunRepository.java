package com.priya.devflow.repository;

import com.priya.devflow.entity.PipelineRun;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.entity.TrackedRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long>, JpaSpecificationExecutor<PipelineRun> {

    Optional<PipelineRun> findByRepoAndExternalId(TrackedRepo repo, Long externalId);

    Optional<PipelineRun> findFirstByRepoOrderByStartedAtDesc(TrackedRepo repo);

    long countByStatus(RunStatus status);

    List<PipelineRun> findByStartedAtAfter(Instant since);

    @Query("select avg(r.durationSeconds) from PipelineRun r where r.durationSeconds is not null")
    Double averageDuration();

    @Query("select r.repo.id, r.status, count(r) from PipelineRun r group by r.repo.id, r.status")
    List<Object[]> countByRepoAndStatus();
}
