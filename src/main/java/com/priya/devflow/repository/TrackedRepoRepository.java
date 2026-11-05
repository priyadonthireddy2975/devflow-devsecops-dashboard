package com.priya.devflow.repository;

import com.priya.devflow.entity.TrackedRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackedRepoRepository extends JpaRepository<TrackedRepo, Long> {

    Optional<TrackedRepo> findByFullName(String fullName);

    List<TrackedRepo> findAllByOrderByFullNameAsc();
}
