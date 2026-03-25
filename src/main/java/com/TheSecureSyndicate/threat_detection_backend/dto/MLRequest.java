package com.TheSecureSyndicate.threat_detection_backend.dto;

import java.util.List;

public class MLRequest {
    private double entropy;
    private long size;
    private List<String> yaraHits;
    private List<String> imports;
    private String hash;
    private Double cuckooScore;
    private int yaraHitCount;
    private String fileName;

    public double getEntropy() { return entropy; }
    public void setEntropy(double entropy) { this.entropy = entropy; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public List<String> getYaraHits() { return yaraHits; }
    public void setYaraHits(List<String> yaraHits) { this.yaraHits = yaraHits; }

    public List<String> getImports() { return imports; }
    public void setImports(List<String> imports) { this.imports = imports; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public Double getCuckooScore() { return cuckooScore; }
    public void setCuckooScore(Double cuckooScore) { this.cuckooScore = cuckooScore; }

    public int getYaraHitCount() { return yaraHitCount; }
    public void setYaraHitCount(int yaraHitCount) { this.yaraHitCount = yaraHitCount; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
