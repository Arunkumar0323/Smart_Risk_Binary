package com.TheSecureSyndicate.threat_detection_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import com.TheSecureSyndicate.threat_detection_backend.service.MLScoringService;
import com.TheSecureSyndicate.threat_detection_backend.dto.MLResponse;
import com.TheSecureSyndicate.threat_detection_backend.model.BinaryFile;
import com.TheSecureSyndicate.threat_detection_backend.model.YaraResult;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(ReportAnalysisController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MLScoringService mlScoringService;

    public ReportAnalysisController(MLScoringService mlScoringService) {
        this.mlScoringService = mlScoringService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Threat Detection Backend");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-report")
    public ResponseEntity<Map<String, Object>> analyzeReport(@RequestParam MultipartFile file) {
        try {
            // Check if file is JSON
            if (file.getSize() == 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".json") && !file.getContentType().contains("json"))) {
                return ResponseEntity.badRequest().body(createErrorResponse("Only JSON files are supported"));
            }

            // Parse JSON
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            JsonNode reportNode = objectMapper.readTree(content);

            // Create analysis response
            Map<String, Object> response = new HashMap<>();
            response.put("filename", fileName);
            response.put("timestamp", Instant.now().toString());
            response.put("fileSize", file.getSize());
            response.put("binaryId", UUID.randomUUID().toString());

            // Extract basic metrics from report
            extractMetrics(reportNode, response);
            response.put("digitallySigned", isDigitallySigned(reportNode));

            // Analyze report for alerts and issues
            // 1) Preserve alerts already present in uploaded JSON
            // 2) Add backend-detected alerts from known report fields
            List<Map<String, String>> providedAlerts = extractProvidedAlerts(reportNode);
            List<Map<String, String>> detectedAlerts = analyzeForIssues(reportNode);
            List<Map<String, String>> alerts = mergeAlerts(providedAlerts, detectedAlerts);
            response.put("alerts", alerts);

            // Add full analysis data
            response.put("analysis", reportNode);

            // Try to build BinaryFile and YaraResult from report where possible
            BinaryFile binary = new BinaryFile();
            binary.setFileName(fileName);
            binary.setSize(file.getSize());
            if (reportNode.has("entropy")) binary.setEntropy(reportNode.get("entropy").asDouble(0.0));
            if (reportNode.has("sha256")) binary.setHash(reportNode.get("sha256").asText(null));
            if (reportNode.has("malscore")) binary.setCuckooScore(reportNode.get("malscore").asDouble());

            YaraResult yaraResult = new YaraResult();
            yaraResult.setMatched(false);
            StringBuilder matched = new StringBuilder();
            if (reportNode.has("signatures") && reportNode.get("signatures").isArray()) {
                for (JsonNode s : reportNode.get("signatures")) {
                    if (s.has("name")) {
                        if (matched.length()>0) matched.append("\n");
                        matched.append(s.get("name").asText());
                    }
                }
            } else if (reportNode.has("yara") && reportNode.get("yara").has("rules")) {
                JsonNode rules = reportNode.get("yara").get("rules");
                if (rules.isArray()) {
                    for (JsonNode r : rules) {
                        if (matched.length()>0) matched.append("\n");
                        matched.append(r.asText());
                    }
                }
            }
            if (matched.length() > 0) {
                yaraResult.setMatched(true);
                yaraResult.setMatchedRules(matched.toString());
            }

            // Call ML scoring service (best-effort)
            MLResponse mlResponse = null;
            try {
                mlResponse = mlScoringService.scoreBinary(binary, yaraResult);
                response.put("mlScore", mlResponse);
            } catch (Exception e) {
                logger.warn("ML scoring failed: {}", e.getMessage());
            }

            // Calculate trust score first (base from heuristics)
            double baseTrustScore = calculateTrustScore(reportNode, alerts);

            // If ML result available, combine ML risk into trust score
            double finalTrustScore = baseTrustScore;
            if (mlResponse != null) {
                // mlResponse.riskScore is 0..1 where higher=more risky; convert to a trust-like 0..100 (higher=more trusted)
                double mlTrust = Math.max(0.0, Math.min(100.0, (1.0 - mlResponse.getRiskScore()) * 100.0));
                // Weighted combination: 60% base heuristics, 40% ML
                finalTrustScore = Math.max(0.0, Math.min(100.0, baseTrustScore * 0.6 + mlTrust * 0.4));
            }

            response.put("trustScore", finalTrustScore);

            // Calculate risk level based on both alerts and trust score
            String riskLevel = calculateRiskLevel(alerts, finalTrustScore);
            response.put("riskLevel", riskLevel);

            logger.info("Successfully analyzed report: {}", fileName);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error reading file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error analyzing report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error analyzing report: " + e.getMessage()));
        }
    }

    private void extractMetrics(JsonNode reportNode, Map<String, Object> response) {
        try {
            // Try to extract common metrics from the report
            if (reportNode.has("sha256")) {
                response.put("sha256", reportNode.get("sha256").asText());
            }
            if (reportNode.has("entropy")) {
                response.put("entropy", reportNode.get("entropy").asDouble());
            } else {
                response.put("entropy", 0.0);
            }
            if (reportNode.has("size")) {
                response.put("fileSize", reportNode.get("size").asLong());
            }
        } catch (Exception e) {
            logger.warn("Error extracting metrics: {}", e.getMessage());
        }
    }

    private List<Map<String, String>> analyzeForIssues(JsonNode reportNode) {
        List<Map<String, String>> alerts = new ArrayList<>();

        try {
            // Check for suspicious patterns
            if (reportNode.has("behaviors")) {
                JsonNode behaviors = reportNode.get("behaviors");
                if (behaviors.isArray()) {
                    for (JsonNode behavior : behaviors) {
                        String behaviorStr = behavior.asText().toLowerCase();
                        if (behaviorStr.contains("inject") || behaviorStr.contains("hook")) {
                            alerts.add(createAlert("High", "Code injection detected"));
                        }
                        if (behaviorStr.contains("registry") && behaviorStr.contains("modify")) {
                            alerts.add(createAlert("High", "Registry modification detected"));
                        }
                        if (behaviorStr.contains("delete") || behaviorStr.contains("overwrite")) {
                            alerts.add(createAlert("Medium", "File system manipulation detected"));
                        }
                    }
                }
            }

            // Check for suspicious APIs
            if (reportNode.has("apicalls")) {
                JsonNode apiCalls = reportNode.get("apicalls");
                if (apiCalls.isArray()) {
                    for (JsonNode call : apiCalls) {
                        String apiName = call.asText().toLowerCase();
                        if (apiName.contains("createremotethread") || apiName.contains("virtualalloc")) {
                            alerts.add(createAlert("High", "Process injection API detected: " + apiName));
                        }
                        if (apiName.contains("regsetvalueex") || apiName.contains("regcreatekeyex")) {
                            alerts.add(createAlert("Medium", "Registry operation detected"));
                        }
                    }
                }
            }

            // Check for network activity
            if (reportNode.has("network")) {
                JsonNode network = reportNode.get("network");
                if (network.isArray() && network.size() > 0) {
                    alerts.add(createAlert("Medium", "Network activity detected: " + network.size() + " connections"));
                }
            }

            // Check for dropped files
            if (reportNode.has("dropped")) {
                JsonNode dropped = reportNode.get("dropped");
                if (dropped.isArray() && dropped.size() > 0) {
                    alerts.add(createAlert("High", "Dropped files detected: " + dropped.size() + " file(s)"));
                }
            }

            // Check for mutex creation
            if (reportNode.has("mutexes")) {
                JsonNode mutexes = reportNode.get("mutexes");
                if (mutexes.isArray() && mutexes.size() > 0) {
                    alerts.add(createAlert("Low", "Mutex created (potential ransomware indicator)"));
                }
            }

            // Support nested schemas under "analysis"
            JsonNode analysisNode = reportNode.path("analysis");
            JsonNode behavioral = analysisNode.path("behavioral_analysis");
            JsonNode staticAnalysis = analysisNode.path("static_analysis");

            JsonNode networkConnections = behavioral.path("network_connections");
            if (networkConnections.isArray() && networkConnections.size() > 0) {
                alerts.add(createAlert("Medium", "Network activity detected: " + networkConnections.size() + " connections"));
            }

            JsonNode registryAccess = behavioral.path("registry_access");
            if (registryAccess.isArray()) {
                for (JsonNode regEntry : registryAccess) {
                    String regPath = regEntry.asText("").toLowerCase();
                    if (regPath.contains("\\run") || regPath.contains("runonce") || regPath.contains("currentversion\\run")) {
                        alerts.add(createAlert("High", "Registry persistence activity detected"));
                        break;
                    }
                }
            }

            JsonNode processesCreated = behavioral.path("processes_created");
            if (processesCreated.isArray()) {
                for (JsonNode process : processesCreated) {
                    String processText = process.asText("").toLowerCase();
                    if (processText.contains("powershell") || processText.contains("cmd.exe")) {
                        alerts.add(createAlert("High", "Suspicious shell process execution detected"));
                        break;
                    }
                }
            }

            if (staticAnalysis.path("packed").asBoolean(false)) {
                alerts.add(createAlert("Critical", "Packed executable detected"));
            }

        } catch (Exception e) {
            logger.warn("Error analyzing report for issues: {}", e.getMessage());
        }

        return alerts;
    }

    private List<Map<String, String>> extractProvidedAlerts(JsonNode reportNode) {
        List<Map<String, String>> alerts = new ArrayList<>();
        JsonNode alertsNode = reportNode.path("alerts");
        if (!alertsNode.isArray()) {
            return alerts;
        }

        for (JsonNode alertNode : alertsNode) {
            String severity = alertNode.path("severity").asText("").trim();
            String message = alertNode.path("message").asText("").trim();
            if (!severity.isEmpty() && !message.isEmpty()) {
                alerts.add(createAlert(severity, message));
            }
        }
        return alerts;
    }

    private List<Map<String, String>> mergeAlerts(List<Map<String, String>> providedAlerts,
                                                  List<Map<String, String>> detectedAlerts) {
        List<Map<String, String>> merged = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Map<String, String> alert : providedAlerts) {
            String key = (alert.getOrDefault("severity", "") + "|" + alert.getOrDefault("message", "")).toLowerCase();
            if (seen.add(key)) {
                merged.add(alert);
            }
        }

        for (Map<String, String> alert : detectedAlerts) {
            String key = (alert.getOrDefault("severity", "") + "|" + alert.getOrDefault("message", "")).toLowerCase();
            if (seen.add(key)) {
                merged.add(alert);
            }
        }

        return merged;
    }

    private Map<String, String> createAlert(String severity, String message) {
        Map<String, String> alert = new HashMap<>();
        alert.put("severity", severity);
        alert.put("message", message);
        return alert;
    }

    private String calculateRiskLevel(List<Map<String, String>> alerts, double trustScore) {
        // Risk level based on trust score (primary factor)
        // Trust Score mapping:
        // 0-30: Critical
        // 31-50: High
        // 51-70: Medium
        // 71-100: Low
        
        String riskFromTrustScore;
        if (trustScore <= 30) {
            riskFromTrustScore = "Critical";
        } else if (trustScore <= 50) {
            riskFromTrustScore = "High";
        } else if (trustScore <= 70) {
            riskFromTrustScore = "Medium";
        } else {
            riskFromTrustScore = "Low";
        }
        
        // If no alerts, return risk based on trust score
        if (alerts.isEmpty()) {
            return riskFromTrustScore;
        }

        // Count critical and high severity alerts
        int criticalCount = 0;
        int highCount = 0;

        for (Map<String, String> alert : alerts) {
            String severity = alert.get("severity");
            if ("Critical".equalsIgnoreCase(severity)) {
                criticalCount++;
            } else if ("High".equalsIgnoreCase(severity)) {
                highCount++;
            }
        }

        // Determine risk from alerts
        String riskFromAlerts;
        if (criticalCount > 0) {
            riskFromAlerts = "Critical";
        } else if (highCount >= 2) {
            riskFromAlerts = "High";
        } else if (highCount > 0 || alerts.size() >= 3) {
            riskFromAlerts = "Medium";
        } else {
            riskFromAlerts = "Low";
        }

        // Combine both factors - take the higher risk
        return compareRiskLevels(riskFromAlerts, riskFromTrustScore);
    }

    private String compareRiskLevels(String riskFromAlerts, String riskFromTrustScore) {
        // Risk severity order: Critical > High > Medium > Low
        int alertsRiskValue = getRiskValue(riskFromAlerts);
        int trustRiskValue = getRiskValue(riskFromTrustScore);
        
        return alertsRiskValue >= trustRiskValue ? riskFromAlerts : riskFromTrustScore;
    }

    private int getRiskValue(String risk) {
        switch(risk.toLowerCase()) {
            case "critical":
                return 4;
            case "high":
                return 3;
            case "medium":
                return 2;
            case "low":
            default:
                return 1;
        }
    }

    private double calculateTrustScore(JsonNode reportNode, List<Map<String, String>> alerts) {
        double score = 100.0; // Start with 100

        // If file is not digitally signed, significantly deduct from trust score
        if (!isDigitallySigned(reportNode)) {
            score -= 30; // Major deduction for unsigned files
        }

        // Deduct points for each alert
        for (Map<String, String> alert : alerts) {
            String severity = alert.get("severity");
            switch (severity) {
                case "Critical":
                    score -= 25;
                    break;
                case "High":
                    score -= 15;
                    break;
                case "Medium":
                    score -= 10;
                    break;
                case "Low":
                    score -= 5;
                    break;
            }
        }

        // Ensure score stays within bounds
        return Math.max(0, Math.min(100, score));
    }

    private boolean isDigitallySigned(JsonNode reportNode) {
        // Schema 1: { "digital_signature": { "signed": true } }
        JsonNode digitalSignature = reportNode.path("digital_signature");
        if (!digitalSignature.isMissingNode() && digitalSignature.path("signed").asBoolean(false)) {
            return true;
        }

        // Schema 2: { "static": { "pe": { "digital_signers": [...] } } }
        JsonNode digitalSigners = reportNode.path("static").path("pe").path("digital_signers");
        if (digitalSigners.isArray() && digitalSigners.size() > 0) {
            return true;
        }
        if (!digitalSigners.isMissingNode() && !digitalSigners.isNull() && !digitalSigners.isArray()) {
            String val = digitalSigners.asText("").trim();
            if (!val.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}
