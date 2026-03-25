package com.TheSecureSyndicate.threat_detection_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CuckooReport {

    @JsonProperty("score")
    private double score;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("domains")
    private List<String> domains = new ArrayList<>();

    @JsonProperty("urls")
    private List<String> urls = new ArrayList<>();

    @JsonProperty("signatures")
    private List<String> signatures = new ArrayList<>();

    @JsonProperty("behaviorSummary")
    private String behaviorSummary;

    public CuckooReport() {}

    public CuckooReport(double score, String summary, List<String> domains, List<String> urls,
                        List<String> signatures, String behaviorSummary) {
        this.score = score;
        this.summary = summary;
        this.domains = domains != null ? domains : new ArrayList<>();
        this.urls = urls != null ? urls : new ArrayList<>();
        this.signatures = signatures != null ? signatures : new ArrayList<>();
        this.behaviorSummary = behaviorSummary;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains != null ? domains : new ArrayList<>();
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls != null ? urls : new ArrayList<>();
    }

    public List<String> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<String> signatures) {
        this.signatures = signatures != null ? signatures : new ArrayList<>();
    }

    public String getBehaviorSummary() {
        return behaviorSummary;
    }

    public void setBehaviorSummary(String behaviorSummary) {
        this.behaviorSummary = behaviorSummary;
    }

    @Override
    public String toString() {
        return "CuckooReport{" +
                "score=" + score +
                ", summary='" + summary + '\'' +
                ", domains=" + domains +
                ", urls=" + urls +
                ", signatures=" + signatures +
                ", behaviorSummary='" + behaviorSummary + '\'' +
                '}';
    }
}
