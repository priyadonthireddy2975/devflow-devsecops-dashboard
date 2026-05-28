package com.priya.devflow.service;

import com.priya.devflow.entity.Build;
import com.priya.devflow.repository.BuildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildService {

    @Autowired
    private BuildRepository buildRepository;

    public List<Build> getAllBuilds() {
        return buildRepository.findAll();
    }

    public Build createBuild(Build build) {
        return buildRepository.save(build);
    }

    public Build updateBuild(Long id, Build updatedBuild) {

        Build build = buildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Build not found"));

        build.setBuildName(updatedBuild.getBuildName());
        build.setStatus(updatedBuild.getStatus());

        return buildRepository.save(build);
    }

    public void deleteBuild(Long id) {
        buildRepository.deleteById(id);
    }
}
