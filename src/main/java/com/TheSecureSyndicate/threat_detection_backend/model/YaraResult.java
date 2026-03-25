package com.TheSecureSyndicate.threat_detection_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class YaraResult {

    @Id
    private String id;
    private String binaryId;
    private boolean matched;
    private String matchedRules;
    private Instant scannedAt;

    public YaraResult() {
    }

    public YaraResult(String id, String binaryId, boolean matched, String matchedRules, Instant scannedAt) {
        this.id = id;
        this.binaryId = binaryId;
        this.matched = matched;
        this.matchedRules = matchedRules;
        this.scannedAt = scannedAt;
    }

    // ---------------- Getters and Setters ----------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBinaryId() {
        return binaryId;
    }

    public void setBinaryId(String binaryId) {
        this.binaryId = binaryId;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getMatchedRules() {
        return matchedRules;
    }

    public void setMatchedRules(String matchedRules) {
        this.matchedRules = matchedRules;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(Instant scannedAt) {
        this.scannedAt = scannedAt;
    }

  

    @Override
    public String toString() {
        return "YaraResult{" +
                "id='" + id + '\'' +
                ", binaryId='" + binaryId + '\'' +
                ", matched=" + matched +
                ", matchedRules='" + matchedRules + '\'' +
                ", scannedAt=" + scannedAt +
                '}';
    }
}
