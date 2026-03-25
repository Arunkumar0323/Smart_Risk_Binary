package com.TheSecureSyndicate.threat_detection_backend.service;

import com.TheSecureSyndicate.threat_detection_backend.model.YaraResult;
import com.TheSecureSyndicate.threat_detection_backend.repository.YaraResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class YaraScanService {

    @Autowired
    private YaraResultRepository yaraResultRepository;

    public YaraResult scanFile(Path filePath, String binaryId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "C:\\Tools\\yara-4.5.5-2368-win64\\yara64.exe",
                "-r",
                "C:\\Tools\\yara-4.5.5-2368-win64\\rules.yar",
                filePath.toString()
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> matchedRules = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                // Extract rule name from YARA output line (e.g., "Suspicious_PE_Name file.exe")
                String ruleName = line.split("\\s+")[0].trim();
                matchedRules.add(ruleName);
            }

            boolean matched = !matchedRules.isEmpty();
            String matchedRulesString = String.join(",", matchedRules);

            YaraResult result = new YaraResult(
                UUID.randomUUID().toString(),
                binaryId,
                matched,
                matchedRulesString,
                Instant.now()
            );

            yaraResultRepository.save(result);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("YARA scan failed: " + e.getMessage(), e);
        }
    }
}
