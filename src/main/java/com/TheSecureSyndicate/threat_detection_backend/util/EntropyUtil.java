package com.TheSecureSyndicate.threat_detection_backend.util;

public class EntropyUtil {

    /**
     * Calculates Shannon entropy of a byte array (range 0.0–8.0).
     */
    public static double calculateEntropy(byte[] data) {
        if (data == null || data.length == 0) {
            return 0.0;
        }

        int[] freq = new int[256];
        for (byte b : data) {
            freq[b & 0xFF]++;
        }

        double entropy = 0.0;
        int len = data.length;

        for (int f : freq) {
            if (f > 0) {
                double p = (double) f / len;
                entropy -= p * (Math.log(p) / Math.log(2));  // Shannon entropy formula
            }
        }

        // ✅ Clamp the entropy between 0.0 and 8.0 to prevent overflow or corruption
        if (Double.isNaN(entropy)) {
            entropy = 0.0;
        } else {
            entropy = Math.min(8.0, Math.max(0.0, entropy));
        }

        // ✅ Round to 3 decimal places for clean JSON output
        entropy = Math.round(entropy * 1000.0) / 1000.0;

        return entropy;
    }
}
