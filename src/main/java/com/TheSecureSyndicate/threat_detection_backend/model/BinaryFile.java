package com.TheSecureSyndicate.threat_detection_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Entity
public class BinaryFile {

    @Id
    private String id;

    private String fileName;
    private String filePath;
    private long size;
    private double entropy;
    private String hash;
    private Instant uploadedAt;

    @Column(length = 512)
    private String originalFilename;

    @Column(length = 1024)
    private String ssdeepHash;

    private Integer cuckooTaskId;
    private Double cuckooScore;
    private String classification;

    public BinaryFile() {}

    public BinaryFile(String id, String fileName, String filePath, long size, double entropy,
                      String hash, Instant uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.size = size;
        this.entropy = entropy;
        this.hash = hash;
        this.uploadedAt = uploadedAt;

        // ✅ Ensure originalFilename is always set properly
        this.originalFilename = fileName;
    }

    // Getters and setters...

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (this.originalFilename == null) {
            this.originalFilename = fileName; // ✅ keep them consistent
        }
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public double getEntropy() { return entropy; }
    public void setEntropy(double entropy) { this.entropy = entropy; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public Integer getCuckooTaskId() { return cuckooTaskId; }
    public void setCuckooTaskId(Integer cuckooTaskId) { this.cuckooTaskId = cuckooTaskId; }

    public Double getCuckooScore() { return cuckooScore; }
    public void setCuckooScore(Double cuckooScore) { this.cuckooScore = cuckooScore; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
        if (this.fileName == null) {
            this.fileName = originalFilename; // ✅ fallback if only one is set
        }
    }

    public String getSsdeepHash() { return ssdeepHash; }
    public void setSsdeepHash(String ssdeepHash) { this.ssdeepHash = ssdeepHash; }

    @Override
    public String toString() {
        return "BinaryFile{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", size=" + size +
                ", entropy=" + entropy +
                ", hash='" + hash + '\'' +
                ", uploadedAt=" + uploadedAt +
                ", originalFilename='" + originalFilename + '\'' +
                ", ssdeepHash='" + ssdeepHash + '\'' +
                ", cuckooTaskId=" + cuckooTaskId +
                ", cuckooScore=" + cuckooScore +
                ", classification='" + classification + '\'' +
                '}';
    }

    // ✅ Ensures MultipartFile always returns a correct name
    public MultipartFile toMultipartFile() throws IOException {
        Path path = Path.of(this.filePath);
        byte[] content = Files.readAllBytes(path);

        final String finalName = (originalFilename != null && !originalFilename.isEmpty())
                ? originalFilename
                : fileName;

        return new MultipartFile() {
            @Override
            public String getName() {
                return finalName;
            }

            @Override
            public String getOriginalFilename() {
                return finalName;
            }

            @Override
            public String getContentType() {
                return "application/octet-stream";
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return content;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.write(dest.toPath(), content);
            }
        };
    }
}
