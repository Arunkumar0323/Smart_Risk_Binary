package com.TheSecureSyndicate.threat_detection_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PESectionInfo {

    private String name;
    private Long rva;
    private Long virtualSize;
    private Long rawSize;

    // ✅ Limit output to 3 decimal places in JSON (e.g. 5.002)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.###")
    @Column(name = "entropy")
    private Double entropy;

    @Column(columnDefinition = "TEXT")  // ✅ Allows long descriptions
    private String characteristics;

    public PESectionInfo() {}

    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRva() {
        return rva;
    }

    public void setRva(long rva) {
        this.rva = rva;
    }

    public Long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public Long getRawSize() {
        return rawSize;
    }

    public void setRawSize(long rawSize) {
        this.rawSize = rawSize;
    }

    public Double getEntropy() {
        return entropy;
    }

    public void setEntropy(Double entropy) {
        this.entropy = entropy;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        // ✅ Optional: truncate if very long (safety check)
        if (characteristics != null && characteristics.length() > 4000) {
            this.characteristics = characteristics.substring(0, 4000);
        } else {
            this.characteristics = characteristics;
        }
    }
}
