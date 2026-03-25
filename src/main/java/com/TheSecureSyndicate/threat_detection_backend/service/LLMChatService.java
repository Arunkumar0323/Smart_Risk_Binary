package com.TheSecureSyndicate.threat_detection_backend.service;

import com.TheSecureSyndicate.threat_detection_backend.dto.ChatRequest;
import com.TheSecureSyndicate.threat_detection_backend.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LLMChatService {

    @Value("${app.openai.enabled:false}")
    private boolean openaiEnabled;

    @Value("${app.openai.api-key:}")
    private String openaiApiKey;

    public ChatResponse askQuestion(ChatRequest request) {
        String question = request.getQuestion().toLowerCase();
        ChatRequest.AnalysisContext context = request.getAnalysisContext();

        // Create default context if not provided
        if (context == null) {
            context = new ChatRequest.AnalysisContext();
            context.setFilename("unknown");
            context.setEntropy(0.0);
            context.setTrustScore(0.5);
            context.setAlertCount(0);
            context.setYaraMatches(new ArrayList<>());
        }

        // Use local heuristics for demo (no API key needed)
        return generateLocalResponse(question, context, request.getConversationId());
    }

    private ChatResponse generateLocalResponse(String question, ChatRequest.AnalysisContext context, String conversationId) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(conversationId);

        // Extract context values - context should never be null now
        Double entropy = context.getEntropy() != null ? context.getEntropy() : 0.0;
        Double trustScore = context.getTrustScore() != null ? context.getTrustScore() : 0.5;
        Integer alertCount = context.getAlertCount() != null ? context.getAlertCount() : 0;
        List<String> yaraMatches = context.getYaraMatches() != null ? context.getYaraMatches() : new ArrayList<>();
        String filename = context.getFilename() != null ? context.getFilename() : "unknown";

        // Calculate risk level
        String severity = calculateSeverity(entropy, trustScore, alertCount, yaraMatches.size());
        Boolean isSuspicious = trustScore < 0.5 || entropy > 7.0 || alertCount > 2;
        Double confidence = calculateConfidence(entropy, alertCount, yaraMatches.size());

        // Generate answer based on question
        String answer = generateAnswer(question, context, severity, isSuspicious);
        List<String> indicators = extractIndicators(context);

        response.setAnswer(answer);
        response.setSeverity(severity);
        response.setConfidence(confidence);
        response.setSuspicious(isSuspicious);
        response.setIndicators(indicators);

        return response;
    }

    private String generateAnswer(String question, ChatRequest.AnalysisContext context, String severity, Boolean isSuspicious) {
        Double entropy = context.getEntropy() != null ? context.getEntropy() : 0.0;
        Double trustScore = context.getTrustScore() != null ? context.getTrustScore() : 0.5;
        Integer alertCount = context.getAlertCount() != null ? context.getAlertCount() : 0;
        List<String> yaraMatches = context.getYaraMatches() != null ? context.getYaraMatches() : new ArrayList<>();
        String filename = context.getFilename() != null ? context.getFilename() : "unknown";

        if (question.contains("suspicious") || question.contains("why")) {
            return String.format(
                "⚠️ Analysis for %s:\n\n" +
                "This file shows %s indicators of malicious activity:\n" +
                "• Entropy Score: %.2f %s\n" +
                "• Trust Score: %.2f (%.1f%% confidence)\n" +
                "• Security Alerts: %d detected\n" +
                "• YARA Matches: %d signature(s) matched\n\n" +
                "Recommendation: %s",
                filename,
                isSuspicious ? "STRONG" : "WEAK",
                entropy, entropy > 7.0 ? "(High - encryption/obfuscation detected)" : "(Normal)",
                trustScore, trustScore * 100,
                alertCount,
                yaraMatches.size(),
                getSeverityRecommendation(severity)
            );
        } else if (question.contains("ransomware") || question.contains("encryption")) {
            return String.format(
                "🔒 Ransomware Analysis:\n\n" +
                "High entropy (%.2f) suggests encryption/obfuscation.\n" +
                "YARA matches: %d\n" +
                "Alert count: %d behavioral indicators\n\n" +
                "This file %s consistent with ransomware patterns.\n\n" +
                "Action: %s",
                entropy,
                yaraMatches.size(),
                alertCount,
                (entropy > 7.5 && alertCount > 2) ? "IS" : "is NOT",
                (entropy > 7.5 && alertCount > 2) ? "ISOLATE IMMEDIATELY" : "Monitor for changes"
            );
        } else if (question.contains("enterprise") || question.contains("deployment")) {
            return String.format(
                "🏢 Enterprise Deployment Assessment:\n\n" +
                "Safety Score: %s\n" +
                "Risk Level: %s\n" +
                "Alerts: %d detected\n\n" +
                "Recommendation: %s deployment in enterprise environment.",
                trustScore > 0.8 ? "HIGH" : trustScore > 0.5 ? "MEDIUM" : "LOW",
                severity,
                alertCount,
                trustScore > 0.7 ? "SAFE FOR" : trustScore > 0.4 ? "CONDITIONAL" : "DO NOT ALLOW"
            );
        } else if (question.contains("severity") || question.contains("risk")) {
            return String.format(
                "📊 Risk Assessment:\n\n" +
                "Severity Level: %s\n" +
                "Risk Score Indicators:\n" +
                "  - Entropy: %.2f/8.0\n" +
                "  - Trust Score: %.2f/1.0\n" +
                "  - Active Alerts: %d\n" +
                "  - YARA Signatures: %d\n\n" +
                "Overall: This file poses a %s risk.",
                severity,
                entropy,
                trustScore,
                alertCount,
                yaraMatches.size(),
                severity.equals("CRITICAL") ? "CRITICAL" : severity.equals("HIGH") ? "significant" : "moderate"
            );
        } else if (question.contains("signature") || question.contains("yara") || question.contains("malware")) {
            return String.format(
                "🔍 Signature Analysis:\n\n" +
                "YARA Matches: %d detected\n%s" +
                "Known Threats: %s\n\n" +
                "These signatures indicate %s malware families.",
                yaraMatches.size(),
                yaraMatches.size() > 0 ? formatYaraMatches(yaraMatches) : "No specific signatures matched\n",
                yaraMatches.size() > 0 ? "YES - Known malware patterns found" : "Unknown or new variants",
                yaraMatches.size() >= 3 ? "MULTIPLE" : yaraMatches.size() > 0 ? "POTENTIAL" : "UNKNOWN"
            );
        } else if (question.contains("entropy")) {
            return String.format(
                "📈 Entropy Analysis:\n\n" +
                "Entropy Score: %.2f/8.0\n\n" +
                "Interpretation: %s\n\n" +
                "Entropy above 7.0 typically indicates:\n" +
                "• Encryption or compression\n" +
                "• Code obfuscation techniques\n" +
                "• Potential malicious intent\n\n" +
                "Status: %s",
                entropy,
                entropy > 7.5 ? "HIGHLY SUSPICIOUS - Strong compression/encryption" :
                entropy > 7.0 ? "SUSPICIOUS - Signs of obfuscation" :
                entropy > 6.5 ? "MODERATE - Some compression detected" : "NORMAL - Natural file entropy",
                entropy > 7.0 ? "Further analysis recommended" : "Likely benign"
            );
        } else {
            return String.format(
                "🔐 General Analysis for %s:\n\n" +
                "File Status: %s\n" +
                "Trust Score: %.2f\n" +
                "Alerts: %d\n" +
                "YARA Matches: %d\n" +
                "Entropy: %.2f\n\n" +
                "Recommendation: %s",
                filename,
                isSuspicious ? "SUSPICIOUS" : "CLEAN",
                trustScore,
                alertCount,
                yaraMatches.size(),
                entropy,
                getSeverityRecommendation(severity)
            );
        }
    }

    private String formatYaraMatches(List<String> yaraMatches) {
        StringBuilder sb = new StringBuilder("Matched Signatures:\n");
        for (int i = 0; i < Math.min(yaraMatches.size(), 5); i++) {
            sb.append("  ✓ ").append(yaraMatches.get(i)).append("\n");
        }
        if (yaraMatches.size() > 5) {
            sb.append("  ... and ").append(yaraMatches.size() - 5).append(" more\n");
        }
        return sb.toString();
    }

    private String calculateSeverity(Double entropy, Double trustScore, Integer alertCount, Integer yaraMatchCount) {
        // Calculate severity based on indicators
        int riskScore = 0;

        if (entropy > 7.5) riskScore += 3;
        else if (entropy > 7.0) riskScore += 2;
        else if (entropy > 6.5) riskScore += 1;

        if (trustScore < 0.3) riskScore += 3;
        else if (trustScore < 0.5) riskScore += 2;
        else if (trustScore < 0.7) riskScore += 1;

        riskScore += Math.min(alertCount, 5);
        riskScore += Math.min(yaraMatchCount, 3);

        if (riskScore >= 12) return "CRITICAL";
        if (riskScore >= 8) return "HIGH";
        if (riskScore >= 4) return "MEDIUM";
        return "LOW";
    }

    private Double calculateConfidence(Double entropy, Integer alertCount, Integer yaraMatchCount) {
        // Confidence increases with more indicators
        Double confidence = 0.5;
        if (entropy > 7.0) confidence += 0.2;
        if (alertCount > 2) confidence += 0.15;
        if (yaraMatchCount > 0) confidence += 0.1;
        return Math.min(confidence, 0.95);
    }

    private String getSeverityRecommendation(String severity) {
        switch (severity) {
            case "CRITICAL":
                return "IMMEDIATE ACTION REQUIRED - Isolate system and perform forensics";
            case "HIGH":
                return "Isolate and investigate - Contact security team";
            case "MEDIUM":
                return "Monitor closely and investigate behavior";
            case "LOW":
                return "Monitor - Likely safe but keep watch";
            default:
                return "Continue monitoring";
        }
    }

    private List<String> extractIndicators(ChatRequest.AnalysisContext context) {
        List<String> indicators = new ArrayList<>();

        if (context.getEntropy() != null && context.getEntropy() > 7.0) {
            indicators.add("High Entropy (" + String.format("%.2f", context.getEntropy()) + ")");
        }

        if (context.getTrustScore() != null && context.getTrustScore() < 0.5) {
            indicators.add("Low Trust Score (" + String.format("%.2f", context.getTrustScore()) + ")");
        }

        if (context.getAlertCount() != null && context.getAlertCount() > 2) {
            indicators.add(context.getAlertCount() + " Behavioral Alerts");
        }

        if (context.getYaraMatches() != null && !context.getYaraMatches().isEmpty()) {
            indicators.add(context.getYaraMatches().size() + " YARA Matches");
        }

        return indicators;
    }
}
