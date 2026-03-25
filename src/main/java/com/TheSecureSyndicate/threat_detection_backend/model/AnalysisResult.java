package com.TheSecureSyndicate.threat_detection_backend.model;

import com.TheSecureSyndicate.threat_detection_backend.util.PEFunctionParser.PEHeaderResult;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class AnalysisResult {

    @Id
    private String binaryId;

    // General analysis
    private boolean yaraMatched;

    @ElementCollection
    private List<String> matchedRules = new ArrayList<>();

    private double mlRiskScore;
    private String classification;

    // File metadata
    private String filename; // ✅ Added field for proper filename tracking

    // Cuckoo sandbox results
    private Integer cuckooTaskId;
    private Double cuckooScore;

    @ElementCollection
    private List<String> cuckooDomains = new ArrayList<>();

    @ElementCollection
    private List<String> cuckooUrls = new ArrayList<>();

    @ElementCollection
    private List<String> cuckooSignatures = new ArrayList<>();

    @Column(length = 5000)
    private String cuckooBehaviorSummary;

    private Instant timestamp;

    // Extracted artifacts
    @ElementCollection
    private List<String> extractedStrings = new ArrayList<>();

    @ElementCollection
    private List<String> detectedIocs = new ArrayList<>();

    @ElementCollection
    private List<String> importedFunctions = new ArrayList<>();

    @ElementCollection
    private List<String> exportedFunctions = new ArrayList<>();

    // PE Header Metadata
    private String machineType;
    private long entryPoint;
    private long imageBase;
    private String subsystem;
    private String dllCharacteristics;
    private int numSections;
    private String characteristics;

    // PE Section Table
    @ElementCollection
    @CollectionTable(name = "section_info", joinColumns = @JoinColumn(name = "analysis_id"))
    private List<PESectionInfo> sections = new ArrayList<>();

    // Packed/unpacked detection
    private Boolean packed = false;

    // Keep transient so it isn’t persisted, but can be used in runtime
    @Transient
    private PEHeaderResult headerResult;

    public AnalysisResult() {}

    public AnalysisResult(String binaryId, boolean yaraMatched, List<String> matchedRules,
                          double mlRiskScore, String classification, Instant timestamp) {
        this.binaryId = binaryId;
        this.yaraMatched = yaraMatched;
        this.matchedRules = matchedRules;
        this.mlRiskScore = mlRiskScore;
        this.classification = classification;
        this.timestamp = timestamp;
    }

    /**
     * ✅ Populate PE header and section metadata.
     */
    public void setHeaderResult(PEHeaderResult header) {
        this.headerResult = header;
        if (header == null) return;

        this.machineType = header.getMachineType();
        this.entryPoint = header.getEntryPoint();
        this.imageBase = header.getImageBase();
        this.subsystem = header.getSubsystem();
        this.dllCharacteristics = header.getDllCharacteristics();
        this.numSections = header.getNumSections();
        this.characteristics = header.getCharacteristics();
        this.sections = header.getSections();
    }

    public PEHeaderResult getHeaderResult() {
        return this.headerResult;
    }

    // ----------------- Getters and Setters -----------------

    public String getBinaryId() { return binaryId; }
    public void setBinaryId(String binaryId) { this.binaryId = binaryId; }

    public boolean isYaraMatched() { return yaraMatched; }
    public void setYaraMatched(boolean yaraMatched) { this.yaraMatched = yaraMatched; }

    public List<String> getMatchedRules() { return matchedRules; }
    public void setMatchedRules(List<String> matchedRules) { this.matchedRules = matchedRules; }

    public double getMlRiskScore() { return mlRiskScore; }
    public void setMlRiskScore(double mlRiskScore) { this.mlRiskScore = mlRiskScore; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getFilename() { return filename; }              // ✅ Added getter
    public void setFilename(String filename) { this.filename = filename; }  // ✅ Added setter

    public Integer getCuckooTaskId() { return cuckooTaskId; }
    public void setCuckooTaskId(Integer cuckooTaskId) { this.cuckooTaskId = cuckooTaskId; }

    public Double getCuckooScore() { return cuckooScore; }
    public void setCuckooScore(Double cuckooScore) { this.cuckooScore = cuckooScore; }

    public List<String> getCuckooDomains() { return cuckooDomains; }
    public void setCuckooDomains(List<String> cuckooDomains) { this.cuckooDomains = cuckooDomains; }

    public List<String> getCuckooUrls() { return cuckooUrls; }
    public void setCuckooUrls(List<String> cuckooUrls) { this.cuckooUrls = cuckooUrls; }

    public List<String> getCuckooSignatures() { return cuckooSignatures; }
    public void setCuckooSignatures(List<String> cuckooSignatures) { this.cuckooSignatures = cuckooSignatures; }

    public String getCuckooBehaviorSummary() { return cuckooBehaviorSummary; }
    public void setCuckooBehaviorSummary(String cuckooBehaviorSummary) { this.cuckooBehaviorSummary = cuckooBehaviorSummary; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<String> getExtractedStrings() { return extractedStrings; }
    public void setExtractedStrings(List<String> extractedStrings) { this.extractedStrings = extractedStrings; }

    public List<String> getDetectedIocs() { return detectedIocs; }
    public void setDetectedIocs(List<String> detectedIocs) { this.detectedIocs = detectedIocs; }

    public List<String> getImportedFunctions() { return importedFunctions; }
    public void setImportedFunctions(List<String> importedFunctions) { this.importedFunctions = importedFunctions; }

    public List<String> getExportedFunctions() { return exportedFunctions; }
    public void setExportedFunctions(List<String> exportedFunctions) { this.exportedFunctions = exportedFunctions; }

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public long getEntryPoint() { return entryPoint; }
    public void setEntryPoint(long entryPoint) { this.entryPoint = entryPoint; }

    public long getImageBase() { return imageBase; }
    public void setImageBase(long imageBase) { this.imageBase = imageBase; }

    public String getSubsystem() { return subsystem; }
    public void setSubsystem(String subsystem) { this.subsystem = subsystem; }

    public String getDllCharacteristics() { return dllCharacteristics; }
    public void setDllCharacteristics(String dllCharacteristics) { this.dllCharacteristics = dllCharacteristics; }

    public int getNumSections() { return numSections; }
    public void setNumSections(int numSections) { this.numSections = numSections; }

    public String getCharacteristics() { return characteristics; }
    public void setCharacteristics(String characteristics) { this.characteristics = characteristics; }

    public List<PESectionInfo> getSections() { return sections; }
    public void setSections(List<PESectionInfo> sections) { this.sections = sections; }

    public Boolean getPacked() { return packed; }
    public void setPacked(Boolean packed) { this.packed = packed; }

    // ✅ Add boolean-style getter for controller compatibility
    public boolean isPacked() {
        return packed != null && packed;
    }

    // ----------------- toString -----------------
    @Override
    public String toString() {
        return "AnalysisResult{" +
                "binaryId='" + binaryId + '\'' +
                ", yaraMatched=" + yaraMatched +
                ", matchedRules=" + matchedRules +
                ", mlRiskScore=" + mlRiskScore +
                ", classification='" + classification + '\'' +
                ", filename='" + filename + '\'' +
                ", cuckooTaskId=" + cuckooTaskId +
                ", cuckooScore=" + cuckooScore +
                ", timestamp=" + timestamp +
                ", machineType='" + machineType + '\'' +
                ", entryPoint=0x" + Long.toHexString(entryPoint) +
                ", imageBase=0x" + Long.toHexString(imageBase) +
                ", subsystem='" + subsystem + '\'' +
                ", numSections=" + numSections +
                ", characteristics='" + characteristics + '\'' +
                ", packed=" + packed +
                '}';
    }
}
