package com.TheSecureSyndicate.threat_detection_backend.dto;

import com.TheSecureSyndicate.threat_detection_backend.model.PESectionInfo;
import java.util.List;

/**
 * DTO that represents the summarized analysis result for a binary upload.
 */
public class AnalysisSummaryResponse {

    private String binaryId;
    private String filename;
    private boolean yaraMatched;
    private List<String> matchedRules;
    private double mlRiskScore;
    private String classification;

    // 🔍 PE metadata fields
    private String machineType;
    private long entryPoint;
    private long imageBase;
    private String subsystem;
    private List<PESectionInfo> sections;

    // ✅ Packed/Unpacked
    private Boolean packed;

    /** Default constructor for JSON deserialization. */
    public AnalysisSummaryResponse() {}

    /** Minimal constructor */
    public AnalysisSummaryResponse(String binaryId, String filename, boolean yaraMatched,
                                   List<String> matchedRules, double mlRiskScore, String classification) {
        this.binaryId = binaryId;
        this.filename = safeFilename(filename);
        this.yaraMatched = yaraMatched;
        this.matchedRules = matchedRules;
        this.mlRiskScore = mlRiskScore;
        this.classification = classification;
    }

    /** ✅ Full constructor with metadata */
    public AnalysisSummaryResponse(String binaryId, String filename, boolean yaraMatched,
                                   List<String> matchedRules, double mlRiskScore, String classification,
                                   String machineType, long entryPoint, long imageBase,
                                   String subsystem, List<PESectionInfo> sections, Boolean packed) {
        this.binaryId = binaryId;
        this.filename = safeFilename(filename);
        this.yaraMatched = yaraMatched;
        this.matchedRules = matchedRules;
        this.mlRiskScore = mlRiskScore;
        this.classification = classification;
        this.machineType = machineType;
        this.entryPoint = entryPoint;
        this.imageBase = imageBase;
        this.subsystem = subsystem;
        this.sections = sections;
        this.packed = packed;
    }

    // ✅ Ensures filename is never null or empty
    private String safeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "unknown";
        }
        return name;
    }

    // Getters and Setters
    public String getBinaryId() { return binaryId; }
    public void setBinaryId(String binaryId) { this.binaryId = binaryId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = safeFilename(filename); }

    public boolean isYaraMatched() { return yaraMatched; }
    public void setYaraMatched(boolean yaraMatched) { this.yaraMatched = yaraMatched; }

    public List<String> getMatchedRules() { return matchedRules; }
    public void setMatchedRules(List<String> matchedRules) { this.matchedRules = matchedRules; }

    public double getMlRiskScore() { return mlRiskScore; }
    public void setMlRiskScore(double mlRiskScore) { this.mlRiskScore = mlRiskScore; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public long getEntryPoint() { return entryPoint; }
    public void setEntryPoint(long entryPoint) { this.entryPoint = entryPoint; }

    public long getImageBase() { return imageBase; }
    public void setImageBase(long imageBase) { this.imageBase = imageBase; }

    public String getSubsystem() { return subsystem; }
    public void setSubsystem(String subsystem) { this.subsystem = subsystem; }

    public List<PESectionInfo> getSections() { return sections; }
    public void setSections(List<PESectionInfo> sections) { this.sections = sections; }

    public Boolean getPacked() { return packed; }
    public void setPacked(Boolean packed) { this.packed = packed; }

    @Override
    public String toString() {
        return "AnalysisSummaryResponse{" +
                "binaryId='" + binaryId + '\'' +
                ", filename='" + filename + '\'' +
                ", yaraMatched=" + yaraMatched +
                ", matchedRules=" + matchedRules +
                ", mlRiskScore=" + mlRiskScore +
                ", classification='" + classification + '\'' +
                ", machineType='" + machineType + '\'' +
                ", entryPoint=" + entryPoint +
                ", imageBase=" + imageBase +
                ", subsystem='" + subsystem + '\'' +
                ", sections=" + (sections != null ? sections.size() : 0) +
                ", packed=" + packed +
                '}';
    }
}
