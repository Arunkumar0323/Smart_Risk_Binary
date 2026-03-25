package com.TheSecureSyndicate.threat_detection_backend.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class FuzzyHashUtil {

    public static String calculateSSDEEP(Path filePath) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                "C:\\Tools\\ssdeep-2.14.1\\ssdeep.exe", // ✅ Full correct path
                filePath.toString()
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String fuzzyHash = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ssdeep")) continue; // skip header line
                if (line.contains(",")) {
                    // Second line => extract hash before the comma
                    fuzzyHash = line.split(",")[0].trim();
                    break;
                }
            }

            process.waitFor();
            return fuzzyHash != null ? fuzzyHash : "N/A";
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
