package com.TheSecureSyndicate.threat_detection_backend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Pattern;

public class IOCExtractor {

    private static final ObjectMapper mapper = new ObjectMapper();

    // 🔹 Existing method for dynamic IOCs from Cuckoo JSON
    public static Map<String, Object> extract(String jsonReport) {
        Map<String, Object> iocs = new HashMap<>();

        try {
            JsonNode root = mapper.readTree(jsonReport);

            double score = root.path("info").path("score").asDouble();
            iocs.put("score", score);

            List<String> domains = new ArrayList<>();
            root.path("network").path("domains").forEach(d -> {
                if (d.has("domain")) {
                    domains.add(d.path("domain").asText());
                }
            });
            iocs.put("domains", domains);

            List<String> urls = new ArrayList<>();
            root.path("network").path("http").forEach(h -> {
                if (h.has("uri")) {
                    urls.add(h.path("uri").asText());
                }
            });
            iocs.put("urls", urls);

            List<String> signatures = new ArrayList<>();
            root.path("signatures").forEach(sig -> {
                if (sig.has("name")) {
                    signatures.add(sig.path("name").asText());
                }
            });
            iocs.put("signatures", signatures);

            JsonNode behavior = root.path("behavior").path("summary");
            iocs.put("behaviorSummary", behavior.isMissingNode() ? "" : behavior.toString());

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to extract IOCs from Cuckoo report", e);
        }

        return iocs;
    }

    // 🔹 New method for static IOCs from extracted strings
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)\\b(?:http|https|ftp|file)://\\S+");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    // Fixed: properly escaped backslashes and grouped HKLM / HKCU branches
    private static final Pattern REGISTRY_PATTERN = Pattern.compile("(?i)\\b(?:HKLM\\\\[^\\s]+|HKCU\\\\[^\\s]+)");
    private static final Pattern CMD_PATTERN = Pattern.compile("(?i)\\b(cmd\\.exe|powershell|wmic|schtasks|rundll32|regedit|netsh|curl|wget)\\b");

    public static List<String> extractFromStrings(List<String> strings) {
        List<String> iocs = new ArrayList<>();
        for (String s : strings) {
            if (s == null) continue;
            String trimmed = s.trim();
            if (trimmed.isEmpty()) continue;

            boolean matched =
                URL_PATTERN.matcher(trimmed).find() ||
                IP_PATTERN.matcher(trimmed).find() ||
                REGISTRY_PATTERN.matcher(trimmed).find() ||
                CMD_PATTERN.matcher(trimmed).find();

            if (matched) {
                iocs.add(trimmed);
            }
        }
        return iocs;
    }
}
