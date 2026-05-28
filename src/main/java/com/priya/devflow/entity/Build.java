package com.priya.devflow.entity;

import jakarta.persistence.*;

@Entity
public class Build {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buildName;

    private String status;

    public Build() {
    }

    public Build(String buildName, String status) {
        this.buildName = buildName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
