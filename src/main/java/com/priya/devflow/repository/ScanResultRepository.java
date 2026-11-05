package com.priya.devflow.repository;

import com.priya.devflow.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ScanResultRepository extends JpaRepository<ScanResult, Long>, JpaSpecificationExecutor<ScanResult> {

    List<ScanResult> findAllByOrderByScannedAtDesc();
}
