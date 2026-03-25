package com.TheSecureSyndicate.threat_detection_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CuckooSubmissionResponse {

    @JsonProperty("task_id")
    private int taskId;

    public CuckooSubmissionResponse() {}

    public CuckooSubmissionResponse(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "CuckooSubmissionResponse{" +
                "taskId=" + taskId +
                '}';
    }
}
