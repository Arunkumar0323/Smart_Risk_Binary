package com.TheSecureSyndicate.threat_detection_backend.dto;

public class MLResponse {
    private double riskScore;
    private String classification;

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
}
