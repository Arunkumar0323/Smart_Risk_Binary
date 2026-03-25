package com.TheSecureSyndicate.threat_detection_backend.service;

import com.TheSecureSyndicate.threat_detection_backend.dto.CuckooSubmissionResponse;
import com.TheSecureSyndicate.threat_detection_backend.dto.CuckooStatusResponse;
import com.TheSecureSyndicate.threat_detection_backend.dto.CuckooReport;
import com.TheSecureSyndicate.threat_detection_backend.model.BinaryFile;
import com.TheSecureSyndicate.threat_detection_backend.model.AnalysisResult;
import com.TheSecureSyndicate.threat_detection_backend.repository.AnalysisResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.TheSecureSyndicate.threat_detection_backend.util.IOCExtractor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.time.Instant;

@Service
public class CuckooSandboxService {

    private static final Logger logger = LoggerFactory.getLogger(CuckooSandboxService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String cuckooHost = "http://localhost:8090";

    private final AnalysisResultRepository resultRepository;

    public CuckooSandboxService(AnalysisResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public int submitFile(Path filePath) {
        FileSystemResource resource = new FileSystemResource(filePath.toFile());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<CuckooSubmissionResponse> response = restTemplate.postForEntity(
            cuckooHost + "/tasks/create/file", request, CuckooSubmissionResponse.class
        );

        return Optional.ofNullable(response.getBody())
                       .map(CuckooSubmissionResponse::getTaskId)
                       .orElseThrow(() -> new RuntimeException("No task ID returned from Cuckoo"));
    }

    public void submitBinary(BinaryFile binary) {
        try {
            Path filePath = Path.of(binary.getFilePath());

            if (!Files.exists(filePath)) {
                logger.error("❌ File not found: {}", filePath);
                return;
            }

            int taskId = submitFile(filePath);
            logger.info("📦 Submitted to Cuckoo: Task ID {}", taskId);

            if (waitForCompletion(taskId)) {
                CuckooReport report = getReport(taskId);
                logger.info("✅ Cuckoo Report Parsed: Score = {}", report.getScore());

                AnalysisResult result = new AnalysisResult();
                result.setBinaryId(binary.getId()); // assuming BinaryFile has getId()
                result.setCuckooTaskId(taskId);
                result.setCuckooScore(report.getScore());
                result.setCuckooDomains(report.getDomains());
                result.setCuckooUrls(report.getUrls());
                result.setCuckooSignatures(report.getSignatures());
                result.setCuckooBehaviorSummary(report.getBehaviorSummary());
                result.setTimestamp(Instant.now());

                resultRepository.save(result);
                
                logger.info("🗂️ Report saved to DB for Task ID {}", taskId);
            } else {
                logger.warn("⏳ Cuckoo analysis timed out for Task ID {}", taskId);
            }

        } catch (Exception e) {
            logger.error("❌ Failed to submit binary to Cuckoo", e);
        }
    }

    public boolean waitForCompletion(int taskId) throws InterruptedException {
        String url = cuckooHost + "/tasks/view/" + taskId;
        for (int i = 0; i < 60; i++) {
            ResponseEntity<CuckooStatusResponse> response = restTemplate.getForEntity(url, CuckooStatusResponse.class);
            String status = Optional.ofNullable(response.getBody())
                                    .map(CuckooStatusResponse::getStatus)
                                    .orElse("unknown");
            logger.debug("🔄 Polling Cuckoo: Task {} Status = {}", taskId, status);
            if ("reported".equals(status)) return true;
            Thread.sleep(5000);
        }
        return false;
    }

    public CuckooReport getReport(int taskId) {
        ResponseEntity<String> response = restTemplate.getForEntity(
            cuckooHost + "/tasks/report/" + taskId, String.class
        );

        try {
            String rawJson = response.getBody();
            Map<String, Object> iocs = IOCExtractor.extract(rawJson);

            CuckooReport report = new CuckooReport();
            report.setScore((Double) iocs.get("score"));
            report.setDomains((List<String>) iocs.get("domains"));
            report.setUrls((List<String>) iocs.get("urls"));
            report.setSignatures((List<String>) iocs.get("signatures"));
            report.setBehaviorSummary((String) iocs.get("behaviorSummary"));
            report.setSummary("Extracted via IOCExtractor"); // Optional placeholder

            return report;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Cuckoo report", e);
        }
    }

}
