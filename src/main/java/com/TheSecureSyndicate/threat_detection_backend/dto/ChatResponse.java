package com.TheSecureSyndicate.threat_detection_backend.dto;

public class ChatResponse {
    private String answer;
    private String severity;
    private Double confidence;
    private Boolean suspicious;
    private String conversationId;
    private Long timestamp;
    private java.util.List<String> indicators;

    public ChatResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatResponse(String answer, String severity, Double confidence, Boolean suspicious) {
        this();
        this.answer = answer;
        this.severity = severity;
        this.confidence = confidence;
        this.suspicious = suspicious;
    }

    // Getters and Setters
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Boolean getSuspicious() {
        return suspicious;
    }

    public void setSuspicious(Boolean suspicious) {
        this.suspicious = suspicious;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public java.util.List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(java.util.List<String> indicators) {
        this.indicators = indicators;
    }
}
