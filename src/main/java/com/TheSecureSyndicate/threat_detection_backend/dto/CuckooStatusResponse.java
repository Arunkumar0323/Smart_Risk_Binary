package com.TheSecureSyndicate.threat_detection_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CuckooStatusResponse {

    @JsonProperty("task_id")
    private Integer taskId;

    @JsonProperty("task")
    private TaskStatus task;

    public CuckooStatusResponse() {}

    public CuckooStatusResponse(Integer taskId, TaskStatus task) {
        this.taskId = taskId;
        this.task = task;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getTask() {
        return task;
    }

    public void setTask(TaskStatus task) {
        this.task = task;
    }

    public String getStatus() {
        return task != null ? task.getStatus() : "unknown";
    }

    @Override
    public String toString() {
        return "CuckooStatusResponse{" +
                "taskId=" + taskId +
                ", status='" + getStatus() + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskStatus {
        @JsonProperty("status")
        private String status;

        public TaskStatus() {}

        public TaskStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
