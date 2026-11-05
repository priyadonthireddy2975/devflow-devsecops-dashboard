package com.priya.devflow.service;

import com.priya.devflow.entity.RunStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunServiceStatusTest {

    @Test
    void completedConclusionsMapToTerminalStatuses() {
        assertThat(RunService.mapStatus("completed", "success")).isEqualTo(RunStatus.SUCCESS);
        assertThat(RunService.mapStatus("completed", "neutral")).isEqualTo(RunStatus.SUCCESS);
        assertThat(RunService.mapStatus("completed", "failure")).isEqualTo(RunStatus.FAILED);
        assertThat(RunService.mapStatus("completed", "timed_out")).isEqualTo(RunStatus.FAILED);
        assertThat(RunService.mapStatus("completed", "cancelled")).isEqualTo(RunStatus.CANCELLED);
        assertThat(RunService.mapStatus("completed", "skipped")).isEqualTo(RunStatus.CANCELLED);
        assertThat(RunService.mapStatus("completed", null)).isEqualTo(RunStatus.FAILED);
    }

    @Test
    void inFlightStatesMapToActiveStatuses() {
        assertThat(RunService.mapStatus("in_progress", null)).isEqualTo(RunStatus.RUNNING);
        assertThat(RunService.mapStatus("queued", null)).isEqualTo(RunStatus.QUEUED);
        assertThat(RunService.mapStatus("requested", null)).isEqualTo(RunStatus.QUEUED);
        assertThat(RunService.mapStatus(null, null)).isEqualTo(RunStatus.QUEUED);
    }
}
