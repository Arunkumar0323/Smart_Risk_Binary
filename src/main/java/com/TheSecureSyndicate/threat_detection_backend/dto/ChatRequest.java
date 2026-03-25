package com.TheSecureSyndicate.threat_detection_backend.dto;

public class ChatRequest {
    private String question;
    private String binaryId;
    private AnalysisContext analysisContext;
    private String conversationId;

    public ChatRequest() {}

    public ChatRequest(String question, String binaryId, AnalysisContext analysisContext, String conversationId) {
        this.question = question;
        this.binaryId = binaryId;
        this.analysisContext = analysisContext;
        this.conversationId = conversationId;
    }

    // Getters and Setters
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getBinaryId() {
        return binaryId;
    }

    public void setBinaryId(String binaryId) {
        this.binaryId = binaryId;
    }

    public AnalysisContext getAnalysisContext() {
        return analysisContext;
    }

    public void setAnalysisContext(AnalysisContext analysisContext) {
        this.analysisContext = analysisContext;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    // Inner class for analysis context
    public static class AnalysisContext {
        private String filename;
        private Double entropy;
        private Double trustScore;
        private Integer alertCount;
        private java.util.List<String> yaraMatches;
        private java.util.List<String> signatures;
        private String fileHash;
        private Double riskScore;

        public AnalysisContext() {}

        // Getters and Setters
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public Double getEntropy() { return entropy; }
        public void setEntropy(Double entropy) { this.entropy = entropy; }

        public Double getTrustScore() { return trustScore; }
        public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }

        public Integer getAlertCount() { return alertCount; }
        public void setAlertCount(Integer alertCount) { this.alertCount = alertCount; }

        public java.util.List<String> getYaraMatches() { return yaraMatches; }
        public void setYaraMatches(java.util.List<String> yaraMatches) { this.yaraMatches = yaraMatches; }

        public java.util.List<String> getSignatures() { return signatures; }
        public void setSignatures(java.util.List<String> signatures) { this.signatures = signatures; }

        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }

        public Double getRiskScore() { return riskScore; }
        public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    }
}
