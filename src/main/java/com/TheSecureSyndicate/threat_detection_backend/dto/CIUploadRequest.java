package com.TheSecureSyndicate.threat_detection_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CIUploadRequest {

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("base64File")
    private String base64File;

    public CIUploadRequest() {}

    public CIUploadRequest(String fileName, String base64File) {
        this.fileName = fileName;
        this.base64File = base64File;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBase64File() {
        return base64File;
    }

    public void setBase64File(String base64File) {
        this.base64File = base64File;
    }

    @Override
    public String toString() {
        return "CIUploadRequest{fileName='" + fileName + "', base64File=[...]}";
    }

    /**
     * Adapter to wrap decoded byte[] as MultipartFile for reuse in parsers.
     */
    public static class MultipartWrapper implements MultipartFile {

        private final byte[] content;
        private final String originalFilename;

        public MultipartWrapper(String originalFilename, byte[] content) {
            this.originalFilename = originalFilename;
            this.content = content;
        }

        @Override
        public String getName() {
            return originalFilename;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
